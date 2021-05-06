package com.pfizer.equip.services.business.types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PrimaryType {
   // TODO : This list to be updated with values from Modeshape Properties - Supported Types
   BASE_ARTIFACT("equipLibrary:baseArtifact"),
   SCRIPT("equipLibrary:script"),
   ATTACHMENT("equipLibrary:attachment"),
   SPECIFICATION("equipLibrary:specification"),
   REPORTING_ITEM("equipLibrary:reportingItem"),
   REPORTING_ITEM_TEMPLATE("equipLibrary:reportingItemTemplate"),
   REPORT_TEMPLATE("equipLibrary:reportTemplate"),
   MCT_FILE("equipLibrary:mct");
   private String primaryType;

   PrimaryType(String delimiter) {
      primaryType = delimiter;
   }

   public String getPrimaryType() {
      return primaryType;
   }

   public static PrimaryType getPrimaryTypeFromValue(String value) {
      Map<String, PrimaryType> primaryTypes = Arrays.stream(PrimaryType.values()).collect(Collectors.toMap(PrimaryType::getPrimaryType, p -> p));
      if (primaryTypes.containsKey(value)) {
         return primaryTypes.get(value); 
      } else {
         return BASE_ARTIFACT;
      }
   }

   @Override
   public String toString() {
      return this.getPrimaryType();
   }

}
