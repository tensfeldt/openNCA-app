package com.pfizer.equip.computeservice.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a key-value pair.
 * @author QUINTJ16
 *
 */
@XmlRootElement
public class Metadatum {
	@XmlElement(name="Key")
	private String key;
	
	@XmlElement(name="Value")
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
