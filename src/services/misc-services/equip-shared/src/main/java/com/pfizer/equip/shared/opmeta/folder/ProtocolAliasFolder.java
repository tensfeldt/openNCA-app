package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.ProtocolAlias;

@SuppressWarnings("serial")
public class ProtocolAliasFolder extends BaseFolder {
   // Cannot be static for Jackson:
   @JsonProperty("jcr:primaryType")
   public final String PRIMARY_TYPE = "opmeta:studyAliasFolder";
   public static final String NAME = "Aliases";
   public static final String CHILD_NODE_NAME = "alias";

   @JsonProperty("children")
   Map<String, ProtocolAlias> children;

   public Map<String, ProtocolAlias> getChildren() {
      return children;
   }

   public void setChildren(Map<String, ProtocolAlias> children) {
      this.children = children;
   }
}
