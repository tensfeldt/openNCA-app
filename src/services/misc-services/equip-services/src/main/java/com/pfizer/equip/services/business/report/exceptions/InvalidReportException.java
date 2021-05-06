package com.pfizer.equip.services.business.report.exceptions;

@SuppressWarnings("serial")
public class InvalidReportException extends RuntimeException {
   public InvalidReportException() {}

   public InvalidReportException(String msg) {
      super(msg);
   }

   public InvalidReportException(String msg, Throwable t) {
      super(msg, t);
   }
}
