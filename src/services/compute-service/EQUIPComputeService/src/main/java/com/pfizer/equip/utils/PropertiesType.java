package com.pfizer.equip.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PropertiesType {
	@XmlElement(name = "properties")
	PropertyType properties[] = {};

	public PropertiesType() {} //Required by JAXB

	public PropertiesType(int size) {
		properties = new PropertyType[size];
	}

	public void put(int i, PropertyType property) {
		properties[i] = property;
	}
}
