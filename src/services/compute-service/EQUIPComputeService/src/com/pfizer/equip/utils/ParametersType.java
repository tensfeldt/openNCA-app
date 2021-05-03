package com.pfizer.equip.utils;

import javax.xml.bind.annotation.XmlElement;

public class ParametersType {
	@XmlElement(name = "parameters")
	public MapEntryType[] entries;

	public ParametersType() {} //Required by JAXB

	public ParametersType(int size) {
		entries = new MapEntryType[size];
	}

	public void put(int i, MapEntryType mapEntryType) {
		entries[i] = mapEntryType;
	}

}
