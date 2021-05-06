package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnAliasParser {
   @XmlElement(name = "Value")
   private List<String> value;

   public List<String> getValue() {
      return value;
   }

   public void setValue(List<String> value) {
      this.value = value;
   }

}
