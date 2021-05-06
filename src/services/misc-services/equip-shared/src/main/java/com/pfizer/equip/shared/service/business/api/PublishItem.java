package com.pfizer.equip.shared.service.business.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishItem {
   List<Object> comments = new ArrayList<>();
   Date created;
   String createdBy;
   List<Metadatum> metadata = new ArrayList<>();
   String publishEventId = "";
   String publishStatus = "Unpublished";
   Date publishedDate;
   String publishedViewFilterCriteria = "{\"include\":{\"script\":false,\"stdout\":false,\"stderr\":false}}";
   String publishedViewSubFilter = "";
   String publishingItemTemplateId = ""; 
   
   public PublishItem() {}
   
   public PublishItem(String createdBy) {
      Date now = new Date();
      this.created = now;
      this.publishedDate = now;
      this.createdBy = createdBy;
   }

   public List<Object> getComments() {
      return comments;
   }

   public void setComments(List<Object> comments) {
      this.comments = comments;
   }

   public Date getCreated() {
      return created;
   }

   public void setCreated(Date created) {
      this.created = created;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }

   public List<Metadatum> getMetadata() {
      return metadata;
   }

   public void setMetadata(List<Metadatum> metadata) {
      this.metadata = metadata;
   }

   public String getPublishEventId() {
      return publishEventId;
   }

   public void setPublishEventId(String publishEventId) {
      this.publishEventId = publishEventId;
   }

   public String getPublishStatus() {
      return publishStatus;
   }

   public void setPublishStatus(String publishStatus) {
      this.publishStatus = publishStatus;
   }

   public Date getPublishedDate() {
      return publishedDate;
   }

   public void setPublishedDate(Date publishedDate) {
      this.publishedDate = publishedDate;
   }

   public String getPublishedViewFilterCriteria() {
      return publishedViewFilterCriteria;
   }

   public void setPublishedViewFilterCriteria(String publishedViewFilterCriteria) {
      this.publishedViewFilterCriteria = publishedViewFilterCriteria;
   }

   public String getPublishedViewSubFilter() {
      return publishedViewSubFilter;
   }

   public void setPublishedViewSubFilter(String publishedViewSubFilter) {
      this.publishedViewSubFilter = publishedViewSubFilter;
   }

   public String getPublishingItemTemplateId() {
      return publishingItemTemplateId;
   }

   public void setPublishingItemTemplateId(String publishingItemTemplateId) {
      this.publishingItemTemplateId = publishingItemTemplateId;
   }
}