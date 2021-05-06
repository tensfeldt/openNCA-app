package com.pfizer.equip.services.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.business.report.dto.ReportDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.api.dataframe.Assembly;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.business.api.exceptions.ComputeFailedException;
import com.pfizer.equip.services.business.report.ReportService;
import com.pfizer.equip.services.controllers.shared.SharedUtilties;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.report.ReportJobInput;
import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.ReportResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.relational.entity.ReportJob;
import com.pfizer.equip.shared.relational.entity.ReportJobOutput;
import com.pfizer.equip.shared.relational.repository.ReportJobRepository;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class ReportController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   // Should we get these from LibrarianService or keep them separate?
   final static String GLOBAL_LIBRARY_START = "library/global";
   final static String HIDDEN_LIBRARY_START = "library/hidden";
   public final static String SYSTEM_ID_NCA = "nca";
   private static final String CLIENT_INFO_HEADER = "Client-Info";

   @Autowired
   ReportService reportService;

   @Autowired
   UserLookupService userLookupService;

   @Autowired
   private AuditService auditService;

   @Autowired
   RepositoryService repositoryService;

   @Autowired
   DataframeService dataframeService;

   @Autowired
   private EventService eventService;

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   ReportJobRepository reportJobRepository;

   @RequestMapping(value = "{systemId}/reports/report/template/{reportTemplateId}", method = RequestMethod.POST)
   public ReportResponse generateReport(@PathVariable("systemId") String systemId, @PathVariable String reportTemplateId, @RequestBody ReportJobInput reportJobInput,
         HttpServletRequest request) {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_REPORT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform generate report.", userId));
         }

         log.info("Generating report with definition ID {} for user {}", reportTemplateId, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         ReportResponse response = reportService.generateReport(userId, reportTemplateId, reportJobInput.getDataframeIds(), reportJobInput.getParameters(),
               reportJobInput.getEquipId(), reportJobInput.getAssemblyIds());
         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportId());
         reportJobOutput.setOutputType("REPORT");
         reportJob.addReportJobOutput(reportJobOutput);
         setReportJob(reportJob, response);
         reportJobRepository.save(reportJob);
         // Create audit entry
         if (response.getResponse() == Response.OK && StringUtils.isNotEmpty(response.getReportId())) {
            Dataframe dataframe = dataframeService.getDataframe(userId, response.getReportId());
            auditService.insertAuditEntry(new AuditEntryInput("Report generated.", dataframe.getEquipId(), EntityType.REPORT.getValue(), userId, ActionStatusType.SUCCESS,
                  dataframe.getVersionNumber().toString(), reportTemplateId, null));
         } else if(response.getResponse() == Response.OK && StringUtils.isNotEmpty(response.getJobInfo().getBatch())) {
            auditService.insertAuditEntry(new AuditEntryInput("Report generated.", response.getJobInfo().getBatch(), EntityType.REPORT.getValue(), userId, ActionStatusType.SUCCESS,
                    response.getJobInfo().getBatch(), reportTemplateId, null));
         } else {
            auditService.insertAuditEntry(new AuditEntryInput("Report failed.", "n/a report generation failed", EntityType.REPORT.getValue(), userId,
                  ActionStatusType.SUCCESS, null, reportTemplateId, null));
         }
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of standard report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/report/atr/{reportingEventId}", method = RequestMethod.POST)
   public ReportResponse generateAuditTrailReport(@PathVariable("systemId") String systemId, @PathVariable("reportingEventId") String reportingEventId,
         HttpServletRequest request) throws Exception {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_PUBLISH_DATA_AUDIT_REPORT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to generate the audit trail report.", userId));
         }

         log.info("Generating audit trail report for reporting event ID {} for user {}", reportingEventId, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         String reportTemplateId = repositoryService.getIdByPath("/library/global/system-scripts/report-audit-trail.R");
         Optional<String> existingReportId = reportService.getAuditTrailReportByReportingEventId(reportingEventId, userId);
         ReportResponse response;

         ReportDTO reportDTO = new ReportDTO.ReportDTOBuilder()
                 .setUserId(userId)
                 .setReportingEventId(reportingEventId)
                 .setReportTemplateId(reportTemplateId)
                 .setExistingReportId(existingReportId)
                 .setClientInfo(request.getHeader(CLIENT_INFO_HEADER))
                 .build();

         response = reportService.generateAuditTrailReport(reportDTO);

         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportId());
         reportJobOutput.setOutputType("REPORT");
         reportJob.addReportJobOutput(reportJobOutput);
         setReportJob(reportJob, response);
         reportJobRepository.save(reportJob);
         // Create audit entry
         if (response.getResponse() == Response.OK) {
            String auditMessage = existingReportId.isPresent() ? "Audit Trail Report re-generated" : "First Audit Trail Report generated for this analysis.";
            auditService.insertAuditEntry(new AuditEntryInput(auditMessage, response.getEquipID(), EntityType.REPORT.getValue(), userId, ActionStatusType.SUCCESS,
                  response.getEquipVersion(), reportTemplateId, reportingEventId));
         } else {
            auditService.insertAuditEntry(new AuditEntryInput("Report failed.", "n/a report generation failed", EntityType.REPORT.getValue(), userId,
                  ActionStatusType.SUCCESS, null, reportTemplateId, reportingEventId));
            String stdout = response.getJobInfo().getStdout();
            String stderr = response.getJobInfo().getStderr();
            throw new ComputeFailedException(String.format("Compute failed for '%s' script with output '%s' and error '%s'", reportTemplateId, stdout, stderr));
         }
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of audit trail report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw e;
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/report/analysis-qc/{analysisId}", method = RequestMethod.POST)
   public ReportResponse generateAnalysisQcReport(@PathVariable("systemId") String systemId, @PathVariable("analysisId") String analysisId, HttpServletRequest request)
         throws Exception {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_REPORT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to generate the analysis QC report.", userId));
         }

         log.info("Generating Analysis QC report for reporting event ID {} for user {}", analysisId, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         String reportTemplateId = repositoryService.getIdByPath("/library/global/system-scripts/report-analysis-qc.R");
         ReportResponse response;
         log.debug("Checking for existing QC report...");
         Optional<String> existingReportId = reportService.getQcReportByAnalysisId(analysisId, userId);

         ReportDTO reportDTO = new ReportDTO.ReportDTOBuilder()
                 .setUserId(userId)
                 .setAnalysisId(analysisId)
                 .setReportTemplateId(reportTemplateId)
                 .setExistingReportId(existingReportId)
                 .setClientInfo(request.getHeader(CLIENT_INFO_HEADER))
                 .build();

         response = reportService.generateAnalysisQcReport(reportDTO);

         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportId());
         reportJobOutput.setOutputType("REPORT");
         reportJob.addReportJobOutput(reportJobOutput);
         setReportJob(reportJob, response);
         reportJobRepository.save(reportJob);
         // Create audit entry
         if (response.getResponse() == Response.OK) {
            String auditMessage = existingReportId.isPresent() ? "Analysis QC re-generated" : "First Analysis QC generated for this analysis.";
            auditService.insertAuditEntry(new AuditEntryInput(auditMessage, response.getEquipID(), EntityType.REPORT.getValue(), userId, ActionStatusType.SUCCESS,
                  response.getEquipVersion(), reportTemplateId, analysisId));
            Assembly assembly = dataframeService.getAssemblyById(userId, analysisId);
            Set<String> studyIds = assembly.getStudyIds();
            String programId = "";
            String protocolId = "";
            for (String studyId : studyIds) {
               String[] ids = studyId.split(":");
               programId = ids[0];
               protocolId = ids[1];
               break;
            }
            eventService.createEvent(this.getClass().toString(), new Date(), response.getEquipID(), EntityType.REPORT.toString(), "qc_report_generated", protocolId,
                  programId, createQcReportDescription(userId, protocolId), properties.getEventQueue());
         } else {
            auditService.insertAuditEntry(new AuditEntryInput("Report failed.", "n/a report generation failed", EntityType.REPORT.getValue(), userId,
                  ActionStatusType.SUCCESS, null, reportTemplateId, analysisId));
            String stdout = response.getJobInfo().getStdout();
            String stderr = response.getJobInfo().getStderr();
            throw new ComputeFailedException(String.format("Compute failed for '%s' script with output '%s' and error '%s'", reportTemplateId, stdout, stderr));
         }
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of Analysis QC report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw e;
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private Map<String, Object> createQcReportDescription(String userId, String protocolId) {
      Map<String, Object> description = new HashMap<>();
      description.put("user_name", userId);
      description.put("protocol_id", protocolId);
      return description;
   }

   @RequestMapping(value = "{systemId}/reports/reporting-item/node/{nodeId}", method = RequestMethod.POST)
   public ReportResponse generateReportingItemFromNode(@PathVariable("systemId") String systemId, @PathVariable String nodeId, HttpServletRequest request) {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADD_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform generate reporting item.", userId));
         }

         log.info("Generating reporting item  from node ID {} for dataframes {} for user {}", nodeId, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         ReportResponse response = reportService.generateReportingItemFromNode(userId, nodeId);

         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportingItemIds().iterator().next()); // only one reporting item
         reportJobOutput.setOutputType("REPORTING_ITEM");
         reportJob.addReportJobOutput(reportJobOutput);
         reportJob.setStatus(response.getResponse().toString());
         reportJob.setEndDate(new Date());
         reportJobRepository.save(reportJob);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Reporting Item generated From Node", response.getEquipID(), EntityType.REPORTING_ITEM.getValue(), userId,
               ActionStatusType.SUCCESS, response.getEquipVersion()));
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of standard report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/reporting-item/template/{reportingItemTemplateId}", method = RequestMethod.POST)
   public ReportResponse generateReportingItemFromTemplate(@PathVariable("systemId") String systemId, @PathVariable String reportingItemTemplateId,
         @RequestBody ReportJobInput reportJobInput, HttpServletRequest request) {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADD_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform generate reporting item.", userId));
         }

         log.info("Generating reporting item with template ID {} for dataframes {} for user {}", reportingItemTemplateId, reportJobInput.getDataframeIds(), userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         ReportResponse response = reportService.generateReportingItem(userId, reportingItemTemplateId, reportJobInput.getDataframeIds(), reportJobInput.getParameters(),
               reportJobInput.getEquipId(), reportJobInput.getAssemblyIds());

         for (String reportingItemId : response.getReportingItemIds()) {
            ReportJobOutput reportJobOutput = new ReportJobOutput();
            reportJobOutput.setOutputId(reportingItemId); // only one reporting item
            reportJobOutput.setOutputType("REPORTING_ITEM");
            reportJob.addReportJobOutput(reportJobOutput);
            // Create audit entry
            Dataframe dataframe = dataframeService.getDataframe(userId, reportingItemId);
            auditService.insertAuditEntry(new AuditEntryInput("Reporting Item generated from Template ID :" + reportingItemTemplateId, dataframe.getEquipId(),
                  EntityType.REPORTING_ITEM.getValue(), userId, ActionStatusType.SUCCESS, dataframe.getVersionNumber().toString()));
         }
         setReportJob(reportJob, response);
         reportJobRepository.save(reportJob);
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of standard report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/reporting-item/table/{tableName}", method = RequestMethod.POST)
   public ReportResponse generateReportingItemFromTable(@PathVariable("systemId") String systemId, @PathVariable String tableName,
         @RequestBody List<SearchCriteria> searchCriteria, HttpServletRequest request) {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADD_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform generate reporting item.", userId));
         }

         log.info("Generating reporting item  from table {} for user {}", tableName, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         ReportResponse response = reportService.generateReportingItemFromTable(userId, tableName, searchCriteria);

         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportingItemIds().iterator().next()); // only one reporting item
         reportJobOutput.setOutputType("REPORTING_ITEM");
         reportJob.addReportJobOutput(reportJobOutput);
         reportJob.setStatus(response.getResponse().toString());
         reportJob.setEndDate(new Date());
         reportJobRepository.save(reportJob);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Reporting Item generated From Table", response.getEquipID(), EntityType.REPORTING_ITEM.getValue(), userId,
               ActionStatusType.SUCCESS, response.getEquipVersion()));
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of standard report.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/reporting-item/lineage/{studyId}", method = RequestMethod.POST)
   public ReportResponse generateReportingItemFromLineage(@PathVariable("systemId") String systemId, @PathVariable String studyId, HttpServletRequest request) {
      Monitor monitor = null;
      ReportJob reportJob = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADD_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform generate reporting item.", userId));
         }

         log.info("Generating reporting item  from Study Id {} for user {}", studyId, userId);
         reportJob = new ReportJob();
         reportJobRepository.save(reportJob);

         // Run the report.
         ReportResponse response = reportService.generateReportingItemFromLineage(userId, studyId);

         ReportJobOutput reportJobOutput = new ReportJobOutput();
         reportJobOutput.setOutputId(response.getReportingItemIds().iterator().next()); // only one reporting item
         reportJobOutput.setOutputType("REPORTING_ITEM");
         reportJob.addReportJobOutput(reportJobOutput);
         reportJob.setStatus(response.getResponse().toString());
         reportJob.setEndDate(new Date());
         reportJobRepository.save(reportJob);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Reporting Item generated From Lineage", response.getEquipID(), EntityType.REPORTING_ITEM.getValue(), userId,
               ActionStatusType.SUCCESS, response.getEquipVersion()));
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during generation of standard report from Lineage.", e);
         if (reportJob != null) {
            reportJob.setStatus(Response.FAILED.toString());
            reportJob.setError(ExceptionUtils.getStackTrace(e));
            reportJob.setEndDate(new Date());
            reportJobRepository.save(reportJob);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/output/{outputId}", method = RequestMethod.GET)
   public ReportResponse getReportOutput(@PathVariable("systemId") String systemId, @PathVariable String outputId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Getting report output metadata '{}' for user ID '{}'...", outputId, userId);

         Dataframe reportOutput = reportService.getReportOutputById(userId, outputId);

         ReportResponse response = new ReportResponse();

         response.setReportOutput(reportOutput);

         return response;
      } catch (Exception e) {
         log.error("Exception occurred during retrieval of reporting artifact", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/output/programs/{programId}/protocols/{protocolId}", method = RequestMethod.GET)
   public ReportResponse getReportOutputByProtocol(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") String protocolId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         String artifactPath = String.format("/Programs/%s/Protocols/%s/Reports", programId, protocolId);
         log.info("Getting report output metadata '{}' for user ID '{}'...", artifactPath, userId);

         Set<Dataframe> reportOutput = reportService.getReportOutput(userId, artifactPath);

         ReportResponse response = new ReportResponse();
         response.setReportOutputs(reportOutput);

         return response;
      } catch (Exception e) {
         log.error("Exception occurred during retrieval of reporting artifact", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/output/{dataframeId}/content", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getReportOutputContentById(@PathVariable("dataframeId") String dataframeId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Getting content for report output with ID '{}' for user ID '{}'...", dataframeId, userId);

         ContentInfo contentInfo = reportService.getReportOutputContent(userId, dataframeId);
         // Adding an audit entry here as download of the report needs to be audited.
         Dataframe dataframe = dataframeService.getDataframe(userId, dataframeId);
         auditService.insertAuditEntry(new AuditEntryInput("Report downloaded for Dataframe", dataframe.getEquipId(), EntityType.REPORT.getValue(), userId,
               ActionStatusType.SUCCESS, dataframe.getVersionNumber().toString()));
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occurred during getting content of report artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/output/{reportingItemId}", method = RequestMethod.PUT)
   public ReportResponse commitReportingItem(@PathVariable("reportingItemId") String reportingItemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Commiting report output with ID '{}' for user ID '{}'...", reportingItemId, userId);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADD_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform commit reporting item.", userId));
         }
         ReportResponse response = reportService.commitReportingItem(userId, reportingItemId);
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput("Reporting Item Committed", response.getEquipID(), EntityType.REPORTING_ITEM.getValue(), userId, null, response.getEquipVersion()));
         return response;
      } catch (Exception e) {
         log.error("Exception occurred during getting content of report artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/reports/output/{reportingItemId}", method = RequestMethod.DELETE)
   public ReportResponse deleteReportingItem(@PathVariable("reportingItemId") String reportingItemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Deleting report output with ID '{}' for user ID '{}'...", reportingItemId, userId);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.DROP_ANY_REPORT_ITEM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform delete reporting item.", userId));
         }
         ReportResponse response = reportService.deleteReportingItem(userId, reportingItemId);
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Reporting Item Deleted", response.getEquipID(), EntityType.REPORTING_ITEM.getValue(), userId,
               ActionStatusType.SUCCESS, response.getEquipVersion()));

         return response;
      } catch (Exception e) {
         log.error("Exception occurred during getting content of report artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private void setReportJob(ReportJob reportJob, ReportResponse reportResponse) {
      reportJob.setStatus(reportResponse.getResponse().toString());
      reportJob.setComputeStderr(reportResponse.getJobInfo().getStderr());
      reportJob.setComputeStdout(reportResponse.getJobInfo().getStdout());
      reportJob.setComputeStdin(reportResponse.getJobInfo().getStdin());
      reportJob.setEndDate(new Date());
   }
}
