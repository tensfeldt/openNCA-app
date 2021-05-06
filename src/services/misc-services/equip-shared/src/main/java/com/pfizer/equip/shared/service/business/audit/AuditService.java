package com.pfizer.equip.shared.service.business.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.pfizer.equip.shared.service.user.UserRolesCache;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.relational.entity.AuditEntry;
import com.pfizer.equip.shared.relational.repository.AuditEntryRepository;
import com.pfizer.equip.shared.responses.AuditHistoryResponse;
import com.pfizer.equip.shared.responses.AuditHistoryResponseItem;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.exceptions.MissingRequiredAuditFieldException;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.audit.input.AuditFilterInput;
import com.pfizer.equip.shared.service.user.UserInfo;
import com.pfizer.equip.shared.service.user.UserLookupService;
import javax.annotation.PostConstruct;

@Service
public class AuditService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private AuditEntryRepository auditEntryRepo;

   public static final String SYSTEM_USER = "system";

   @Autowired
   SharedApplicationProperties applicationProperties;

   public static final String SYSTEM_ID = "nca";

   @Autowired
   private UserRolesCache userRolesCache;

   @PostConstruct
   public void initialize() {
      if (!applicationProperties.isStandaloneMode()) {
         userRolesCache.load();
      }
   }

   public long insertAuditEntry(AuditEntryInput input) throws JsonProcessingException, IOException, ExecutionException {
      return insertAuditEntry(input, SYSTEM_ID, true);
   }

   public long insertAuditEntry(AuditEntryInput input, boolean userIdRequired) throws JsonProcessingException, IOException, ExecutionException {
      return insertAuditEntry(input, SYSTEM_ID, userIdRequired);
   }

   public long insertAuditEntry(AuditEntryInput input, String systemId) throws JsonProcessingException, IOException, ExecutionException {
      return insertAuditEntry(input, systemId, true);
   }

   public long insertAuditEntry(AuditEntryInput input, String systemId, boolean userIdRequired) throws JsonProcessingException, IOException, ExecutionException {
      if (applicationProperties.isStandaloneMode()) {
         return 0;
      }

      if (input.getAction() == null || input.getAction().trim().isEmpty()) {
         throw new MissingRequiredAuditFieldException("'action' field is missing/blank");
      }
      if (input.getEntityId() == null || input.getEntityId().trim().isEmpty()) {
         throw new MissingRequiredAuditFieldException("'entityId' field is missing/blank");
      }
      if (input.getEntityType() == null || input.getEntityType().trim().isEmpty()) {
         throw new MissingRequiredAuditFieldException("'entityType' field is missing/blank");
      }
      if (userIdRequired && (input.getUserId() == null || input.getUserId().trim().isEmpty())) {
         throw new MissingRequiredAuditFieldException("'userId' field is missing/blank");
      }

      // Cannot be null/empty, checked above:
      String action = input.getAction();
      String entityId = input.getEntityId();
      String entityType = input.getEntityType();
      String userId = input.getUserId();

      // Used by df service:
      String scriptId = StringUtils.defaultIfBlank(input.getScriptId(), "");
      String contextEntityId = StringUtils.defaultIfBlank(input.getContextEntityId(), "");

      // Used by compute service:
      String hostname = StringUtils.defaultIfBlank(input.getHostname(), "");
      String operatingSystem = StringUtils.defaultIfBlank(input.getOperatingSystem(), "");
      String executionEngineName = StringUtils.defaultIfBlank(input.getExecutionEngineName(), "");
      Integer executionEngineVersion = input.getExecutionEngineVersion();
      String runtimeEnvironment = StringUtils.defaultIfBlank(input.getRuntimeEnvironment(), "");
      String runtimePath = StringUtils.defaultIfBlank(input.getRuntimePath(), "");
      String content = StringUtils.defaultIfBlank(input.getContent(), "");

      Set<String> roles = getUserRoles(systemId, userId);

      try {
         JSONObject json = new JSONObject(content);
         json.put("userRoles", new JSONArray(roles.toArray()));
         content = json.toString();
      } catch (Exception e) {
         if (content.equals("")) {
            JSONObject obj = new JSONObject();
            obj.put("userRoles", new JSONArray(roles.toArray()));
            content = obj.toString();
         } else {
            JSONObject obj = new JSONObject();
            obj.put("otherContent", content);
            obj.put("userRoles", new JSONArray(roles.toArray()));
            content = obj.toString();
         }
      }

      log.info("Adding new audit action '{}' for entity ID '{}' and entity type '{}' for user ID '{}'...", action, entityId, entityType, userId);

      // do the audit log call
      AuditEntry auditEntry = new AuditEntry();
      auditEntry.setAction(action);
      auditEntry.setSystemId(systemId);
      auditEntry.setEntityId(entityId);
      auditEntry.setEntityType(entityType);
      auditEntry.setCreateDate(new Date());
      if (userId != null && !userId.isEmpty() && !userId.equals(SYSTEM_USER)) {
         auditEntry.setUserId(userId);
         // perform user ID lookup to get additional user information
         UserInfo userInfo = getUserInfo(userId);
         auditEntry.setFirstName(userInfo.getFirstName());
         auditEntry.setLastName(userInfo.getLastName());
         auditEntry.setEmailAddress(userInfo.getEmailAddress());
      } else if (userId != null && userId.equals(SYSTEM_USER)) {
         auditEntry.setUserId(SYSTEM_USER);
         auditEntry.setFirstName("n/a");
         auditEntry.setLastName("n/a");
         auditEntry.setEmailAddress("n/a");
      } else if (userId == null) {
         // failed authentication, should only happen when SSO is bypassed (i.e., non-prod)
         auditEntry.setUserId("(unknown)");
         auditEntry.setFirstName("(unknown)");
         auditEntry.setLastName("(unknown)");
         auditEntry.setEmailAddress("(unknown)");
      }
      auditEntry.setActionStatus(input.getActionStatus());
      auditEntry.setEntityVersion(input.getEntityVersion());
      auditEntry.setScriptId(scriptId);
      auditEntry.setContextEntityId(contextEntityId);
      auditEntry.setHostname(hostname);
      auditEntry.setOperatingSystem(operatingSystem);
      auditEntry.setExecutionEngineName(executionEngineName);
      auditEntry.setExecutionEngineVersion(executionEngineVersion);
      auditEntry.setRuntimeEnvironment(runtimeEnvironment);
      auditEntry.setRuntimePath(runtimePath);
      auditEntry.setContent(content);
      auditEntryRepo.save(auditEntry);
      return auditEntry.getAuditEntryId();
   }

   private synchronized Set<String> getUserRoles(String systemId, String userId) {
      Set<String> roles;
      try {
         roles = userRolesCache.query(systemId + "/" + userId);
      } catch (Exception e) {
         log.info("user {} does not have access to system {}", userId, systemId);
         roles = new HashSet<>();
      }
      return roles;
   }
   
   private synchronized UserInfo getUserInfo(String userId) throws JsonProcessingException, IOException, ExecutionException {
      return userLookupService.lookupUser(userId);
   }

   public AuditHistoryResponse getAuditEntriesByEntity(String entityId, AuditFilterInput input) {
      String[] actions = input.getActionFilter();
      // version is stored as a VARCHAR in the database
      String currentVersion = input.getCurrentVersion().toString();
      List<AuditEntry> auditEntries = auditEntryRepo.findAuditEntriesWithFilter(entityId, actions, currentVersion);
      List<AuditHistoryResponseItem> responseItems = new ArrayList<AuditHistoryResponseItem>();
      for (AuditEntry auditEntry : auditEntries) {
         AuditHistoryResponseItem responseItem = new AuditHistoryResponseItem();
         responseItem.setId(auditEntry.getAuditEntryId());
         responseItem.setEquipId(auditEntry.getEntityId());
         responseItem.setVersionNumber(auditEntry.getEntityVersion());
         responseItem.setActionBy(auditEntry.getUserId());
         responseItem.setActionDate(auditEntry.getCreateDate());
         responseItem.setAction(auditEntry.getAction());
         responseItems.add(responseItem);
      }

      AuditHistoryResponse response = new AuditHistoryResponse();
      response.setAuditHistory(responseItems);
      response.setResponse(Response.OK);
      return response;
   }
}
