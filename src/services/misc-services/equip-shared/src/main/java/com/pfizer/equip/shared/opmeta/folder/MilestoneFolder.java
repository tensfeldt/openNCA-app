package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.Milestone;

@SuppressWarnings("serial")
public class MilestoneFolder extends BaseFolder {
   @JsonProperty("jcr:primaryType")
   public static final String PRIMARY_TYPE = "opmeta:milestoneFolder";
   public static final String NAME = "Milestones";
   public static final String CHILD_NODE_NAME = "milestone";

   @JsonProperty("children")
   Map<String, Milestone> children;

   public Map<String, Milestone> getChildren() {
      return children;
   }

   public void setChildren(Map<String, Milestone> children) {
      this.children = children;
   }
}