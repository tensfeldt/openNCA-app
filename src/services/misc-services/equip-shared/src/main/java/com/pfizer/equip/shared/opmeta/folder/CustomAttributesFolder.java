package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.KeyValuePair;

@SuppressWarnings("serial")
public class CustomAttributesFolder extends BaseFolder {
   @JsonProperty("jcr:primaryType")
   public static final String PRIMARY_TYPE = "opmeta:kvpFolder";
   public static final String NAME = "CustomAttributes";
   public static final String CHILD_NODE_NAME = "kvp";

   @JsonProperty("children")
   Map<String, KeyValuePair> children;

   public Map<String, KeyValuePair> getChildren() {
      return children;
   }

   public void setChildren(Map<String, KeyValuePair> children) {
      this.children = children;
   }
}