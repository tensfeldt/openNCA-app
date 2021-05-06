package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileLevelRulesParser {

   @XmlAttribute(name = "LogLevel")
   private String logLevel;
   
   @XmlAttribute(name = "RuleType")
   private String ruleType;

   @XmlElement(name = "Columns")
   private ColumnsParser columnsInput;

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

   public String getRuleType() {
      return ruleType;
   }

   public void setRuleType(String ruleType) {
      this.ruleType = ruleType;
   }

   public ColumnsParser getColumnsInput() {
      return columnsInput;
   }

   public void setColumnsInput(ColumnsParser columnsInput) {
      this.columnsInput = columnsInput;
   }


}
