package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.equip.utils.TypedValue;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

public class ParentData {
	private String modeshapeServer;
	private String modeshapeUsername;
	private String modeshapePassword;
	private String servicesServer;
	private Map<String, String> headers = new HashMap<>();
	private String username;
	private String subType;
	private List<String> equipIds = new ArrayList<>();
	private List<String> studyIds = new ArrayList<>();
	private List<String> dataframeIds = new ArrayList<>();
	private List<String> assemblyIds = new ArrayList<>();
	private List<String> protocolIds = new ArrayList<>();
	private List<String> projectIds = new ArrayList<>();
	private List<String> programIds = new ArrayList<>();
	private List<String> dataframeTypes = new ArrayList<>();
	private boolean useBatches;
	private boolean batch;
	private boolean rstudio;
	private Map<String, String> filenameToTypeMap = new HashMap<>();
	private List<String> profileConfig = new ArrayList<>();
	private String scriptId;
	private Script script;
	private Map<String, TypedValue> parameters = new HashMap<>();
	private String dataStatus;
	private String dataBlindingStatus;
	private String promotionStatus;
	private String qcStatus;
	private String restrictionStatus;
	private String derivedDataStatus;
	private List<Metadatum> metadata = new ArrayList<>();
	private List<Comment> comments = new ArrayList<>();
	private String stdIn;
	private String stdOut;
	private String stdErr;
	
	public String getModeshapeServer() {
		return modeshapeServer;
	}
	public void setModeshapeServer(String modeshapeServer) {
		this.modeshapeServer = modeshapeServer;
	}
	public String getModeshapeUsername() {
		return modeshapeUsername;
	}
	public void setModeshapeUsername(String modeshapeUsername) {
		this.modeshapeUsername = modeshapeUsername;
	}
	public String getModeshapePassword() {
		return modeshapePassword;
	}
	public void setModeshapePassword(String modeshapePassword) {
		this.modeshapePassword = modeshapePassword;
	}
	public String getServicesServer() {
		return servicesServer;
	}
	public void setServicesServer(String servicesServer) {
		this.servicesServer = servicesServer;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public List<String> getEquipIds() {
		return equipIds;
	}
	public void setEquipIds(List<String> equipIds) {
		this.equipIds = equipIds;
	}
	public List<String> getStudyIds() {
		return studyIds;
	}
	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}
	public List<String> getDataframeIds() {
		return dataframeIds;
	}
	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframeIds = dataframeIds;
	}
	public List<String> getAssemblyIds() {
		return assemblyIds;
	}
	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblyIds = assemblyIds;
	}
	public List<String> getProtocolIds() {
		return protocolIds;
	}
	public void setProtocolIds(List<String> protocolIds) {
		this.protocolIds = protocolIds;
	}
	public List<String> getProjectIds() {
		return projectIds;
	}
	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}
	public List<String> getProgramIds() {
		return programIds;
	}
	public void setProgramIds(List<String> programIds) {
		this.programIds = programIds;
	}
	public List<String> getDataframeTypes() {
		return dataframeTypes;
	}
	public void setDataframeTypes(List<String> dataframeTypes) {
		this.dataframeTypes = dataframeTypes;
	}
	public boolean isUseBatches() {
		return useBatches;
	}
	public void setUseBatches(boolean useBatches) {
		this.useBatches = useBatches;
	}
	public boolean isBatch() {
		return batch;
	}
	public void setBatch(boolean batch) {
		this.batch = batch;
	}
	public boolean isRstudio() {
		return rstudio;
	}
	public void setRstudio(boolean rstudio) {
		this.rstudio = rstudio;
	}
	public Map<String, String> getFilenameToTypeMap() {
		return filenameToTypeMap;
	}
	public void setFilenameToTypeMap(Map<String, String> filenameToTypeMap) {
		this.filenameToTypeMap = filenameToTypeMap;
	}
	public List<String> getProfileConfig() {
		return profileConfig;
	}
	public void setProfileConfig(List<String> profileConfig) {
		this.profileConfig = profileConfig;
	}
	public String getScriptId() {
		return scriptId;
	}
	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}
	public Script getScript() {
		return script;
	}
	public void setScript(Script script) {
		this.script = script;
	}
	public Map<String, TypedValue> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, TypedValue> parameters) {
		this.parameters = parameters;
	}
	public String getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}
	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}
	public String getPromotionStatus() {
		return promotionStatus;
	}
	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}
	public String getQcStatus() {
		return qcStatus;
	}
	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}
	public String getRestrictionStatus() {
		return restrictionStatus;
	}
	public void setRestrictionStatus(String restrictionStatus) {
		this.restrictionStatus = restrictionStatus;
	}
	public String getDerivedDataStatus() {
		return derivedDataStatus;
	}
	public void setDerivedDataStatus(String derivedDataStatus) {
		this.derivedDataStatus = derivedDataStatus;
	}
	public List<Metadatum> getMetadata() {
		return metadata;
	}
	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getStdIn() {
		return stdIn;
	}
	public void setStdIn(String stdIn) {
		this.stdIn = stdIn;
	}
	public String getStdOut() {
		return stdOut;
	}
	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}
	public String getStdErr() {
		return stdErr;
	}
	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}
}
