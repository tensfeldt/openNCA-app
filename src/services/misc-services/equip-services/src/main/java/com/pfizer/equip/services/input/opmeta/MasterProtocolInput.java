package com.pfizer.equip.services.input.opmeta;

import java.util.Date;

public class MasterProtocolInput {
   Date modified = new Date();
   
   String modifiedBy;

   public Date getModified() {
      return modified;
   }

   public String getModifiedBy() {
      return modifiedBy;
   }

   public void setModifiedBy(String modifiedBy) {
      this.modifiedBy = modifiedBy;
   }
}