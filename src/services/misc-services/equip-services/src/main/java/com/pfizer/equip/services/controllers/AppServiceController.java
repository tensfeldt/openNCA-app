package com.pfizer.equip.services.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.InputBuilder;
import com.pfizer.equip.services.input.dataframe.DataloadInput;
import com.pfizer.equip.services.input.dataframe.DataloadInputBuilder;
import com.pfizer.equip.services.input.dataframe.MergeInput;
import com.pfizer.equip.services.input.dataframe.ProfileUpdateInput;
import com.pfizer.equip.services.input.dataframe.PromoteInput;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.UserAuthResponse;
import com.pfizer.equip.services.responses.dataframe.DataloadResponse;
import com.pfizer.equip.services.responses.dataframe.DatasetResponse;
import com.pfizer.equip.services.responses.dataframe.ProfileUpdateReponse;
import com.pfizer.equip.services.responses.dataframe.PromoteResponse;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class AppServiceController extends AbstractServicesController {

   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);
   private final static String SYSTEM_ID_NCA = "nca";

   @Autowired
   private DataframeService dataframeService;

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private EventService eventService;

   @Autowired
   private ApplicationProperties properties;

   @RequestMapping(value = "{systemId}/api/dataset/{id}", method = RequestMethod.GET)
   public DatasetResponse getDataset(@PathVariable String id, @RequestParam Map<String, String> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      Integer maxValues = null;
      boolean asCSV = false;
      try {
         if (inputs.get(InputBuilder.KEY_MAX_VALUES) != null) {
            maxValues = Integer.parseInt(inputs.get(InputBuilder.KEY_MAX_VALUES));
         }
         asCSV = Boolean.parseBoolean(inputs.get("asCSV"));
      } catch (NumberFormatException e) {
         log.warn("Invalid " + InputBuilder.KEY_MAX_VALUES + " value", e);
      }
      try {
         return dataframeService.getDataset(id, maxValues, getUserId(request), asCSV);
      } catch (Exception e) {
         log.error("Exception occured during Dataset retrieval", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/api/dataload/{programId}/{protocolId}", method = RequestMethod.POST)
   public DataloadResponse createDataLoad(@PathVariable("programId") String programId, @PathVariable("protocolId") String protocolId,
         @RequestParam Map<String, Object> inputs, @RequestParam("fileContent") MultipartFile fileContent, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_DATALOAD)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform create data load.", userId));
         }

         // add to inputs map as needed
         inputs.put(DataloadInput.KEY_PROGRAM_ID, programId);
         inputs.put(DataloadInput.KEY_PROTOCOL_ID, protocolId);
         inputs.put(DataloadInput.KEY_FILE_CONTENT, fileContent);
         DataloadInputBuilder builder = new DataloadInputBuilder();
         DataloadInput input = builder.build(userId, inputs);

         return dataframeService.createDataload(userId, input);
      } catch (Exception e) {
         log.error("Exception occurred during Dataload creation.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/api/multifile-dataload/{programId}/{protocolId}", method = RequestMethod.POST)
   public DataloadResponse createDataLoad(@PathVariable("programId") String programId, @PathVariable("protocolId") String protocolId,
         @RequestParam("fileMetadata") String inputsListString, @RequestParam("fileContent") List<MultipartFile> fileContentList, @RequestParam("mergeKey") Optional<String> mergeKeyString, HttpServletRequest request) {
      Monitor monitor = null;
      List<DataloadInput> dataloadInputs = null;
      DataloadResponse dataloadResponse = null;
      try {
         String userId = getUserId(request);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_DATALOAD)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform create data load.", userId));
         }

         // add to inputs map as needed
         ObjectMapper objectMapper = new ObjectMapper();
         dataloadInputs = new ArrayList<DataloadInput>();
         List<Map<String, Object>> inputsList = objectMapper.readValue(inputsListString, new TypeReference<List<Map<String, Object>>>(){});
         for(int idx = 0; idx < inputsList.size(); idx++) {
            Map<String, Object> inputs = inputsList.get(idx);
            MultipartFile fileContent = fileContentList.get(idx);
            inputs.put(DataloadInput.KEY_PROGRAM_ID, programId);
            inputs.put(DataloadInput.KEY_PROTOCOL_ID, protocolId);
            inputs.put(DataloadInput.KEY_FILE_CONTENT, fileContent);
            DataloadInputBuilder builder = new DataloadInputBuilder();
            dataloadInputs.add(builder.build(userId, inputs));
         }

         if (mergeKeyString.isPresent()) {
            List<MergeInput> mergeInputs = objectMapper.readValue(mergeKeyString.get(), new TypeReference<List<MergeInput>>(){});
            for (MergeInput mergeInput : mergeInputs) {
               mergeInput.setProgramId(programId);
               mergeInput.setProtocolId(protocolId);
            }
            dataloadResponse = dataframeService.createDataload(userId, dataloadInputs, Optional.of(mergeInputs));
         } else {
            dataloadResponse = dataframeService.createDataload(userId, dataloadInputs);
         }
         
         String equipId = dataframeService.getAssemblyById(userId, dataloadResponse.getAssemblyId()).getEquipId();
         Map<String, Object> description = createDataloadEventDescription(dataloadInputs, userId, "SUCCESS");
         eventService.createEvent(this.getClass().toString(), new Date(), equipId, EntityType.DATALOAD.toString(), "data_loading", protocolId, programId, description, properties.getEventQueue());
         auditService.insertAuditEntry(new AuditEntryInput("Data Loading", equipId, EntityType.DATALOAD.getValue(), userId, ActionStatusType.SUCCESS, "1"));
         return dataloadResponse;
      } catch (Exception e) {
         log.error("Exception occurred during Dataload creation.", e);
         try {
            Map<String, Object> description = createDataloadEventDescription(dataloadInputs, getUserId(request), String.format("FAILURE: %s", e.getMessage()));
            eventService.createEvent(this.getClass().toString(), new Date(), null, EntityType.DATALOAD.toString(), "data_loading", protocolId, programId, description, properties.getEventQueue());
            auditService.insertAuditEntry(new AuditEntryInput("Data Loading", "n/a", EntityType.DATALOAD.getValue(), getUserId(request), ActionStatusType.FAILURE, null));
         } catch (Exception e2) {
            // Intentionally silence this error so we can throw the real one below. 
            log.error("Dataload failure notification has failed", e2);
         }
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/api/multifile-dataload/preview-merge", method = RequestMethod.POST)
   @ResponseBody
   public String previewMerge(@RequestParam("fileContent") List<MultipartFile> fileContentList, @RequestParam("mergeKey") String mergeKeyString, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_DATALOAD)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform create data load.", userId));
         }

         ObjectMapper objectMapper = new ObjectMapper();
         MergeInput mergeKey = objectMapper.readValue(mergeKeyString, MergeInput.class);
         String mergedCsv = dataframeService.previewMerge(userId, fileContentList, mergeKey);
         return mergedCsv;

      } catch (Exception e) {
         log.error("Exception occurred during Dataload creation.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
   
   @RequestMapping(value = "{systemId}/api/convert", method = RequestMethod.POST)
   public String convertFileToCsv(@RequestParam("fileContent") MultipartFile fileContent, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         return dataframeService.convertFileToCsv(userId, fileContent, Optional.empty(), Optional.empty());
      } catch (Exception e) {
         log.error("Exception occurred during file conversion.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/api/promote", method = RequestMethod.POST)
   public PromoteResponse promoteData(@RequestBody PromoteInput promoteInput, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ALTER_DATA_PROMOTION)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform data promotion.", userId));
         }

         return dataframeService.promoteData(userId, promoteInput);
      } catch (Exception e) {
         log.error("Exception occurred during promotion.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
   
   @RequestMapping(value = "{systemId}/api/profile", method = RequestMethod.POST)
   public ProfileUpdateReponse updateProfile(@RequestBody ProfileUpdateInput profileUpdateInput, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_DATA_TRANSFORM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to update profile configuration.", userId));
         }

         return dataframeService.updateProfileConfig(userId, profileUpdateInput);
      } catch (Exception e) {
         log.error("Exception occurred during profile config update.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/api/user/auth", method = RequestMethod.GET)
   public UserAuthResponse userAuth(HttpServletRequest request) {
      Monitor monitor = null;
      UserAuthResponse userAuthResponse = new UserAuthResponse();
      try {
         String userId = getUserId(request);
         userAuthResponse.setUserId(userId);
         userAuthResponse.setPrivileges(userLookupService.getUserFunctions(SYSTEM_ID_NCA, userId));
         userAuthResponse.setRoles(userLookupService.getUserRoleNames(SYSTEM_ID_NCA, userId));
         userAuthResponse.setGroups(userLookupService.getUserGroupNames(userId));
         userAuthResponse.setUserInfo(userLookupService.lookupUser(userId));
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("User Login", userAuthResponse.getUserId(), EntityType.USER.getValue(), userId, ActionStatusType.SUCCESS, null));

         return userAuthResponse;
      } catch (Exception e) {
         log.error(e.getMessage(), e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
   
   private Map<String, Object> createDataloadEventDescription(List<DataloadInput> dataloadInputs, String userId, String statusError) {
         Map<String, Object> description = new HashMap<>();
         List<Map<String, Object>> dataloadInfos = new ArrayList<>();
         description.put("status_error", statusError);
         description.put("system_initiated", "false");
         description.put("user_name", userId);
         description.put("comments", "n/a");
         for (DataloadInput dataloadInput : dataloadInputs) {
            Map<String, Object> dataloadInfo = new HashMap<>();
            dataloadInfo.put("filename", dataloadInput.getFileContent().getOriginalFilename());
            dataloadInfo.put("specification", String.format("%s %s", dataloadInput.getSpecificationType(), dataloadInput.getSpecificationVersion()));
            dataloadInfo.put("description", dataloadInput.getDescription());
            dataloadInfo.put("number_rows", dataloadInput.getNumberRows());
            dataloadInfo.put("is_pims", dataloadInput.isDoPimsLoad());
            dataloadInfos.add(dataloadInfo);
         }
         description.put("dataload_infos", dataloadInfos);
         return description;
   }

}
