package com.pfizer.equip.utils;

public class MapStringStringEntryType
{
	public String key;
	public String value;

  	@SuppressWarnings("unused")
	private MapStringStringEntryType() {} //Required by JAXB

  	public MapStringStringEntryType(String key, String value)
  	{
  		this.key   = key;
  		this.value = value;
  	}

}