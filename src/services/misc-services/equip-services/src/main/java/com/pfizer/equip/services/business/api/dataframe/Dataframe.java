package com.pfizer.equip.services.business.api.dataframe;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.services.business.api.input.CommentInput;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.service.business.api.Metadatum;

// Class for storing dataframes returned or passed from Dataframe Service
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataframe {
   private String id;
   private String dataframeType;
   private String promotionStatus;
   private String dataBlindingStatus;
   private String restrictionStatus;
   private String studyBlindingStatus = StudyBlindingStatus.BLINDED.getValue();
   private String createdBy;
   private Date createdDate;
   private String modifiedBy;
   private Date modifiedDate;
   private String equipId;
   private String dataStatus;
   private String qcStatus;
   private String name;
   private String itemType;
   private Boolean deleteFlag;
   private Boolean obsoleteFlag;
   private Set<String> profileConfig;
   private Boolean isCommitted;
   private Set<String> dataframeIds;
   private Set<String> assemblyIds;
   private Set<String> programIds;
   private Set<String> projectIds;
   private Set<String> protocolIds;
   private Set<String> reportingEventIds;
   private Long versionNumber;
   private Boolean versionSuperSeded;
   private Dataset dataset;
   private String subType;

   // Added for Lineage
   private String releaseStatus;
   private List<String> parentAssemblyIds;
   private List<String> parentDataframeIds;
   private String publishStatus;
   private String nodeType;
   private String equipVersion;
   private String lastModifiedBy;
   private Date lastModifiedDate;
   private List<Dataframe> childDataframes;
   private List<Dataframe> childAssemblies;
   private Boolean versionComitted;
   private Boolean isLocked;
   private Boolean isDeleted;
   List<CommentInput> comments;

   @JsonIgnore
   private Set<String[]> programStudyIds = new HashSet<String[]>();
   private Set<Protocol> protocols = new HashSet<Protocol>();

   private List<Metadatum> metadata;

   private final String DELIM = ":";

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getDataframeType() {
      return dataframeType;
   }

   public void setDataframeType(String dataframeType) {
      this.dataframeType = dataframeType;
   }

   public String getPromotionStatus() {
      return promotionStatus;
   }

   public void setPromotionStatus(String promotionStatus) {
      this.promotionStatus = promotionStatus;
   }

   public String getDataBlindingStatus() {
      return dataBlindingStatus;
   }

   public void setDataBlindingStatus(String dataBlindingStatus) {
      this.dataBlindingStatus = dataBlindingStatus;
   }

   public String getRestrictionStatus() {
      return restrictionStatus;
   }

   public void setRestrictionStatus(String restrictionStatus) {
      this.restrictionStatus = restrictionStatus;
   }

   public boolean getIsPromoted() {
      boolean isPromoted = false;
      if (this.promotionStatus.equals("Promoted")) {
         isPromoted = true;
      } else {
         isPromoted = false;
      }
      return isPromoted;
   }

   public boolean getIsDataUnblinded() {
      boolean isDataUnblinded = false;
      if (this.dataBlindingStatus.equals("Unblinded")) {
         isDataUnblinded = true;
      } else {
         isDataUnblinded = false;
      }
      return isDataUnblinded;
   }

   public boolean getIsRestricted() {
      boolean isRestricted = false;
      if (this.restrictionStatus.equals("Restricted")) {
         isRestricted = true;
      } else {
         isRestricted = false;
      }
      return isRestricted;
   }

   public List<Metadatum> getMetadata() {
      return metadata;
   }

   public void setMetadata(List<Metadatum> metadata) {
      this.metadata = metadata;
   }

   public String getStudyBlindingStatus() {
      return studyBlindingStatus;
   }

   public void setStudyBlindingStatus(String studyBlindingStatus) {
      this.studyBlindingStatus = studyBlindingStatus;
   }

   public boolean getIsStudyBlinded() {
      boolean isStudyBlinded = false;
      if (this.studyBlindingStatus.equals(StudyBlindingStatus.BLINDED.getValue())) {
         isStudyBlinded = true;
      } else {
         isStudyBlinded = false;
      }
      return isStudyBlinded;
   }

   public Set<Protocol> getProtocols() {
      return protocols;
   }

   public void setProtocols(Set<Protocol> protocols) {
      this.protocols = protocols;
   }

   public void addProtocol(Protocol protocol) {
      this.protocols.add(protocol);
   }

   @JsonIgnore
   public Set<String[]> getProgramStudyIds() {
      return programStudyIds;
   }

   public void addProgramStudyId(String programCode, String studyId) {
      String[] programStudyId = { programCode, studyId };
      programStudyIds.add(programStudyId);
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }

   public Date getCreatedDate() {
      return createdDate;
   }

   public void setCreatedDate(Date createdDate) {
      this.createdDate = createdDate;
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

   public String getQcStatus() {
      return qcStatus;
   }

   public void setQcStatus(String qcStatus) {
      this.qcStatus = qcStatus;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getItemType() {
      return itemType;
   }

   public void setItemType(String itemType) {
      this.itemType = itemType;
   }

   public Boolean getDeleteFlag() {
      return deleteFlag;
   }

   public void setDeleteFlag(Boolean deleteFlag) {
      this.deleteFlag = deleteFlag;
   }

   public Boolean getObsoleteFlag() {
      return obsoleteFlag;
   }

   public void setObsoleteFlag(Boolean obsoleteFlag) {
      this.obsoleteFlag = obsoleteFlag;
   }

   public Set<String> getProfileConfig() {
      return profileConfig;
   }

   public void setProfileConfig(Set<String> profileConfig) {
      this.profileConfig = profileConfig;
   }

   public Boolean getisCommitted() {
      return isCommitted;
   }

   public void setisCommited(Boolean isCommitted) {
      this.isCommitted = isCommitted;
   }

   public Set<String> getDataframeIds() {
      return dataframeIds;
   }

   public void setDataframeIds(Set<String> dataframeIds) {
      this.dataframeIds = dataframeIds;
   }

   public Set<String> getAssemblyIds() {
      return assemblyIds;
   }

   public void setAssemblyIds(Set<String> assemblyIds) {
      this.assemblyIds = assemblyIds;
   }

   public Set<String> getProgramIds() {
      return programIds;
   }

   public void setProgramIds(Set<String> programIds) {
      this.programIds = programIds;
   }

   public Set<String> getProjectIds() {
      return projectIds;
   }

   public void setProjectIds(Set<String> projectIds) {
      this.projectIds = projectIds;
   }

   public Set<String> getProtocolIds() {
      return protocolIds;
   }

   public void setProtocolIds(Set<String> protocolIds) {
      this.protocolIds = protocolIds;
   }

   public Set<String> getReportingEventIds() {
      return reportingEventIds;
   }

   public void setReportingEventIds(Set<String> reportingEventIds) {
      this.reportingEventIds = reportingEventIds;
   }

   public Long getVersionNumber() {
      return versionNumber;
   }

   public void setVersionNumber(Long versionNumber) {
      this.versionNumber = versionNumber;
   }

   public Boolean getVersionSuperSeded() {
      return versionSuperSeded;
   }

   public void setVersionSuperSeded(Boolean versionSuperSeded) {
      this.versionSuperSeded = versionSuperSeded;
   }

   @JsonProperty("studyIds")
   public Set<String> getProgramStudyIdsConcatenated() {
      Set<String> concatenatedStudyIds = new HashSet<String>();
      for (String[] programStudyId : this.programStudyIds) {
         concatenatedStudyIds.add(String.format("%s%s%s", programStudyId[0], DELIM, programStudyId[1]));
      }
      return concatenatedStudyIds;
   }

   @JsonProperty("studyIds")
   public void setProgramStudyIdsConcatenated(Set<String> studyIds) {
      for (String programStudyId : studyIds) {
         this.programStudyIds.add(programStudyId.split(DELIM));
      }
   }

   public Dataset getDataset() {
      return dataset;
   }

   public void setDataset(Dataset dataset) {
      this.dataset = dataset;
   }


   public String getReleaseStatus() {
      return releaseStatus;
   }

   public void setReleaseStatus(String releaseStatus) {
      this.releaseStatus = releaseStatus;
   }

   public List<String> getParentAssemblyIds() {
      return parentAssemblyIds;
   }

   public void setParentAssemblyIds(List<String> parentAssemblyIds) {
      this.parentAssemblyIds = parentAssemblyIds;
   }

   public String getPublishStatus() {
      return publishStatus;
   }

   public void setPublishStatus(String publishStatus) {
      this.publishStatus = publishStatus;
   }

   public String getNodeType() {
      return nodeType;
   }

   public void setNodeType(String nodeType) {
      this.nodeType = nodeType;
   }

   public String getEquipVersion() {
      return equipVersion;
   }

   public void setEquipVersion(String equipVersion) {
      this.equipVersion = equipVersion;
   }

   public String getLastModifiedBy() {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
   }

   public Date getLastModifiedDate() {
      return lastModifiedDate;
   }

   public void setLastModifiedDate(Date lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
   }

   public Boolean getVersionComitted() {
      return versionComitted;
   }

   public void setVersionComitted(Boolean versionComitted) {
      this.versionComitted = versionComitted;
   }

   public Boolean getIsLocked() {
      return isLocked;
   }

   public void setIsLocked(Boolean isLocked) {
      this.isLocked = isLocked;
   }

   public Boolean getIsDeleted() {
      return isDeleted;
   }

   public void setIsDeleted(Boolean isDeleted) {
      this.isDeleted = isDeleted;
   }

   public List<CommentInput> getComments() {
      return comments;
   }

   public void setComments(List<CommentInput> comments) {
      this.comments = comments;
   }

   public List<Dataframe> getChildDataframes() {
      return childDataframes;
   }

   public void setChildDataframes(List<Dataframe> childDataframes) {
      this.childDataframes = childDataframes;
   }

   public List<Dataframe> getChildAssemblies() {
      return childAssemblies;
   }

   public void setChildAssemblies(List<Dataframe> childAssemblies) {
      this.childAssemblies = childAssemblies;
   }

   public List<String> getParentDataframeIds() {
      return parentDataframeIds;
   }

   public void setParentDataframeIds(List<String> parentDataframeIds) {
      this.parentDataframeIds = parentDataframeIds;
   }

   public String getSubType() {
      return subType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }
}