package com.pfizer.equip.services.business.modeshape.nodes;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.services.business.librarian.dto.Parameter;

/**
 * 
 * This class represents the (super) set of properties a library artifact node in the system will contain. This class can represent any library artifact, which means any
 * type that extends equipLibrary:baseArtifact. We use the Include.NON_ABSENT annotation to remove properties that are not present when serializing.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class BaseLibraryArtifactNode {
   @JsonProperty("jcr:primaryType")
   private String primaryType;

   // custom properties from equipLibrary:baseArtifact
   @JsonProperty("equip:name")
   private String equipName;

   @JsonProperty("equip:description")
   private String description;

   @JsonProperty("equipLibrary:customTags")
   private List<String> customTags;

   @JsonProperty("equip:created")
   private String equipCreated;

   @JsonProperty("equip:createdBy")
   private String equipCreatedBy;

   @JsonProperty("equip:modified")
   private String equipModified;

   @JsonProperty("equip:modifiedBy")
   private String equipModifiedBy;

   @JsonProperty("equip:deleteFlag")
   private String deleted;

   @JsonProperty("jcr:data/base64/")
   private String encodedContent;
   
   // Used for display purposes:
   @JsonProperty("equip:subType")
   private String subType;

   // custom properties from equipLibrary:attachment
   @JsonProperty("equipLibrary:parentType")
   private String parentType;

   @JsonProperty("equipLibrary:parentKey")
   private String parentKey;

   // custom properties from equipLibrary:script
   @JsonProperty("equipLibrary:parameterNames")
   private List<String> parameterNames;

   @JsonProperty("equipLibrary:params")
   private List<String> params;

   // custom properties from equipLibrary:savedSearch, equipLibrary:savedSearchResults, equipLibrary:reportingItem
   @JsonProperty("equipLibrary:type")
   private String type;

   @JsonProperty("equipLibrary:qcStatus")
   private String qcStatus;

   // custom properties from equipLibrary:reportingItemTemplate
   @JsonProperty("equipLibrary:studyDesign")
   String studyDesign;

   @JsonProperty("equipLibrary:fileNameFormat")
   String fileNameFormat;

   @JsonProperty("equipLibrary:dataFrames")
   List<String> dataFrames;

   @JsonProperty("equipLibrary:parameters")
   protected Map<String, KeyValuePairNode> parameters;

   // custom properties from equipLibrary:reportDefinition
   @JsonProperty("equipLibrary:defaultOutputFiletype")
   String defaultOutputFiletype;

   @JsonProperty("equipLibrary:reportingItems")
   List<String> reportingItems;

   @JsonProperty("equipLibrary:summary")
   String summary;

   // custom properties from equipLibrary:specification
   @JsonProperty("equipLibrary:specificationVersion")
   private String specificationVersion;

   @JsonProperty("equipLibrary:specificationType")
   private String specificationType;

   // custom properties from equipLibrary:validationReport
   private String assemblyId;
   
   // properties for Comments
   @JsonProperty("equip:commentType")
   private String commentType;
   
   @JsonProperty("equip:body")
   private String body;

   public String getPrimaryType() {
      return primaryType;
   }

   public void setPrimaryType(String primaryType) {
      this.primaryType = primaryType;
   }

   public String getEquipName() {
      return equipName;
   }

   public void setEquipName(String equipName) {
      this.equipName = equipName;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public List<String> getCustomTags() {
      return customTags;
   }

   public void setCustomTags(List<String> customTags) {
      this.customTags = customTags;
   }

   public String getEquipCreated() {
      return equipCreated;
   }

   public void setEquipCreated(String equipCreated) {
      this.equipCreated = equipCreated;
   }

   public String getEquipCreatedBy() {
      return equipCreatedBy;
   }

   public void setEquipCreatedBy(String equipCreatedBy) {
      this.equipCreatedBy = equipCreatedBy;
   }

   public String getEquipModified() {
      return equipModified;
   }

   public void setEquipModified(String equipModified) {
      this.equipModified = equipModified;
   }

   public String getEquipModifiedBy() {
      return equipModifiedBy;
   }

   public void setEquipModifiedBy(String equipModifiedBy) {
      this.equipModifiedBy = equipModifiedBy;
   }

   public String getDeleted() {
      return deleted;
   }

   public void setDeleted(String deleted) {
      this.deleted = deleted;
   }

   public String getEncodedContent() {
      return encodedContent;
   }

   public void setEncodedContent(String encodedContent) {
      this.encodedContent = encodedContent;
   }

   public String getParentType() {
      return parentType;
   }

   public void setParentType(String parentType) {
      this.parentType = parentType;
   }

   public String getParentKey() {
      return parentKey;
   }

   public void setParentKey(String parentKey) {
      this.parentKey = parentKey;
   }

   public List<String> getParameterNames() {
      return parameterNames;
   }

   public void setParameterNames(List<String> parameterNames) {
      this.parameterNames = parameterNames;
   }

   public List<String> getParams() {
      return params;
   }

   public void setParams(List<String> params) {
      this.params = params;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getQcStatus() {
      return qcStatus;
   }

   public void setQcStatus(String qcStatus) {
      this.qcStatus = qcStatus;
   }

   public String getStudyDesign() {
      return studyDesign;
   }

   public void setStudyDesign(String studyDesign) {
      this.studyDesign = studyDesign;
   }

   public String getFileNameFormat() {
      return fileNameFormat;
   }

   public void setFileNameFormat(String fileNameFormat) {
      this.fileNameFormat = fileNameFormat;
   }

   public List<String> getDataFrames() {
      return dataFrames;
   }

   public void setDataFrames(List<String> dataFrames) {
      this.dataFrames = dataFrames;
   }

   public Map<String, KeyValuePairNode> getParameters() {
      return parameters;
   }

   public void setParameters(Map<String, KeyValuePairNode> parameters) {
      this.parameters = parameters;
   }

   public String getDefaultOutputFiletype() {
      return defaultOutputFiletype;
   }

   public void setDefaultOutputFiletype(String defaultOutputFiletype) {
      this.defaultOutputFiletype = defaultOutputFiletype;
   }

   public List<String> getReportingItems() {
      return reportingItems;
   }

   public void setReportingItems(List<String> reportingItems) {
      this.reportingItems = reportingItems;
   }

   public String getSummary() {
      return summary;
   }

   public void setSummary(String summary) {
      this.summary = summary;
   }

   public String getSpecificationVersion() {
      return specificationVersion;
   }

   public void setSpecificationVersion(String specificationVersion) {
      this.specificationVersion = specificationVersion;
   }

   public String getSpecificationType() {
      return specificationType;
   }

   public void setSpecificationType(String specificationType) {
      this.specificationType = specificationType;
   }

   public String getAssemblyId() {
      return assemblyId;
   }

   public void setAssemblyId(String assemblyId) {
      this.assemblyId = assemblyId;
   }

   public String getSubType() {
      return subType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }

   public String getCommentType() {
      return commentType;
   }

   public void setCommentType(String commentType) {
      this.commentType = commentType;
   }

   public String getBody() {
      return body;
   }

   public void setBody(String body) {
      this.body = body;
   }
}
