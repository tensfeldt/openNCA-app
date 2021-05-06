package com.pfizer.equip.services.business.report;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.pfizer.equip.services.business.report.dto.ReportDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.business.api.compute.ComputeService;
import com.pfizer.equip.services.business.api.dataframe.Assembly;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.business.api.dataframe.Dataset;
import com.pfizer.equip.services.business.api.input.KeyValuePairInput;
import com.pfizer.equip.services.business.api.response.ComputeResponse;
import com.pfizer.equip.services.business.modeshape.nodes.BaseReportArtifactNode;
import com.pfizer.equip.services.business.modeshape.nodes.SearchResultsNode;
import com.pfizer.equip.services.business.report.exceptions.InvalidReportException;
import com.pfizer.equip.services.business.report.exceptions.InvalidReportingItemException;
import com.pfizer.equip.services.business.report.exceptions.ReportingItemNotFoundException;
import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.services.responses.ReportResponse;
import com.pfizer.equip.services.utils.MimeTypeExtensions;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.relational.entity.AuditEntry;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.api.Metadatum;
import com.pfizer.equip.shared.service.business.api.PublishItem;
import com.pfizer.equip.shared.service.business.api.ReportingEvent;
import com.pfizer.equip.shared.service.business.api.ReportingEventItem;
import com.pfizer.equip.shared.service.business.api.ReportingEventService;

@Service
// Provides data manipulation services for controller
// Includes retrieval and combination of master + snapshot nodes
// And master node user-editable attribute modifications (secured in controller)
public class ReportService {
   @Autowired
   private RepositoryService repositoryService;
   @Autowired
   private ComputeService computeService;
   @Autowired
   private DataframeService dataframeService;
   @Autowired
   private ReportingEventService reportingEventService;

   @Autowired
   private RelationalDatabaseTableReportService relationalDatabaseTableReportService;

   private final Logger log = LoggerFactory.getLogger(this.getClass());
   
   public static final String ATR_TYPE = "ATR";
   public static final String AQC_TYPE = "Analysis QC";
   
   public static final String PREVIOUS_ATR_RE_ID = "previousAtrReiId";

   @Autowired
   ObjectMapper mapper;

   MimeTypeExtensions mimeTypeExtensions = new MimeTypeExtensions();

   public ContentInfo getReportOutputContent(String requestUserId, String dataframeId) {
      log.debug("Calling dataframe service API to retrieve report output content");
      ContentInfo contentInfo = dataframeService.getComplexData(requestUserId, dataframeId);
      return contentInfo;
   }

