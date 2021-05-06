package com.pfizer.equip.services.business.modeshape.exceptions;

@SuppressWarnings("serial")
public class TransactionException extends RuntimeException {
   public TransactionException() {}

   public TransactionException(String msg) {
      super(msg);
   }

   public TransactionException(String msg, Throwable t) {
      super(msg, t);
   }
}