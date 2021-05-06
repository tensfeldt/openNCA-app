package com.pfizer.equip.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends EquipException {
   public NotAuthorizedException() {}

   public NotAuthorizedException(String msg) {
      super(msg);
   }

   public NotAuthorizedException(String msg, Throwable t) {
      super(msg, t);
   }
}