package com.pfizer.equip.shared.exceptions;

@SuppressWarnings("serial")
public class EquipException extends RuntimeException {
   public EquipException() {}

   public EquipException(String msg) {
      super(msg);
   }

   public EquipException(String msg, Throwable t) {
      super(msg, t);
   }
}