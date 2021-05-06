package com.pfizer.equip.services.business.authorization.exceptions;

@SuppressWarnings("serial")
public class ExternalGroupAlreadyMappedException extends RuntimeException {
   public ExternalGroupAlreadyMappedException() {}

   public ExternalGroupAlreadyMappedException(String msg) {
      super(msg);
   }

   public ExternalGroupAlreadyMappedException(String msg, Throwable t) {
      super(msg, t);
   }
}
