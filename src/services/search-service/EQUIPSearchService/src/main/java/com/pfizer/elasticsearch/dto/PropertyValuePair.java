package com.pfizer.elasticsearch.dto;

/**
 * Property value pair for search queries
 * 
 * @author HeinemanWP
 *
 */
public class PropertyValuePair {
	private String property;
	private Object value;
	
	public PropertyValuePair() {}

	public PropertyValuePair(String property, Object value) {
		this.property = property;
		this.value = value;
	}
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
