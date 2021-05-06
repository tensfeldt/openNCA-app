package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class AllowedValuesParser {

   @XmlElement(name = "Value")
   private List<String> value;

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   @XmlAttribute(name = "CaseSensitive")
   private boolean caseSensitive;

   public List<String> getValue() {
      return value;
   }

   public void setValue(List<String> value) {
      this.value = value;
   }

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

   public boolean getCaseSensitive() {
      return caseSensitive;
   }

   public void setCaseSensitive(boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
   }
   
   

}
