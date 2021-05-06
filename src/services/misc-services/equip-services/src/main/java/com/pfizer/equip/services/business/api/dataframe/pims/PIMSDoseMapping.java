package com.pfizer.equip.services.business.api.dataframe.pims;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PIMSDoseMapping {
   String pkterm;
   String[] dosep;

   public String getPkterm() {
      return pkterm;
   }

   public void setPkterm(String pkterm) {
      this.pkterm = pkterm;
   }

   public String[] getDosep() {
      return dosep;
   }

   public void setDosep(String[] dosep) {
      this.dosep = dosep;
   }
}
