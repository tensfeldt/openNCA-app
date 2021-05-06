package com.pfizer.equip.services.business.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class UniqueSet {
   private ValidationStatusTypes logLevel;
   private LinkedHashSet<String> uniqueColumns;
   private List<List<String>> rows = new ArrayList<>();
   private List<String> previousValue;

   public LinkedHashSet<String> getUniqueColumns() {
      return uniqueColumns;
   }
   public void setUniqueColumns(LinkedHashSet<String> uniqueColumns) {
      this.uniqueColumns = uniqueColumns;
   }
   public List<List<String>> getRows() {
      return rows;
   }
   public void setRows(List<List<String>> rows) {
      this.rows = rows;
   }
   public ValidationStatusTypes getLogLevel() {
      return logLevel;
   }
   public void setLogLevel(ValidationStatusTypes logLevel) {
      this.logLevel = logLevel;
   }
   public List<String> getPreviousValue() {
      return previousValue;
   }
   public void setPreviousValue(List<String> previousValue) {
      this.previousValue = previousValue;
   }
}
