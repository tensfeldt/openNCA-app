package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchProtocolAliasException extends EquipException {
   public NoSuchProtocolAliasException() {}

   public NoSuchProtocolAliasException(String msg) {
      super(msg);
   }

   public NoSuchProtocolAliasException(String msg, Throwable t) {
      super(msg, t);
   }
}