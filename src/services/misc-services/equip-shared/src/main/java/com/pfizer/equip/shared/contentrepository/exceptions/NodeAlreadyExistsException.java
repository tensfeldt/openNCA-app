package com.pfizer.equip.shared.contentrepository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.CONFLICT)
public class NodeAlreadyExistsException extends RuntimeException {
   public NodeAlreadyExistsException() {}

   public NodeAlreadyExistsException(String msg) {
      super(msg);
   }

   public NodeAlreadyExistsException(String msg, Throwable t) {
      super(msg, t);
   }
}
