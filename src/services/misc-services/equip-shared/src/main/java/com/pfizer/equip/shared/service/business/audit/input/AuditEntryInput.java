package com.pfizer.equip.shared.service.business.audit.input;

import com.pfizer.equip.shared.types.ActionStatusType;

public class AuditEntryInput {
   private String action;
   private String entityId;
   private String entityType;
   private String userId;
  
   private ActionStatusType actionStatus;
   private String entityVersion;

   // Used by DF:
   private String scriptId;
   private String contextEntityId;

   // Used by Compute:
   private String hostname;
   private String operatingSystem;
   private String executionEngineName;
   private Integer executionEngineVersion;
   private String runtimeEnvironment;
   private String runtimePath;
   private String content;

   public AuditEntryInput() {
      // Retaining the default constructor for Jackson Json Mapper.
   }

   public AuditEntryInput(String action, String entityId, String entityType, String userId, ActionStatusType actionStatus, String entityVersion) {
      this.action = action;
      this.entityId = entityId;
      this.entityType = entityType;
      this.userId = userId;
      this.actionStatus = actionStatus;
      this.entityVersion = entityVersion;
   }

   public AuditEntryInput(String action, String entityId, String entityType, String userId, ActionStatusType actionStatus, String entityVersion, String scriptId, String contextEntityId) {
      this.action = action;
      this.entityId = entityId;
      this.entityType = entityType;
      this.userId = userId;
      this.actionStatus = actionStatus;
      this.entityVersion = entityVersion;
      this.scriptId = scriptId;
      this.contextEntityId = contextEntityId;
   }

   public String getAction() {
      return action;
   }

   public String getEntityId() {
      return entityId;
   }

   public String getEntityType() {
      return entityType;
   }

   public String getUserId() {
      return userId;
   }

   public void setAction(String action) {
      this.action = action;
   }

   public void setEntityId(String entityId) {
      this.entityId = entityId;
   }

   public void setEntityType(String entityType) {
      this.entityType = entityType;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public ActionStatusType getActionStatus() {
      return actionStatus;
   }

   public void setActionStatus(ActionStatusType actionStatus) {
      this.actionStatus = actionStatus;
   }

   public String getEntityVersion() {
      return entityVersion;
   }

   public void setEntityVersion(String entityVersion) {
      this.entityVersion = entityVersion;
   }

   public String getScriptId() {
      return scriptId;
   }

   public void setScriptId(String scriptId) {
      this.scriptId = scriptId;
   }

   public String getContextEntityId() {
      return contextEntityId;
   }

   public void setContextEntityId(String contextEntityId) {
      this.contextEntityId = contextEntityId;
   }

   public String getHostname() {
      return hostname;
   }

   public void setHostname(String hostname) {
      this.hostname = hostname;
   }

   public String getOperatingSystem() {
      return operatingSystem;
   }

   public void setOperatingSystem(String operatingSystem) {
      this.operatingSystem = operatingSystem;
   }

   public String getExecutionEngineName() {
      return executionEngineName;
   }

   public void setExecutionEngineName(String executionEngineName) {
      this.executionEngineName = executionEngineName;
   }

   public Integer getExecutionEngineVersion() {
      return executionEngineVersion;
   }

   public void setExecutionEngineVersion(Integer executionEngineVersion) {
      this.executionEngineVersion = executionEngineVersion;
   }

   public String getRuntimeEnvironment() {
      return runtimeEnvironment;
   }

   public void setRuntimeEnvironment(String runtimeEnvironment) {
      this.runtimeEnvironment = runtimeEnvironment;
   }

   public String getRuntimePath() {
      return runtimePath;
   }

   public void setRuntimePath(String runtimePath) {
      this.runtimePath = runtimePath;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }
}
