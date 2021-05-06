package com.pfizer.equip.shared.service.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchUserException extends EquipException {
   public NoSuchUserException() {}

   public NoSuchUserException(String msg) {
      super(msg);
   }

   public NoSuchUserException(String msg, Throwable t) {
      super(msg, t);
   }
}
