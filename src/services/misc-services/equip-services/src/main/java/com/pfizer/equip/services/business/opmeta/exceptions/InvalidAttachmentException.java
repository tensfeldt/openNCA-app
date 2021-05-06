package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidAttachmentException extends RuntimeException {
   public InvalidAttachmentException() {}

   public InvalidAttachmentException(String msg) {
      super(msg);
   }

   public InvalidAttachmentException(String msg, Throwable t) {
      super(msg, t);
   }
}