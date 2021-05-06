package com.pfizer.equip.services.exceptions;

@SuppressWarnings("serial")
public class FailedGroupCopyAccessException extends RuntimeException {
   public FailedGroupCopyAccessException(String msg) {
      super(msg);
   }

   public FailedGroupCopyAccessException(String msg, Throwable t) {
      super(msg, t);
   }
}
