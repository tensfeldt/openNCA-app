package com.pfizer.equip.services.input.validation;

public class FileValidationErrorInput {

   private final int rowIndex;

   private final String columnName;

   private final String columnValue;

   private final String errorMsg;

   public FileValidationErrorInput(final int rowIndex, final String columnName, final String columnValue, final String errorMsg) {
      this.rowIndex = rowIndex;
      this.columnName = columnName;
      this.columnValue = columnValue;
      this.errorMsg = errorMsg;
   }

   public int getRowIndex() {
      return rowIndex;
   }

   public String getColumnName() {
      return columnName;
   }

   public String getColumnValue() {
      return columnValue;
   }

   public String getErrorMsg() {
      return errorMsg;
   }

}
