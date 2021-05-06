package com.pfizer.equip.services.business.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class UniqueSetSDEID {
   private ValidationStatusTypes logLevel;
   private Set<String> uniqueColumns;
   private Map<List<String>, List<List<String>>> scopedRows = new HashMap<>();
   private Map<List<String>, List<String>> previousValues = new HashMap<>();

   public Set<String> getUniqueColumns() {
      return uniqueColumns;
   }
   public void setUniqueColumns(Set<String> uniqueColumns) {
      this.uniqueColumns = uniqueColumns;
   }
   public Map<List<String>, List<List<String>>> getScopedRows() {
      return scopedRows;
   }
   public void setScopedRows(Map<List<String>, List<List<String>>> scopedRows) {
      this.scopedRows = scopedRows;
   }
   public ValidationStatusTypes getLogLevel() {
      return logLevel;
   }
   public void setLogLevel(ValidationStatusTypes logLevel) {
      this.logLevel = logLevel;
   }
   public Map<List<String>, List<String>> getPreviousValues() {
      return previousValues;
   }
   public void setPreviousValues(Map<List<String>, List<String>> previousValues) {
      this.previousValues = previousValues;
   }
}
