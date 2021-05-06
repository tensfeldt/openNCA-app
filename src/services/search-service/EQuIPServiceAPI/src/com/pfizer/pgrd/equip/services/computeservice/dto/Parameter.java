package com.pfizer.pgrd.equip.services.computeservice.dto;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class Parameter {
	private String key;
	private String value;
	private String type;
	
	public Parameter() {
		this(null, null, null);
	}
	public Parameter(String key, String value, String type) {
		this.key = key;
		this.type = type;
		this.value = value;
	}
	
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public static class ParameterAdapter implements JsonDeserializer<Parameter> {
		private static final Gson GENERIC_GSON = new Gson();
		
		@SuppressWarnings("unchecked")
		@Override
		public Parameter deserialize(JsonElement ele, Type eleType, JsonDeserializationContext context) {
			Parameter param = null;
			if(ele != null) {
				param = new Parameter();
				
				JsonObject jo = ele.getAsJsonObject();
				if(jo.has("key")) {
					JsonElement keyEle = jo.get("key");
					if(!(keyEle instanceof JsonNull)) {
						param.setKey(keyEle.getAsString());
					}
				}
				if(jo.has("type")) {
					JsonElement typeEle = jo.get("type");
					if(!(typeEle instanceof JsonNull)) {
						param.setType(typeEle.getAsString());
					}
				}
				if(jo.has("value")) {
					JsonElement valEle = jo.get("value");
					if(!(valEle instanceof JsonNull)) {
						String val = null;
						if(valEle instanceof JsonArray) {
							JsonArray ja = valEle.getAsJsonArray();
							if(ja.size() > 0) {
								val = ja.get(0).getAsString();
							}
						}
						else {
							val = valEle.getAsString();
						}
						
						param.setValue(val);
					}
				}
			}
			
			return param;
		}
	}
}
