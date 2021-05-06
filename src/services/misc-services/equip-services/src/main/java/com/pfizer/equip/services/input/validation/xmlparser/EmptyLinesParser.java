package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class EmptyLinesParser {
   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }
}
