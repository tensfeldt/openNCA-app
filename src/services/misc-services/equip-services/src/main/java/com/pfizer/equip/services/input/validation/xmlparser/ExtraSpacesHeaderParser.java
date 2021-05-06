package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExtraSpacesHeaderParser {

   @XmlValue
   private boolean value;

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

  
   public boolean isValue() {
      return value;
   }

   public void setValue(boolean value) {
      this.value = value;
   }

   public String getLogLevel() {
      return logLevel;
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

}
