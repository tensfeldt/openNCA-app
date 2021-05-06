package com.pfizer.equip.services.responses.library;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.services.business.modeshape.nodes.ArtifactNode;
import com.pfizer.equip.services.business.modeshape.nodes.BaseExistingNode;
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactNode;
import com.pfizer.equip.services.business.modeshape.nodes.NewFolderNode;
import com.pfizer.equip.shared.responses.AbstractResponse;

@JsonInclude(Include.NON_ABSENT)
public class LibraryArtifactResponse extends AbstractResponse {
   private String artifactId;
   private String artifactPath;
   private String created;
   private String createdBy;
   private String lastModified;
   private String lastModifiedBy;
   private String primaryType;
   private String mimeType;
   private String deleted;
   private String comments;
   private List<VersionHistoryResponse> versionHistory;
   private Map<String, Object> properties;
   private Map<String, ArtifactNode> children;

   public LibraryArtifactResponse() {}

   @SuppressWarnings("unchecked")
   public LibraryArtifactResponse(ArtifactNode artifactNode, List<String> supportedTypes, String stripUrl) throws Exception {
      // set base level response data
      this.artifactId = artifactNode.getId();
      this.artifactPath = artifactNode.getSelf().replace(stripUrl, "");
      this.created = artifactNode.getCreated();
      this.createdBy = artifactNode.getCreatedBy();
      this.lastModified = artifactNode.getLastModified();
      this.lastModifiedBy = artifactNode.getLastModifiedBy();
      this.primaryType = artifactNode.getPrimaryType();
      this.mimeType = artifactNode.getMimeType();
      this.deleted = artifactNode.getDeleted();
      this.comments = artifactNode.getComments();

      if (supportedTypes.contains(primaryType)) {
         // set custom metadata using Spring bean reflection
         Field[] fields = BaseLibraryArtifactNode.class.getDeclaredFields();
         properties = new HashMap<String, Object>();
         for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(artifactNode);
            //The below condition (!name.startsWith("$")) is added to skip the $jacoco field created by Java code coverage plugin
            if (value != null && (!name.startsWith("$"))) {
               if (!Collection.class.isAssignableFrom(field.getType())) {
                  properties.put(name, (String) value);
               } else {
                  properties.put(name, (List<String>) value);
               }
            }
         }
      } else if (primaryType.equalsIgnoreCase(NewFolderNode.EQUIP_FOLDER_TYPE)) {
         children = artifactNode.getChildren();
         if (this.children != null) {
            for (Map.Entry<String, ArtifactNode> entry : this.children.entrySet()) {
               BaseExistingNode node = entry.getValue();
               node.setSelf(node.getSelf().replace(stripUrl, ""));
               node.setUp(node.getUp().replace(stripUrl, ""));
            }
         }
      }
   }

   public String getArtifactId() {
      return artifactId;
   }

   public void setArtifactId(String artifactId) {
      this.artifactId = artifactId;
   }

   public String getArtifactPath() {
      return artifactPath;
   }

   public void setArtifactPath(String artifactPath) {
      this.artifactPath = artifactPath;
   }

   public String getCreated() {
      return created;
   }

   public void setCreated(String created) {
      this.created = created;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }

   public String getLastModified() {
      return lastModified;
   }

   public void setLastModified(String lastModified) {
      this.lastModified = lastModified;
   }

   public String getLastModifiedBy() {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
   }

   public String getPrimaryType() {
      return primaryType;
   }

   public void setPrimaryType(String primaryType) {
      this.primaryType = primaryType;
   }

   public String getMimeType() {
      return mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }

   public String getDeleted() {
      return deleted;
   }

   public void setDeleted(String deleted) {
      this.deleted = deleted;
   }

   public Map<String, Object> getProperties() {
      return properties;
   }

   public void setProperties(Map<String, Object> properties) {
      this.properties = properties;
   }

   public List<VersionHistoryResponse> getVersionHistory() {
      return versionHistory;
   }

   public void setVersionHistory(List<VersionHistoryResponse> versionHistory) {
      this.versionHistory = versionHistory;
   }

   public Map<String, ArtifactNode> getChildren() {
      return children;
   }

   public void setChildren(Map<String, ArtifactNode> children) {
      this.children = children;
   }

   public String getComments() {
      return comments;
   }

   public void setComments(String comments) {
      this.comments = comments;
   }
}