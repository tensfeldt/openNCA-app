package com.pfizer.equip.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotAuthenticatedException extends EquipException {
   public NotAuthenticatedException() {}
   
   public NotAuthenticatedException(String msg) {
      super(msg);
   }
   
   public NotAuthenticatedException(String msg, Throwable t) {
      super(msg, t);
   }
}
