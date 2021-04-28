package com.pfizer.equip.lineage.dto;

import java.time.Instant;
import java.util.Arrays;

public class Promotion {
	private String id;
	private String equipId;
	private String dataStatus;
	private String promotionStatus;
	private String restrictionStatus;
	private Comment[] comments;
	private Metadata[] metadata;
	private Instant created;
	private String createdBy;
	private Instant modifiedDate;
	private String modifiedBy;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	public String getPromotionStatus() {
		return promotionStatus;
	}
	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}
	public String getRestrictionStatus() {
		return restrictionStatus;
	}
	public void setRestrictionStatus(String restrictionStatus) {
		this.restrictionStatus = restrictionStatus;
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
	@Override
	public String toString() {
		return "Promotion [id=" + id + ", equipId=" + equipId + ", dataStatus=" + dataStatus + ", promotionStatus="
				+ promotionStatus + ", restrictionStatus=" + restrictionStatus + ", comments="
				+ Arrays.toString(comments) + ", metadata=" + Arrays.toString(metadata) + ", created=" + created
				+ ", createdBy=" + createdBy + ", modifiedDate=" + modifiedDate + ", modifiedBy=" + modifiedBy + "]";
	}
}
