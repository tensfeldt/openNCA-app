package com.pfizer.equip.searchable.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores data for equip:searchable nodes
 * 
 * @author HeinemanWP
 *
 */
public class SearchableData {
	private transient String indexKey;
	@Expose
	@SerializedName("jcr:path")
	private String jcrPath;
	@Expose
	@SerializedName("jcr:uuid")
	private String jcrUuid;
	@Expose
	@SerializedName("jcr:name")
	private String jcrName;
	@Expose
	@SerializedName("jcr:primaryType")
	private String jcrPrimaryType;
	@Expose
	@SerializedName("jcr:mimeType")
	private String jcrMimeType;
	@Expose
	@SerializedName("jcr:created")
	private Instant jcrCreated;
	@Expose
	@SerializedName("jcr:lastModified")
	private Instant jcrLastModified;
	@Expose
	@SerializedName("equip:name")
	private String equipName;
	@Expose
	@SerializedName("equip:description")
	private String equipDescription;
	@Expose
	@SerializedName("equip:itemType")
	private String equipItemType;
	@Expose
	@SerializedName("equip:columnNames")
	private String[] equipColumnNames;
	@Expose
	@SerializedName("equip:published")
	private Boolean equipPublished;
	@Expose
	@SerializedName("equip:publishStatus")
	private String equipPublishStatus;
	@Expose
	@SerializedName("equip:released")
	private Boolean equipReleased;
	@Expose
	@SerializedName("equip:releaseStatus")
	private String equipReleaseStatus;
	@Expose
	@SerializedName("equip:equipId")
	private String equipEquipId;
	@Expose
	@SerializedName("equip:qcStatus")
	private String equipQcStatus;
	@Expose
	@SerializedName("equip:dataStatus")
	private String equipDataStatus;
	@Expose
	@SerializedName("equip:promotionStatus")
	private String equipPromotionStatus;
	@Expose
	@SerializedName("equip:studyId")
	private String[] equipStudyId;
	@Expose
	@SerializedName("equip:assemblyType")
	private String equipAssemblyType;
	@Expose
	@SerializedName("equip:dataframeType")
	private String equipDataframeType;
	@Expose
	@SerializedName("equip:subType")
	private String equipSubType;
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String equipRestrictionStatus;
	@Expose
	@SerializedName("equip:dataBlindingStatus")
	private String equipDataBlindingStatus;
	@Expose
	@SerializedName("equip:derivedDataStatus")
	private String equipDerivedDataStatus;
	@Expose
	@SerializedName("equip:versionNumber")
	private Long equipVersionNumber;
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private Boolean equipVersionSuperseded;
	@Expose
	@SerializedName("equip:versionCommitted")
	private Boolean equipVersionCommitted;	
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
	@SerializedName("equip:commentBody")
	private String[] equipComment;
	@Expose
	@SerializedName("equip:parentEquipId")
	private String equipParentEquipId;
	@Expose
	@SerializedName("equip:assemblyEquipIds")
	private String[] equipAssemblyEquipIds;
	@Expose
	@SerializedName("equip:dataframeEquipIds")
	private String[] equipDataframeEquipIds;
	@Expose
	@SerializedName("lastIndexed")
	private Instant lastIndexed;
	
	private String fileId;
	private String self;

	private Map<String, List<String>> rowValues = new HashMap<>();
	
	public String getIndexKey() {
		return indexKey;
	}
	public void setIndexKey(String indexKey) {
		this.indexKey = indexKey;
	}
	
