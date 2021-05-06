package com.pfizer.equip.utils;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PropertyType {
	String key;
	Object value;
	
	public PropertyType() {}

	public PropertyType(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	
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

}
