package com.pfizer.equip.shared.opmeta.entity.mixins;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public abstract class ApplicationMixin {
   @JsonIgnore
   private String versionHistoryUrl;

   @JsonIgnore
   private String studyIds;
   
   @JsonInclude(Include.ALWAYS)
   private String customTags;

   @JsonInclude(Include.ALWAYS)
   private String comment;

   @JsonInclude(Include.ALWAYS)
   private Boolean deleted;

   @JsonInclude(Include.ALWAYS)
   private String description;

   @JsonInclude(Include.ALWAYS)
   private String documentSource;

   @JsonInclude(Include.ALWAYS)
   private String equipCreated;

   @JsonInclude(Include.ALWAYS)
   private String equipCreatedBy;

   @JsonInclude(Include.ALWAYS)
   private String equipModified;

   @JsonInclude(Include.ALWAYS)
   private String equipModifiedBy;

   @JsonInclude(Include.ALWAYS)
   private String equipName;

   @JsonInclude(Include.ALWAYS)
   private String id;

   @JsonInclude(Include.ALWAYS)
   private String mimeType;

   @JsonIgnore
   private String primaryType;

   @JsonIgnore
   private String subType;

   @JsonIgnore
   public abstract Set<String> getStudyIds();

   @JsonIgnore
   public abstract String getSubType();

   @JsonIgnore
   public abstract String getVersionHistoryUrl();

   @JsonIgnore
   public abstract String getPrimaryType();
}