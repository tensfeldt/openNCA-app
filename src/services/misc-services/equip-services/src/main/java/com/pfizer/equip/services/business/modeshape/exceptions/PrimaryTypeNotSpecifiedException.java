package com.pfizer.equip.services.business.modeshape.exceptions;

@SuppressWarnings("serial")
public class PrimaryTypeNotSpecifiedException extends RuntimeException {
   public PrimaryTypeNotSpecifiedException() {}

   public PrimaryTypeNotSpecifiedException(String msg) {
      super(msg);
   }

   public PrimaryTypeNotSpecifiedException(String msg, Throwable t) {
      super(msg, t);
   }
}
