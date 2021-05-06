package com.pfizer.equip.services.input.validation.types;

public enum DelimiterInputTypes {
   COMMA(","),
   PIPELINE("\\|"),
   ASTERISK("\\*"),
   CARAT("\\^"),
   SEMICOLON(";");
   
   private String delimterType;

   DelimiterInputTypes(String delimiter) {
      delimterType = delimiter;
   }

   public String getDelimterType() {
      return delimterType;
   }

}
