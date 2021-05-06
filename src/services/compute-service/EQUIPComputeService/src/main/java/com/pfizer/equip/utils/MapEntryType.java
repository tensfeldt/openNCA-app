package com.pfizer.equip.utils;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MapEntryType
{
	public String key;
	public String type;
 	public String value;

  	@SuppressWarnings("unused")
	private MapEntryType() {} //Required by JAXB

  	public MapEntryType(String key, String type, String value)
  	{
  		this.key   = key;
  		this.type = type;
  		this.value = value;
  	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
