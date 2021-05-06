package com.pfizer.equip.shared.opmeta;

public enum StudyBlindingStatusSource {
   MANUAL("Manual"),
   GRAABS("GRAABS");

   private final String value;

   private StudyBlindingStatusSource(String value) {
         this.value = value;
      }

   public String getValue() {
      return value;
   }

   public static StudyBlindingStatusSource fromString(String value) {
      if (value != null) {
         for (StudyBlindingStatusSource mode : StudyBlindingStatusSource.values()) {
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
