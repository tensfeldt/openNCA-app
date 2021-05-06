package com.pfizer.equip.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ParametersAdapter extends XmlAdapter<ParametersType, Map<String, TypedValue>> {
	
	public ParametersAdapter() {}
	
    public ParametersType marshal(Map<String, TypedValue> arg0) throws Exception {
    	ParametersType returnValue = new ParametersType(arg0.size());
    	int i = 0;
        for(Map.Entry<String, TypedValue> entry : arg0.entrySet()) {
        	MapEntryType mapEntryType = new MapEntryType(entry.getKey(), entry.getValue().getType(), entry.getValue().getValue());
            returnValue.put(i, mapEntryType);
        	i += 1;
         }
         return returnValue;
    }

    public Map<String, TypedValue> unmarshal(ParametersType arg0) throws Exception {
        Map<String, TypedValue> returnValue = new HashMap<>();
        for(MapEntryType myEntryType : arg0.entries) {
            returnValue.put(myEntryType.key, new TypedValue(myEntryType.type, myEntryType.value));
         }
         return returnValue;
    }

}
