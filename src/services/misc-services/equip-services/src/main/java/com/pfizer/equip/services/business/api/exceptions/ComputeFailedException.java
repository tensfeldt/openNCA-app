package com.pfizer.equip.services.business.api.exceptions;

@SuppressWarnings("serial")
public class ComputeFailedException extends RuntimeException {
   public ComputeFailedException() {}

   public ComputeFailedException(String msg) {
      super(msg);
   }

   public ComputeFailedException(String msg, Throwable t) {
      super(msg, t);
   }
}
