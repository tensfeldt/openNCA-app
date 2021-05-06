package com.pfizer.equip.shared.service.list;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ListNotFoundException extends RuntimeException {
   public ListNotFoundException() {}

   public ListNotFoundException(String msg) {
      super(msg);
   }

   public ListNotFoundException(String msg, Throwable t) {
      super(msg, t);
   }
}
