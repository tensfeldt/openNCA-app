package com.pfizer.equip.services.business.api.dataframe.pims;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PIMSLoadInfo {
   PIMSDoseMapping[] dosepMapping;
   String assemblyId;
   String pkDefDataframeId;
   String createdBy;

   public PIMSDoseMapping[] getDosepMapping() {
      return dosepMapping;
   }

   public void setDosepMapping(PIMSDoseMapping[] dosepMapping) {
      this.dosepMapping = dosepMapping;
   }

   public String getAssemblyId() {
      return assemblyId;
   }

   public void setAssemblyId(String assemblyId) {
      this.assemblyId = assemblyId;
   }

   public String getPkDefDataframeId() {
      return pkDefDataframeId;
   }

   public void setPkDefDataframeId(String pkDefDataframeId) {
      this.pkDefDataframeId = pkDefDataframeId;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }
}
