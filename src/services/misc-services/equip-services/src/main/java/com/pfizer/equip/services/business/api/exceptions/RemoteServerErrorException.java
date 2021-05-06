package com.pfizer.equip.services.business.api.exceptions;

@SuppressWarnings("serial")
public class RemoteServerErrorException extends RuntimeException {
   public RemoteServerErrorException() {}

   public RemoteServerErrorException(String msg) {
      super(msg);
   }

   public RemoteServerErrorException(String msg, Throwable t) {
      super(msg, t);
   }
}
