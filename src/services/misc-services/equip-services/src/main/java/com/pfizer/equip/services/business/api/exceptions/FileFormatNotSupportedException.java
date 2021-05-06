package com.pfizer.equip.services.business.api.exceptions;

@SuppressWarnings("serial")
public class FileFormatNotSupportedException extends RuntimeException {
   public FileFormatNotSupportedException() {}

   public FileFormatNotSupportedException(String msg) {
      super(msg);
   }

   public FileFormatNotSupportedException(String msg, Throwable t) {
      super(msg, t);
   }
}
