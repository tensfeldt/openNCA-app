package com.pfizer.equip.services.responses.dataframe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.responses.AbstractResponse;
import com.pfizer.equip.shared.responses.Response;

public class DatasetResponse extends AbstractResponse {
   @JsonProperty("columns")
   private List<Column> columns;
   
   @JsonProperty("rows")
   private List<Map<String, Object>> rows;
   
   @JsonProperty("valueSets")
   private Map<String, TreeSet<Object>> valueSets;

   public DatasetResponse() {
      columns = new ArrayList<>();
      rows = new ArrayList<>();
      valueSets = new LinkedHashMap<>();
   }
   
   public List<Column> getColumns() {
      return columns;
   }
   
   public void addColumn(String columnName) {
      columns.add(new Column(columnName));
   }
   
   public void addRow(Map<String, Object> row) {
      rows.add(row);
   }
   
   public void addValueSet(String column, TreeSet<Object> values) {
      valueSets.put(column, values);
   }
   
   public TreeSet<Object> getValueSet(String column) {
      return valueSets.get(column);
   }
   
   @JsonIgnore
   @Override
   public Response getResponse() {
      return Response.EMPTY;
   }
   
   public static class Column {
      @JsonProperty("field")
      private String field;

      public Column(String field) {
         this.field = field;
      }
      
      public String getField() {
         return field;
      }

   }

}
