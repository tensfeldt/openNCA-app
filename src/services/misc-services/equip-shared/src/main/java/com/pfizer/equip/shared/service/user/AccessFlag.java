package com.pfizer.equip.shared.service.user;

/** This class unifies Facts and Permissions.
 */
public enum AccessFlag {
   NON_PROMOTED("NON_PROMOTED"),
   RESTRICTED("RESTRICTED"),
   STUDY_BLINDED("STUDY_BLINDED"),
   STUDY_RESTRICTED("STUDY_RESTRICTED");

   private final String value;

   private AccessFlag(String value) {
      this.value = value;
   }

   public String getValue() {
      return value;
   }

   @Override
   public String toString() {
      return this.getValue();
   }
}
