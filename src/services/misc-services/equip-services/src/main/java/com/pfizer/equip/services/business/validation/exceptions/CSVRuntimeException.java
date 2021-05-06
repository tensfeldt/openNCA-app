package com.pfizer.equip.services.business.validation.exceptions;

public class CSVRuntimeException extends RuntimeException {

   /**
    * Custom Runtime Exception Class for CSV File Reader
    */
   private static final long serialVersionUID = -1762638256881461535L;

   public CSVRuntimeException() {}

   public CSVRuntimeException(String msg) {
      super(msg);
   }

   public CSVRuntimeException(String msg, Throwable t) {
      super(msg, t);
   }

}
