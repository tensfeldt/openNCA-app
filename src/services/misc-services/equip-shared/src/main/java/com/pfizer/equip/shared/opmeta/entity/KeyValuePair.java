package com.pfizer.equip.shared.opmeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.folder.CustomAttributesFolder;

@SuppressWarnings("serial")
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown=true)
public class KeyValuePair extends BaseProtocolChildNode {

   @JsonIgnore
   private final String PRIMARY_TYPE = "opmeta:kvp";
   
   public KeyValuePair() {
      this.primaryType = PRIMARY_TYPE;
   }

   public KeyValuePair(String key, Object value) {
      this.primaryType = PRIMARY_TYPE;
      this.key = key;
      this.value = value;
   }

   @JsonProperty("opmeta:key")
   private String key;

   @JsonProperty("opmeta:value")
   private Object value;

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public Object getValue() {
      return value;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   @Override
   public String getPath() {
      return String.format("%s/%s", CustomAttributesFolder.NAME, CustomAttributesFolder.CHILD_NODE_NAME);
   }
}
