package com.pfizer.equip.shared.service.user.exceptions;

@SuppressWarnings("serial")
public class NoSuchExternalGroupException extends RuntimeException {
   public NoSuchExternalGroupException() {}

   public NoSuchExternalGroupException(String msg) {
      super(msg);
   }

   public NoSuchExternalGroupException(String msg, Throwable t) {
      super(msg, t);
   }
}
