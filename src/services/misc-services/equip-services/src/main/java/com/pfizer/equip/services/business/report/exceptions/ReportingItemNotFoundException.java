package com.pfizer.equip.services.business.report.exceptions;

@SuppressWarnings("serial")
public class ReportingItemNotFoundException extends RuntimeException {
   public ReportingItemNotFoundException() {}

   public ReportingItemNotFoundException(String msg) {
      super(msg);
   }

   public ReportingItemNotFoundException(String msg, Throwable t) {
      super(msg, t);
   }
}
