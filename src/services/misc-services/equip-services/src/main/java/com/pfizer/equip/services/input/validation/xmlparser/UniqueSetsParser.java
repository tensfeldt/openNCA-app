package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class UniqueSetsParser {

   @XmlElement(name = "Unique")
   private List<GroupingParser> unique;

   @XmlElement(name = "Identical")
   private List<GroupingParser> identical;

   public List<GroupingParser> getUnique() {
      return unique;
   }

   public void setUnique(List<GroupingParser> unique) {
      this.unique = unique;
   }

   public List<GroupingParser> getIdentical() {
      return identical;
   }

   public void setIdentical(List<GroupingParser> identical) {
      this.identical = identical;
   }
}
