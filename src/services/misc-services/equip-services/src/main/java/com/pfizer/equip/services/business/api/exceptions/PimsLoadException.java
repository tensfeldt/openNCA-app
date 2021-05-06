package com.pfizer.equip.services.business.api.exceptions;

@SuppressWarnings("serial")
public class PimsLoadException extends RuntimeException {
   public PimsLoadException() {}

   public PimsLoadException(String msg) {
      super(msg);
   }

   public PimsLoadException(String msg, Throwable t) {
      super(msg, t);
   }
}
