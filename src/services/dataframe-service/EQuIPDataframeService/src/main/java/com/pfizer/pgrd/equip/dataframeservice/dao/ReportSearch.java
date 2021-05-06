package com.pfizer.pgrd.equip.dataframeservice.dao;

public class ReportSearch {
	private String parentAssemblyId;
	private String subType;
	private boolean includeUncommitted;
	private boolean includeDeleted;
	private boolean includeSuperseded;
	
	public String getParentAssemblyId() {
		return parentAssemblyId;
	}
	public void setParentAssemblyId(String parentAssemblyId) {
		this.parentAssemblyId = parentAssemblyId;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public boolean includeUncommitted() {
		return includeUncommitted;
	}
	public void setIncludeUncommitted(boolean isCommitted) {
		this.includeUncommitted = isCommitted;
	}
	public boolean includeSuperseded() {
		return includeSuperseded;
	}
	public void setIncludeSuperseded(boolean isSuperseded) {
		this.includeSuperseded = isSuperseded;
	}
	public boolean includeDeleted() {
		return includeDeleted;
	}
	public void setIncludeDeleted(boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
	}
}