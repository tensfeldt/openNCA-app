package com.pfizer.equip.shared.responses;

public enum Response {
   OK("OK"), 
   FAILED("FAILED"),
   WARNING("WARNING"),
   EMPTY("");
   
   private final String value;
   private Response(String value) {
      this.value = value;
   }
   
   public String getValue() {
      return value;
   }
   
   @Override
   public String toString() {
      return this.getValue();
   }
}
