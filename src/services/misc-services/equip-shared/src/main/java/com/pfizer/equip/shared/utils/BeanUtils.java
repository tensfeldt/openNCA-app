package com.pfizer.equip.shared.utils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public abstract class BeanUtils extends org.springframework.beans.BeanUtils {

   public static void copyNonNullProperties(Object source, Object target, String[] copyEvenIfNullProperties, String... ignoreProperties) {
      String[] nullProperties = getNullPropertyNames(source);
      List<String> doNotCopyPropertiesList = new ArrayList<String>();
      doNotCopyPropertiesList.addAll(Arrays.asList(nullProperties)); // Do not copy null properties...
      if (copyEvenIfNullProperties != null) {
         doNotCopyPropertiesList.removeAll(Arrays.asList(copyEvenIfNullProperties)); // ...Unless we said copyEvenIfNull
      }
      if (ignoreProperties != null) {
         doNotCopyPropertiesList.addAll(Arrays.asList(ignoreProperties)); // But don't copy anything the caller wants to explicitly ignore.
      }
      String[] doNotCopyProperties = new String[doNotCopyPropertiesList.size()];
      doNotCopyProperties = doNotCopyPropertiesList.toArray(doNotCopyProperties);
      copyProperties(source, target, doNotCopyProperties);
   }

   // https://stackoverflow.com/a/19739041
   public static String[] getNullPropertyNames(Object source) {
      final BeanWrapper sourceBean = new BeanWrapperImpl(source);
      PropertyDescriptor[] propertyDescriptors = sourceBean.getPropertyDescriptors();
   
      Set<String> emptyNames = new HashSet<String>();
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
         Object sourceBeanValue = sourceBean.getPropertyValue(propertyDescriptor.getName());
         if (sourceBeanValue == null)
            emptyNames.add(propertyDescriptor.getName());
      }
      String[] result = new String[emptyNames.size()];
      return emptyNames.toArray(result);
   }

   public static String[] getNonNullPropertyNames(Object source, String... ignoreProperties) {
      final BeanWrapper sourceBean = new BeanWrapperImpl(source);
      PropertyDescriptor[] propertyDescriptors = sourceBean.getPropertyDescriptors();
      
      List<String> ignorePropertiesList = Arrays.asList(ignoreProperties);
   
      Set<String> emptyNames = new HashSet<String>();
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
         if (!ignorePropertiesList.contains(propertyDescriptor.getName())) {
            Object sourceBeanValue = sourceBean.getPropertyValue(propertyDescriptor.getName());
            if (sourceBeanValue != null)
               emptyNames.add(propertyDescriptor.getName());
         }
      }
      String[] result = new String[emptyNames.size()];
      return emptyNames.toArray(result);
   }

   public static String[] getPropertyNames(Object source) {
      final BeanWrapper sourceBean = new BeanWrapperImpl(source);
      PropertyDescriptor[] propertyDescriptors = sourceBean.getPropertyDescriptors();
      
      Set<String> propertyNames = new HashSet<String>();
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
         propertyNames.add(propertyDescriptor.getName());
      }
      String[] result = new String[propertyNames.size()];
      return propertyNames.toArray(result);
   }

   // These don't quite belong here, will probably move:
   public static String[] getNullMapKeys(Map<String, Object> map) {
      Set<String> nullKeysList = new HashSet<String>();
      for (String key : map.keySet()){
         if (map.get(key) == null) {
            nullKeysList.add(key);
         }
      }
      String[] result = new String[nullKeysList.size()];
      return nullKeysList.toArray(result);
   }

   public static String[] getNullMapKeys(Map<String, Object> map, String search, String replace) {
      Set<String> nullKeysList = new HashSet<String>();
      for (String key : map.keySet()){
         if (map.get(key) == null) {
            nullKeysList.add(key.replace(search, replace));
         }
      }
      String[] result = new String[nullKeysList.size()];
      return nullKeysList.toArray(result);
   }

   public static String[] getMapKeys(Map<String, Object> map) {
      Set<String> keysList = new HashSet<String>();
      for (String key : map.keySet()){
         keysList.add(key);
      }
      String[] result = new String[keysList.size()];
      return keysList.toArray(result);
   }

   public static void replaceMapKeys(Map<String, Object> map, String search, String replace) {
      Set<String> keys = new HashSet<String>(map.keySet()); // need a copy to iterate while modifying
      for (String key : keys){
         String newKey = key.replace(search, replace);
         // Only replace if they differ:
         if (!key.equals(newKey)) {
            map.put(newKey, map.get(key));
            map.remove(key);
         }
      }
   }
}
