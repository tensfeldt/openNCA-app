package com.pfizer.elasticsearch.dto;

/**
 * Stores the predicate of a search query
 * 
 * @author HeinemanWP
 *
 */
public class Predicate {
	private String name;
	private PropertyValuePair value;
	
	public Predicate() {}
	
	public Predicate(String name) {
		this.name = name;
	}
	
	public Predicate(String name, PropertyValuePair value) {
		this.name = name;
		this.value = value;
	}

	public Predicate(String name, String property, Object value ) {
		this.name = name;
		this.value = new PropertyValuePair(property, value);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PropertyValuePair getValue() {
		return value;
	}

	public void setValue(PropertyValuePair value) {
		this.value = value;
	}
	
}
