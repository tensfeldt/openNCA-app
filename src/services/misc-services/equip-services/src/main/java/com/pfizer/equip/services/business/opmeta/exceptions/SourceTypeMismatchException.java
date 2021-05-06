package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class SourceTypeMismatchException extends RuntimeException {
   public SourceTypeMismatchException() {}

   public SourceTypeMismatchException(String msg) {
      super(msg);
   }

   public SourceTypeMismatchException(String msg, Throwable t) {
      super(msg, t);
   }
}