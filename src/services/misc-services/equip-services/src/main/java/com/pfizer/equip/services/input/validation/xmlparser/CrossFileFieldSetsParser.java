package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

@XmlAccessorType(XmlAccessType.FIELD)
public class CrossFileFieldSetsParser {
   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   @XmlElement(name = "ColumnName")
   private List<String> columnName;

   public ValidationStatusTypes getLogLevel() {
      return ValidationStatusTypes.valueOf(logLevel.toUpperCase());
   }

   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }

   public List<String> getColumnName() {
      return columnName;
   }

   public void setColumnName(List<String> columnName) {
      this.columnName = columnName;
   }
}