   public Set<Dataframe> getReportOutput(String requestUserId, String reportFolderPath) throws JsonProcessingException {
      log.debug("Calling dataframe service API to retrieve report output");
      JsonNode reportsFolderJson = repositoryService.getNode(JsonNode.class, reportFolderPath, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      Set<String> dataframeIds = new HashSet<>();
      Set<Dataframe> dataframes = new HashSet<>();
      if (reportsFolderJson.has("children")) {
         for (JsonNode reportsJson : reportsFolderJson.get("children")) {
            dataframeIds.add(reportsJson.get("id").asText());
         }
         dataframes = getReportOutputByIds(requestUserId, dataframeIds);
      }
      return dataframes;
   }

   public Dataframe getReportOutputById(String requestUserId, String dataframeId) throws JsonProcessingException {
      Set<String> dataframeIds = new HashSet<String>();
      dataframeIds.add(dataframeId);
      Set<Dataframe> dataframes = dataframeService.getDataframes(requestUserId, dataframeIds);
      return dataframes.iterator().next(); // only one
   }

   public Set<Dataframe> getReportOutputByIds(String requestUserId, Set<String> dataframeIds) throws JsonProcessingException {
      Set<Dataframe> dataframes = dataframeService.getDataframes(requestUserId, dataframeIds);
      return dataframes;
   }

   public ReportResponse generateReportingItem(String requestUserId, String reportingItemTemplateId, List<String> dataframeIds, Set<KeyValuePairInput> parameters,
         String equipId, List<String> assemblyIds) throws JsonProcessingException, IOException {
      repositoryService.getNodeById(BaseReportArtifactNode.class, reportingItemTemplateId, DepthType.WITH_DIRECT_CHILDREN);
      Set<Dataframe> reportItems = dataframeService.getDataframes(requestUserId, dataframeIds);
      ReportResponse reportResponse = new ReportResponse();


      if (reportItems.size() != dataframeIds.size()) {
         List<String> equipIds = new ArrayList<String>();
         reportItems.forEach(reportItem -> equipIds.add(reportItem.getEquipId()));
         throw new ReportingItemNotFoundException(String.format("Some of the following reporting items were not found by the DF service: %s", equipIds));
      }

      for (Dataframe reportItem : reportItems) {
         // error checks
         if (reportItem.getDeleteFlag()) {
            throw new InvalidReportingItemException(String.format("Reporting Item '%s' is flagged as deleted.", reportItem.getEquipId()));
         }
         if (!reportItem.getisCommitted()) {
            throw new InvalidReportingItemException(String.format("Reporting Item '%s' is uncommitted.", reportItem.getEquipId()));
         }
         if (reportItem.getVersionSuperSeded()) {
            String warningMessage = String.format("Reporting Item '%s' has been superseded.", reportItem.getEquipId());
            log.warn(warningMessage);
            reportResponse.addWarningMessage(warningMessage);
         }
         if (reportItem.getObsoleteFlag()) {
            String warningMessage = String.format("Reporting Item '%s' has been obsoleted.", reportItem.getEquipId());
            log.warn(warningMessage);
            reportResponse.addWarningMessage(warningMessage);
         }
      }

      log.debug("Calling compute service API to generate reporting item with reporting item template ID {}", reportingItemTemplateId);
      ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, reportingItemTemplateId, parameters, dataframeIds, assemblyIds, "Report Item",
            equipId);
      if (computeResponse.getStatus().equals("Success")) {
         reportResponse.setReportingItemIds(computeResponse.getDataframes());
         reportResponse.setResponse(reportResponse.getWarningMessages() == null ? Response.OK : Response.WARNING);
         log.debug("Reporting item job successful, commiting report item");
      } else {
         reportResponse.setResponse(Response.FAILED);
         log.debug("Reporting item job failed");
      }
      reportResponse.setJobInfo(computeResponse);
      return reportResponse;
   }

   public ReportResponse generateReportingItemFromNode(String requestUserId, String nodeId) throws JsonProcessingException, IOException {
      Protocol sourceNode = repositoryService.getNodeById(Protocol.class, nodeId, DepthType.WITH_DIRECT_CHILDREN);
      String primaryType = sourceNode.getPrimaryType();
      
      if (!primaryType.contains("opmeta:protocol")) {
         // TODO: support at least also children of protocol, keep simple for now.
         throw new RuntimeException(String.format("This node is of type '%s'. Report service can only convert nodes of type protocol", 
               primaryType));
      }
      String sql = String.format("select * from [%s] where [jcr:uuid] = '%s'", primaryType, nodeId);
      SearchResultsNode results = repositoryService.executeQuery(SearchResultsNode.class, sql);

      StringBuilder builder = new StringBuilder();
      try (CSVPrinter printer = new CSVPrinter(builder, CSVFormat.DEFAULT)) {
	
	      List<String> sortedKeys = new ArrayList<String>(results.getColumns().keySet());
	      Collections.sort(sortedKeys);
	      for (String column : sortedKeys) {
	         printer.print(column);
	      }
	      printer.println();
	
	      for (Map<String, String> rowMap : results.getRows()) {
	         for (String key : sortedKeys) {
	            printer.print(rowMap.get(key));
	         }
	         printer.println();
	      }
      }
      
      byte[] bytes = builder.toString().getBytes("UTF-8");

      Dataframe dataframe = new Dataframe();
      dataframe.addProgramStudyId(sourceNode.getProgramCode(), sourceNode.getStudyId());
      dataframe.setDataframeType("Report Item");
      dataframe.setDataStatus("Draft");
      dataframe.setPromotionStatus("Promoted");
      dataframe.setRestrictionStatus("Not Restricted");
      dataframe.setDataBlindingStatus(StudyBlindingStatus.BLINDED.getValue()); // least restrictive
      String dataframeId = dataframeService.addDataframe(requestUserId, dataframe);
      
      Dataset dataset = new Dataset();
      String datasetId = dataframeService.addDataset(requestUserId, dataframeId, dataset);

      dataframeService.addDatasetContent(requestUserId, datasetId, bytes);

      // TODO: atomic operations / transaction handling?

      ReportResponse reportResponse = new ReportResponse();
      reportResponse.setResponse(Response.OK);
      reportResponse.addReportingItemId(dataframeId);
      
      dataframe = dataframeService.getDataframe(requestUserId, dataframeId);
      reportResponse.setEquipID(dataframe.getEquipId());
      reportResponse.setEquipVersion(dataframe.getVersionNumber().toString());
      
      return reportResponse;
   }

