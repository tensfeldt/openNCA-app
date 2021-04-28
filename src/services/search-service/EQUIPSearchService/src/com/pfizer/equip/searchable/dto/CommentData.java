package com.pfizer.equip.searchable.dto;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores equip:comment data
 * 
 * @author HeinemanWP
 *
 */
public class CommentData {
	private transient String indexKey;
	@Expose
	@SerializedName("jcr:path")
	private String jcrPath;
	@Expose
	@SerializedName("jcr:primaryType")
	private String jcrPrimaryType;
	@Expose
	@SerializedName("equip:commentType")
	private String equipCommentType;
	@Expose
	@SerializedName("equip:body")
	private String equipBody;
	@Expose
	@SerializedName("equip:createdBy")
	private String equipCreatedBy;
	@Expose
	@SerializedName("equip:created")
	private Instant equipCreated;
	@Expose
	@SerializedName("equip:modifiedBy")
	private String equipModifiedBy;
	@Expose
	@SerializedName("equip:modified")
	private Instant equipModified;
	@Expose
	@SerializedName("equip:deleteFlag")
	private Boolean equipDeleteFlag;

	
	public String getJcrPath() {
		return jcrPath;
	}
	public void setJcrPath(String jcrPath) {
		this.jcrPath = jcrPath;
	}
	public String getJcrPrimaryType() {
		return jcrPrimaryType;
	}
	public void setJcrPrimaryType(String jcrPrimaryType) {
		this.jcrPrimaryType = jcrPrimaryType;
	}
	public String getIndexKey() {
		return indexKey;
	}
	public void setIndexKey(String indexKey) {
		this.indexKey = indexKey;
	}
	public String getEquipCommentType() {
		return equipCommentType;
	}
	public void setEquipCommentType(String equipCommentType) {
		this.equipCommentType = equipCommentType;
	}
	public String getEquipBody() {
		return equipBody;
	}
	public void setEquipBody(String equipBody) {
		this.equipBody = equipBody;
	}
	public String getEquipCreatedBy() {
		return equipCreatedBy;
	}
	public void setEquipCreatedBy(String equipCreatedBy) {
		this.equipCreatedBy = equipCreatedBy;
	}
	public Instant getEquipCreated() {
		return equipCreated;
	}
	public void setEquipCreated(Instant equipCreated) {
		this.equipCreated = equipCreated;
	}
	public String getEquipModifiedBy() {
		return equipModifiedBy;
	}
	public void setEquipModifiedBy(String equipModifiedBy) {
		this.equipModifiedBy = equipModifiedBy;
	}
	public Instant getEquipModified() {
		return equipModified;
	}
	public void setEquipModified(Instant equipModified) {
		this.equipModified = equipModified;
	}
	public Boolean getEquipDeleteFlag() {
		return equipDeleteFlag;
	}
	public void setEquipDeleteFlag(Boolean equipDeleteFlag) {
		this.equipDeleteFlag = equipDeleteFlag;
	}
	@Override
	public String toString() {
		return "CommentData [indexKey=" + indexKey + ", jcrPath=" + jcrPath + ", jcrPrimaryType=" + jcrPrimaryType
				+ ", equipCommentType=" + equipCommentType + ", equipBody=" + equipBody + ", equipCreatedBy="
				+ equipCreatedBy + ", equipCreated=" + equipCreated + ", equipModifiedBy=" + equipModifiedBy
				+ ", equipModified=" + equipModified + ", equipDeleteFlag=" + equipDeleteFlag + "]";
	}
	
}
