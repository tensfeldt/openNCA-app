package com.pfizer.equip.services.business.authorization.exceptions;

@SuppressWarnings("serial")
public class DuplicateAccessRecordException extends RuntimeException {
   public DuplicateAccessRecordException() {}

   public DuplicateAccessRecordException(String msg) {
      super(msg);
   }

   public DuplicateAccessRecordException(String msg, Throwable t) {
      super(msg, t);
   }
}
