package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class GroupingParser {

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   @XmlAttribute(name = "Scope")
   private String scope;

   @XmlElement(name = "Columns")
   private ColumnsParser columnsInput;

   @XmlElement(name = "ScopeColumns")
   private ColumnsParser scopeColumnsInput;

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

   public String getScope() {
      return scope;
   }

   public void setScope(String scope) {
      this.scope = scope;
   }

   public ColumnsParser getScopeColumnsInput() {
      return scopeColumnsInput;
   }

   public void setScopeColumnsInput(ColumnsParser scopeColumnsInput) {
      this.scopeColumnsInput = scopeColumnsInput;
   }
}
