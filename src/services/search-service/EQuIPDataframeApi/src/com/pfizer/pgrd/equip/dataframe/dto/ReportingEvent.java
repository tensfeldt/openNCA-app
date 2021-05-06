package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipLockable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipSearchable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

public class ReportingEvent extends EquipObject
		implements EquipVersionable, EquipID, EquipCreatable, EquipModifiable, EquipCommentable, EquipMetadatable, EquipSearchable, EquipLockable {
	
	public static final String ENTITY_TYPE = Assembly.REPORTING_EVENT_TYPE;
	
	private List<Metadatum> metadata;
	
	// ReportingEvent specific
	private String reportingEventTemplateId = "";
	private String reportingEventName = "";
	private String reportingEventTypeId = "";
	private String reportingEventReleaseStatusKey;
	private String reportingEventReleaseDate;
	private String qcStatus;
	private boolean userHasAccess;
	private boolean atrIsCurrent;
	
	public ReportingEvent() {
		this.setEntityType(ReportingEvent.ENTITY_TYPE);
	}

	public boolean isUserHasAccess() {
		return userHasAccess;
	}

	public void setUserHasAccess(boolean userHasAccess) {
		this.userHasAccess = userHasAccess;
	}

	private List<String> reportingEventItemIds = new ArrayList<String>();

	private List<ReportingEventStatusChangeWorkflow> reportingEventStatusChangeWorkflows = new ArrayList<ReportingEventStatusChangeWorkflow>();
	
	// EquipCreatable
	private String createdBy;
	private Date created;

	// EquipModifiable
	private String modifiedBy;
	private Date modifiedDate;

	// EquipID
	private String equipId;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	// EquipLockable
	private boolean isLocked;
	private String lockedByUser;

	//EquipSearchable
	private boolean published = false;
	private boolean released = false;
	private String description;
	private String subType;
	
	// EquipVersionable
	private boolean isCommitted;
	private long versionNumber = 1;
	private boolean obsoleteFlag;
	private boolean versionSuperSeded;
	private boolean deleteFlag;

	private String name;
	private List<String> studyIds;
	private String itemType;

	public List<ReportingEventStatusChangeWorkflow> getReportingEventStatusChangeWorkflows() {
		return reportingEventStatusChangeWorkflows;
	}

	public void setReportingEventStatusChangeWorkflows(
			List<ReportingEventStatusChangeWorkflow> reportingEventStatusChangeWorkflows) {
		this.reportingEventStatusChangeWorkflows = reportingEventStatusChangeWorkflows;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}

	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}

	public Boolean getVersionSuperSeded() {
		return versionSuperSeded;
	}

	public void setVersionSuperSeded(Boolean superSeded) {
		this.versionSuperSeded = superSeded;
	}

//	public String getQcStatus() {
//		return qcStatus;
//	}
//
//	public void setQcStatus(String qcStatus) {
//		this.qcStatus = qcStatus;
//	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getEquipId() {
		return this.equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<String> getReportingEventItemIds() {
		return reportingEventItemIds;
	}

	public void setReportingEventItemIds(List<String> reportingEventItemIds) {
		this.reportingEventItemIds = reportingEventItemIds;
	}

	public String getReportingEventTemplateId() {
		return reportingEventTemplateId;
	}

	public void setReportingEventTemplateId(String reportingEventTemplateId) {
		this.reportingEventTemplateId = reportingEventTemplateId;
	}

	public String getReportingEventName() {
		return reportingEventName;
	}

	public void setReportingEventName(String reportingEventName) {
		this.reportingEventName = reportingEventName;
	}

	public String getReportingEventReleaseStatusKey() {
		return reportingEventReleaseStatusKey;
	}

	public void setReportingEventReleaseStatusKey(String reportingEventReleaseStatusKey) {
		this.reportingEventReleaseStatusKey = reportingEventReleaseStatusKey;
	}

	public String getReportingEventTypeId() {
		return reportingEventTypeId;
	}

	public void setReportingEventTypeId(String reportingEventTypeId) {
		this.reportingEventTypeId = reportingEventTypeId;
	}

	public String getReportingEventReleaseDate() {
		return reportingEventReleaseDate;
	}

	public void setReportingEventReleaseDate(String reportingEventReleaseDate) {
		this.reportingEventReleaseDate = reportingEventReleaseDate;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getStudyIds() {
		return studyIds;
	}

	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public Metadatum getMetadatum(String key) {
		Metadatum metadatum = null;
		if(this.metadata != null && key != null) {
			for(Metadatum md : this.metadata) {
				if(md.getKey().equalsIgnoreCase(key)) {
					metadatum = md;
					break;
				}
			}
		}
		
		return metadatum;
	}
	
	@Override
	public String getMetadatumValue(String key) {
		String value = null;
		Metadatum md = this.getMetadatum(key);
		if(md != null && md.getValue() != null && !md.getValue().isEmpty()) {
			return md.getValue().get(0);
		}
		
		return value;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	@Override
	public String getSubType() {
		return this.subType;
	}

	@Override
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	public boolean atrIsCurrent() {
		return this.atrIsCurrent;
	}
	
	public void setAtrIsCurrent(Boolean atrIsCurrent) {
		if(atrIsCurrent == null) {
			atrIsCurrent = false;
		}
		
		this.atrIsCurrent = atrIsCurrent;
	}

}
