package com.pfizer.equip.services.business.api.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// Class for storing "metadatum" key-value pairs sent from dataframe service
// Probably want to convert to custom serializer + Map instead, at some point.
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValuePairInput implements GenericInput {
   private String id;
   private String key;
   private List<String> value;
   private String type;
   
   public final static String STRING_TYPE = "string";
   public final static String METADATUM_TYPE = "metadatum";

   public KeyValuePairInput(String key, String value, String type) {
      this.key = key;
      this.type = type;
      this.value = new ArrayList<String>();
      this.value.add(value);
   }

   public KeyValuePairInput() {
      // Constructor used for creating empty KVP node, need to include empty node if no parameters for compute service API
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getKey() {
      return key;
   }
   public void setKey(String key) {
      this.key = key;
   }

   public List<String> getValue() {
      return value;
   }
   public void setValue(List<String> value) {
      this.value = value;
   }

   public String getType() {
      return type;
   }
   public void setType(String type) {
      this.type = type;
   }
   
   @Override
   public int hashCode() {
      return Objects.hash(id, key, type, value);
   }
   
   @Override
   public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof KeyValuePairInput)) {
          return false;
      }
      KeyValuePairInput input = (KeyValuePairInput) o;
      return Objects.equals(id, input.id) && Objects.equals(key, input.key) 
            && Objects.equals(type, input.type) && Objects.equals(value, input.value);
   }
}