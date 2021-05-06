package com.pfizer.equip.shared.service.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchRoleException extends RuntimeException {
   public NoSuchRoleException() {}

   public NoSuchRoleException(String msg) {
      super(msg);
   }

   public NoSuchRoleException(String msg, Throwable t) {
      super(msg, t);
   }
}
