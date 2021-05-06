package com.pfizer.equip.services.business.modeshape.nodes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_ABSENT)
public class NewFolderNode {
   public static String EQUIP_FOLDER_TYPE = "equipLibrary:baseFolder";
   
   // folders always have this primary type value
   @JsonProperty("jcr:primaryType")
   private String primaryType;
   
   @JsonProperty("equip:deleteFlag")
   private String deleted;
   
   public NewFolderNode() {
      // we want to set this to a copy of the static constant
      primaryType = String.valueOf(EQUIP_FOLDER_TYPE);
   }

   public String getPrimaryType() {
      return primaryType;
   }
   
   public String getDeleted() {
      return deleted;
   }
   
   public void setDeleted(String deleted) {
      this.deleted = deleted;
   }
}
