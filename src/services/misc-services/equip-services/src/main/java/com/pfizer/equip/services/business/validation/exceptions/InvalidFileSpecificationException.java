package com.pfizer.equip.services.business.validation.exceptions;

public class InvalidFileSpecificationException extends RuntimeException {
   /**
    * Custom Runtime Exception Class for File Validation
    */

   private static final long serialVersionUID = 7951913324017256718L;

   public InvalidFileSpecificationException() {}

   public InvalidFileSpecificationException(String msg) {
      super(msg);
   }

   public InvalidFileSpecificationException(String msg, Throwable t) {
      super(msg, t);
   }

   public InvalidFileSpecificationException(Throwable cause) {
      super(cause);
   }
}
