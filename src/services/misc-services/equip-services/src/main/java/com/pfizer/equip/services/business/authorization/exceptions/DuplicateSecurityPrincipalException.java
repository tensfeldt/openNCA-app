package com.pfizer.equip.services.business.authorization.exceptions;

@SuppressWarnings("serial")
public class DuplicateSecurityPrincipalException extends RuntimeException {
   public DuplicateSecurityPrincipalException() {}

   public DuplicateSecurityPrincipalException(String msg) {
      super(msg);
   }

   public DuplicateSecurityPrincipalException(String msg, Throwable t) {
      super(msg, t);
   }
}
