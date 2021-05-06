package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.Protocol;

@SuppressWarnings("serial")
public class ProtocolFolder extends BaseFolder {
   // Cannot be static for Jackson:
   @JsonProperty("jcr:primaryType")
   public final String PRIMARY_TYPE =  "opmeta:protocolFolder";
   public static final String NAME = "Protocols";

   @JsonProperty("children")
   Map<String, Protocol> children;

   public Map<String, Protocol> getChildren() {
      return children;
   }

   public void setChildren(Map<String, Protocol> children) {
      this.children = children;
   }
}
