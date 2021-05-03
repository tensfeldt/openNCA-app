package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PromotedDataTransformation extends DataTransformation {
	private Comment promotionComment;
	
	private String dataStatus;
	private List<Comment> dataStatusComments = new ArrayList<>();
	
	private String dataReviewStatus;
	private List<Comment> dataReviewComments = new ArrayList<>();
	
	private boolean isLocked;
	private String dataBlindingStatus;
	private Date publishedDate;
	private String publishedBy;
	
	private List<String> scripts = new ArrayList<>();
	
	public Comment getPromotionComment() {
		return promotionComment;
	}
	public void setPromotionComment(Comment promotionComment) {
		this.promotionComment = promotionComment;
	}
	public String getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	public String getDataReviewStatus() {
		return dataReviewStatus;
	}
	public void setDataReviewStatus(String dataReviewStatus) {
		this.dataReviewStatus = dataReviewStatus;
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
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}
	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}
	public List<String> getScripts() {
		return scripts;
	}
	public void setScripts(List<String> scripts) {
		this.scripts = scripts;
	}
	public List<Comment> getDataStatusComments() {
		return dataStatusComments;
	}
	public void setDataStatusComments(List<Comment> dataStatusComments) {
		this.dataStatusComments = dataStatusComments;
	}
	public List<Comment> getDataReviewComments() {
		return dataReviewComments;
	}
	public void setDataReviewComments(List<Comment> dataReviewComments) {
		this.dataReviewComments = dataReviewComments;
	}
}
