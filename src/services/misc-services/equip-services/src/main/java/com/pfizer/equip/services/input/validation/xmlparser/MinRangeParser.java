package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class MinRangeParser {
   @XmlValue
   private String value;

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

}
