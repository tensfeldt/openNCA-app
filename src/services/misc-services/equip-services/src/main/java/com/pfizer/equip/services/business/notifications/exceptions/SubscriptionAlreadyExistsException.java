package com.pfizer.equip.services.business.notifications.exceptions;

@SuppressWarnings("serial")
public class SubscriptionAlreadyExistsException extends RuntimeException {
   public SubscriptionAlreadyExistsException() {}

   public SubscriptionAlreadyExistsException(String msg) {
      super(msg);
   }

   public SubscriptionAlreadyExistsException(String msg, Throwable t) {
      super(msg, t);
   }
}
