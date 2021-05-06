package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeParser {
   @XmlValue
   private String value;

   @XmlAttribute(name = "SubType")
   private String subType;

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

   public String getSubType() {
      return subType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }

}
