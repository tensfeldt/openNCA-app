package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_NULL)
@MappedSuperclass
public abstract class BaseProtocolChildNode extends BaseNode {
   
   @Transient
   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:isChanged")
   Boolean isChanged;

   @Transient
   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:created")
   Date created;

   @Transient
   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:createdBy")
   String createdBy;

   @JsonInclude(Include.NON_NULL)
   public Boolean isChanged() {
      return isChanged;
   }

   public void setChanged(Boolean isChanged) {
      this.isChanged = isChanged;
   }

   public void clearChanged() {
      this.isChanged = null;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }

   public Date getCreated() {
      return created;
   }

   public void setCreated(Date created) {
      this.created = created;
   }
}