package com.pfizer.equip.utils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TypedValue {
	private String type;
	private String value;
	
	public TypedValue() {}

	public TypedValue(String type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public TypedValue(String value) {
		type = "string";
		this.value = value;
	}
	
	public TypedValue(Integer value) {
		type = "int";
		this.value = Integer.toString(value);
	}
	
	public TypedValue(Double value) {
		type = "double";
		this.value = Double.toString(value);
	}
	
	public TypedValue(Date value) {
		type = "date";
		OffsetDateTime ldt = OffsetDateTime.ofInstant(value.toInstant(), ZoneId.of("UTC"));
		this.value = DateTimeFormatter.ISO_DATE_TIME.format(ldt);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type == null) {
			type = "string";
		}
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "<" + getType() + "> " + getValue();
	}
	
	public int getIntegerValue() {
		return Integer.parseInt(value);
	}
	
	public double getDoubleValue() {
		return Double.parseDouble(value);
	}
	
	public Date getDateValue() {
		DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
		OffsetDateTime ldt = OffsetDateTime.parse(value, format);
		return Date.from(ldt.atZoneSameInstant(ZoneId.of("UTC")).toInstant());
	}
	
}
