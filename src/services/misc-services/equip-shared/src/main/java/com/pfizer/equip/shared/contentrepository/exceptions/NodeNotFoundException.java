package com.pfizer.equip.shared.contentrepository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends RuntimeException {
   public NodeNotFoundException() {}

   public NodeNotFoundException(String msg) {
      super(msg);
   }

   public NodeNotFoundException(String msg, Throwable t) {
      super(msg, t);
   }
}
