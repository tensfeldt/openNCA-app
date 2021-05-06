package com.pfizer.equip.shared.exceptions;

@SuppressWarnings("serial")
public class UnsupportedFeatureException extends RuntimeException {
   public UnsupportedFeatureException() {}

   public UnsupportedFeatureException(String msg) {
      super(msg);
   }

   public UnsupportedFeatureException(String msg, Throwable t) {
      super(msg, t);
   }
}
