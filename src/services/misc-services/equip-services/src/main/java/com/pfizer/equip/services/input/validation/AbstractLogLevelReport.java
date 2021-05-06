package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public abstract class AbstractLogLevelReport {
   //It might be an Attibute Name, Field Name, Global Information attribute
   private String name;
   
   //message for various log levels
   private String message;
   
   //It may be an Information/Warning/Error
   private ValidationStatusTypes logLevel;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public ValidationStatusTypes getLogLevel() {
      return logLevel;
   }

   public void setLogLevel(ValidationStatusTypes logLevel) {
      this.logLevel = logLevel;
   }
   
}
