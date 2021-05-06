package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class FieldSetsParser {

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   @XmlElement(name = "Columns")
   private ColumnsParser columnsInput;

   @XmlElement(name = "Rules")
   private RulesParser rulesInput;

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

   public ColumnsParser getColumnsInput() {
      return columnsInput;
   }

   public void setColumnsInput(ColumnsParser columnsInput) {
      this.columnsInput = columnsInput;
   }

   public RulesParser getRulesInput() {
      return rulesInput;
   }

   public void setRulesInput(RulesParser rulesInput) {
      this.rulesInput = rulesInput;
   }

}
