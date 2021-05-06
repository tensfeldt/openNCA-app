package com.pfizer.equip.services.business.report.exceptions;

@SuppressWarnings("serial")
public class InvalidReportingItemException extends RuntimeException {
   public InvalidReportingItemException() {}

   public InvalidReportingItemException(String msg) {
      super(msg);
   }

   public InvalidReportingItemException(String msg, Throwable t) {
      super(msg, t);
   }
}
