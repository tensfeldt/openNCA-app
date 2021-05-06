package com.pfizer.equip.services.business.api.dataframe;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import com.pfizer.equip.services.business.report.dto.ReportDTO;
import com.pfizer.equip.services.input.dataframe.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.hazelcast.util.Base64;
import com.pfizer.equip.services.business.api.compute.ComputeService;
import com.pfizer.equip.services.business.api.dataframe.pims.PIMSDoseMapping;
import com.pfizer.equip.services.business.api.dataframe.pims.PIMSLoadInfo;
import com.pfizer.equip.services.business.api.exceptions.ComputeFailedException;
import com.pfizer.equip.services.business.api.exceptions.FileFormatNotSupportedException;
import com.pfizer.equip.services.business.api.exceptions.PimsLoadException;
import com.pfizer.equip.services.business.api.exceptions.RemoteClientErrorException;
import com.pfizer.equip.services.business.api.exceptions.RemoteServerErrorException;
import com.pfizer.equip.services.business.api.input.CommentInput;
import com.pfizer.equip.services.business.api.input.ComputeParameterInput;
import com.pfizer.equip.services.business.api.input.KeyValuePairInput;
import com.pfizer.equip.services.business.api.input.MappingInput;
import com.pfizer.equip.services.business.api.response.ComputeResponse;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.business.report.ReportService;
import com.pfizer.equip.services.business.types.PrimaryType;
import com.pfizer.equip.services.business.validation.Specification;
import com.pfizer.equip.services.business.validation.ValidationService;
import com.pfizer.equip.services.input.InputBuilder;
import com.pfizer.equip.services.input.library.LibraryInput;
import com.pfizer.equip.services.input.library.LibraryInputBuilder;
import com.pfizer.equip.services.input.validation.MappingInformation;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.dataframe.DataloadResponse;
import com.pfizer.equip.services.responses.dataframe.DatasetResponse;
import com.pfizer.equip.services.responses.dataframe.ProfileUpdateReponse;
import com.pfizer.equip.services.responses.dataframe.PromoteResponse;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.utils.SimulatedMultipartFile;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.api.Metadatum;
import com.pfizer.equip.shared.service.business.api.ReportingEventItem;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@Service
public class DataframeService {

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private static final String RESPONSE_TYPE = "application/json";

   public static final String[] DEFAULT_PROFILE_CONFIGURATION = { "STUDY", "SITEID", "SUBJID", "RAND", "TREATXT", "TRTCD", "PKCOLL", "PKBDFLD", "PKTERM", "PERIOD",
         "PERIODU", "VISIT", "VISITU", "PHASE" };

   public static final String SCRIPT_NO_OP = "no-op.R";
   public static final String SCRIPT_DE_IDENTIFY_AND_CALC_SDEID = "de-identify-and-calc-sdeid.R";
   public static final String SCRIPT_DATA_BLINDING = "data-blinding.R";
   public static final String SCRIPT_BLQ_ADJUSTMENTS = "blq-adjustments.r";
   public static final String SCRIPT_ATTRIBUTE_MAPPING = "attribute-mapping.R";
   public static final String SCRIPT_MERGE = "file-merge.R";
   public static final String SCRIPT_XPT_TO_CSV = "xpt-to-csv.r";
   public static final String SCRIPT_XLSX_TO_CSV = "xlsx-to-csv.r";
   public static final String SCRIPT_SDEID_RECALC = "sdeid-recalc.R";

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   private ComputeService computeService;

   @Autowired
   private ValidationService validationService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   // TODO: remove hardcoding
   private static final String SYSTEM_ID = "nca";
   private static final String CLIENT_INFO_HEADER = "Client-Info";

   @Autowired
   ObjectMapper mapper;

   private CsvMapper csvMapper;
   private RestTemplate restTemplate;
   private String assemblyUrl;
   private String assemblyGetUrl;
   private String dataframeListUrl;
   private String entityCommitUrl;
   private String entityDeleteUrl;
   private String entityUpdateUrl;
   private String metadataUrl;
   private String commentUrl;
   private String datasetUrl;
   private String dataframePromoteUrl;
   private String dataframeUrl;
   private String lineageUrl;
   private String auditTrailReportUrl;
   private String qcAnalysisReportUrl;
   private String previousQcAnalysisReportUrl;
   private String previousAtrUrl;
   private String analysisUrl;
   private String reportingEventItemUrl;
   private String dataloadDeleteUrl;
   private String dataloadCommitUrl;
   private String dataSetContentUrl;
   private String pimsLoadUrl;
   private String supersedeUrl;
   private CsvSchema schema;

   @PostConstruct
   private void initialize() {
      restTemplate = restTemplateBuilder.build();
      String baseUrl = properties.getDataframeBaseUrl() + SYSTEM_ID + "/";
      assemblyUrl = baseUrl + "assemblies";
      assemblyGetUrl = baseUrl + "assemblies/%s";
      entityCommitUrl = baseUrl + "entities/%s/commit";
      entityDeleteUrl = baseUrl + "entities/%s/delete";
      entityUpdateUrl = baseUrl + "entities/%s/update";
      metadataUrl = baseUrl + "entities/%s/metadata";
      commentUrl = baseUrl + "entities/%s/comments";
      dataframeUrl = baseUrl + "dataframes";
      dataframeListUrl = baseUrl + "dataframes/list";
      datasetUrl = baseUrl + "dataframes/%s/data";
      dataframePromoteUrl = baseUrl + "dataframes/%s/promotions";
      dataframeUrl = baseUrl + "dataframes";
      dataSetContentUrl = baseUrl + "dataframes/data/%s";
      pimsLoadUrl = baseUrl + "pims/%s/loaddata";
      lineageUrl = baseUrl + "lineage";
      auditTrailReportUrl = baseUrl + "reports/atr/%s";
      qcAnalysisReportUrl = baseUrl + "reports/analysisqc/%s";
      previousAtrUrl = baseUrl + "reportingEvent/%s/reports?includeUncommitted=true&subType=" + ReportService.ATR_TYPE;
      previousQcAnalysisReportUrl = baseUrl + "analysis/%s/reports?includeUncommitted=true&subType=" + ReportService.AQC_TYPE;
      supersedeUrl = baseUrl + "entities/%s/supersede";
      analysisUrl = baseUrl + "analyses/%s";
      reportingEventItemUrl = baseUrl + "reportingeventitems";
      dataloadDeleteUrl = baseUrl + "/lineage/dataload/%s/delete";
      dataloadCommitUrl = baseUrl + "/lineage/dataload/%s/commit";
      csvMapper = new CsvMapper();
      csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
      // schema = CsvSchema.builder().build().withoutQuoteChar(); // Keep double quotes
   }

   private String getScriptId(String scriptName, String userId, boolean createCopy) throws Exception {
      LibraryArtifactResponse response = librarianService.getArtifact(LibrarianService.GLOBAL_LIBRARY_START + "/system-scripts/" + scriptName, true);
      String scriptId = response.getArtifactId();

      if (createCopy) {
         // create a copy of the script in the hidden library to save the state of the script at the time this was run
         scriptId = copyScriptToHiddenLibrary(response, userId);
      }

      // return the copy's ID
      return scriptId;
   }

