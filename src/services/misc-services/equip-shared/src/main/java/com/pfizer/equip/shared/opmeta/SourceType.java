package com.pfizer.equip.shared.opmeta;

/** Source type for opmeta nodes
 */
public enum SourceType {
   PODS("PODS"),
   EQUIP("NCA");
   
   private final String value;
   private SourceType(String value) {
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