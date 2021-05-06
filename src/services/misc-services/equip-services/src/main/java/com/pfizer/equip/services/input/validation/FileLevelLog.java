package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class FileLevelLog extends AbstractLogLevelReport {
   public FileLevelLog() {

   }

   public FileLevelLog(String columnName, ValidationStatusTypes logLevel, String message) {
      this.setName(columnName);
      this.setMessage(message);
      this.setLogLevel(logLevel);
   }
}
