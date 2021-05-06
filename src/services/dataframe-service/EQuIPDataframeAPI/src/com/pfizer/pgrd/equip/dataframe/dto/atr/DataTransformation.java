package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataTransformation {
	private String equipId;
	private long version;
	private List<String> lineages = new ArrayList<>();
	private List<String> legacyLineages = new ArrayList<>();
	private Date createdDate;
	private String createdBy;
	private String method;
	private String currentStatus;
	private List<Comment> comments = new ArrayList<>();
	private String scriptName;
	private String scriptBody;
	private List<Subset> subsets = new ArrayList<>();
	private String id;
	private boolean userHasAccess;
	
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public List<String> getLineages() {
		return lineages;
	}
	public void setLineages(List<String> lineages) {
		this.lineages = lineages;
	}
	public List<String> getLegacyLineages() {
		return legacyLineages;
	}
	public void setLegacyLineages(List<String> legacyLineages) {
		this.legacyLineages = legacyLineages;
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
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public String getScriptBody() {
		return scriptBody;
	}
	public void setScriptBody(String scriptBody) {
		this.scriptBody = scriptBody;
	}
	public List<Subset> getSubsets() {
		return subsets;
	}
	public void setSubsets(List<Subset> subsets) {
		this.subsets = subsets;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean userHasAccess() {
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
}
