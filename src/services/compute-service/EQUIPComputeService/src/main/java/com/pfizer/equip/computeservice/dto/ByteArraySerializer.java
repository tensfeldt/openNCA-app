package com.pfizer.equip.computeservice.dto;

import java.lang.reflect.Type;
import java.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ByteArraySerializer implements JsonSerializer<byte[]> {

	@Override
	public JsonElement serialize(byte[] value, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(value));
	}

}
