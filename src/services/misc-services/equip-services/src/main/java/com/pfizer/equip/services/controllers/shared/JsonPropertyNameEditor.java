package com.pfizer.equip.services.controllers.shared;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

@SuppressWarnings("serial")
public class JsonPropertyNameEditor extends JacksonAnnotationIntrospector
{
   Map<String, String> searchReplacePairs = new HashMap<String, String>();

   public JsonPropertyNameEditor(String search, String replace) {
      this.searchReplacePairs.put(search, replace);
   }

   public JsonPropertyNameEditor(String... searchReplacePairs) {
      for (int idx = 0; idx < searchReplacePairs.length; idx += 2) {
         this.searchReplacePairs.put(searchReplacePairs[idx], searchReplacePairs[idx + 1]);
      }
   }

   @Override
   public PropertyName findNameForSerialization(Annotated annotated) {
      PropertyName propertyName = super.findNameForSerialization(annotated);
      for (String searchKey : searchReplacePairs.keySet()) {
         if (propertyName != null && propertyName.getSimpleName().contains(searchKey)) {
            return propertyName.withSimpleName(propertyName.getSimpleName().replace(searchKey, searchReplacePairs.get(searchKey)));
         }
      }
      return propertyName;
   }
}