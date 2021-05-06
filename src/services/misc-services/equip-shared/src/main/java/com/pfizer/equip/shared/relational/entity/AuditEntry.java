package com.pfizer.equip.shared.relational.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pfizer.equip.shared.types.ActionStatusType;

@Entity
@Table(name = "audit_entry", schema = "equip_owner")
public class AuditEntry {
   @Id
   @Column(name = "audit_entry_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long auditEntryId;

   @Column(name = "system_key")
   private String systemId;

   @Column(name = "entity_id")
   private String entityId;

   @Column(name = "entity_type")
   private String entityType;

   @Column(name = "entity_version")
   private String entityVersion;

   @Column(name = "action", nullable = false)
   private String action;

   @Column(name = "action_status")
   @Enumerated(EnumType.STRING)
   private ActionStatusType actionStatus;

   @Column(name = "create_date", nullable = false)
   private Date createDate;

   @Column(name = "user_id", nullable = false)
   private String userId;

   @Column(name = "first_name", nullable = false)
   private String firstName;

   @Column(name = "last_name", nullable = false)
   private String lastName;

   @Column(name = "email_address", nullable = false)
   private String emailAddress;

   @Column(name = "script_id")
   private String scriptId;

   @Column(name = "context_entity_id")
   private String contextEntityId;

   @Column(name = "hostname")
   private String hostname;

   @Column(name = "operating_system")
   private String operatingSystem;

   @Column(name = "execution_engine_name")
   private String executionEngineName;

   @Column(name = "execution_engine_version")
   private Integer executionEngineVersion;

   @Column(name = "runtime_environment")
   private String runtimeEnvironment;

   @Column(name = "runtime_path")
   private String runtimePath;
   
   @Column(name = "content")
   private String content;

   public Long getAuditEntryId() {
      return auditEntryId;
   }

   public void setAuditEntryId(Long auditEntryId) {
      this.auditEntryId = auditEntryId;
   }

   public String getEntityId() {
      return entityId;
   }

   public void setEntityId(String entityId) {
      this.entityId = entityId;
   }

   public String getEntityType() {
      return entityType;
   }

   public void setEntityType(String entityType) {
      this.entityType = entityType;
   }

   public String getEntityVersion() {
      return entityVersion;
   }

   public void setEntityVersion(String entityVersion) {
      this.entityVersion = entityVersion;
   }

   public String getAction() {
      return action;
   }

   public void setAction(String action) {
      this.action = action;
   }

   public ActionStatusType getActionStatus() {
      return actionStatus;
   }

   public void setActionStatus(ActionStatusType actionStatus) {
      this.actionStatus = actionStatus;
   }

   public Date getCreateDate() {
      return createDate;
   }

   public void setCreateDate(Date createDate) {
      this.createDate = createDate;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getEmailAddress() {
      return emailAddress;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
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

   public String getSystemId() {
      return systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }
}
