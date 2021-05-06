package com.pfizer.equip.shared.opmeta;

public enum StudyBlindingStatus {
   BLINDED("Blinded"),
   UNBLINDED("Unblinded");

   private final String value;

   private StudyBlindingStatus(String value) {
         this.value = value;
      }

   public String getValue() {
      return value;
   }

   public static StudyBlindingStatus fromString(String value) {
      if (value != null) {
         for (StudyBlindingStatus mode : StudyBlindingStatus.values()) {
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
