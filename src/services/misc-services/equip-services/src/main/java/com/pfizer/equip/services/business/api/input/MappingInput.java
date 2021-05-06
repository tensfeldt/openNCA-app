package com.pfizer.equip.services.business.api.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

// simple class to represent the input parameters for the column remapping script
@JsonInclude(Include.NON_ABSENT)
public class MappingInput {
   @JsonProperty("COLUMN_FROM")
   private String columnFrom;

   @JsonProperty("COLUMN_TO")
   private String columnTo;

   public String getColumnFrom() {
      return columnFrom;
   }

   public void setColumnFrom(String columnFrom) {
      this.columnFrom = columnFrom;
   }

   public String getColumnTo() {
      return columnTo;
   }

   public void setColumnTo(String columnTo) {
      this.columnTo = columnTo;
   }
}
