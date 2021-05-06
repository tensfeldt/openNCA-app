package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileNamePatternParser {
   @XmlElement(name = "Pattern")
   private List<String> value;

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   public List<String> getValue() {
      return value;
   }

   public void setValue(List<String> value) {
      this.value = value;
   }

   public ValidationStatusTypes getLogLevel() {
      return logLevel != null ? ValidationStatusTypes.valueOf(logLevel.toUpperCase()) : ValidationStatusTypes.INFO;
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }
}
