package com.pfizer.equip.services.business.modeshape.nodes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * This class represents a key/value pair node.
 *
 */
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown=true)
public class KeyValuePairNode {
   @JsonProperty("jcr:primaryType")
   private String primaryType = "equipLibrary:kvp";

   // custom properties from equip:kvp
   @JsonProperty("equipLibrary:key")
   private String key;

   @JsonProperty("equipLibrary:value")
   private String value;

   @JsonProperty("equipLibrary:type")
   private String type;

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
