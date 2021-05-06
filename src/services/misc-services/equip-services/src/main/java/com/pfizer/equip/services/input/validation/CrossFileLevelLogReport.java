package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class CrossFileLevelLogReport extends AbstractLogLevelReport {
   private String columnValue;
   private int rowIndex;
   private String pksColumnValue;

   public CrossFileLevelLogReport(int rowIndex, String columnName, String columnValue, String message, ValidationStatusTypes logLevel, String pksColumnValue) {
      this.setRowIndex(rowIndex);
      this.setName(columnName);
      this.setColumnValue(columnValue);
      this.setMessage(message);
      this.setLogLevel(logLevel);
      this.setPksColumnValue(pksColumnValue);
   }

   public CrossFileLevelLogReport() {}

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

   public String getPksColumnValue() {
      return pksColumnValue;
   }

   public void setPksColumnValue(String pksColumnValue) {
      this.pksColumnValue = pksColumnValue;
   }

}
