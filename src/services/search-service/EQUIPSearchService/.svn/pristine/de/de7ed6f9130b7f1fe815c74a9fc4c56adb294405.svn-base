package com.pfizer.equip.searchable.dto;

import java.time.Instant;
import java.util.Arrays;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetadatumData {
	/***
		- equip:deleteFlag (BOOLEAN) = 'false' autocreated COPY
	    - equip:versionNumber(LONG) COPY
	    - equip:versionSuperSeded(BOOLEAN) COPY
	    - equip:versionCommitted(BOOLEAN) COPY
	    - equip:key (STRING) COPY mandatory
		- equip:value (undefined) COPY multiple
		- equip:valueType (STRING) COPY
		+ equip:complexValue (nt:file) IGNORE

		  "self": "http://amrvlp000001137.pfizer.com:8080/modeshape-rest/equip/nca/items/Programs/X142/Protocols/X1421001/DataLoads/DF-1553620900038/equip%3ametadatum%5b2%5d",
		  "up": "http://amrvlp000001137.pfizer.com:8080/modeshape-rest/equip/nca/items/Programs/X142/Protocols/X1421001/DataLoads/DF-1553620900038",
		  "id": "2d4f3ab2-af20-4f8b-b6da-4595907c9cfb",
		  "equip:value": ["851"],
		  "jcr:primaryType": "equip:kvp",
		  "equip:valueType": "STRING",
		  "equip:key": "Data Load Row Count",
		  "equip:deleteFlag": "false"
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
	@SerializedName("equip:deleteFlag")
	private Boolean equipDeleteFlag;
	@Expose
	@SerializedName("equip:versionNumber")
	private Long equipVersionNumber;
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private Boolean equipVersionSuperSeded;
	@Expose
	@SerializedName("equip:versionCommitted")
	private Boolean equipVersionCommitted;
	@Expose
	@SerializedName("equip:key")
	private String equipKey;
	@Expose
	@SerializedName("equip:value")
	private String[] equipValue;
	@Expose
	@SerializedName("equip:valueType")
	private String equipValueType;

	
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
	public Boolean getEquipDeleteFlag() {
		return equipDeleteFlag;
	}
	public void setEquipDeleteFlag(Boolean equipDeleteFlag) {
		this.equipDeleteFlag = equipDeleteFlag;
	}
	public Long getEquipVersionNumber() {
		return equipVersionNumber;
	}
	public void setEquipVersionNumber(Long equipVersionNumber) {
		this.equipVersionNumber = equipVersionNumber;
	}
	public Boolean getEquipVersionSuperSeded() {
		return equipVersionSuperSeded;
	}
	public void setEquipVersionSuperSeded(Boolean equipVersionSuperSeded) {
		this.equipVersionSuperSeded = equipVersionSuperSeded;
	}
	public Boolean getEquipVersionCommitted() {
		return equipVersionCommitted;
	}
	public void setEquipVersionCommitted(Boolean equipVersionCommitted) {
		this.equipVersionCommitted = equipVersionCommitted;
	}
	public String getEquipKey() {
		return equipKey;
	}
	public void setEquipKey(String equipKey) {
		this.equipKey = equipKey;
	}
	public String[] getEquipValue() {
		return equipValue;
	}
	public void setEquipValue(String[] equipValue) {
		this.equipValue = equipValue;
	}
	public String getEquipValueType() {
		return equipValueType;
	}
	public void setEquipValueType(String equipValueType) {
		this.equipValueType = equipValueType;
	}
	@Override
	public String toString() {
		return "MetadatumData [jcrPath=" + jcrPath + ", jcrPrimaryType=" + jcrPrimaryType + ", equipCreatedBy="
				+ equipCreatedBy + ", equipCreated=" + equipCreated + ", equipModifiedBy=" + equipModifiedBy
				+ ", equipModified=" + equipModified + ", equipDeleteFlag=" + equipDeleteFlag + ", equipVersionNumber="
				+ equipVersionNumber + ", equipVersionSuperSeded=" + equipVersionSuperSeded + ", equipVersionCommitted="
				+ equipVersionCommitted + ", equipKey=" + equipKey + ", equipValue=" + Arrays.toString(equipValue)
				+ ", equipValueType=" + equipValueType + "]";
	}
	
}
