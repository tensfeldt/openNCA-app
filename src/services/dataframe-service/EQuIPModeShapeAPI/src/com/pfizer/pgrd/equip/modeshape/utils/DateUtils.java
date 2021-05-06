package com.pfizer.pgrd.equip.modeshape.utils;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A utility class to aid in parsing and serializing dates.
 */
public class DateUtils implements JsonSerializer<Date> {
	/**
	 * Returns a {@link String} representation of the provided {@link Date} in ISO format.
	 * @param d the date
	 * @return {@link String} the ISO representation of the date
	 */
	public static String stringifyDate(Date d) {
		String formatted = null;
		if (d != null) {
			OffsetDateTime ldt = OffsetDateTime.ofInstant(d.toInstant(), ZoneId.of("UTC"));
			formatted = DateTimeFormatter.ISO_DATE_TIME.format(ldt);
		}
		
		return formatted;
	}

	/**
	 * Returns a {@link Date} parsed from the provided ISO date representation.
	 * @param dateString the ISO representation
	 * @return {@link Date} the date
	 */
	public static Date parseDate(String dateString) {
		DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
		OffsetDateTime ldt = OffsetDateTime.parse(dateString, format);
		return Date.from(ldt.atZoneSameInstant(ZoneId.of("UTC")).toInstant());
	}

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		JsonElement ele = null;
		if (src != null) {
			String formatted = DateUtils.stringifyDate(src);
			ele = context.serialize(formatted);
		}

		return ele;
	}
}