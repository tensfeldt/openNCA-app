package com.pfizer.equip.shared.opmeta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class MissingSnapshotException extends RuntimeException {
   public MissingSnapshotException() {}

   public MissingSnapshotException(String msg) {
      super(msg);
   }

   public MissingSnapshotException(String msg, Throwable t) {
      super(msg, t);
   }
}
