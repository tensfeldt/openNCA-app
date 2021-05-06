package com.pfizer.equip.services.input.library;

import java.util.List;
import java.util.Map;

import com.pfizer.equip.services.business.librarian.dto.Parameter;
import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.business.modeshape.nodes.KeyValuePairNode;
import com.pfizer.equip.services.input.AbstractInput;

/**
 * 
 * Using the builder design pattern here. This class has no setters so that its state cannot be changed at compile time.
 *
 */
public class LibraryInput extends AbstractInput {
   public final static String PROPERTY_FILE_CONTENT = "fileContent";
   public final static String PROPERTY_ARTIFACT_PATH = "artifactPath";
   public final static String PROPERTY_DELETED = "deleted";
   public final static String PROPERTY_PRIMARY_TYPE = "primaryType";
   public final static String PROPERTY_EQUIP_NAME = "equipName";
   public final static String PROPERTY_ORIGINAL_EQUIP_NAME = "originalEquipName";
   public final static String PROPERTY_DESCRIPTION = "description";
   public final static String PROPERTY_COMMENTS = "comments";

   private String primaryType;
   private String artifactPath;
   private String artifactType;
   private String userId;
   private String targetArtifact;
   private MultipartFile fileContent;

   // custom properties from equipLibrary:baseArtifact
   private String equipName;

   private String description;

   private List<String> customTags;

   private String equipCreated;

   private String equipCreatedBy;

   private String comments;

   private String deleted;

   // custom properties from equipLibrary:script
   private List<String> parameterNames;

   private List<Parameter> params;

   // custom properties from equipLibrary:attachment
   private String parentType;

   private String parentKey;

   // custom properties from equipLibrary:savedSearch, equipLibrary:savedSearchResults, equipLibrary:reportingItem
   private String type;

   private String qcStatus;

   // custom properties from equipLibrary:reportingItemTemplate
   private String studyDesign;

   private String fileNameFormat;

   // custom properties from equipLibrary:reportTemplate
   private String defaultOutputFiletype;

   List<String> reportingItems;

   String summary;

   private Map<String, KeyValuePairNode> parameters;

   // custom properties from equipLibrary:specification
   private String specificationVersion;

   private String specificationType;

   // custom properties from equipLibrary:validationReport
   private String assemblyId;
   
   public LibraryInput() {}

   public String getPrimaryType() {
      return primaryType;
   }

   public String getArtifactPath() {
      return artifactPath;
   }

   public String getArtifactType() {
      return artifactType;
   }

   public String getUserId() {
      return userId;
   }

   public String getTargetArtifact() {
      return targetArtifact;
   }

   public MultipartFile getFileContent() {
      return fileContent;
   }

   public String getEquipName() {
      return equipName;
   }

   public String getDescription() {
      return description;
   }

   public List<String> getCustomTags() {
      return customTags;
   }

   public String getEquipCreated() {
      return equipCreated;
   }

   public String getEquipCreatedBy() {
      return equipCreatedBy;
   }

   public String getComments() {
      return comments;
   }

   public String getDeleted() {
      return deleted;
   }

   public List<String> getParameterNames() {
      return parameterNames;
   }

   public List<Parameter> getParams() {
      return params;
   }

   public String getParentType() {
      return parentType;
   }

   public String getParentKey() {
      return parentKey;
   }

   public String getType() {
      return type;
   }

   public String getQcStatus() {
      return qcStatus;
   }

   public String getStudyDesign() {
      return studyDesign;
   }

   public String getFileNameFormat() {
      return fileNameFormat;
   }

   public String getDefaultOutputFiletype() {
      return defaultOutputFiletype;
   }

   public List<String> getReportingItems() {
      return reportingItems;
   }

   public String getSummary() {
      return summary;
   }

   public Map<String, KeyValuePairNode> getParameters() {
      return parameters;
   }

   public String getSpecificationVersion() {
      return specificationVersion;
   }

   public String getSpecificationType() {
      return specificationType;
   }
   
   public String getAssemblyId() {
      return assemblyId;
   }
}
