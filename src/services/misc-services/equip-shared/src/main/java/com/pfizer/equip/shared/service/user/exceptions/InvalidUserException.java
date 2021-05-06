package com.pfizer.equip.shared.service.user.exceptions;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
public class InvalidUserException extends EquipException {
   public InvalidUserException() {}

   public InvalidUserException(String msg) {
      super(msg);
   }

   public InvalidUserException(String msg, Throwable t) {
      super(msg, t);
   }
}
