package com.pfizer.equip.services.responses.validation;

import com.pfizer.equip.shared.responses.AbstractResponse;

public class CreateUpdateDeleteSpecification extends AbstractResponse {
   private String id;
   private String path;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

}
