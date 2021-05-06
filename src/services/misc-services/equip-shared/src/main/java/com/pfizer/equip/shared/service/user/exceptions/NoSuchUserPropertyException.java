package com.pfizer.equip.shared.service.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchUserPropertyException extends RuntimeException {
   public NoSuchUserPropertyException() {}

   public NoSuchUserPropertyException(String msg) {
      super(msg);
   }

   public NoSuchUserPropertyException(String msg, Throwable t) {
      super(msg, t);
   }
}
