package com.pfizer.equip.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {
   public InvalidRequestException(String msg) {
      super(msg);
   }

   public InvalidRequestException(String msg, Throwable t) {
      super(msg, t);
   }
}
