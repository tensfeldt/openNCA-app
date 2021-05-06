package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.Program;

@SuppressWarnings("serial")
public class ProgramFolder extends BaseFolder {
   // Cannot be static for Jackson:
   @JsonProperty("jcr:primaryType")
   public final String PRIMARY_TYPE = "opmeta:programFolder";
   public static final String NAME = "Programs";
   
   @JsonProperty("children")
   Map<String, Program> children;

   public Map<String, Program> getChildren() {
      return children;
   }

   public void setChildren(Map<String, Program> children) {
      this.children = children;
   }
}
