package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class UniqueSetLevelLogReport extends AbstractLogLevelReport {

   private String columnValue;
   private int rowIndex;
   private String scope;

   public UniqueSetLevelLogReport(int rowIndex, String columnName, String columnValue, String message, ValidationStatusTypes logLevel, String scope) {
      this.setRowIndex(rowIndex);
      this.setName(columnName);
      this.setColumnValue(columnValue);
      this.setMessage(message);
      this.setLogLevel(logLevel);
      this.setScope(scope);
   }

   public UniqueSetLevelLogReport() {}

   public String getColumnValue() {
      return columnValue;
   }

   public void setColumnValue(String columnValue) {
      this.columnValue = columnValue;
   }

   public int getRowIndex() {
      return rowIndex;
   }

   public void setRowIndex(int rowIndex) {
      this.rowIndex = rowIndex;
   }

   public String getScope() {
      return scope;
   }

   public void setScope(String scope) {
      this.scope = scope;
   }

}
