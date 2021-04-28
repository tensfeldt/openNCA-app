package com.pfizer.equip.searchable.dto;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Gson deserializer for java.time.Instant instances
 * 
 * @author HeinemanWP
 *
 */
public class InstantDeserializer implements JsonDeserializer<Instant> {

	@Override
	public Instant deserialize(JsonElement source, Type typeOfSource, JsonDeserializationContext context) throws JsonParseException {
		String value = source.getAsJsonPrimitive().getAsString();
		OffsetDateTime odt;
		try {
			long n = Long.parseLong(value);
			return Instant.ofEpochMilli(n);
		} catch (NumberFormatException ex) {
			odt = OffsetDateTime.parse(value);
		}
		return odt.toInstant();
	}
	
}
