package com.pfizer.equip.services.business.authorization.exceptions;

@SuppressWarnings("serial")
public class MissingSecurityPrincipalException extends RuntimeException {
   public MissingSecurityPrincipalException() {}

   public MissingSecurityPrincipalException(String msg) {
      super(msg);
   }

   public MissingSecurityPrincipalException(String msg, Throwable t) {
      super(msg, t);
   }
}
