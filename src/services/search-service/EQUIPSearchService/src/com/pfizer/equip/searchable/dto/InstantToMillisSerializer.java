package com.pfizer.equip.searchable.dto;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson serializer for java.time.Instant instances. 
 * Serializes to longs of epoch milliseconds.
 * 
 * @author HeinemanWP
 *
 */
public class InstantToMillisSerializer implements JsonSerializer<Instant> {

	@Override
	public JsonElement serialize(Instant value, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(value.toEpochMilli());
	}

}
