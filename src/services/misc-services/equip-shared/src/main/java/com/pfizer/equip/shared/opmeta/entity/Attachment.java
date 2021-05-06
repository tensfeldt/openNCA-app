package com.pfizer.equip.shared.opmeta.entity;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
   
   public Attachment(){}

   @SuppressWarnings("unchecked")
   public Attachment(MultiValueMap<String, Object> inputs, byte[] content) {
      // set custom metadata using reflection
      Field[] fields = this.getClass().getDeclaredFields();
      for (Field field : fields) {
         String name = field.getName();
         Object value = inputs.get(name);
         if (value != null) {
            if (!Collection.class.isAssignableFrom(field.getType())) {
               // if the field is *not* a list, we can safely always get the 1st and only value in the list
               // returned by the MultiValueMap
               value = ((List<Object>) value).get(0);
            }
            // set the value on the field object from the input map
            field.setAccessible(true);
            try {
               field.set(this, value);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      }
      if (content != null) {
         this.setContent(Base64.getEncoder().encodeToString(content));
      }
      this.subType = "attachment";
      if (inputs.get("comments") != null) {
         this.setComment((String) inputs.get("comments").get(0));
      }
   }
   
   public static final String COMPLEX_DATA = "equip:complexData";
   private static final String CONTENT = "jcr:content";
   
   @JsonProperty("id")
   private String id;
   
   @JsonProperty("jcr:primaryType")
   private String primaryType = "opmeta:attachment";

   @JsonProperty("equip:name")
   private String equipName;

   @JsonProperty("equip:description")
   private String description;

   // equip:searchable requires this to be a MULTIPLE property, though attachments will only have one parent study for now:
   @JsonProperty("equip:studyId")
   private Set<String> studyIds;

   @JsonProperty("opmeta:customTags")
   private List<String> customTags;

   @JsonProperty("equip:created")
   private String equipCreated;

   @JsonProperty("equip:createdBy")
   private String equipCreatedBy;

   @JsonProperty("equip:modified")
   private String equipModified;

   @JsonProperty("equip:modifiedBy")
   private String equipModifiedBy;

   @JsonProperty("comments")
   private String comment;

   @JsonProperty("equip:deleteFlag")
   private Boolean deleted;

   @JsonProperty("jcr:data/base64/")
   private String encodedContent;

   @JsonProperty("opmeta:documentSource")
   private String documentSource;
   
   @JsonProperty("jcr:mimeType")
   private String mimeType;
   
   @JsonProperty("jcr:versionHistory")
   private String versionHistoryUrl;

   @JsonProperty("equip:subType")
   private String subType;

   @JsonProperty("children")
   private Map<String, Object> children;

   public void setContent(String base64Content) {
      Attachment complexData = new Attachment();
      Attachment content = new Attachment();
      content.setPrimaryType("nt:resource");
      content.setEncodedContent(base64Content);
      complexData.setPrimaryType("nt:file");
      complexData.putChild(CONTENT, content);
      this.putChild(COMPLEX_DATA, complexData);
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

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

   public String getComment() {
      return comment;
   }

   public void setComment(String comments) {
      Comment comment = new Comment(comments);
      this.putChild(comment.getPrimaryType(), comment);
   }

   public Boolean getDeleted() {
      return deleted;
   }

   public void setDeleted(Boolean deleted) {
      this.deleted = deleted;
   }

   public String getEncodedContent() {
      return encodedContent;
   }

   public void setEncodedContent(String encodedContent) {
      this.encodedContent = encodedContent;
   }

   public String getDocumentSource() {
      return documentSource;
   }

   public void setDocumentSource(String documentSource) {
      this.documentSource = documentSource;
   }

   public String getMimeType() {
      return mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }

   public String getVersionHistoryUrl() {
      return versionHistoryUrl;
   }

   public void setVersionHistoryUrl(String versionHistoryUrl) {
      this.versionHistoryUrl = versionHistoryUrl;
   }
   
   public Map<String, Object> getChildren() {
      return children;
   }

   public void setChildren(Map<String, JsonNode> children) {
      ObjectMapper mapper = new ObjectMapper();
      try {
         if (children != null) {
            if (children.get(Comment.PRIMARY_TYPE) != null) {
               JsonNode jsonComment = children.get(Comment.PRIMARY_TYPE);
               Comment comment = mapper.treeToValue(jsonComment, Comment.class);
               this.comment = comment.getBody();
            }
            if (children.get(COMPLEX_DATA) != null) {
               JsonNode jsonAttachmentContent = children.get(COMPLEX_DATA).get("children").get(CONTENT);
               Attachment attachment = mapper.treeToValue(jsonAttachmentContent, Attachment.class);
               this.mimeType = attachment.getMimeType();
            }
         }
      } catch (JsonProcessingException e) {
         throw new RuntimeException("Error deserializing children for attachment node.");
      }
   }

   public void putChild(String name, Object child) {
      if (this.children == null) {
         this.children = new HashMap<String, Object>();
      }
      this.children.put(name, child);
   }

   public Object getChild(String name) {
      if (this.children == null) {
         return null;
      }
      return this.children.get(name);
   }

   public String getSubType() {
      return subType;
   }

   public Set<String> getStudyIds() {
      return studyIds;
   }

   public void setStudyIds(Set<String> studyId) {
      this.studyIds = studyId;
   }

   public void addStudyId(String studyId) {
      if (studyIds == null) {
         studyIds = new HashSet<String>();
      }
      studyIds.add(studyId);
   }
}