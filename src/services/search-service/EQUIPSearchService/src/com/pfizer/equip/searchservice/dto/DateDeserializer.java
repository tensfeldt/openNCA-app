package com.pfizer.equip.searchservice.dto;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Gson deserializer for Date instances.
 * 
 * @author HeinemanWP
 *
 */
public class DateDeserializer implements JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonElement source, Type typeOfSource, JsonDeserializationContext context) throws JsonParseException {
		Date returnValue = new Date();
		long value = source.getAsJsonPrimitive().getAsLong();
		returnValue.setTime(value);
		return returnValue;
	}

}
