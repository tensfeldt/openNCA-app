package com.pfizer.equip.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AuditRuntimeException extends RuntimeException {
   public AuditRuntimeException() {}

   public AuditRuntimeException(String msg) {
      super(msg);
   }

   public AuditRuntimeException(String msg, Throwable t) {
      super(msg, t);
   }
}
