package com.pfizer.equip.shared.service.business.notifications.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidEventInputException extends RuntimeException {
   public InvalidEventInputException() {}

   public InvalidEventInputException(String msg) {
      super(msg);
   }

   public InvalidEventInputException(String msg, Throwable t) {
      super(msg, t);
   }
}
