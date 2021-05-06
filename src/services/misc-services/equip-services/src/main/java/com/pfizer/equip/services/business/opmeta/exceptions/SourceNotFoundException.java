package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SourceNotFoundException extends RuntimeException {
   public SourceNotFoundException() {}

   public SourceNotFoundException(String msg) {
      super(msg);
   }

   public SourceNotFoundException(String msg, Throwable t) {
      super(msg, t);
   }
}