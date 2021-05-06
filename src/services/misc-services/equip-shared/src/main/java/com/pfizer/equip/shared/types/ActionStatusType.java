package com.pfizer.equip.shared.types;

/**
 * Entity types used in security tables.
 */
public enum ActionStatusType {
   // TODO: May need to add more status like warning .
   SUCCESS("SUCCESS"),
   FAILURE("FAILURE");

   private final String value;

   private ActionStatusType(String value) {
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
