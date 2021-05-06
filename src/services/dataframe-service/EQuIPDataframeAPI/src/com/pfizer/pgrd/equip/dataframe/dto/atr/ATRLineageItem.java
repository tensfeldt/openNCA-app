package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.Date;

public class ATRLineageItem {
	private int lineageIndex;
	private String eventItemName;
	private String equipId;
	private String status;
	private Date publishedDate;
	private String publishedBy;
	
	public int getLineageIndex() {
		return lineageIndex;
	}
	public void setLineageIndex(int lineageIndex) {
		this.lineageIndex = lineageIndex;
	}
	public String getEventItemName() {
		return eventItemName;
	}
	public void setEventItemName(String eventItemName) {
		this.eventItemName = eventItemName;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getPublishedDate() {
		return publishedDate;
	}
	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
	public String getPublishedBy() {
		return publishedBy;
	}
	public void setPublishedBy(String publishedBy) {
		this.publishedBy = publishedBy;
	}
}
