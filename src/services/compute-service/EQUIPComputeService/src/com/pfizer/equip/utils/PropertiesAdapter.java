/**
 * 
 */
package com.pfizer.equip.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author HeinemanWP
 *
 */
public class PropertiesAdapter extends XmlAdapter<PropertiesType, Map<String, Object>> {
	
	public PropertiesAdapter() {}

    public PropertiesType marshal(Map<String, Object> value) throws Exception {
    	PropertiesType returnValue = new PropertiesType(value.size());
    	int i = 0;
        for(Map.Entry<String, Object> entry : value.entrySet()) {
        	PropertyType property = new PropertyType(entry.getKey(), entry.getValue());
            returnValue.put(i, property);
        	i += 1;
         }
         return returnValue;
    }

   public Map<String, Object> unmarshal(PropertiesType value) throws Exception {
        Map<String, Object> returnValue = new HashMap<>();
        for(PropertyType property : value.properties) {
            returnValue.put(property.getKey(), property.getValue());
         }
         return returnValue;
    }

}
