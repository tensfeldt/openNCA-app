package com.pfizer.equip.services.business.authorization.exceptions;

@SuppressWarnings("serial")
public class EntityTypeNotSupportedException extends RuntimeException {
   public EntityTypeNotSupportedException() {}

   public EntityTypeNotSupportedException(String msg) {
      super(msg);
   }

   public EntityTypeNotSupportedException(String msg, Throwable t) {
      super(msg, t);
   }
}
