package com.pfizer.equip.services.exceptions;

@SuppressWarnings("serial")
public class InvalidUserException extends RuntimeException {
   public InvalidUserException() {}
   
   public InvalidUserException(String msg) {
      super(msg);
   }
   
   public InvalidUserException(String msg, Throwable t) {
      super(msg, t);
   }
}