	public String getJcrPath() {
		if ((jcrPath == null) && (self != null)) {
			jcrPath = self.substring(self.indexOf("items") + "items".length());
		}
		return jcrPath;
	}
	public void setJcrPath(String jcrPath) {
		this.jcrPath = jcrPath;
	}
	public String getJcrUuid() {
		return jcrUuid;
	}
	public void setJcrUuid(String jcrUuid) {
		this.jcrUuid = jcrUuid;
	}
	public String getJcrName() {
		return jcrName;
	}
	public void setJcrName(String jcrName) {
		this.jcrName = jcrName;
	}
	public String getJcrPrimaryType() {
		return jcrPrimaryType;
	}
	public void setJcrPrimaryType(String jcrPrimaryType) {
		this.jcrPrimaryType = jcrPrimaryType;
	}
	public String getJcrMimeType() {
		return jcrMimeType;
	}
	public void setJcrMimeType(String jcrMimeType) {
		this.jcrMimeType = jcrMimeType;
	}
	public Instant getJcrCreated() {
		return jcrCreated;
	}
	public void setJcrCreated(Instant jcrCreated) {
		this.jcrCreated = jcrCreated;
	}
	public Instant getJcrLastModified() {
		return jcrLastModified;
	}
	public void setJcrLastModified(Instant jcrLastModified) {
		this.jcrLastModified = jcrLastModified;
	}
	public String getEquipName() {
		return equipName;
	}
	public void setEquipName(String equipName) {
		this.equipName = equipName;
	}
	public String getEquipDescription() {
		return equipDescription;
	}
	public void setEquipDescription(String equipDescription) {
		this.equipDescription = equipDescription;
	}
	public String getEquipItemType() {
		return equipItemType;
	}
	public void setEquipItemType(String equipItemType) {
		this.equipItemType = equipItemType;
	}
	public String[] getEquipColumnNames() {
		return equipColumnNames;
	}
	public void setEquipColumnNames(String[] equipColumnNames) {
		this.equipColumnNames = equipColumnNames;
	}
	public Boolean getEquipPublished() {
		return equipPublished;
	}
	public void setEquipPublished(Boolean equipPublished) {
		this.equipPublished = equipPublished;
	}
	public String getEquipPublishStatus() {
		return equipPublishStatus;
	}
	public void setEquipPublishStatus(String equipPublishStatus) {
		this.equipPublishStatus = equipPublishStatus;
	}
	public Boolean getEquipReleased() {
		return equipReleased;
	}
	public void setEquipReleased(Boolean equipReleased) {
		this.equipReleased = equipReleased;
	}
	public String getEquipReleaseStatus() {
		return equipReleaseStatus;
	}
	public void setEquipReleaseStatus(String equipReleaseStatus) {
		this.equipReleaseStatus = equipReleaseStatus;
	}
	public String getEquipEquipId() {
		return equipEquipId;
	}
	public void setEquipEquipId(String equipEquipId) {
		this.equipEquipId = equipEquipId;
	}
	public String getEquipQcStatus() {
		return equipQcStatus;
	}
	public void setEquipQcStatus(String equipQcStatus) {
		this.equipQcStatus = equipQcStatus;
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
	public String[] getEquipStudyId() {
		return equipStudyId;
	}
	public void setEquipStudyId(String[] equipStudyId) {
		this.equipStudyId = equipStudyId;
	}
	public String getEquipAssemblyType() {
		return equipAssemblyType;
	}
	public void setEquipAssemblyType(String equipAssemblyType) {
		this.equipAssemblyType = equipAssemblyType;
	}
	public String getEquipDataframeType() {
		return equipDataframeType;
	}
	public void setEquipDataframeType(String equipDataframeType) {
		this.equipDataframeType = equipDataframeType;
	}
	public String getEquipSubType() {
		return equipSubType;
	}
	public void setEquipSubType(String equipSubType) {
		this.equipSubType = equipSubType;
	}
	public String getEquipRestrictionStatus() {
		return equipRestrictionStatus;
	}
	public void setEquipRestrictionStatus(String equipRestrictionStatus) {
		this.equipRestrictionStatus = equipRestrictionStatus;
	}
	public String getEquipDataBlindingStatus() {
		return equipDataBlindingStatus;
	}
	public void setEquipDataBlindingStatus(String equipDataBlindingStatus) {
		this.equipDataBlindingStatus = equipDataBlindingStatus;
	}
	public String getEquipDerivedDataStatus() {
		return equipDerivedDataStatus;
	}
	public void setEquipDerivedDataStatus(String equipDerivedDataStatus) {
		this.equipDerivedDataStatus = equipDerivedDataStatus;
	}
	public Long getEquipVersionNumber() {
		return equipVersionNumber;
	}
	public void setEquipVersionNumber(Long equipVersionNumber) {
		this.equipVersionNumber = equipVersionNumber;
	}
	public Boolean getEquipVersionSuperseded() {
		return equipVersionSuperseded;
	}
	public void setEquipVersionSuperseded(Boolean equipVersionSuperseded) {
		this.equipVersionSuperseded = equipVersionSuperseded;
	}
	public Boolean getEquipVersionCommitted() {
		return equipVersionCommitted;
	}
	public void setEquipVersionCommitted(Boolean equipVersionCommitted) {
		this.equipVersionCommitted = equipVersionCommitted;
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
	public String[] getEquipComment() {
		return equipComment;
	}
	public void setEquipComment(String[] equipComment) {
		this.equipComment = equipComment;
	}
	public void setEquipComment(List<String> equipComment) {
		setEquipComment(equipComment.toArray(new String[equipComment.size()]));
	}
	public void setEquipCommentFromCommentData(List<CommentData> comments) {
		List<String> commentBodies = new ArrayList<>();
		for (CommentData comment : comments) {
			commentBodies.add(comment.getEquipBody());
		}
		setEquipComment(commentBodies);
	}
	public String getParentEquipId() {
		return equipParentEquipId;
	}
	public void setParentEquipId(String parentEquipId) {
		this.equipParentEquipId = parentEquipId;
	}
	public String[] getAssemblyEquipIds() {
		return equipAssemblyEquipIds;
	}
	public void setAssemblyEquipIds(String[] assemblyEquipIds) {
		this.equipAssemblyEquipIds = assemblyEquipIds;
	}
	public String[] getDataframeEquipIds() {
		return equipDataframeEquipIds;
	}
	public void setDataframeEquipIds(String[] dataframeEquipIds) {
		this.equipDataframeEquipIds = dataframeEquipIds;
	}
	public Instant getLastIndexed() {
		return lastIndexed;
	}
	public void setLastIndexed(Instant lastIndexed) {
		this.lastIndexed = lastIndexed;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		if (fileId.length() > 36) {
			fileId = fileId.substring(14);
		}
		this.fileId = fileId;
	}
	public String getSelf() {
		return self;
	}
	public void setSelf(String self) {
		this.self = self;
	}

	public Map<String, List<String>> getRowValues() {
		return rowValues;
	}
	public void setRowValues(Map<String, List<String>> rowValues) {
		this.rowValues = rowValues;
	}

	public String getUniqueId() {
		if ((equipEquipId != null) && !equipEquipId.isEmpty()) {
			return equipEquipId;
		}
		return jcrUuid;
	}
	
	public SearchableData copy() {
		SearchableData returnValue = new SearchableData();
		returnValue.indexKey = indexKey;
		returnValue.jcrPath = jcrPath;
		returnValue.jcrUuid = jcrUuid;
		returnValue.jcrName = jcrName;
		returnValue.jcrPrimaryType = jcrPrimaryType;
		returnValue.jcrMimeType = jcrMimeType;
		returnValue.jcrCreated = jcrCreated;
		returnValue.jcrLastModified = jcrLastModified;
		returnValue.equipName = equipName;
		returnValue.equipDescription = equipDescription;
		returnValue.equipItemType = equipItemType;
		returnValue.equipColumnNames = equipColumnNames;
		returnValue.equipPublished = equipPublished;
		returnValue.equipPublishStatus = equipPublishStatus;
		returnValue.equipReleased = equipReleased;
		returnValue.equipReleaseStatus = equipReleaseStatus;
		returnValue.equipEquipId = equipEquipId;
		returnValue.equipQcStatus = equipQcStatus;
		returnValue.equipDataStatus = equipDataStatus;
		returnValue.equipPromotionStatus = equipPromotionStatus;
		returnValue.equipStudyId = equipStudyId;
		returnValue.equipAssemblyType = equipAssemblyType;
		returnValue.equipDataframeType = equipDataframeType;
		returnValue.equipSubType = equipSubType;
		returnValue.equipRestrictionStatus = equipRestrictionStatus;
		returnValue.equipDataBlindingStatus = equipDataBlindingStatus;
		returnValue.equipDerivedDataStatus = equipDerivedDataStatus;
		returnValue.equipVersionNumber = equipVersionNumber;
		returnValue.equipVersionSuperseded = equipVersionSuperseded;
		returnValue.equipVersionCommitted = equipVersionCommitted;
		returnValue.equipCreatedBy = equipCreatedBy;
		returnValue.equipCreated = equipCreated;
		returnValue.equipModifiedBy = equipModifiedBy;
		returnValue.equipModified = equipModified;
		returnValue.equipDeleteFlag = equipDeleteFlag;
		returnValue.equipComment = equipComment;
		returnValue.equipParentEquipId = equipParentEquipId;
		returnValue.equipAssemblyEquipIds = equipAssemblyEquipIds;
		returnValue.equipDataframeEquipIds = equipDataframeEquipIds;
		returnValue.lastIndexed = lastIndexed;
		returnValue.fileId = fileId;
		return returnValue;
	}

}
