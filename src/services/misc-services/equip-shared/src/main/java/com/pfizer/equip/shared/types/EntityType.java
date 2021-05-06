package com.pfizer.equip.shared.types;

/**
 * Entity types used in security tables.
 */
public enum EntityType {
   PROGRAM("Program"),
   PROJECT("Project"),
   PROTOCOL("Protocol"),
   DATAFRAME("Dataframe"),
   REPORTING_EVENT("Reporting Event"),
   GROUP("Group"),
   SPECIFICATION("Specification"),
   ARTIFACT("Artifact"),
   ROLE("Role"),
   SEARCH("Search"),
   USER("User"),
   REPORT("Rreport"),
   REPORTING_ITEM("Reporting Item"),
   REPORTING_ITEM_TEMPLATE("Reporting Item Template"),
   REPORT_TEMPLATE("Report Template"),
   SUBSCRIPTION("Subscription"),
   DATALOAD("Data Load");

   private final String value;

   private EntityType(String value) {
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