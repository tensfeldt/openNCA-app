package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublishStatus {
	private String publishItemId;	
	private String publishedDate;
	private String publishStatus;
	
	private String modifiedBy;
	private Date modifiedDate;
	
	private List<Comment> comments = new ArrayList<Comment>();
	
	public Date expirationDate;
	public String publishedViewFilterCriteria;

	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getPublishedViewFilterCriteria() {
		return publishedViewFilterCriteria;
	}
	public void setPublishedViewFilterCriteria(String publishedViewFilterCriteria) {
		this.publishedViewFilterCriteria = publishedViewFilterCriteria;
	}
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
	public String getPublishItemId() {
		return publishItemId;
	}
	public void setPublishItemId(String publishItemId) {
		this.publishItemId = publishItemId;
	}
	public String getPublishedDate() {
		return publishedDate;
	}
	public void setPublishedDate(String publishDate) {
		this.publishedDate = publishDate;
	}
	public String getPublishStatus() {
		return publishStatus;
	}
	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
	}
}
