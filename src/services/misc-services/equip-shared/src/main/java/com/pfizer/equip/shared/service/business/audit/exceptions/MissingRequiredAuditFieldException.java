package com.pfizer.equip.shared.service.business.audit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingRequiredAuditFieldException extends RuntimeException {
   public MissingRequiredAuditFieldException() {}

   public MissingRequiredAuditFieldException(String msg) {
      super(msg);
   }

   public MissingRequiredAuditFieldException(String msg, Throwable t) {
      super(msg, t);
   }
}
