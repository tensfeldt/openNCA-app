package com.pfizer.equip.services.input.dataframe;

public enum PromoteMode {
   PROMOTE("PROMOTE"),
   REVOKE("REVOKE");

   private final String value;

   private PromoteMode(String value) {
      this.value = value;
   }

   public String getValue() {
      return value;
   }

   public static PromoteMode fromString(String value) {
      if (value != null) {
         for (PromoteMode mode : PromoteMode.values()) {
            if (value.equalsIgnoreCase(mode.value)) {
               return mode;
            }
         }
      }
      return null;
   }

   @Override
   public String toString() {
      return this.getValue();
   }
}
