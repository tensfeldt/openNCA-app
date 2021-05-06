package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Analysis {
	private String equipId;
	private long version;
	private Date createdDate;
	private String createdBy;
	private String currentStatus;
	private String method;
	private String lineage;
	private String legacyLineage;
	private List<Comment> comments = new ArrayList<>();
	private List<Subset> subsets = new ArrayList<>();
	private PublishedParameter publishedParameters;
	private String id;
	private boolean userHasAccess;
	private String wrapperScriptName;
	private Integer wrapperScriptVersion;
	private String computeEngineVersion;
	private String clientName;
	private String clientVersion;
	
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getLineage() {
		return lineage;
	}
	public void setLineage(String lineage) {
		this.lineage = lineage;
	}
	public String getLegacyLineage() {
		return legacyLineage;
	}
	public void setLegacyLineage(String legacyLineage) {
		this.legacyLineage = legacyLineage;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public List<Subset> getSubsets() {
		return subsets;
	}
	public void setSubsets(List<Subset> subsets) {
		this.subsets = subsets;
	}
	public PublishedParameter getPublishedParameters() {
		return publishedParameters;
	}
	public void setPublishedParameters(PublishedParameter publishedParameters) {
		this.publishedParameters = publishedParameters;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isUserHasAccess() {
		return userHasAccess;
	}
	public void setUserHasAccess(boolean userHasAccess) {
		this.userHasAccess = userHasAccess;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public String getWrapperScriptName() {
		return wrapperScriptName;
	}
	public void setWrapperScriptName(String wrapperScriptName) {
		this.wrapperScriptName = wrapperScriptName;
	}
	public Integer getWrapperScriptVersion() {
		return wrapperScriptVersion;
	}
	public void setWrapperScriptVersion(Integer wrapperScriptVersion) {
		this.wrapperScriptVersion = wrapperScriptVersion;
	}
	public String getComputeEngineVersion() {
		return computeEngineVersion;
	}
	public void setComputeEngineVersion(String computeEngineVersion) {
		this.computeEngineVersion = computeEngineVersion;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientVersion() {
		return clientVersion;
	}
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}
}
