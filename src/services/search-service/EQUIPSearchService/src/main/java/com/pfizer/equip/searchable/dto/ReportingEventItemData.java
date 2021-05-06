package com.pfizer.equip.searchable.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportingEventItemData {
	
	/***
		[equip:reportingEventItem] > equip:created, equip:version, equip:equipId, mix:versionable, equip:deletable
		    - equip:obsoleteFlag(BOOLEAN) COPY
		    - equip:dataframeId(STRING) COPY
		    - equip:assemblyId(STRING) COPY
		    - equip:included (BOOLEAN)    
		    - equip:name (STRING) COPY
		    - equip:parentReportingEventId (STRING) COPY
		    + equip:comment (equip:comment) IGNORE sns
		    + equip:metadatum (equip:kvp) IGNORE sns
		    + equip:Item (equip:publishedItem) IGNORE sns
		    + equip:reportingEventStatusChangeWorkflow (equip:reportingEventStatusChangeWorkflow) IGNORE sns
		    
            "equip:equipId": "REI25061",
            "jcr:created": 1558638809540,
            "equip:createdBy": "shelkovas",
            "jcr:uuid": "ad759293-2528-497e-b351-4949035ab098",
            "equip:modifiedBy": "shelkovas",
            "equip:deleteFlag": true,
            "equip:dataframeId": "bd319282-c15c-4965-9e65-53c8dd9a672d",
            "jcr:path": "/Programs/X142/Protocols/X1421001/ReportingEvents/REI-1558638809522",
            "equip:modified": 1558639547896,
            "jcr:lastModified": 1558639548120,
            "jcr:primaryType": "equip:reportingEventItem",
            "equip:assemblyId": "d0a5f1ba-10bb-4a7d-9942-91e1858fc9c7",
            "equip:created": 1558638809504,
            "equip:included": true
		    
	 ***/
	private static String studyRegex = "/[a-z A-Z 0-9 - _]+/([a-z A-Z 0-9 - _]+)/[a-z A-Z 0-9 - _]+/([a-z A-Z 0-9 \\s - _]+)/.+";
	private static Pattern studyPattern = Pattern.compile(studyRegex, Pattern.CASE_INSENSITIVE);
	
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
	@SerializedName("jcr:created")
	private Instant jcrCreated;
	@Expose
	@SerializedName("jcr:lastModified")
	private Instant jcrLastModified;
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
	@SerializedName("equip:obsoleteFlag")
	private Boolean equipObsoleteFlag;
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
	@SerializedName("equip:studyId")
	private String[] equipStudyId;
	@Expose
	@SerializedName("equip:equipId")
	private String equipEquipId;
	@Expose
	@SerializedName("equip:assemblyId")
	private String equipAssemblyId;
	@Expose
	@SerializedName("equip:assemblyEquipIds")
	private String[] equipAssemblyEquipIds;
	@Expose
	@SerializedName("equip:dataframeId")
	private String equipDataframeId;
	@Expose
	@SerializedName("equip:dataframeEquipIds")
	private String[] equipDataframeEquipIds;
	@Expose
	@SerializedName("equip:included")
	private Boolean equipIncluded;
	@Expose
	@SerializedName("equip:name")
	private String equipName;
	@Expose
	@SerializedName("equip:parentReportingEventId")
	private String equipParentReportingEventId;
	@Expose
	@SerializedName("equip:parentEquipId")
	private String equipParentEquipId;
	@Expose
	@SerializedName("equip:qcStatus")
	private String equipQcStatus;
	@Expose
	@SerializedName("equip:dataStatus")
	private String equipDataStatus;
	@Expose
	@SerializedName("equip:derivedDataStatus")
	private String equipDerivedDataStatus;
	@Expose
	@SerializedName("equip:promotionStatus")
	private String equipPromotionStatus;
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String equipRestrictionStatus;
	@Expose
	@SerializedName("equip:dataBlindingStatus")
	private String equipDataBlindingStatus;
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
	@SerializedName("equip:commentBody")
	private String[] equipComment;

	@Expose
	@SerializedName("lastIndexed")
	private Instant lastIndexed;

	private String fileId;
	private String self;
	
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
        // "jcr:path": "/Programs/X142/Protocols/X1421001/ReportingEvents/REI-1558638809522",
		Matcher m = studyPattern.matcher(jcrPath);
		if (m.matches()) {
			String studyId = String.format("%s:%s", m.group(1), m.group(2));
			String[] studyIds = { studyId };
			setEquipStudyId(studyIds);
		}
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
	public String getEquipEquipId() {
		return equipEquipId;
	}
	public void setEquipEquipId(String equipEquipId) {
		this.equipEquipId = equipEquipId;
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
	public Boolean getEquipObsoleteFlag() {
		return equipObsoleteFlag;
	}
	public void setEquipObsoleteFlag(Boolean equipObsoleteFlag) {
		this.equipObsoleteFlag = equipObsoleteFlag;
	}
	public Boolean getEquipVersionSuperseded() {
		return equipVersionSuperseded;
	}
	public void setEquipVersionSuperseded(Boolean equipVersionSuperseded) {
		this.equipVersionSuperseded = equipVersionSuperseded;
	}
	public Long getEquipVersionNumber() {
		return equipVersionNumber;
	}
	public void setEquipVersionNumber(Long equipVersionNumber) {
		this.equipVersionNumber = equipVersionNumber;
	}
	public Boolean getEquipVersionCommitted() {
		return equipVersionCommitted;
	}
	public void setEquipVersionCommitted(Boolean equipVersionCommitted) {
		this.equipVersionCommitted = equipVersionCommitted;
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
	public String getEquipDerivedDataStatus() {
		return equipDerivedDataStatus;
	}
	public void setEquipDerivedDataStatus(String equipDerivedDataStatus) {
		this.equipDerivedDataStatus = equipDerivedDataStatus;
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
	public String getEquipDataBlindingStatus() {
		return equipDataBlindingStatus;
	}
	public void setEquipDataBlindingStatus(String equipDataBlindingStatus) {
		this.equipDataBlindingStatus = equipDataBlindingStatus;
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
	
	public String[] getEquipStudyId() {
		if ((equipStudyId == null) && (jcrPath != null)) {
			Matcher m = studyPattern.matcher(jcrPath);
			if (m.matches()) {
				String studyId = String.format("%s:%s", m.group(1), m.group(2));
				String[] studyIds = { studyId };
				equipStudyId = studyIds;
			}
		}
		return equipStudyId;
	}
	public void setEquipStudyId(String[] equipStudyId) {
		this.equipStudyId = equipStudyId;
	}
	public String getEquipAssemblyId() {
		return equipAssemblyId;
	}
	public void setEquipAssemblyIds(String equipAssemblyId) {
		this.equipAssemblyId = equipAssemblyId;
	}
	public String[] getEquipAssemblyEquipIds() {
		return equipAssemblyEquipIds;
	}
	public void setEquipAssemblyEquipIds(String[] equipAssemblyEquipIds) {
		this.equipAssemblyEquipIds = equipAssemblyEquipIds;
	}
	public String getEquipDataframeId() {
		return equipDataframeId;
	}
	public void setEquipDataframeId(String equipDataframeId) {
		this.equipDataframeId = equipDataframeId;
	}
	public String[] getEquipDataframeEquipIds() {
		return equipDataframeEquipIds;
	}
	public void setEquipDataframeEquipIds(String[] equipDataframeEquipIds) {
		this.equipDataframeEquipIds = equipDataframeEquipIds;
	}
	public String getEquipParentEquipId() {
		return equipParentEquipId;
	}
	public Boolean getEquipIncluded() {
		return equipIncluded;
	}
	public void setEquipIncluded(Boolean equipIncluded) {
		this.equipIncluded = equipIncluded;
	}
	public String getEquipName() {
		return equipName;
	}
	public void setEquipName(String equipName) {
		this.equipName = equipName;
	}
	public String getEquipParentReportingEventId() {
		return equipParentReportingEventId;
	}
	public void setEquipParentReportingEventId(String equipParentReportingEventId) {
		this.equipParentReportingEventId = equipParentReportingEventId;
	}
	public void setEquipParentEquipId(String equipParentReportingEventEquipId) {
		this.equipParentEquipId = equipParentReportingEventEquipId;
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
	public String getDuplicateId() {
		if ((equipEquipId != null) && !equipEquipId.isEmpty()) {
			return equipEquipId;
		}
		return jcrUuid;
	}
	public Instant getLastIndexed() {
		return lastIndexed;
	}
	public void setLastIndexed(Instant lastIndexed) {
		this.lastIndexed = lastIndexed;
	}

	public ReportingEventItemData copy() {
		ReportingEventItemData returnValue = new ReportingEventItemData();
		returnValue.indexKey = indexKey;
		returnValue.jcrPath = jcrPath;
		returnValue.jcrUuid = jcrUuid;
		returnValue.jcrName = jcrName;
		returnValue.jcrPrimaryType = jcrPrimaryType;
		returnValue.jcrCreated = jcrCreated;
		returnValue.jcrLastModified = jcrLastModified;
		returnValue.equipAssemblyId = equipAssemblyId;
		returnValue.equipDataframeId = equipDataframeId;
		returnValue.equipParentReportingEventId = equipParentReportingEventId;
		returnValue.equipIncluded = equipIncluded;
		returnValue.equipName = equipName;
		returnValue.equipStudyId = equipStudyId;
		returnValue.equipEquipId = equipEquipId;
		returnValue.equipCreatedBy = equipCreatedBy;
		returnValue.equipCreated = equipCreated;
		returnValue.equipModifiedBy = equipModifiedBy;
		returnValue.equipModified = equipModified;
		returnValue.equipDeleteFlag = equipDeleteFlag;
		returnValue.equipObsoleteFlag = equipObsoleteFlag;
		returnValue.equipQcStatus = equipQcStatus;
		returnValue.equipDataStatus = equipDataStatus;
		returnValue.equipPromotionStatus = equipPromotionStatus;
		returnValue.equipRestrictionStatus = equipRestrictionStatus;
		returnValue.equipDataBlindingStatus = equipDataBlindingStatus;
		returnValue.equipDerivedDataStatus = equipDerivedDataStatus;
		returnValue.equipPublished = equipPublished;
		returnValue.equipPublishStatus = equipPublishStatus;
		returnValue.equipReleased = equipReleased;
		returnValue.equipReleaseStatus = equipReleaseStatus;
		returnValue.equipVersionNumber = equipVersionNumber;
		returnValue.equipVersionSuperseded = equipVersionSuperseded;
		returnValue.equipVersionCommitted = equipVersionCommitted;
		returnValue.equipComment = equipComment;
		returnValue.equipParentEquipId = equipParentEquipId;
		returnValue.equipAssemblyEquipIds = equipAssemblyEquipIds;
		returnValue.equipDataframeEquipIds = equipDataframeEquipIds;
		returnValue.lastIndexed = lastIndexed;
		returnValue.fileId = fileId;
		return returnValue;
	}
	
}
