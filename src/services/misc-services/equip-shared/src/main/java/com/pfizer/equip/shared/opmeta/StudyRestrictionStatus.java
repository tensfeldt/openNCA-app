package com.pfizer.equip.shared.opmeta;

public enum StudyRestrictionStatus {
   RESTRICTED("Restricted"),
   NOT_RESTRICTED("Not Restricted");

   private final String value;

   private StudyRestrictionStatus(String value) {
         this.value = value;
      }

   public String getValue() {
      return value;
   }

   public static StudyRestrictionStatus fromString(String value) {
      if (value != null) {
         for (StudyRestrictionStatus mode : StudyRestrictionStatus.values()) {
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
