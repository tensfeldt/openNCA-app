package com.pfizer.equip.searchable.dto;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * Gson serializer for java.time.Instant instances
 * 
 * @author HeinemanWP
 *
 */
public class InstantSerializer implements JsonSerializer<Instant> {

	@Override
	public JsonElement serialize(Instant value, Type type, JsonSerializationContext context) {
		OffsetDateTime ldt = OffsetDateTime.ofInstant(value, ZoneOffset.systemDefault());
		return new JsonPrimitive(ldt.toString());	
	}

}
