package com.pfizer.equip.shared.service.business.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportingEventItem {
   // Note the capitalized F:
   private String id;
   @JsonProperty("dataFrameId")
   private String dataframeId;
   private String assemblyId;
   private String reportingEventId;
   private boolean included;
   private List<Metadatum> metadata;
   private PublishItem publishItem;
   private boolean isCommited;
   private boolean deleteFlag;
   private boolean versionSuperSeded;

   public ReportingEventItem() {}

   public ReportingEventItem(String reportingEventId, String dataframeId, List<Metadatum> metadata, PublishItem publishItem) {
      this.reportingEventId = reportingEventId;
      this.dataframeId = dataframeId;
      this.metadata = metadata;
      this.publishItem = publishItem;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getDataframeId() {
      return dataframeId;
   }

   public void setDataframeId(String dataframeId) {
      this.dataframeId = dataframeId;
   }

   public String getAssemblyId() {
      return assemblyId;
   }

   public void setAssemblyId(String assemblyId) {
      this.assemblyId = assemblyId;
   }

   public String getReportingEventId() {
      return reportingEventId;
   }

   public void setReportingEventId(String reportingEventId) {
      this.reportingEventId = reportingEventId;
   }

   public boolean isIncluded() {
      return included;
   }

   public void setIncluded(boolean included) {
      this.included = included;
   }

   public List<Metadatum> getMetadata() {
      return metadata;
   }

   public void setMetadata(List<Metadatum> metadata) {
      this.metadata = metadata;
   }

   public PublishItem getPublishItem() {
      return publishItem;
   }

   public void setPublishItem(PublishItem publishItem) {
      this.publishItem = publishItem;
   }

   public boolean getIsCommited() {
      return isCommited;
   }

   public void setIsCommited(boolean isCommited) {
      this.isCommited = isCommited;
   }

   public boolean isDeleteFlag() {
      return deleteFlag;
   }

   public void setDeleteFlag(boolean deleteFlag) {
      this.deleteFlag = deleteFlag;
   }

   public boolean isVersionSuperSeded() {
      return versionSuperSeded;
   }

   public void setVersionSuperSeded(boolean versionSuperSeded) {
      this.versionSuperSeded = versionSuperSeded;
   }

   public boolean isAtr() {
      for (Metadatum metadatum : metadata) {
         if (!metadatum.isDeleted() && metadatum.getValueType() != null && metadatum.getValueType().equals(Metadatum.METADATUM_TYPE)
               && metadatum.getKey().equals("subType") && metadatum.getValue().iterator().next().equals("ATR")) {
            return true;
         }
      }
      return false;
   }
}