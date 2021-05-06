package com.pfizer.equip.services.business.modeshape.nodes;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * This class represents the results of a JCR query.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class SearchResultsNode {
   private Map<String, String> columns;

   private List<Map<String, String>> rows;

   public Map<String, String> getColumns() {
      return columns;
   }

   public void setColumns(Map<String, String> columns) {
      this.columns = columns;
   }

   public List<Map<String, String>> getRows() {
      return rows;
   }

   public void setRows(List<Map<String, String>> rows) {
      this.rows = rows;
   }

   /**
    * Returns the Java class representing the type for the specified column.
    */
   @JsonIgnore
   public Class<?> getColumnType(String columnName) {
      String stringType = columns.get(columnName);

      switch (stringType) {
      case "STRING":
         return String.class;
      case "LONG":
         return Long.class;
      case "DOUBLE":
         return Double.class;
      case "BOOLEAN":
         return Boolean.class;
      case "DATE":
         return Date.class;

      default:
         return String.class;
      }
   }
}
