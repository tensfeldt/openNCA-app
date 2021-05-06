package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

public class ReportingEventStatusChangeWorkflow extends EquipObject
		implements EquipCommentable, EquipCreatable, EquipModifiable, EquipMetadatable {
	
	public static final String ENTITY_TYPE = "Reporting Event Status Change Workflow";

	public static final String UNRELEASED_STATUS = "Unreleased";
	// ReportingEventStatusChangeWorkflow specific
	private String reportingEventId;
	private String reportingEventStatusWorkflowDescription;
	private String reportingEventReleaseStatus;
	private String reportingEventReopenReasonKey;
	private String reportingEventReopenReasonAttachmentId;

	public String getReportingEventReopenReasonAttachmentId() {
		return reportingEventReopenReasonAttachmentId;
	}

	public void setReportingEventReopenReasonAttachmentId(String reportingEventReopenReasonAttachmentId) {
		this.reportingEventReopenReasonAttachmentId = reportingEventReopenReasonAttachmentId;
	}

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipMetadatable
	List<Metadatum> metadata = new ArrayList<>();

	// EquipCreatable
	private String createdBy;
	private Date created;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	public ReportingEventStatusChangeWorkflow() {
		this.setEntityType(ReportingEventStatusChangeWorkflow.ENTITY_TYPE);
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getReportingEventId() {
		return reportingEventId;
	}

	public void setReportingEventId(String reportingEventId) {
		this.reportingEventId = reportingEventId;
	}

	public String getReportingEventStatusWorkflowDescription() {
		return reportingEventStatusWorkflowDescription;
	}

	public void setReportingEventStatusWorkflowDescription(String reportingEventStatusWorkflowDescription) {
		this.reportingEventStatusWorkflowDescription = reportingEventStatusWorkflowDescription;
	}

	public String getReportingEventReleaseStatus() {
		return reportingEventReleaseStatus;
	}

	public void setReportingEventReleaseStatusKey(String reportingEventReleaseStatus) {
		this.reportingEventReleaseStatus = reportingEventReleaseStatus;
	}

	public String getReportingEventReopenReasonKey() {
		return reportingEventReopenReasonKey;
	}

	public void setReportingEventReopenReasonKey(String reportingEventReopenReasonKey) {
		this.reportingEventReopenReasonKey = reportingEventReopenReasonKey;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}

	public void setReportingEventReleaseStatus(String reportingEventReleaseStatus) {
		this.reportingEventReleaseStatus = reportingEventReleaseStatus;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
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
}
