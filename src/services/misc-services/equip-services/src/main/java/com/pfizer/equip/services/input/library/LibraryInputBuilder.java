package com.pfizer.equip.services.input.library;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.pfizer.equip.services.input.MultiValueInputBuilder;

public class LibraryInputBuilder implements MultiValueInputBuilder {
   public LibraryInput build(String userId, Map<String, Object> inputs) {
      LibraryInput input = new LibraryInput();

      // set custom metadata using reflection
      Field[] fields = LibraryInput.class.getDeclaredFields();
      for (Field field : fields) {
         String name = field.getName();
         Object value = inputs.get(name);
         if (value != null) {
            // set the value on the field object from the input map
            field.setAccessible(true);
            try {
               field.set(input, value);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      }

      return input;
   }

   @SuppressWarnings("unchecked")
   public LibraryInput build(String userId, MultiValueMap<String, Object> inputs) {
      LibraryInput input = new LibraryInput();

      // set custom metadata using reflection
      Field[] fields = LibraryInput.class.getDeclaredFields();
      for (Field field : fields) {
         String name = field.getName();
         Object value = inputs.get(name);
         if (value != null) {
            if (!Collection.class.isAssignableFrom(field.getType())) {
               // if the field is *not* a list, we can safely always get the 1st and only value in the list
               // returned by the MultiValueMap
               value = ((List<Object>) value).get(0);
            }
            // set the value on the field object from the input map
            field.setAccessible(true);
            try {
               field.set(input, value);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      }

      return input;
   }
}
