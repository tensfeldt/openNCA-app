package com.pfizer.equip.services.business.api.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// class for representing a string parameter value to the compute service
@JsonInclude(Include.NON_ABSENT)
public class ComputeParameterInput implements GenericInput {
   private String key;
   private String value;
   private String type;

   public ComputeParameterInput(String key, String value, String type) {
      this.key = key;
      this.value = value;
      this.type = type;
   }
   
   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
