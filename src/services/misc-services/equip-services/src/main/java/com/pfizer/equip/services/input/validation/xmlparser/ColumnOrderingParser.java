package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnOrderingParser {

   @XmlValue
   private boolean value;

   public boolean isValue() {
      return value;
   }

   public void setValue(boolean value) {
      this.value = value;
   }

}
