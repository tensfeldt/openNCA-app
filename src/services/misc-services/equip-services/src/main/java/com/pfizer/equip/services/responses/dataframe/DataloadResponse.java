package com.pfizer.equip.services.responses.dataframe;

import java.util.Set;

import com.pfizer.equip.shared.responses.AbstractResponse;

public class DataloadResponse extends AbstractResponse {
   private String assemblyId;
   private Set<String> dataframeIds;

   public String getAssemblyId() {
      return assemblyId;
   }

   public void setAssemblyId(String assemblyId) {
      this.assemblyId = assemblyId;
   }

   public Set<String> getDataframeIds() {
      return dataframeIds;
   }

   public void setDataframeIds(Set<String> dataframeId) {
      this.dataframeIds = dataframeId;
   }
}
