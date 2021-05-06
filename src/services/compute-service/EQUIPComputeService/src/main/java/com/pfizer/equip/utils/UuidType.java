package com.pfizer.equip.utils;

import java.util.regex.Pattern;

public class UuidType {
	private static String uuidRegexString = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";
	private static Pattern uuidRegex = Pattern.compile(uuidRegexString);
	
	private UuidType() {}
	
	public static boolean isUUID(String id) {
		return uuidRegex.matcher(id).matches();
	}
}
