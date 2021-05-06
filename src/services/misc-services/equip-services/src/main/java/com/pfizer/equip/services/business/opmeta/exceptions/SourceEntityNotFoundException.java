package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SourceEntityNotFoundException extends RuntimeException {
   public SourceEntityNotFoundException() {}

   public SourceEntityNotFoundException(String msg) {
      super(msg);
   }

   public SourceEntityNotFoundException(String msg, Throwable t) {
      super(msg, t);
   }
}