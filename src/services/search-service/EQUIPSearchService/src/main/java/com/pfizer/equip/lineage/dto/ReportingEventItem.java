package com.pfizer.equip.lineage.dto;

import java.time.Instant;

public class ReportingEventItem {
	private String id;
	private String nodeType = "ReportingEventItem";
	private String name;
	private String equipId;

	private String parentReportingEventId;
	private String dataframeId;
	private String assemblyId;
	private boolean included;

	private boolean deleteFlag;
	private boolean versionSuperSeded;
	private Long versionNumber;
	private boolean isCommitted;	// *
	private boolean obsoleteFlag;
	private Instant created;
	private String createdBy;
	private Instant modifiedDate;
	private String modifiedBy;

	private Comment[] comments;
	private Metadata[] metadata;
	private ReportingEventStatusChangeWorkflow[] reportingEventStatusChangeWorkflows;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getParentReportingEventId() {
		return parentReportingEventId;
	}
	public void setParentReportingEventId(String parentReportingEventId) {
		this.parentReportingEventId = parentReportingEventId;
	}
	public String getDataframeId() {
		return dataframeId;
	}
	public void setDataframeId(String dataframeId) {
		this.dataframeId = dataframeId;
	}
	public String getAssemblyId() {
		return assemblyId;
	}
	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}
	public boolean isIncluded() {
		return included;
	}
	public void setIncluded(boolean included) {
		this.included = included;
	}
	public boolean isDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public boolean isVersionSuperSeded() {
		return versionSuperSeded;
	}
	public void setVersionSuperSeded(boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}
	public Long getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(Long versionNumber) {
		this.versionNumber = versionNumber;
	}
	public boolean isCommitted() {
		return isCommitted;
	}
	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}
	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}
	public Instant getCreated() {
		return created;
	}
	public void setCreated(Instant created) {
		this.created = created;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Instant getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Instant modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Comment[] getComments() {
		return comments;
	}
	public void setComments(Comment[] comments) {
		this.comments = comments;
	}
	public Metadata[] getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata[] metadata) {
		this.metadata = metadata;
	}
	public ReportingEventStatusChangeWorkflow[] getReportingEventStatusChangeWorkflows() {
		return reportingEventStatusChangeWorkflows;
	}
	public void setReportingEventStatusChangeWorkflows(
			ReportingEventStatusChangeWorkflow[] reportingEventStatusChangeWorkflows) {
		this.reportingEventStatusChangeWorkflows = reportingEventStatusChangeWorkflows;
	}
	
}
