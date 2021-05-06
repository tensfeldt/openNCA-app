package com.pfizer.equip.shared.service.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchGroupException extends RuntimeException {
   public NoSuchGroupException() {}

   public NoSuchGroupException(String msg) {
      super(msg);
   }

   public NoSuchGroupException(String msg, Throwable t) {
      super(msg, t);
   }
}
