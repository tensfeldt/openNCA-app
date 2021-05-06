package com.pfizer.equip.services.business.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RemoteClientErrorException extends RuntimeException {
   public RemoteClientErrorException() {}

   public RemoteClientErrorException(String msg) {
      super(msg);
   }

   public RemoteClientErrorException(String msg, Throwable t) {
      super(msg, t);
   }
}