   private String getScriptIdByPath(String scriptPath, String userId, boolean createCopy) throws Exception {
      LibraryArtifactResponse response = librarianService.getArtifact(scriptPath, true);
      String scriptId = response.getArtifactId();

      if (createCopy) {
         // create a copy of the script in the hidden library to save the state of the script at the time this was run
         scriptId = copyScriptToHiddenLibrary(response, userId);
      }

      // return the copy's ID
      return scriptId;
   }

   private void createKvp(List<Metadatum> metadata, String key, String value) {
      Metadatum kvp = new Metadatum(key, value, "metadatum");
      metadata.add(kvp);
   }

   private String calculateDataType(DataloadInput input) {
      String dataType = "";
      String specificationType = input.getSpecificationType();
      if (StringUtils.isNotEmpty(specificationType)) {
         switch (specificationType.toUpperCase()) {
            case "ARD":
               dataType = "Analysis Ready Dataset";
               break;
            case "FINALPKDEF":
               dataType = "PK Definition File";
               break;
            case "PKTDD":
            case "LCD":
               dataType = "Concentration";
               break;
            case "LPD":
               dataType = "Parameter";
               break;
            case "Supplemental":
               // the data type for Supplemental spec files will come from the front end
               dataType = input.getDataType();
               break;
            default:
               break;
         }
      }

      return dataType;
   }

   private void setLoadDataframeMetadata(Dataframe dataframe, DataloadInput input) throws Exception {
      // set top level properties
      dataframe.addProgramStudyId(input.getProgramId(), input.getProtocolId());
      dataframe.setDataframeType("Dataset");
      dataframe.setDataStatus(input.getDataStatus());
      dataframe.setPromotionStatus("Pending Review");
      dataframe.setRestrictionStatus("Not Restricted");
      dataframe.setDataBlindingStatus("Unblinded");
      dataframe.setQcStatus("Not QC'd");
      String dataType = calculateDataType(input);
      dataframe.setItemType(dataType);

      // get profile columns from the specification
      String specificationType = input.getSpecificationType();
      String specificationVersion = input.getSpecificationVersion();
      Optional<String> specificationId = StringUtils.isEmpty(input.getSpecificationId()) ? Optional.empty() : Optional.of(input.getSpecificationId());
      List<String> profileConfig = null;
      if (StringUtils.isNotEmpty(specificationType) && StringUtils.isNotEmpty(specificationVersion)) {
         Specification specification = validationService.getSpecificationFiles(Optional.of(specificationType), Optional.of(specificationVersion), specificationId).get(0);
         profileConfig = specification.getSdeidColumns();
      } else {
         throw new RuntimeException("Specification type and version are required to create data loads.");
      }

      // set KVP values
      List<Metadatum> metadata = new ArrayList<>();
      createKvp(metadata, "Data Load Row Count", input.getNumberRows());
      createKvp(metadata, "Description", input.getDescription());
      createKvp(metadata, "Location Source Path", input.getFilePath());
      createKvp(metadata, "File Name Full", input.getFileContent().getOriginalFilename());
      createKvp(metadata, "File Name Format", FilenameUtils.getExtension(input.getFileContent().getOriginalFilename()));
      createKvp(metadata, "Data Load Status", "Completed");
      createKvp(metadata, "Specification Type", input.getSpecificationType());
      createKvp(metadata, "Specification Version", input.getSpecificationVersion());
      createKvp(metadata, "Data Type", dataType);
      createKvp(metadata, "Profile Configuration", String.join(",", profileConfig));
      dataframe.setMetadata(metadata);
   }

   private String parseComputeResult(ComputeResponse computeResponse, String scriptName, boolean getDataset) {
      String retVal;
      Iterator<String> iter = computeResponse.getDataframes().iterator();
      if (!getDataset) {
         if (!iter.hasNext()) {
            String stdOut = computeResponse.getStdout();
            String stdErr = computeResponse.getStderr();
            throw new ComputeFailedException(String.format("Compute failed for '%s' script with output '%s' and error '%s'", scriptName, stdOut, stdErr));
         }
         retVal = (String) iter.next();
      } else {
         if (computeResponse.getDatasetData() == null || computeResponse.getDatasetData().size() == 0) {
            String stdOut = computeResponse.getStdout();
            String stdErr = computeResponse.getStderr();
            throw new ComputeFailedException(String.format("Compute failed for '%s' script with output '%s' and error '%s'", scriptName, stdOut, stdErr));
         }
         retVal = computeResponse.getDatasetData().get(0);
      }

      return retVal;
   }

   private String convertMappingInformationForCompute(String mappingInformationListStr) throws Exception {
      // we have to convert the JSON type that the validation service returns
      // to a type that the compute service expects, as a JSON escaped string
      MappingInformation[] mappingInformationList = mapper.readValue(mappingInformationListStr, MappingInformation[].class);
      List<MappingInput> mappingInputList = new ArrayList<MappingInput>();
      for (MappingInformation mappingInfo : mappingInformationList) {
         MappingInput input = new MappingInput();
         input.setColumnFrom(mappingInfo.getColumnName());
         input.setColumnTo(mappingInfo.getColumnMappingName());
         mappingInputList.add(input);
      }

      // convert back to string
      String mappingInputArrayStr = mapper.writeValueAsString(mappingInputList);
      return mappingInputArrayStr;
   }

   public String loadPimsData(String requestUserId, String programId, String protocolId, PIMSLoadInfo loadInfo) throws JsonProcessingException {
      String url = String.format(pimsLoadUrl, programId + ":" + protocolId);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.add(properties.getUserIdHeader(), requestUserId);
      String jsonValue = mapper.writeValueAsString(loadInfo);
      String response = "";
      try {
         response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpClientErrorException e) {
         String exceptionBody = e.getResponseBodyAsString();
         throw new PimsLoadException(exceptionBody);
      }

      // the dataframe ID returned has double quotes around it, strip them
      response = response.replaceAll("\"", "");
      return response;
   }

