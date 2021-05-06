package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnNameParser {
   @XmlValue
   private String value;
   
   @XmlAttribute(name = "CaseSensitive")
   private boolean caseSensitive;
   
   @XmlAttribute(name = "CaseSensitivityLogLevel")
   private String caseSensitivityLogLevel;
   
   @XmlAttribute(name = "ExtraSpacesHeaderCheck")
   private boolean extraSpacesHeaderCheck;
   
   @XmlAttribute(name = "ExtraSpacesHeaderLogLevel")
   private String extraSpacesHeaderLogLevel;

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public boolean isCaseSensitive() {
      return caseSensitive;
   }

   public void setCaseSensitive(boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
   }

   public ValidationStatusTypes getCaseSensitivityLogLevel() {
      return ValidationStatusTypes.valueOf(caseSensitivityLogLevel.toUpperCase());
   }

   public void setCaseSensitivityLogLevel(String caseSensitivityLogLevel) {
      this.caseSensitivityLogLevel = caseSensitivityLogLevel;
   }

   public boolean isExtraSpacesHeaderCheck() {
      return extraSpacesHeaderCheck;
   }

   public void setExtraSpacesHeaderCheck(boolean extraSpacesHeaderCheck) {
      this.extraSpacesHeaderCheck = extraSpacesHeaderCheck;
   }

   public ValidationStatusTypes getExtraSpacesHeaderLogLevel() {
      return ValidationStatusTypes.valueOf(extraSpacesHeaderLogLevel.toUpperCase());
   }

   public void setExtraSpacesHeaderLogLevel(String extraSpacesHeaderLogLevel) {
      this.extraSpacesHeaderLogLevel = extraSpacesHeaderLogLevel;
   }

   

}
