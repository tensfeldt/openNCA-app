package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.shared.exceptions.EquipException;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class DuplicateProtocolAliasException extends EquipException {
   public DuplicateProtocolAliasException() {}

   public DuplicateProtocolAliasException(String msg) {
      super(msg);
   }

   public DuplicateProtocolAliasException(String msg, Throwable t) {
      super(msg, t);
   }
}