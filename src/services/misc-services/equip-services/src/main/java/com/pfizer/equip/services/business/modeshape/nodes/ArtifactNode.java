package com.pfizer.equip.services.business.modeshape.nodes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * 
 * This class represents an existing artifact node in ModeShape.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class ArtifactNode extends BaseExistingNode {
   // built in ModeShape properties
   @JsonProperty("jcr:mimeType")
   private String mimeType;

   @JsonProperty("jcr:created")
   private String created;

   @JsonProperty("jcr:createdBy")
   private String createdBy;

   @JsonProperty("jcr:lastModified")
   private String lastModified;

   @JsonProperty("jcr:lastModifiedBy")
   private String lastModifiedBy;

   @JsonProperty("jcr:versionHistory")
   private String versionHistoryPath;

   // this is only used for folder types
   @JsonProperty(value = "children", access = Access.WRITE_ONLY)
   private Map<String, ArtifactNode> children;

   public String getMimeType() {
      return mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
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

   public String getVersionHistoryPath() throws UnsupportedEncodingException {
      // ModeShape returns an encoded URL
      // we need to decode the URL here since the Sprint REST client will attempt to re-encode an already encoded URL
      if (StringUtils.isNotEmpty(versionHistoryPath)) {
         return URLDecoder.decode(versionHistoryPath, "UTF-8");
      } else {
         return "";
      }
   }

   public void setVersionHistoryPath(String versionHistoryPath) {
      this.versionHistoryPath = versionHistoryPath;
   }

   public Map<String, ArtifactNode> getChildren() {
      return children;
   }

   public void setChildren(Map<String, ArtifactNode> children) {
      this.children = children;
   }

   // Comments are a child node, must be retrieved from children field after deserialiation. Only one comment per artifact.
   @JsonProperty("equip:comment")
   public String getComments() {
      if (children != null && children.get("equip:comment") != null) {
         return children.get("equip:comment").getBody();
      } else {
         return null;
      }
   }
}
