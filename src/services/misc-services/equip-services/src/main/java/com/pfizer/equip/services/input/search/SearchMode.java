package com.pfizer.equip.services.input.search;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchMode {
   MODE_AND("AND"),
   MODE_OR("OR");

   private final String value;

   private SearchMode(String value) {
      this.value = value;
   }

   @JsonValue
   public String getValue() {
      return value;
   }

   @Override
   public String toString() {
      return this.getValue();
   }
}
