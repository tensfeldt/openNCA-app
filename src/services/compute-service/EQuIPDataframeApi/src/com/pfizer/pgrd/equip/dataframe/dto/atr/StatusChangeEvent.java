package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusChangeEvent {
	private String eventType;
	private Date eventDate;
	private String userId;
	private List<Comment> comments = new ArrayList<>();
	private String fileName;
	private String fileEquipId;
	private String fileId;
	private long fileVersion;
	private String reopenReason;
	
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileEquipId() {
		return fileEquipId;
	}
	public void setFileEquipId(String fileEquipId) {
		this.fileEquipId = fileEquipId;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public long getFileVersion() {
		return fileVersion;
	}
	public void setFileVersion(long fileVersion) {
		this.fileVersion = fileVersion;
	}
	public String getReopenReason() {
		return reopenReason;
	}
	public void setReopenReason(String reopenReason) {
		this.reopenReason = reopenReason;
	}
}