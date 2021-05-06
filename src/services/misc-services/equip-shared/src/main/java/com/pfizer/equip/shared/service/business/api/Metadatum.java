package com.pfizer.equip.shared.service.business.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// Class for storing "metadatum" key-value pairs sent from dataframe service
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadatum {
   private String id;
   private String key;
   private List<String> value;
   private String valueType;
   private boolean isDeleted;
   
   public final static String STRING_TYPE = "STRING";
   public final static String METADATUM_TYPE = "metadatum";

   public Metadatum(String key, String value, String type) {
      this.key = key;
      this.valueType = type;
      this.value = new ArrayList<String>();
      this.value.add(value);
   }

   public Metadatum() {
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

   public String getValueType() {
      return valueType;
   }
   public void setValueType(String type) {
      this.valueType = type;
   }

   public boolean isDeleted() {
      return isDeleted;
   }

   public void setDeleted(boolean isDeleted) {
      this.isDeleted = isDeleted;
   }
}