   public ReportResponse generateReport(String requestUserId, String reportTemplateId, List<String> dataframeIds, Set<KeyValuePairInput> parameters, String equipId, List<String> assemblyIds)
         throws UnsupportedEncodingException, JsonProcessingException, IOException {
      repositoryService.getNodeById(BaseReportArtifactNode.class, reportTemplateId, DepthType.WITH_DIRECT_CHILDREN);

      Set<Dataframe> reportItems = new HashSet<Dataframe>();
      ReportResponse reportResponse = new ReportResponse();

      if (dataframeIds.size() > 0) {
         reportItems = dataframeService.getDataframes(requestUserId, dataframeIds);
      } else {
         // still a valid job but may indicate a user error
         String warningMessage = "No reporting items submitted with this job.";
         log.warn(warningMessage); 
         reportResponse.addWarningMessage(warningMessage);
      }

      if (reportItems.size() != dataframeIds.size()) {
         List<String> equipIds = new ArrayList<String>();
         reportItems.forEach(reportItem -> equipIds.add(reportItem.getEquipId()));
         throw new ReportingItemNotFoundException(String.format("Some of the following reporting items were not found by the DF service: %s", equipIds));
      }

      int idx = 1;
      for (Dataframe reportItem : reportItems) {
         // error checks
         if (reportItem.getDeleteFlag()) {
            throw new InvalidReportingItemException(String.format("Reporting Item '%s' is flagged as deleted.", reportItem.getEquipId()));
         }
         if (!reportItem.getisCommitted()) {
            throw new InvalidReportingItemException(String.format("Reporting Item '%s' is uncommitted.", reportItem.getEquipId()));
         }
         if (reportItem.getVersionSuperSeded()) {
            String warningMessage = String.format("Reporting Item '%s' has been superseded.", reportItem.getEquipId());
            log.warn(warningMessage);
            reportResponse.addWarningMessage(warningMessage);
         }
         if (reportItem.getObsoleteFlag()) {
            String warningMessage = String.format("Reporting Item '%s' has been obsoleted.", reportItem.getEquipId());
            log.warn(warningMessage);
            reportResponse.addWarningMessage(warningMessage);
         }

         // equip-r-markdown container needs to have the file extensions, so use the DF's file naming feature to name each file
         // results are reporting-item1.png, reporting-item2.png, etc. passed as parameters with their dataframe GUID as the key
         String mimeType = reportItem.getDataset().getMimeType();
         String fileExtension = mimeTypeExtensions.get(mimeType);
         String fileName = String.format("reporting-item%d.%s", idx, fileExtension);
         KeyValuePairInput parameter = new KeyValuePairInput(reportItem.getId(), fileName, KeyValuePairInput.STRING_TYPE);
         parameters.add(parameter);
         idx++;
      }

      log.debug("Calling compute service API to generate report with definition ID {}", reportTemplateId);
      ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_MARKDOWN, reportTemplateId, parameters, dataframeIds, assemblyIds, "Report", equipId);
      if (computeResponse.getDataframes().size() == 1) {
         reportResponse.setReportId(computeResponse.getDataframes().iterator().next()); // only one
         reportResponse.setResponse(reportResponse.getWarningMessages() == null ? Response.OK : Response.WARNING);
         log.debug("Report job successful, commiting report");
      } else if (StringUtils.isNotEmpty(computeResponse.getBatch())) {
         reportResponse.setResponse(reportResponse.getWarningMessages() == null ? Response.OK : Response.WARNING);
      } else {
         reportResponse.setResponse(Response.FAILED);
         log.debug("Report job failed");
      }
      reportResponse.setJobInfo(computeResponse);
      return reportResponse;
   }
   
   public ReportResponse generateAuditTrailReport(ReportDTO reportDTO) throws IOException {
      String auditTrailInfo = dataframeService.getAuditTrailReportData(reportDTO);
      ReportingEvent reportingEvent = reportingEventService.getReportingEventById(reportDTO.getReportingEventId(), reportDTO.getUserId());
      
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput reportData = new KeyValuePairInput("reportdata", auditTrailInfo, KeyValuePairInput.STRING_TYPE);
      parameters.add(reportData);
      
      JsonNode auditTrailJson = mapper.readTree(auditTrailInfo);
      List<ReportingEventDataframe> reportingEventDataframes = mapper.convertValue(auditTrailJson.get("dataframes"), new TypeReference<List<ReportingEventDataframe>>() {});
      List<String> dataframeIds = new ArrayList<>();
      
      for (ReportingEventDataframe reportingEventDataframe: reportingEventDataframes) {
         String dataframeId = reportingEventDataframe.getId();
         // Name the dataframes which maps a GUID to a filename, compute will name them as such in the container:
         // Also add to the dataframeIds list
         parameters.add(new KeyValuePairInput(dataframeId, dataframeId, KeyValuePairInput.STRING_TYPE));
         dataframeIds.add(dataframeId);
      }
      
      // TODO: Script is just a mockup
      List<String> reportingEventAssemblyIds = Collections.singletonList(reportDTO.getReportingEventId());
      ComputeResponse computeResponse;
      if (reportDTO.getExistingReportId().isPresent()) {
         String equipId = dataframeService.getDataframe(reportDTO.getUserId(), reportDTO.getExistingReportId().get()).getEquipId();
         // Must supersede before re-generation
         dataframeService.supersedeEntity(reportDTO.getUserId(), reportDTO.getExistingReportId().get());
         computeResponse = computeService.compute(reportDTO.getUserId(), ComputeService.CONTAINER_R_OPENNCA, reportDTO.getReportTemplateId(), parameters, dataframeIds, reportingEventAssemblyIds, "Report", equipId, ATR_TYPE);
      } else {
         computeResponse = computeService.compute(reportDTO.getUserId(), ComputeService.CONTAINER_R_OPENNCA, reportDTO.getReportTemplateId(), parameters, dataframeIds, reportingEventAssemblyIds, "Report", null, ATR_TYPE);
      }
      
      ReportResponse reportResponse = new ReportResponse();
      if (computeResponse.getDataframes().size() == 1) {
         String reportId = computeResponse.getDataframes().iterator().next(); // only one
         log.debug("Generated ATR report with GUID {}", reportId);
         dataframeService.commitEntity(reportDTO.getUserId(), reportId);
         reportResponse.setReportId(reportId); 
         reportResponse.setResponse(Response.OK);
         
         List<Metadatum> metadata = new ArrayList<>();
         if (reportDTO.getExistingReportId().isPresent()) {
            String atrReportingEventItemId = null;
            Set<ReportingEventItem> reportingEventItems = reportingEventService.getReportingEventItems(reportingEvent.getReportingEventItemIds(), reportDTO.getUserId());
            for (ReportingEventItem reportingEventItem : reportingEventItems) {
               if (reportingEventItem.isAtr() && !reportingEventItem.isDeleteFlag()) {
                  atrReportingEventItemId = reportingEventItem.getId();
                  metadata.add(new Metadatum(PREVIOUS_ATR_RE_ID, atrReportingEventItemId, KeyValuePairInput.METADATUM_TYPE));
               }
            }

            if (atrReportingEventItemId != null) {
               dataframeService.deleteEntity(reportDTO.getUserId(), atrReportingEventItemId);
            }
         }

         Dataframe dataframe = dataframeService.getDataframe(reportDTO.getUserId(), reportResponse.getReportId());
         metadata.add(new Metadatum("dataframeEntityType", "Report", KeyValuePairInput.METADATUM_TYPE));
         metadata.add(new Metadatum("dataframeEquipId", dataframe.getEquipId(), KeyValuePairInput.METADATUM_TYPE));
         metadata.add(new Metadatum("subType", "ATR", KeyValuePairInput.METADATUM_TYPE));
         PublishItem publishItem = new PublishItem(reportDTO.getUserId());
         ReportingEventItem reportingEventItem = new ReportingEventItem(reportDTO.getReportingEventId(), reportId, metadata, publishItem);
         dataframeService.addReportingEventItem(reportingEventItem, reportDTO.getUserId());
         
         dataframe = dataframeService.getDataframe(reportDTO.getUserId(), reportResponse.getReportId());
         reportResponse.setEquipID(dataframe.getEquipId());
         reportResponse.setEquipVersion(dataframe.getVersionNumber().toString());
         
         log.debug("Report job successful");
      } else {
         reportResponse.setResponse(Response.FAILED);
         log.debug("Report job failed");
      }
      reportResponse.setJobInfo(computeResponse);
      return reportResponse;
   }

   public ReportResponse generateAnalysisQcReport(ReportDTO reportDTO) throws IOException {
      List<String> assemblyIds = Collections.singletonList(reportDTO.getAnalysisId());
      log.debug("Getting analysis data from dataframe service...");
      String analysisQcReportInfo = dataframeService.getAnalysisQcData(reportDTO);
      log.debug("Retrieving analysis assembly from dataframe service...");
      Assembly analysis = dataframeService.getAnalysisById(reportDTO.getUserId(), reportDTO.getAnalysisId());
      log.debug("Retrieved assembly.");
      List<String> dataframeIds = new ArrayList<>();
      
      // must be in order:
      String concentrationDataframeId = analysis.getDataframeIds().iterator().next();
      Dataframe concentrationDataframe = dataframeService.getDataframe(reportDTO.getUserId(), concentrationDataframeId);
      dataframeIds.add(concentrationDataframe.getId());
      dataframeIds.add(analysis.getKelFlagsDataframeId());
      dataframeIds.add(analysis.getModelConfigurationDataframeId());
      dataframeIds.add(analysis.getParametersDataframeId());
      dataframeIds.add(analysis.getEstimatedConcDataframeId());
      
      Set<KeyValuePairInput> parameters = new HashSet<>();
      parameters.add(new KeyValuePairInput("reportdata", analysisQcReportInfo, KeyValuePairInput.STRING_TYPE));
      parameters.add(new KeyValuePairInput("sortby", "subject", KeyValuePairInput.STRING_TYPE));
      
      ComputeResponse computeResponse;
      if (reportDTO.getExistingReportId().isPresent())  {
         log.debug("Retrieving existing QC report...");
         String equipId = dataframeService.getDataframe(reportDTO.getUserId(), reportDTO.getExistingReportId().get()).getEquipId();
         log.debug("Successfully retrieved existing QC report.");
         // Must supersede before re-generation
         log.debug("Superseding existing report...");
         dataframeService.supersedeEntity(reportDTO.getUserId(), reportDTO.getExistingReportId().get());
         log.debug("Successfully superseded.");
         log.debug("Calling compute...");
         computeResponse = computeService.compute(reportDTO.getUserId(), ComputeService.CONTAINER_R_OPENNCA, reportDTO.getReportTemplateId(), parameters, dataframeIds, assemblyIds, "Report", equipId, AQC_TYPE);
      } else {
         log.debug("Calling compute...");
         computeResponse = computeService.compute(reportDTO.getUserId(), ComputeService.CONTAINER_R_OPENNCA, reportDTO.getReportTemplateId(), parameters, dataframeIds, assemblyIds, "Report", null, AQC_TYPE);
      }
      log.debug("Compute call complete.");

      ReportResponse reportResponse = new ReportResponse();
      if (computeResponse.getDataframes().size() == 1) {
         String reportId = computeResponse.getDataframes().iterator().next(); // only one
         log.debug("Generated Analysis QC report with GUID {}", reportId);
         dataframeService.commitEntity(reportDTO.getUserId(), reportId);
         reportResponse.setReportId(reportId);
         reportResponse.setResponse(Response.OK);
         // No commit, must be performed by the user
         
         Dataframe dataframe = dataframeService.getDataframe(reportDTO.getUserId(), reportResponse.getReportId());
         reportResponse.setEquipID(dataframe.getEquipId());
         reportResponse.setEquipVersion(dataframe.getVersionNumber().toString());
         log.debug("Report job successful");
      } else {
         reportResponse.setResponse(Response.FAILED);
         log.debug("Report job failed");
      }
      reportResponse.setJobInfo(computeResponse);
      return reportResponse;
   }

   public ReportResponse commitReportingItem(String requestUserId, String reportingItemId) throws JsonProcessingException {
      ReportResponse response = new ReportResponse();
      Dataframe dataframe = dataframeService.getDataframe(requestUserId, reportingItemId);
      response.setEquipID(dataframe.getEquipId());
      response.setEquipVersion(dataframe.getVersionNumber().toString());
      
      dataframeService.commitEntity(requestUserId, reportingItemId);
      
      return response;
   }

   public ReportResponse deleteReportingItem(String requestUserId, String reportingItemId) throws JsonProcessingException {
      // Check if this is an ATR, if so we have to handle the special case of REI restore
      ReportResponse response = new ReportResponse();
      Dataframe dataframe = dataframeService.getDataframe(requestUserId, reportingItemId);
      response.setEquipID(dataframe.getEquipId());
      response.setEquipVersion(dataframe.getVersionNumber().toString());
      if (dataframe.getSubType().equals(ATR_TYPE)) {
         String reportingEventId = dataframe.getAssemblyIds().iterator().next();
         ReportingEvent reportingEvent = reportingEventService.getReportingEventById(reportingEventId, requestUserId);
         Set<ReportingEventItem> reportingEventItems = reportingEventService.getReportingEventItems(reportingEvent.getReportingEventItemIds(),
               requestUserId);
         String previousAtrReportingEventItemId = null;
         String atrReportingEventItemId = null;
         for (ReportingEventItem reportingEventItem : reportingEventItems) {
            if (reportingEventItem.isAtr() && !reportingEventItem.isDeleteFlag()) {
               atrReportingEventItemId = reportingEventItem.getId();
               List<Metadatum> metadata = reportingEventItem.getMetadata();
               for (Metadatum metadatum : metadata) {
                  if (metadatum.getKey().equals(PREVIOUS_ATR_RE_ID)) {
                     previousAtrReportingEventItemId = metadatum.getValue().iterator().next();
                  }
               }
            }
         }
         if (atrReportingEventItemId == null) {
            throw new RuntimeException("Received existing ATR ID but could not find existing REI for this ATR");
         }
         // If we're discarding the first report, we won't have a previous REI ID
         dataframeService.deleteEntity(requestUserId, atrReportingEventItemId);
         if (previousAtrReportingEventItemId != null) {
            dataframeService.restoreEntity(requestUserId, previousAtrReportingEventItemId);
         }
      }
      
      dataframeService.deleteEntity(requestUserId, reportingItemId);
      return response;
   }

   public ReportResponse generateReportingItemFromTable(String requestUserId, String tableName, List<SearchCriteria> searchCriteria)
         throws JsonProcessingException, IOException, NoSuchFieldException, SecurityException {

      StringBuilder builder = new StringBuilder();
      try (CSVPrinter printer = new CSVPrinter(builder, CSVFormat.DEFAULT)) {
	      switch (tableName.toLowerCase()) {
	      case "audit-entry":
	         List<AuditEntry> results = relationalDatabaseTableReportService.retrieveAuditEntries(searchCriteria);
	         Field[] sortedKeys = AuditEntry.class.getDeclaredFields();
	         for (Field column : sortedKeys) {
	            printer.print(column.getName());
	         }
	         printer.println();
	         for (AuditEntry auditEntry : results) {
	            printer.print(auditEntry.getAuditEntryId());
	            printer.print(auditEntry.getEntityId());
	            printer.print(auditEntry.getEntityType());
	            printer.print(auditEntry.getEntityVersion());
	            printer.print(auditEntry.getAction());
	            printer.print(auditEntry.getActionStatus());
	            printer.print(auditEntry.getCreateDate());
	            printer.print(auditEntry.getUserId());
	            printer.print(auditEntry.getFirstName());
	            printer.print(auditEntry.getLastName());
	            printer.print(auditEntry.getEmailAddress());
	            printer.println();
	         }
	
	         break;
	      default:
	         break;
	      }
      }
      byte[] bytes = builder.toString().getBytes("UTF-8");
      Dataframe dataframe = new Dataframe();
      // TODO add a generic dataframe / relevant study id
      dataframe.addProgramStudyId("X123", "X1231043");
      dataframe.setDataframeType("Report Item");
      dataframe.setDataStatus("Draft");
      dataframe.setPromotionStatus("Promoted");
      dataframe.setRestrictionStatus("Not Restricted");
      dataframe.setDataBlindingStatus(StudyBlindingStatus.BLINDED.getValue()); // least restrictive

      String dataframeId = dataframeService.addDataframe(requestUserId, dataframe);

      Dataset dataset = new Dataset();
      String datasetId = dataframeService.addDataset(requestUserId, dataframeId, dataset);

      dataframeService.addDatasetContent(requestUserId, datasetId, bytes);

      // TODO: atomic operations / transaction handling?
      ReportResponse reportResponse = new ReportResponse();
      reportResponse.setResponse(Response.OK);
      reportResponse.addReportingItemId(dataframeId);
      
      dataframe = dataframeService.getDataframe(requestUserId, dataframeId);
      reportResponse.setEquipID(dataframe.getEquipId());
      reportResponse.setEquipVersion(dataframe.getVersionNumber().toString());
      return reportResponse;

   }

   public ReportResponse generateReportingItemFromLineage(String requestUserId, String studyId)
         throws IOException {
      Set<Dataframe> analysisLineages = dataframeService.getLineage(requestUserId, studyId);

      List<Dataframe> hierarchicalDataframesList = new LinkedList<>();
      Map<String, String> idEquipIdMap = new LinkedHashMap<>();
      for (Dataframe analysisLineage : analysisLineages) {
         // Parent Dataframe
         hierarchicalDataframesList.add(analysisLineage);
         // Child Dataframes
         for (Dataframe firstLevelDataframe : analysisLineage.getChildDataframes()) {
            hierarchicalDataframesList.add(firstLevelDataframe);
            hierarchicalDataframesList = getChildDataFrames(firstLevelDataframe, hierarchicalDataframesList, new LinkedList<>(), idEquipIdMap, false);
         }
         // Child Assemblies
         for (Dataframe firstLevelAssembly : analysisLineage.getChildAssemblies()) {
            hierarchicalDataframesList.add(firstLevelAssembly);
            hierarchicalDataframesList = getChildDataFrames(firstLevelAssembly, hierarchicalDataframesList, new LinkedList<>(), idEquipIdMap, false);
         }

      }

      StringBuilder builder = new StringBuilder();
      try (CSVPrinter printer = new CSVPrinter(builder, CSVFormat.DEFAULT)) {
	
	      // TODO : for headers of csv has been hardcoded
	      printer.print("equipId");
	      printer.print("parentId");
	      printer.print("dataStatus");
	      printer.print("releaseStatus");
	      printer.print("publishStatus");
	      printer.println();
	
	      for (Dataframe dataframe : hierarchicalDataframesList) {
	         printer.print(dataframe.getEquipId());
	         String parentEquipId = null;
	         if (dataframe.getParentDataframeIds() != null && !dataframe.getParentDataframeIds().isEmpty()) {
	            parentEquipId = idEquipIdMap.get(dataframe.getParentDataframeIds().iterator().next());
	         } else if (dataframe.getParentAssemblyIds() != null && !dataframe.getParentAssemblyIds().isEmpty()) {
	            parentEquipId = idEquipIdMap.get(dataframe.getParentAssemblyIds().iterator().next());
	         }
	         printer.print(parentEquipId);
	         printer.print(dataframe.getDataStatus());
	         printer.print(dataframe.getReleaseStatus());
	         printer.print(dataframe.getPublishStatus());
	         printer.println();
	      }
      }
      byte[] bytes = builder.toString().getBytes("UTF-8");

      Dataframe dataframe = new Dataframe();
      String[] proramIdStudyId = studyId.split(":");
      dataframe.addProgramStudyId(proramIdStudyId[0], proramIdStudyId[1]);
      dataframe.setDataframeType("Report Item");
      dataframe.setDataStatus("Draft");
      dataframe.setPromotionStatus("Promoted");
      dataframe.setRestrictionStatus("Not Restricted");
      dataframe.setDataBlindingStatus(StudyBlindingStatus.BLINDED.getValue()); // least restrictive
      String dataframeId = dataframeService.addDataframe(requestUserId, dataframe);

      Dataset dataset = new Dataset();
      String datasetId = dataframeService.addDataset(requestUserId, dataframeId, dataset);

      dataframeService.addDatasetContent(requestUserId, datasetId, bytes);

      // TODO: atomic operations / transaction handling?

      ReportResponse reportResponse = new ReportResponse();
      reportResponse.setResponse(Response.OK);
      reportResponse.addReportingItemId(dataframeId);
      
      dataframe = dataframeService.getDataframe(requestUserId, dataframeId);
      reportResponse.setEquipID(dataframe.getEquipId());
      reportResponse.setEquipVersion(dataframe.getVersionNumber().toString());
      
      return reportResponse;

   }

   public List<Dataframe> getChildDataFrames(Dataframe dataframe, List<Dataframe> hierarchicalDataframesList, List<Dataframe> childLevelDataframesList,
         Map<String, String> idEquipIdMap, boolean callWithinChild) {
      if (!childLevelDataframesList.isEmpty() && callWithinChild) {
         for (Dataframe childLevelDataframe : childLevelDataframesList) {
            if (!hierarchicalDataframesList.contains(childLevelDataframe)) {
               hierarchicalDataframesList.add(childLevelDataframe);
               idEquipIdMap.put(childLevelDataframe.getId(), childLevelDataframe.getEquipId());
            }
         }
         childLevelDataframesList.remove(dataframe);
      }

      // Child dataframes
      Iterator<Dataframe> childrenDataframes = dataframe.getChildDataframes().iterator();

      while (childrenDataframes.hasNext()) {
         Dataframe childDataframe = childrenDataframes.next();

         if (childDataframe.getEquipId() != null) {
            childLevelDataframesList.add(childDataframe);
         }
      }

      // Child Assemblies
      Iterator<Dataframe> childrenAssemblies = dataframe.getChildAssemblies().iterator();

      while (childrenAssemblies.hasNext()) {
         Dataframe childAssembly = childrenAssemblies.next();

         if (childAssembly.getEquipId() != null) {
            childLevelDataframesList.add(childAssembly);
         }
      }

      for (Dataframe childDataframe : childLevelDataframesList) {
         getChildDataFrames(childDataframe, hierarchicalDataframesList, childLevelDataframesList, idEquipIdMap, true);
      }

      return hierarchicalDataframesList;
   }

   public Optional<String> getQcReportByAnalysisId(String analysisId, String requestUserId)
         throws JsonParseException, JsonMappingException, IOException, JsonProcessingException {
      // First, figure out the study and get all the reports:
      List<Dataframe> reports = dataframeService.getQcReportsByAnalysisId(requestUserId, analysisId);
      
      // Now find the QC report for this analysis
      String existingReportId = null;
      boolean reportFound = false;
      for (Dataframe report : reports) {
         String assemblyId = report.getAssemblyIds().iterator().hasNext() ? report.getAssemblyIds().iterator().next() : ""; // only one, does not support cross study
         if (report.getSubType() != null && report.getSubType().equals(AQC_TYPE) && analysisId.equals(assemblyId) && !report.getVersionSuperSeded() && !report.getDeleteFlag()) {
            if (reportFound)  {
               throw new RuntimeException("Found more than one Analaysis QC report, invalid data, cannot continue");
            } else {
               if (report.getisCommitted()) {
                  reportFound = true;
                  existingReportId = report.getId();
               } else {
                  throw new InvalidReportException(String.format(
                        "Found an uncommited, undeleted QC report. Cannot continue: save or discard the previous report with ID %s",
                        report.getId(), report.getCreatedBy()));
               }
            }
         }
      }
      return Optional.ofNullable(existingReportId);
   }

   public Optional<String> getAuditTrailReportByReportingEventId(String reportingEventId, String requestUserId)
         throws JsonParseException, JsonMappingException, IOException, JsonProcessingException {
      // First, figure out the study and get all the reports:
      List<Dataframe> reports = dataframeService.getAtrsByReportingEventId(requestUserId, reportingEventId);
      
      // Now find the QC report for this analysis
      String existingReportId = null;
      boolean reportFound = false;
      for (Dataframe report : reports) {
         String assemblyId = report.getAssemblyIds().iterator().hasNext() ? report.getAssemblyIds().iterator().next() : ""; // only one, does not support cross study
         if (report.getSubType() != null && report.getSubType().equals(ATR_TYPE) && reportingEventId.equals(assemblyId) && !report.getVersionSuperSeded() && !report.getDeleteFlag()) {
            if (reportFound)  {
               throw new RuntimeException("Found more than one Audit Trail Report, invalid data, cannot continue");
            } else {
               if (report.getisCommitted()) {
                  reportFound = true;
                  existingReportId = report.getId();
               } else {
                  throw new InvalidReportException(String.format(
                        "Found an uncommited, undeleted ATR Report. Cannot continue: save or discard the previous report with ID %s from user %s.",
                        report.getId(), report.getCreatedBy()));
               }
            }
         }
      }
      return Optional.ofNullable(existingReportId);
   }
}