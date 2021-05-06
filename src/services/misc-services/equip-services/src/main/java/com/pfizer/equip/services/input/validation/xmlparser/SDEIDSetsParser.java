package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class SDEIDSetsParser {

   @XmlAttribute(name = "LogLevel")
   private String logLevel;

   @XmlElement(name = "ColumnName")
   private List<String> columnName;

   public String getLogLevel() {
      return logLevel;
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