   private String mapColumns(String requestUserId, String mappingInformationListStr, String dataframeId, String assemblyId, boolean performedTransform) throws Exception {
      log.debug("Remapping columns for the new data load...");
      // call the librarian service to retrieve the GUID of the mapping script
      String scriptId = getScriptId(SCRIPT_ATTRIBUTE_MAPPING, requestUserId, true);

      String mappingJson = convertMappingInformationForCompute(mappingInformationListStr);
      KeyValuePairInput parameter = new KeyValuePairInput("map", mappingJson, "string");
      Set<KeyValuePairInput> parameters = new HashSet<KeyValuePairInput>();
      parameters.add(parameter);

      List<String> dataframeIdsList = new ArrayList<String>();
      ComputeResponse computeResponse = null;
      dataframeIdsList.add(dataframeId);
      List<String> assemblyIdsList = null;
      if (!performedTransform) {
         // only add the assembly reference if we have not already performed a prior transform
         // e.g. a format conversion for example
         assemblyIdsList = new ArrayList<String>();
         assemblyIdsList.add(assemblyId);
      }
      computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, scriptId, parameters, dataframeIdsList, assemblyIdsList,
            "Data Transformation", null);
      String mappedDataframeId = parseComputeResult(computeResponse, "Map Columns", false);
      log.info("New mapped dataframe ID is {}", mappedDataframeId);
      return mappedDataframeId;
   }

   private String callNoOpScript(String requestUserId, String dataframeId, String assemblyId) throws Exception {
      // call the librarian service to retrieve the GUID of the no-op script
      String scriptId = getScriptId(SCRIPT_NO_OP, requestUserId, true);

      List<String> dataframeIdsList = new ArrayList<String>();
      dataframeIdsList.add(dataframeId);
      List<String> assemblyIdsList = new ArrayList<String>();
      assemblyIdsList.add(assemblyId);
      ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, scriptId, null, dataframeIdsList, assemblyIdsList,
            "Data Transformation", null);
      String transformedDataframeId = parseComputeResult(computeResponse, "No Op", false);
      log.info("New no op transform dataframe ID is {}", transformedDataframeId);
      return transformedDataframeId;
   }

   private void callPromoteScripts(String requestUserId, String dataframeId, String dataStatus, String dataBlindingStatus,
                                   List<CommentInput> comments, ScriptDto[] scripts) throws Exception {

      String childDataframeId = dataframeId;
      for (ScriptDto script : scripts) {
         // call the librarian service to retrieve the GUID of the various scripts
         String scriptId = getScriptIdByPath(script.getPath(), requestUserId, true);
         Set<KeyValuePairInput> parameters = new HashSet<>();

         if (script.getParameters() != null) {
            for (KVP kvp : script.getParameters()) {
               KeyValuePairInput parameter = new KeyValuePairInput(kvp.getName(), kvp.getValue(), "string");
               parameters.add(parameter);
            }
         }

         List<String> dataframeIdsList = new ArrayList<>();
         dataframeIdsList.add(childDataframeId);
         ComputeResponse response = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, scriptId,
                 parameters, dataframeIdsList, "Data Transformation", null);
         childDataframeId = parseComputeResult(response, script.getName(), false);
         commitEntity(requestUserId, childDataframeId);
         log.info("{} dataframe ID is {}.", script.getName(), childDataframeId);

         boolean applyPromotion = script.isApplyPromotionDataBlindingOnOutput() != null ? script.isApplyPromotionDataBlindingOnOutput() : false;
         if (applyPromotion) {
            // promote the blinded / unblinded dataframe
            Promotion promotion = new Promotion();
            promotion.setPromotionStatus(Promotion.TYPE_PROMOTED);
            promotion.setDataStatus(dataStatus);
            promotion.setDataBlindingStatus(dataBlindingStatus);
            if (comments.size() > 0) {
               promotion.setComments(comments.toArray(new CommentInput[0]));
            }
            promoteDataframe(requestUserId, childDataframeId, promotion);
         }
      }
   }

   public DataloadResponse createDataload(String requestUserId, DataloadInput input) throws Exception {
      return createDataload(requestUserId, new ArrayList<DataloadInput>(Arrays.asList(input)), Optional.empty());
   }

   public DataloadResponse createDataload(String requestUserId, List<DataloadInput> dataloadInputs) throws Exception {
      return createDataload(requestUserId, dataloadInputs, Optional.empty());
   }

   public DataloadResponse createDataload(String requestUserId, List<DataloadInput> inputs, Optional<List<MergeInput>> mergeInputs) throws Exception {
      // Steps to create a new Dataload:
      // 1. For each input file and its associated metadata
      //    a. Create a dataset
      //    b. Add it to a dataframe 
      //    c. Upload its content
      // 2. Create a new "Data Load" assembly, add the dataframes to the assembly
      // 3. For each input file and its associated metadata:
      //    a. Convert to CSV if needed
      //    b. Perform optional PIMS load
      //    c. Perform optional column mapping
      //    d. Associate the assembly to each validation report if present
      // 4. For each merge specification:
      //    a. Set the mapping between the dataframes and the file variables in the merge key
      //    b. Perform the merge
      //    c. Perform optional column mapping with the merged dataframe
      //    d. Perform optional PIMS load with the merged dataframe
      // 5. If any dataframe has not undergone a transform, perform a no-op
      // 6. If successful, call recursive commit endpoint. Otherwise recursive delete.
      Set<String> datasetIds = new HashSet<>();
      List<String> dataTransformationIds = new ArrayList<>();
      // need a container object for this in case it fails in the middle, so we can roll things back (otherwise String is lost):
      StringBuilder assemblyIdContainer = new StringBuilder();
      try {
         String assemblyId = createDataload(requestUserId, inputs, mergeInputs, datasetIds, dataTransformationIds, assemblyIdContainer);
         commitDataload(requestUserId, assemblyId);

         DataloadResponse response = new DataloadResponse();
         response.setAssemblyId(assemblyId);
         response.setDataframeIds(datasetIds);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Dataload failed", e);
         try {
            if (!assemblyIdContainer.toString().isEmpty()) {
               log.error("Discarding created artifacts for assembly {}", assemblyIdContainer.toString());
               // Delete dataload will recursively delete anything under the DL assembly:
               deleteDataload(requestUserId, assemblyIdContainer.toString());
            } else {
               // But if we don't have an assemblyId, then only the datasets were created, so
               // we need to delete those individually:
               for (String datasetId : datasetIds) {
                  deleteEntity(requestUserId, datasetId);
               }
            }
         } catch (Exception e2) {
            // Avoid obscuring the original error:
            log.error("Dataload cleanup failed");
            throw new RuntimeException("Dataload failed and subsequent cleanup actions also failed", e2);
         }
         throw e;
      }
   }

   private String createDataload(String requestUserId, List<DataloadInput> inputs, Optional<List<MergeInput>> mergeInputs, Set<String> datasetIds,
         List<String> dataTransformationIds, StringBuilder assemblyIdContainer) throws Exception {
      log.debug("Creating new data load assembly...");
      List<DataloadTracker> dataloadTrackers = new ArrayList<>();
      for (DataloadInput input : inputs) {
         Dataframe dataframe = new Dataframe();
         setLoadDataframeMetadata(dataframe, input);

         // set comment at the dataframe level
         CommentInput comment = new CommentInput();
         comment.setBody(input.getDescription());
         List<CommentInput> comments = new ArrayList<CommentInput>();
         comments.add(comment);
         dataframe.setComments(comments);

         String dataframeId = addDataframe(requestUserId, dataframe);
         dataloadTrackers.add(new DataloadTracker(input, dataframeId));
         datasetIds.add(dataframeId);

         Dataset dataset = new Dataset();
         String datasetId = addDataset(requestUserId, dataframeId, dataset);

         MultipartFile fileContent = input.getFileContent();
         addDatasetContent(requestUserId, datasetId, fileContent.getBytes());

         log.info("Created new dataframe ID {}", dataframeId);
      }

      Assembly assembly = new Assembly();
      assembly.setAssemblyType("Data Load");
      assembly.setDataframeIds(datasetIds);
      Set<String> studyIds = new HashSet<String>();
      studyIds.add(inputs.get(0).getProgramId() + ":" + inputs.get(0).getProtocolId()); // should all be the same
      assembly.setStudyIds(studyIds);

      // set assembly level metadata
      List<Metadatum> metadata = new ArrayList<>();
      createKvp(metadata, "Load Status", "Complete");
      assembly.setMetadata(metadata);

      // set comment at the assembly level
      // assembly.setComments(new ArrayList<CommentInput>(Arrays.asList(new CommentInput("TODO: Assembly-level comment?"))));

      String assemblyId = addAssembly(requestUserId, assembly);
      List<String> assemblyIdsList = new ArrayList<String>();
      assemblyIdsList.add(assemblyId);
      // In case the job fails, need to update the container so it can be deleted one-level up.
      assemblyIdContainer.append(assemblyId);
      log.info("Created new assembly ID {}", assemblyId);

      for (DataloadTracker tracker : dataloadTrackers) {
         boolean performedTransform = false;
         // call converStringsion transform if necessary
         DataloadInput input = tracker.getInput();
         MultipartFile fileContent = input.getFileContent();
         String extension = FilenameUtils.getExtension(fileContent.getOriginalFilename()).toLowerCase();
         if (!extension.equalsIgnoreCase("csv")) {
            String dataframeId = convertFileToCsv(requestUserId, fileContent, Optional.of(tracker.getDataframeId()), Optional.of(assemblyId));
            tracker.setDataframeId(dataframeId);
            dataTransformationIds.add(dataframeId);
            performedTransform = true;
         }

         // PIMS load for individual files if needed
         String specificationType = input.getSpecificationType().toUpperCase();
         boolean isPkDef = specificationType.toUpperCase().contains("PKDEF");
         if (input.isDoPimsLoad()) {
            log.info("Performing individual file PIMS load for dataframe {}", tracker.getDataframeId());
            if (!isPkDef) {
               throw new RuntimeException("PIMS load on non-PKDEF file not supported.");
            }
            String pkterm = input.getPkterm();
            if (StringUtils.isNotEmpty(pkterm)) {
               // set the mapping
               String[] analytes = input.getAnalytes().split(";");
               PIMSDoseMapping mapping = new PIMSDoseMapping();
               mapping.setPkterm(pkterm);
               mapping.setDosep(analytes);

               // set the assembly and dataframe references
               PIMSLoadInfo loadInfo = new PIMSLoadInfo();
               loadInfo.setCreatedBy(requestUserId);
               loadInfo.setAssemblyId(assemblyId);
               loadInfo.setPkDefDataframeId(tracker.getDataframeId());
               loadInfo.setDosepMapping(new PIMSDoseMapping[] { mapping });

               String dataframeId = loadPimsData(requestUserId, input.getProgramId(), input.getProtocolId(), loadInfo);
               tracker.setDataframeId(dataframeId);
               dataTransformationIds.add(dataframeId);
               performedTransform = true;
            }
         }

         // call the compute service to perform re-mapping of columns if needed
         String mappingInformationListStr = input.getMappingInformationList();
         // check for empty string array
         if (mappingInformationListStr.equalsIgnoreCase("[]")) {
            // the script cannot take an empty array as a parameter
            mappingInformationListStr = "";
         }

         if (StringUtils.isNotEmpty(mappingInformationListStr)) {
            String dataframeId = mapColumns(requestUserId, mappingInformationListStr, tracker.getDataframeId(), assemblyId, performedTransform);
            tracker.setDataframeId(dataframeId);
            dataTransformationIds.add(dataframeId);
            performedTransform = true;
         }

         // save the assembly ID to the validation report that was used for this dataset
         String validationReportId = input.getValidationReportId();
         if (StringUtils.isNotEmpty(validationReportId)) {
            String validationReportPath = librarianService.getArtifactById(validationReportId).getArtifactPath();// build inputs map to simulate
                                                                                                                 // normal add artifact call
            Map<String, Object> libraryInputs = new HashMap<String, Object>();
            libraryInputs.put(InputBuilder.KEY_ASSEMBLY_ID, assemblyId);
            libraryInputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, validationReportPath + "/");
            LibraryInputBuilder builder = new LibraryInputBuilder();
            LibraryInput libInput = builder.build(requestUserId, libraryInputs);
            librarianService.updateArtifact(validationReportPath, libInput, requestUserId);
         }
      }

      String mergeScriptId = getScriptId(SCRIPT_MERGE, requestUserId, true);
      if (mergeInputs.isPresent()) {
         log.info("Merge key(s) detected, performing merge for dataload assembly {}", assemblyId);
         for (MergeInput mergeInput : mergeInputs.get()) {
            List<MergeKeyVariable> mergeKeyVariables = new ArrayList<MergeKeyVariable>();
            MergeKey mergeKey = new MergeKey();

            List<Integer> fileIndexes = mergeInput.getFileIndexes();

            List<String> dataframeIds = new ArrayList<>();
            for (Integer fileIndex : fileIndexes) {
               mergeKeyVariables.add(new MergeKeyVariable(String.format("file%d", fileIndex)));
               dataframeIds.add(dataloadTrackers.get(fileIndex - 1).getDataframeId());
            }

            mergeKey.setFiles(mergeKeyVariables);
            mergeKey.setJoins(mergeInput.getJoins());
            String mergeKeyString = mapper.writeValueAsString(mergeKey);
            KeyValuePairInput mergeKeyInput = new KeyValuePairInput("mergecrit", mergeKeyString, "string");
            Set<KeyValuePairInput> parameters = new HashSet<>(Arrays.asList(mergeKeyInput));

            log.info("Merging dataframes {}", dataframeIds);
            ComputeResponse computeResponse;
            // If any of the dataframes we are merging are the original datasets (no DT yet), need to pass in assembly ID to associate them properly
            if (datasetIds.stream().anyMatch(dataframeIds::contains)) {
               computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, mergeScriptId, parameters, dataframeIds, assemblyIdsList,
                     "Data Transformation", null, false, Optional.of("Merge"));
            } else {
               computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, mergeScriptId, parameters, dataframeIds, null,
                     "Data Transformation", null, false, Optional.of("Merge"));
            }
            String dataframeId = parseComputeResult(computeResponse, SCRIPT_MERGE, false);
            dataTransformationIds.add(dataframeId);

            // Set the trackers to the merged dataframe, so we know whether to do no-op later
            for (Integer fileIndex : fileIndexes) {
               dataloadTrackers.get(fileIndex - 1).setDataframeId(dataframeId);
            }

            // check for empty string array
            String mappingInformationListStr = mergeInput.getMappingInformationList();
            if (mappingInformationListStr.equalsIgnoreCase("[]")) {
               // the script cannot take an empty array as a parameter
               mappingInformationListStr = "";
            }

            if (StringUtils.isNotEmpty(mappingInformationListStr)) {
               dataTransformationIds.add(mapColumns(requestUserId, mappingInformationListStr, dataframeId, assemblyId, true));
            }

            if (mergeInput.isDoPimsLoad()) {
               log.info("Performing merged PIMS load for dataframe {}", dataframeId);
               String pkterm = mergeInput.getPkterm();
               if (StringUtils.isNotEmpty(pkterm)) {
                  // set the mapping
                  String[] analytes = mergeInput.getAnalytes().split(";");
                  PIMSDoseMapping mapping = new PIMSDoseMapping();
                  mapping.setPkterm(pkterm);
                  mapping.setDosep(analytes);

                  // set the assembly and dataframe references
                  PIMSLoadInfo loadInfo = new PIMSLoadInfo();
                  loadInfo.setCreatedBy(requestUserId);
                  loadInfo.setAssemblyId(assemblyId);
                  loadInfo.setPkDefDataframeId(dataframeId);
                  loadInfo.setDosepMapping(new PIMSDoseMapping[] { mapping });

                  dataTransformationIds.add(loadPimsData(requestUserId, mergeInput.getProgramId(), mergeInput.getProtocolId(), loadInfo));
               }
            }
         }
      }

      // check to see if a transform was performed, if not, we need to call the no-op
      // script
      for (DataloadTracker tracker : dataloadTrackers) {
         String dataframeId = tracker.getDataframeId();
         // If the tracker's current DF ID is a DS ID, then no DT was ever performed. Do noop.
         if (datasetIds.contains(dataframeId)) {
            dataframeId = callNoOpScript(requestUserId, dataframeId, assemblyId);
            tracker.setDataframeId(dataframeId);
            dataTransformationIds.add(dataframeId);
         }
      }

      return assemblyId;
   }

   public PromoteResponse promoteData(String requestUserId, PromoteInput input) throws Exception {
      // get the specification for the dataframe, so we can pull the SDEID columns list
      String dataframeId = input.getId();
      String dataStatus = input.getDataStatus();
      String dataBlindingStatus = input.getDataBlindingStatus();
      String commentStr = input.getComment();
      PromoteMode mode = input.getMode();
      ScriptDto[] scripts = input.getScripts();
      if (mode == PromoteMode.PROMOTE) {
         log.info("Promoting dataframe {}...", dataframeId);
         Dataframe dataframe = getDataframe(requestUserId, dataframeId);
         /*List<Metadatum> kvps = dataframe.getMetadata();
         String[] sdeIdColumns = null;
         for (Metadatum kvp : kvps) {
            String key = kvp.getKey();
            if (key.equalsIgnoreCase("Profile Configuration")) {
               if (kvp.getValue() != null && kvp.getValue().size() > 0) {
                  sdeIdColumns = kvp.getValue().get(0).split(",");
                  break;
               }
            }
         }

         if (sdeIdColumns == null) {
            // use a default configuration if none has been set
            sdeIdColumns = DEFAULT_PROFILE_CONFIGURATION;
         }*/

         // set comment at the dataframe level
         List<CommentInput> comments = new ArrayList<CommentInput>();
         if (StringUtils.isNotEmpty(commentStr)) {
            CommentInput comment = new CommentInput();
            comment.setCommentType("Status Change Comment");
            comment.setBody(commentStr);
            comments.add(comment);
            addCommentToEntity(requestUserId, dataframeId, comment);
         }

         // call the compute service for all the promotion scripts
         callPromoteScripts(requestUserId, dataframeId, dataStatus, dataBlindingStatus, comments, scripts);
      } else if (mode == PromoteMode.REVOKE) {
         log.info("Revoking promotion of dataframe {}...", dataframeId);
         Promotion promotion = new Promotion();
         promotion.setPromotionStatus(Promotion.TYPE_REVOKED);
         List<CommentInput> comments = new ArrayList<CommentInput>();
         CommentInput comment = new CommentInput();
         comment.setCommentType("Status Change Comment");
         comment.setBody(commentStr);
         comments.add(comment);
         promotion.setComments(comments.toArray(new CommentInput[0]));
         promoteDataframe(requestUserId, dataframeId, promotion);
      }

      PromoteResponse response = new PromoteResponse();
      response.setResponse(Response.OK);
      return response;
   }

   public ProfileUpdateReponse updateProfileConfig(String requestUserId, ProfileUpdateInput input) throws Exception {
      String profileRecalculateScriptId = getScriptId(SCRIPT_SDEID_RECALC, requestUserId, true);

      String dataframeId = input.getId();
      String[] profileConfig = input.getProfileConfig();
      String profileConfigStr = mapper.writeValueAsString(profileConfig);
      Dataframe dataframe = getDataframe(requestUserId, dataframeId);
      String equipId = dataframe.getEquipId();
      String dataBlindingStatus = dataframe.getDataBlindingStatus();

      KeyValuePairInput parameter = new KeyValuePairInput("profileConfig", profileConfigStr, "string");
      Set<KeyValuePairInput> parameters = new HashSet<KeyValuePairInput>();
      parameters.add(parameter);
      parameter = new KeyValuePairInput("dataBlindingStatus", dataBlindingStatus, "string");
      parameters.add(parameter);

      List<String> dataframeIdsList = new ArrayList<String>();
      dataframeIdsList.add(dataframeId);
      ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, profileRecalculateScriptId, parameters, dataframeIdsList,
            "Data Transformation", null);
      String childDataframeId = parseComputeResult(computeResponse, "SDEID Recalculation", false);

      // save the update profile config on the new dataframe
      dataframe = getDataframe(requestUserId, childDataframeId);
      List<Metadatum> metadata = dataframe.getMetadata();
      String metadataId = null;
      for (Metadatum data : metadata) {
         if (data.getKey().equals("Profile Configuration")) {
            metadataId = data.getId();
            break;
         }
      }

      KeyValuePairInput kvp = new KeyValuePairInput("Profile Configuration", String.join(",", profileConfig), "metadatum");
      if (metadataId != null) {
         updateMetadataKvp(requestUserId, metadataId, kvp);
      } else {
         addMetadataKvp(requestUserId, childDataframeId, kvp);
      }

      commitEntity(requestUserId, childDataframeId);
      log.info("Updated profile dataframe ID is {}.", childDataframeId);

      // Create audit entry
      auditService.insertAuditEntry(
            new AuditEntryInput("Updated profile configuration", equipId, EntityType.DATAFRAME.getValue(), requestUserId, ActionStatusType.SUCCESS, null));

      ProfileUpdateReponse response = new ProfileUpdateReponse();
      response.setResponse(Response.OK);
      response.setDataframeId(childDataframeId);
      return response;
   }

   public String convertFileToCsv(String requestUserId, MultipartFile fileContent, Optional<String> dataframeId, Optional<String> assemblyId) throws Exception {
      log.info("Converting file '{}' to csv...", fileContent.getOriginalFilename());
      // check the file extension to determine the file type to use
      String extension = FilenameUtils.getExtension(fileContent.getOriginalFilename()).toLowerCase();
      String scriptName = null;
      switch (extension) {
         case "csv":
            // just return the CSV file as a string
            return new String(fileContent.getBytes(), "UTF-8");
         case "xlsx":
            scriptName = SCRIPT_XLSX_TO_CSV;
            break;
         case "xpt":
            scriptName = SCRIPT_XPT_TO_CSV;
            break;
         default:
            throw new FileFormatNotSupportedException(String.format("The '%s' file format is not supported for conversion to CSV.", extension));
      }

      // call the librarian service to retrieve the GUID of the conversion script
      boolean dataframesPresent = dataframeId.isPresent() && assemblyId.isPresent();
      String scriptId = getScriptId(scriptName, requestUserId, dataframesPresent);

      // if we have pass in dataframe, we are calling compute in normal mode and it will get the content from the dataframe
      // if we do not, we are calling compute in virtual mode and passing in the content as a base64 encoded string
      if (dataframesPresent) {
         List<String> dataframeIdsList = new ArrayList<String>();
         dataframeIdsList.add(dataframeId.get());
         List<String> assemblyIdsList = new ArrayList<String>();
         assemblyIdsList.add(assemblyId.get());
         log.debug("Calling compute service to convert file...");
         ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, scriptId, null, dataframeIdsList, assemblyIdsList,
               "Data Transformation", null, false);
         String convertedDataframeId = parseComputeResult(computeResponse, "Convert " + scriptName, false);
         log.info("New converted dataframe ID is {}", convertedDataframeId);
         return convertedDataframeId;
      } else {
         // convert file to base 64 encoded string
         String base64File = new String(Base64.encode(fileContent.getBytes()));

         // set up call to compute
         ComputeParameterInput parameter = new ComputeParameterInput("filedata", base64File, "string");
         Set<ComputeParameterInput> parameters = new HashSet<ComputeParameterInput>();
         parameters.add(parameter);

         log.debug("Calling compute service in virtual mode to convert file...");
         ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, scriptId, parameters, null, null, "Data Transformation",
               null, true);
         base64File = parseComputeResult(computeResponse, "Convert " + scriptName, true);
         log.debug("Sucessfully converted file.");

         // decode base 64 response
         String csvFile = new String(Base64.decode(base64File.getBytes()), "UTF-8");
         return csvFile;
      }
   }

   // helper method modeled on the LibrarianController.addHiddenArtifact() method
   // to re-use the existing LibrarianService.addArtifact() method
   private String copyScriptToHiddenLibrary(LibraryArtifactResponse script, String userId) throws Exception {
      String artifactPath = "library/hidden/";
      String id = script.getArtifactId();
      ContentInfo info = librarianService.getArtifactContentById(id);
      byte[] content = info.getContent();
      String fileName = (String) script.getProperties().get(LibraryInput.PROPERTY_EQUIP_NAME);
      SimulatedMultipartFile fileContent = new SimulatedMultipartFile(content, fileName, info.getMimeType());

      // add to inputs map
      Map<String, Object> inputs = new HashMap<String, Object>();
      inputs.put(LibraryInput.PROPERTY_PRIMARY_TYPE, PrimaryType.SCRIPT.toString());
      inputs.put(LibraryInput.PROPERTY_EQUIP_NAME, fileName);
      inputs.put(LibraryInput.PROPERTY_ORIGINAL_EQUIP_NAME, script.getProperties().get(LibraryInput.PROPERTY_ORIGINAL_EQUIP_NAME));
      inputs.put(LibraryInput.PROPERTY_DESCRIPTION, script.getProperties().get(LibraryInput.PROPERTY_DESCRIPTION));
      inputs.put(LibraryInput.PROPERTY_COMMENTS, script.getProperties().get(LibraryInput.PROPERTY_COMMENTS));
      inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, fileContent);
      LibraryInputBuilder builder = new LibraryInputBuilder();
      LibraryInput input = builder.build(userId, inputs);

      LibraryArtifactResponse response = librarianService.addArtifact(artifactPath, input, userId, true);
      String newId = response.getArtifactId();

      // Create audit entry
      auditService.insertAuditEntry(new AuditEntryInput("Added Hidden Artifact. Artifact Name :" + info.getFileName(), newId, EntityType.ARTIFACT.getValue(), userId,
            ActionStatusType.SUCCESS, null));

      return newId;
   }

   public DatasetResponse getDataset(String complexDataId, Integer maxValues, String userId, boolean asCsv) throws IOException {
      if (maxValues == null || maxValues <= 0) {
         maxValues = 50; // default to 50 max values in value sets
      }
      String dataUrl = String.format(dataSetContentUrl, complexDataId);
      if (asCsv) {
         dataUrl = dataUrl + "?asCSV=true";
      }
      log.info("Retrieving dataset content from {}", dataUrl);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), userId);
      headers.add("Accept", RESPONSE_TYPE);
      byte[] content = null;
      Charset charset = null;
      try {
         ResponseEntity<byte[]> response = restTemplate.exchange(dataUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
         HttpHeaders responseHeaders = response.getHeaders();
         MediaType mediaType = responseHeaders.getContentType();
         charset = mediaType.getCharset();
         content = response.getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Dataset retrieval failed: {}", complexDataId);
         handleHttpError(e);
      }
      log.info("Retrived {} bytes", content.length);

      // trim the returned string from the dataframe service to remove empty rows
      if (charset == null) charset = StandardCharsets.UTF_8;
      String csvStr = new String(content, charset.name());
      csvStr = csvStr.trim();
      content = csvStr.getBytes();

      MappingIterator<String[]> iterator = csvMapper.readerFor(String[].class).with(schema).readValues(content);
      DatasetResponse response = new DatasetResponse();
      boolean firstRow = true;
      while (iterator.hasNext()) {
         if (firstRow) { // get column names
            String[] columnNames = iterator.next();
            for (String columnName : columnNames) {
               response.addColumn(columnName); // store the columnNames
               response.addValueSet(columnName, new TreeSet<Object>()); // set valueset columns
            }

            firstRow = false;
         } else { // row data
            LinkedHashMap<String, Object> row = new LinkedHashMap<>(); // keep iteration order
            String[] values = iterator.next();
            for (int i = 0; i < values.length; i++) {
               String columnName = response.getColumns().size() > 1 ? response.getColumns().get(i).getField() : response.getColumns().get(0).getField();
               String columnValue = values[i];
               row.put(columnName, columnValue);
               if (StringUtils.isNotBlank(columnValue) && response.getValueSet(columnName).size() < maxValues) {
                  response.getValueSet(columnName).add(columnValue);
               }
            }
            response.addRow(row);
         }
      }
      return response;
   }

   public String commitEntity(String requestUserId, String entityId) {
      String commitUrl = String.format(entityCommitUrl, entityId);

      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         log.debug("Committing entity with URL: {}", commitUrl);
         response = restTemplate.exchange(commitUrl, HttpMethod.PUT, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Commit failed.");
         handleHttpError(e);
      }
      return response;
   }

   public String addCommentToEntity(String requestUserId, String entityId, CommentInput comment) throws JsonProcessingException {
      String addCommentUrl = String.format(commentUrl, entityId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(comment);
      String commentId = null;
      try {
         commentId = restTemplate.exchange(addCommentUrl, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Comment add failed: {}", entityId);
         handleHttpError(e);
      }
      return commentId;
   }

   public String deleteEntity(String requestUserId, String entityId) {
      String deleteUrl = String.format(entityDeleteUrl, entityId);
      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         response = restTemplate.exchange(deleteUrl, HttpMethod.PUT, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Delete failed.");
         handleHttpError(e);
      }
      return response;
   }

   public String deleteDataload(String requestUserId, String dataloadId) {
      String deleteUrl = String.format(dataloadDeleteUrl, dataloadId);
      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         response = restTemplate.exchange(deleteUrl, HttpMethod.PUT, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Delete dataload failed.");
         handleHttpError(e);
      }
      return response;
   }

   public String commitDataload(String requestUserId, String dataloadId) {
      String commitUrl = String.format(dataloadCommitUrl, dataloadId);
      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         response = restTemplate.exchange(commitUrl, HttpMethod.PUT, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Commit dataload failed.");
         handleHttpError(e);
      }
      return response;
   }

   public Dataframe getDataframe(String requestUserId, String dataframeId) throws JsonProcessingException {
      log.info("Retrieving dataframe metadata for dataframe IDs {} for user {}", dataframeId, requestUserId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      JsonNode dataframeJson = null;
      try {
         dataframeJson = restTemplate.exchange(dataframeUrl + "/" + dataframeId, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Get dataframe failed: {}", dataframeId);
         handleHttpError(e);
      }
      Dataframe dataframe = mapper.treeToValue(dataframeJson, Dataframe.class);
      return dataframe;
   }

   public Set<Dataframe> getLineage(String requestUserId, String studyId) throws JsonProcessingException {
      log.info("Retrieving lineage metadata for equip ID {} for user {}", studyId, requestUserId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      URI uri = UriComponentsBuilder.fromUriString(lineageUrl + "/" + "analysisprep").queryParam("studyId", studyId).build().toUri();
      JsonNode dataframeJson = null;
      try {
         dataframeJson = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Get lineage failed: {}", studyId);
         handleHttpError(e);
      }
      Dataframe[] dataframes = mapper.treeToValue(dataframeJson, Dataframe[].class);
      return new HashSet<Dataframe>(Arrays.asList(dataframes));
   }

   public Set<Dataframe> getDataframes(String requestUserId, Collection<String> dataframeIds) throws JsonProcessingException {
      // "dataframes/9cca8dc8-73ef-441a-abab-ff77cf6d5382/data"
      log.info("Retrieving dataframe metadata for dataframe IDs {} from {} for user {}", dataframeIds, dataframeListUrl, requestUserId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(dataframeIds);
      JsonNode dataframeJson = null;
      try {
         dataframeJson = restTemplate.exchange(dataframeListUrl, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), JsonNode.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval of dataframes failed: {}", dataframeIds);
         handleHttpError(e);
      }
      Dataframe[] dataframes = mapper.treeToValue(dataframeJson, Dataframe[].class);
      return new HashSet<Dataframe>(Arrays.asList(dataframes));
   }

   public ContentInfo getComplexData(String requestUserId, String dataframeId) {
      // "dataframes/9cca8dc8-73ef-441a-abab-ff77cf6d5382/data"
      String dataframeUrl = String.format(datasetUrl, dataframeId);
      log.info("Retrieving dataframe complex data content from {} for user {}", dataframeUrl, requestUserId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      JsonNode jsonNode = null;
      try {
         jsonNode = restTemplate.exchange(dataframeUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval of complex data failed: {}", dataframeId);
         handleHttpError(e);
      }
      String complexDataId = jsonNode.get("complexDataId").asText();
      String dataUrl = String.format(dataSetContentUrl, complexDataId);
      ContentInfo contentInfo = retrieveContent(dataUrl, headers);
      return contentInfo;
   }

   public String addAssembly(String requestUserId, Assembly assembly) throws JsonProcessingException {
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(assembly);
      String assemblyId = null;
      try {
         assemblyId = restTemplate.exchange(assemblyUrl, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Creation of assembly failed: {}", assemblyId);
         handleHttpError(e);
      }
      return assemblyId;
   }

   public String promoteDataframe(String requestUserId, String dataframeId, Promotion promotion) throws JsonProcessingException {
      String promotionUrl = String.format(dataframePromoteUrl, dataframeId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(promotion);
      String promotionId = null;
      try {
         promotionId = restTemplate.exchange(promotionUrl, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Promotion of dataframe failed: {}", dataframeId);
         handleHttpError(e);
      }
      return promotionId;
   }

   public String addDataframe(String requestUserId, Dataframe dataframe) throws JsonProcessingException {
      return postJson(requestUserId, dataframeUrl, dataframe, String.class);
   }

   public String addDataset(String requestUserId, String dataframeId, Dataset dataset) throws JsonProcessingException {
      String url = String.format(datasetUrl, dataframeId);
      return postJson(requestUserId, url, dataset, String.class);
   }

   public String addDatasetContent(String requestUserId, String datasetId, byte[] bytes) throws JsonProcessingException {
      // https://stackoverflow.com/a/19108432
      MultiValueMap<String, Object> requestContent = new LinkedMultiValueMap<String, Object>();
      final String filename = "dataset";
      requestContent.add("name", filename);
      requestContent.add("filename", filename);
      ByteArrayResource contentsAsResource = new ByteArrayResource(bytes) {
         @Override
         public String getFilename() {
            return filename;
         }
      };
      requestContent.add("file", contentsAsResource);

      String url = String.format(dataSetContentUrl, datasetId);

      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("multipart", "form-data");
      headers.setContentType(mediaType);
      String complexDataId = null;
      try {
         complexDataId = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestContent, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Adding of dataset content failed: {}", datasetId);
         handleHttpError(e);
      }
      return complexDataId;
   }

   private void updateMetadataKvp(String requestUserId, String metadataId, KeyValuePairInput kvp) throws JsonProcessingException {
      log.info("Updating metadata for metadata ID{} for user {}", metadataId, requestUserId);
      String metadataUpdateUrl = String.format(metadataUrl, metadataId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(kvp);
      try {
         restTemplate.exchange(metadataUpdateUrl, HttpMethod.PUT, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Update of KVP failed: {}", metadataId);
         handleHttpError(e);
      }
   }

   private String addMetadataKvp(String requestUserId, String dataframeId, KeyValuePairInput kvp) throws JsonProcessingException {
      log.info("Adding metadata for dataframe ID{} for user {}", dataframeId, requestUserId);
      String metadataUpdateUrl = String.format(metadataUrl, dataframeId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(kvp);
      String metadataId = null;
      try {
         metadataId = restTemplate.exchange(metadataUpdateUrl, HttpMethod.PUT, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Update of KVP failed: {}", metadataId);
         handleHttpError(e);
      }
      return metadataId;
   }

   private <T> T postJson(String requestUserId, String url, Object object, Class<T> outputClass) throws JsonProcessingException {
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue = mapper.writeValueAsString(object);
      T response = null;
      try {
         response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), outputClass).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("POST-ing of JSON failed: {} : {}", url, jsonValue);
         handleHttpError(e);
      }
      return response;
   }

   // TODO: Is it OK to use ContentInfo here?
   private ContentInfo retrieveContent(String url, HttpHeaders headers) {
      log.debug("Retrieving content from {}", url);
      ResponseEntity<byte[]> response = null;
      try {
         response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval of dataframe content failed: {}", url);
         handleHttpError(e);
      }
      String fileName = response.getHeaders().getContentDisposition().getFilename();
      String mimeType = response.getHeaders().getContentType().toString();
      byte[] content = response.getBody();

      ContentInfo contentInfo = new ContentInfo(content, mimeType, fileName);
      return contentInfo;
   }

   private void handleHttpError(HttpStatusCodeException e) {
      if (e instanceof HttpClientErrorException) {
         throw new RemoteClientErrorException("Dataframe service returned 4xx (client error): " + e.getResponseBodyAsString());
      } else if (e instanceof HttpServerErrorException) {
         throw new RemoteServerErrorException("Dataframe service returned 5xx (server error): " + e.getResponseBodyAsString());
      } else {
         throw e;
      }
   }

   public String previewMerge(String requestUserId, List<MultipartFile> fileContentList, MergeInput mergeKeyInput) throws Exception {
      Set<KeyValuePairInput> parameters = new LinkedHashSet<>();
      List<MergeKeyVariable> mergeKeyVariables = new ArrayList<MergeKeyVariable>();

      MergeKey mergeKey = new MergeKey();

      mergeKey.setJoins(mergeKeyInput.getJoins());

      for (Integer fileIndex : mergeKeyInput.getFileIndexes()) {
         byte[] csvBytes = convertFileToCsv(requestUserId, fileContentList.get(fileIndex - 1), Optional.empty(), Optional.empty()).getBytes();
         String csvString = new String(csvBytes);
         KeyValuePairInput csvKvp = new KeyValuePairInput(String.format("file%d", fileIndex), csvString, "string");
         parameters.add(csvKvp);
         mergeKeyVariables.add(new MergeKeyVariable(String.format("file%d", fileIndex)));
      }
      mergeKey.setFiles(mergeKeyVariables);

      String mergeKeyString = mapper.writeValueAsString(mergeKey);
      KeyValuePairInput mergeKeyKvp = new KeyValuePairInput("mergecrit", mergeKeyString, "string");
      String mergeScriptId = getScriptId(SCRIPT_MERGE, requestUserId, false);
      parameters.add(mergeKeyKvp);

      List<String> dataframeIds = new ArrayList<>();
      ComputeResponse computeResponse = computeService.compute(requestUserId, ComputeService.CONTAINER_R_BASE, mergeScriptId, parameters, dataframeIds, null,
            "Data Transformation", null, true);
      String mergedCsv = parseComputeResult(computeResponse, SCRIPT_MERGE, true);
      return new String(Base64.decode(mergedCsv.getBytes()), "UTF-8");
   }

   public String getAuditTrailReportData(ReportDTO reportDTO) {
      String auditTrailReportData = null;
      String url = String.format(auditTrailReportUrl, reportDTO.getReportingEventId());
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), reportDTO.getUserId());
      headers.add(CLIENT_INFO_HEADER, reportDTO.getClientInfo());
      try {
         auditTrailReportData = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Audit trail report data retrieval failed for reporting event: {}", reportDTO.getReportingEventId());
         handleHttpError(e);
      }
      return auditTrailReportData;
   }

   public String getAnalysisQcData(ReportDTO reportDTO) {
      String analysisQcData = null;
      String url = String.format(qcAnalysisReportUrl, reportDTO.getAnalysisId());
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), reportDTO.getUserId());
      headers.add(CLIENT_INFO_HEADER, reportDTO.getClientInfo());
      try {
         analysisQcData = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Analysis QC data retrieval failed for analysis: {}", reportDTO.getAnalysisId());
         handleHttpError(e);
      }
      return analysisQcData;
   }

   public Assembly getAnalysisById(String requestUserId, String analysisId) throws JsonParseException, JsonMappingException, IOException {
      Assembly analysis = null;
      String url = String.format(analysisUrl, analysisId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         // TODO: report bug in DF where this response is "text/html;charset=utf-8" instead of application/json. Accept header doesn't help.
         String responseString = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
         analysis = mapper.readValue(responseString, Assembly.class);
      } catch (HttpStatusCodeException e) {
         log.error("Analysis retrieval failed for analysis: {}", analysisId);
         handleHttpError(e);
      }
      return analysis;
   }

   public Assembly getAssemblyById(String requestUserId, String assemblyId) throws JsonParseException, JsonMappingException, IOException {
      Assembly assembly = null;
      String url = String.format(assemblyGetUrl, assemblyId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         assembly = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Assembly.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval failed for assembly: {}", assemblyId);
         handleHttpError(e);
      }
      return assembly;
   }

   public String supersedeEntity(String requestUserId, String entityId) {
      String url = String.format(supersedeUrl, entityId);

      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      try {
         response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Supersede failed.");
         handleHttpError(e);
      }
      return response;
   }

   public String addReportingEventItem(ReportingEventItem reportingEventItem, String requestUserId) throws JsonProcessingException {
      return postJson(requestUserId, reportingEventItemUrl, reportingEventItem, String.class);
   }

   public String restoreEntity(String requestUserId, String entityId) throws JsonProcessingException {
      String url = String.format(entityUpdateUrl, entityId);
      String response = null;
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      Map<String, Boolean> requestData = new HashMap<>();
      requestData.put("equip:deleteFlag", false);
      String jsonValue = mapper.writeValueAsString(requestData);
      try {
         response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(jsonValue, headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Restore failed.");
         handleHttpError(e);
      }
      return response;
   }

   public List<Dataframe> getQcReportsByAnalysisId(String requestUserId, String analysisId) {
      String url = String.format(previousQcAnalysisReportUrl, analysisId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      Dataframe[] qcReports = null;
      try {
         qcReports = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Dataframe[].class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval failed for QC Report by Analysis Id: {}", analysisId);
         handleHttpError(e);
      }
      return Arrays.asList(qcReports);
   }

   public List<Dataframe> getAtrsByReportingEventId(String requestUserId, String reportingEventId) {
      String url = String.format(previousAtrUrl, reportingEventId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      Dataframe[] atrs = null;
      try {
         atrs = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Dataframe[].class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieval failed for ATR by reporting event ID: {}", reportingEventId);
         handleHttpError(e);
      }
      return Arrays.asList(atrs);
   }
}
