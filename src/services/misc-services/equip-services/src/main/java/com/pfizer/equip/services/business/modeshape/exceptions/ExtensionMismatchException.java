package com.pfizer.equip.services.business.modeshape.exceptions;

@SuppressWarnings("serial")
public class ExtensionMismatchException extends RuntimeException {
   public ExtensionMismatchException() {}

   public ExtensionMismatchException(String msg) {
      super(msg);
   }

   public ExtensionMismatchException(String msg, Throwable t) {
      super(msg, t);
   }
}
