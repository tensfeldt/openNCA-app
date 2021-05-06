package com.pfizer.equip.services.input.search;

public class SearchCriteria {
   private String field;
   private String operator;
   private String value;

   public String getField() {
      return field;
   }

   public String getOperator() {
      return operator;
   }

   public String getValue() {
      return value;
   }

   public void setField(String field) {
      this.field = field;
   }

   public void setOperator(String operator) {
      this.operator = operator;
   }

   public void setValue(String value) {
      this.value = value;
   }

}
