package com.pfizer.pgrd.equip.services.audit;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuditEntryRequestBody {
	private String action;
	private String entityId;
	private String entityType;
	private Long entityVersion;
	private String userId;
	private String content;
	
	private String actionStatus;
	private String scriptId;
	private String contextEntityId;
	private String hostname;
	private String operatingSystem;
	private String executionEngineName;
	private String executionEngineVersion;
	private String runtimeEnvironment;
	private String runtimePath;

	//JAXB requires a constructor
	public AuditEntryRequestBody() {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// We don't have any logger defined for the EQuIPServiceAPI.
			// This will show up in the server logs, but should be
			// replaced when we add a logger to the project.
			e.printStackTrace();
		}
		operatingSystem = String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version"));
	}

	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
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

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getActionStatus() {
		return actionStatus;
	}

	public void setActionStatus(String actionStatus) {
		this.actionStatus = actionStatus;
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

	public void setHostname(String hostName) {
		this.hostname = hostName;
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

	public String getExecutionEngineVersion() {
		return executionEngineVersion;
	}

	public void setExecutionEngineVersion(String executionEngineVersion) {
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

	@Override
	public String toString() {
		return action + "|" + entityId + "|" + entityType + "|" + userId;
	}

	public Long getEntityVersion() {
		return entityVersion;
	}

	public void setEntityVersion(Long entityVersion) {
		this.entityVersion = entityVersion;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}

