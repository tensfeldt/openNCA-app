package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReleaseStatus {
	private String reportingEventId;
	private String reportingEventReleaseStatusKey;
	private String reopenReason;
	private String reopenReasonAttachmentId;
	
	private String modifiedBy;
	private Date modifiedDate;
	
	private List<Comment> comments = new ArrayList<Comment>();

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getReopenReason() {
		return reopenReason;
	}

	public void setReopenReason(String reopenReason) {
		this.reopenReason = reopenReason;
	}

	public String getReopenReasonAttachmentId() {
		return reopenReasonAttachmentId;
	}

	public void setReopenReasonAttachmentId(String reopenReasonAttachmentId) {
		this.reopenReasonAttachmentId = reopenReasonAttachmentId;
	}

	public String getReportingEventReleaseStatusKey() {
		return reportingEventReleaseStatusKey;
	}

	public void setReportingEventReleaseStatusKey(String reportingEventReleaseStatusKey) {
		this.reportingEventReleaseStatusKey = reportingEventReleaseStatusKey;
	}

	public String getReportingEventId() {
		return reportingEventId;
	}

	public void setReportingEventId(String reportingEventId) {
		this.reportingEventId = reportingEventId;
	}
}
