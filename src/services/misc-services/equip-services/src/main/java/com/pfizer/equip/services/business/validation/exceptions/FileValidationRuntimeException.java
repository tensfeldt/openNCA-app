package com.pfizer.equip.services.business.validation.exceptions;

public class FileValidationRuntimeException extends RuntimeException {
   /**
    * Custom Runtime Exception Class for File Validation
    */

   private static final long serialVersionUID = 7951913324017256718L;

   public FileValidationRuntimeException() {}

   public FileValidationRuntimeException(String msg) {
      super(msg);
   }

   public FileValidationRuntimeException(String msg, Throwable t) {
      super(msg, t);
   }

   public FileValidationRuntimeException(Throwable cause) {
      super(cause);
   }
}
