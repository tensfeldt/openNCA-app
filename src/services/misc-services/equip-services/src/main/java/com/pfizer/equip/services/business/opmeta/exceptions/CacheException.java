package com.pfizer.equip.services.business.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CacheException extends RuntimeException {
   public CacheException() {}

   public CacheException(String msg) {
      super(msg);
   }

   public CacheException(String msg, Throwable t) {
      super(msg, t);
   }
}