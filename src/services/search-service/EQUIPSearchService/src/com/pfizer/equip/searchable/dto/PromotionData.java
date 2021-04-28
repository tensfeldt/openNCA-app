package com.pfizer.equip.searchable.dto;

import java.time.Instant;
import java.util.Arrays;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PromotionData {
	/***
		- equip:createdBy (STRING) COPY
		- equip:created (DATE) COPY
		- equip:modifiedBy (STRING) COPY 
		- equip:modified (DATE) COPY
	    - equip:equipId (STRING) COPY
		- equip:dataStatus (STRING) COPY 
		- equip:promotionStatus (STRING) COPY 
		- equip:restrictionStatus (STRING) COPY 
		+ equip:metadatum (equip:kvp) IGNORE sns
		+ equip:comment (equip:comment) IGNORE sns

        "pr.equip:dataStatus": "Final",
        "pr.equip:promotionStatus": "Promoted",
        "pr.equip:createdBy": "gavanr01",
        "pr.equip:created": "2019-03-26T17:21:46.067Z",
        "pr.jcr:primaryType": "equip:promotion",
        "pr.jcr:created": "2019-03-26T13:21:46.816-04:00",
        "pr.jcr:createdBy": "admin",
        "pr.jcr:lastModified": "2019-03-26T13:21:46.816-04:00",
        "pr.jcr:lastModifiedBy": "admin",
        "pr.mode:localName": "promotion",
        "pr.jcr:parentPath": "/Programs/X142/Protocols/X1421001/DataLoads/DF-1553620900038",
        "pr.mode:id": "92e57cae-b0e2-4057-8738-e68013f8e630",
        "pr.mode:depth": "7",
        "pr.jcr:score": "1.0",
        "pr.jcr:path": "/Programs/X142/Protocols/X1421001/DataLoads/DF-1553620900038/equip:promotion",
        "pr.jcr:name": "equip:promotion",
        "mode:uri": "http://amrvlp000001137.pfizer.com:8080/modeshape-rest/equip/nca/items/Programs/X142/Protocols/X1421001/DataLoads/DF-1553620900038/equip%3apromotion"
	 ***/
	private transient String indexKey;
	@Expose
	@SerializedName("jcr:path")
	private String jcrPath;
	@Expose
	@SerializedName("jcr:primaryType")
	private String jcrPrimaryType;
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
	@SerializedName("equip:dataStatus")
	private String equipDataStatus;
	@Expose
	@SerializedName("equip:promotionStatus")
	private String equipPromotionStatus;
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String equipRestrictionStatus;
	@Expose
	@SerializedName("equip:commentBody")
	private String[] equipComment;
	@Expose
	@SerializedName("lastIndexed")
	private Instant lastIndexed;
	

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
	public String getEquipDataStatus() {
		return equipDataStatus;
	}
	public void setEquipDataStatus(String equipDataStatus) {
		this.equipDataStatus = equipDataStatus;
	}
	public String getEquipPromotionStatus() {
		return equipPromotionStatus;
	}
	public void setEquipPromotionStatus(String equipPromotionStatus) {
		this.equipPromotionStatus = equipPromotionStatus;
	}
	public String getEquipRestrictionStatus() {
		return equipRestrictionStatus;
	}
	public void setEquipRestrictionStatus(String equipRestrictionStatus) {
		this.equipRestrictionStatus = equipRestrictionStatus;
	}
	public String[] getEquipComment() {
		return equipComment;
	}
	public void setEquipComment(String[] equipComment) {
		this.equipComment = equipComment;
	}
	@Override
	public String toString() {
		return "PromotionData [jcrPath=" + jcrPath + ", jcrPrimaryType=" + jcrPrimaryType + ", equipCreatedBy="
				+ equipCreatedBy + ", equipCreated=" + equipCreated + ", equipModifiedBy=" + equipModifiedBy
				+ ", equipModified=" + equipModified + ", equipDataStatus=" + equipDataStatus
				+ ", equipPromotionStatus=" + equipPromotionStatus + ", equipRestrictionStatus="
				+ equipRestrictionStatus + ", equipComment=" + Arrays.toString(equipComment) + "]";
	}
	
}
