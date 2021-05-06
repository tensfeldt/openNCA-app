package com.pfizer.equip.services.input.validation.types;

import com.pfizer.equip.services.utils.PatternUtils;

public enum FieldInputDataTypes {
   ALPHANUMERIC(PatternUtils.ALPHANUMERIC),
   ALPHANUMERICWITHSPACE(PatternUtils.ALPHANUMERIC_WITHSPACE),
   ALPHANUMERICWITHHYPHEN(PatternUtils.ALPHANUMERIC_WITHHYPHEN),
   ALPHABETONLY(PatternUtils.ALPHABETS_ONLY),
   NUMBER(PatternUtils.NUMBER),
   DATE(PatternUtils.PATTERN_DATE),
   TIME(PatternUtils.PATTERN_TIME),
   DATETIME(PatternUtils.PATTERN_DATETIME),
   ALPHANUMERICNOSPLCHAR(PatternUtils.ALPHANUMERIC_NOSPLCHAR);

   FieldInputDataTypes(String validatorDataType) {
      this.setValidatorDataType(validatorDataType);
   }

   private String validatorDataType;

   public String getValidatorDataType() {
      return validatorDataType;
   }

   public void setValidatorDataType(String validatorDataType) {
      this.validatorDataType = validatorDataType;
   }

}
