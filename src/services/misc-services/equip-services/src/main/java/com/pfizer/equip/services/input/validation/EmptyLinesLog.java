package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class EmptyLinesLog extends AbstractLogLevelReport {
   // Row Index of the input file where the empty lines are detected
   private int rowIndex;

   public EmptyLinesLog(int rowindex, ValidationStatusTypes logLevel, String message) {
      this.setRowIndex(rowindex);
      this.setMessage(message);
      this.setLogLevel(logLevel);
   }

   public EmptyLinesLog() {

   }

   public int getRowIndex() {
      return rowIndex;
   }

   public void setRowIndex(int rowIndex) {
      this.rowIndex = rowIndex;
   }

}
