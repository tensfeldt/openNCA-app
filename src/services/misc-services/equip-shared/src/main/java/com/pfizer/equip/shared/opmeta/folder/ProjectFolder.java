package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.Project;

@SuppressWarnings("serial")
public class ProjectFolder extends BaseFolder {
   // Cannot be static for Jackson:
   @JsonProperty("jcr:primaryType")
   public final String PRIMARY_TYPE = "opmeta:projectFolder";
   public static final String NAME = "Projects";

   @JsonProperty("children")
   Map<String, Project> children;

   public Map<String, Project> getChildren() {
      return children;
   }

   public void setChildren(Map<String, Project> children) {
      this.children = children;
   }
}
