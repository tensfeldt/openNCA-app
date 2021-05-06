package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;

public class PropertiesPayload {
	protected static Gson propGson;
	public Map<String, Object> properties = new HashMap<>(); // this needs to be exposed so that the update function can
																// check each property to see if it is allowed

	public void addProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public String getString(String name) {
		String v = null;
		Object o = this.getProperty(name);
		if (o != null) {
			v = (String) o;
		}

		return v;
	}

	public String[] getStringArray(String name) {
		String[] a = null;
		Object o = this.getProperty(name);
		if (o != null) {
			a = (String[]) o;
		}

		return a;
	}

	public Long getLong(String name) {
		Long l = null;
		Object o = this.getProperty(name);
		if (o != null) {
			l = (Long) o;
		}

		return l;
	}

	public Integer getInteger(String name) {
		Integer i = null;
		Object o = this.getProperty(name);
		if (o != null) {
			i = (Integer) o;
		}

		return i;
	}

	public Date getDate(String name) {
		Date d = null;
		Object o = this.getProperty(name);
		if (o != null) {
			if (o instanceof String) {
				d = DateUtils.parseDate((String) o);
			} else {
				d = (Date) o;
			}
		}

		return d;
	}

	public Boolean getBoolean(String name) {
		Boolean b = null;
		Object o = this.getProperty(name);
		if (o != null) {
			b = (Boolean) o;
		}

		return b;
	}

	public Object getProperty(String name) {
		Object v = null;
		if (this.properties.containsKey(name)) {
			v = this.properties.get(name);
		}

		return v;
	}

	public String marshal() {
		initGson();
		return propGson.toJson(this);
	}

	public static PropertiesPayload unmarshal(String json) throws Exception {
		PropertiesPayload pp = null;
		if (json != null) {
			initGson();
			pp = propGson.fromJson(json, PropertiesPayload.class);
		}

		return pp;
	}

	private static void initGson() {
		if (propGson == null) {
			GsonBuilder b = new GsonBuilder();
			b.registerTypeHierarchyAdapter(PropertiesPayload.class, new PropertiesAdapter());
			b.registerTypeHierarchyAdapter(Date.class, new DateUtils());
			b.setPrettyPrinting();
			b.serializeNulls();

			propGson = b.create();
		}
	}
}

class PropertiesAdapter implements JsonSerializer<PropertiesPayload>, JsonDeserializer<PropertiesPayload> {
	private static final Gson GSON = new Gson();

	@Override
	public JsonElement serialize(PropertiesPayload src, Type srcType, JsonSerializationContext context) {
		JsonElement tree = null;
		if (src != null) {
			Map<String, Object> map = src.properties;
			src.properties = null;

			tree = GSON.toJsonTree(src);
			Set<String> keys = map.keySet();
			for (String key : keys) {
				Object o = map.get(key);
				JsonElement ele = context.serialize(o);
				tree.getAsJsonObject().add(key, ele);
			}
		}

		return tree;
	}

	@Override
	public PropertiesPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		PropertiesPayload pp = null;
		if (json != null) {
			pp = new PropertiesPayload();

			JsonObject jo = json.getAsJsonObject();
			Set<String> keys = jo.keySet();
			for (String key : keys) {
				JsonElement ele = jo.get(key);
				if (ele != null) {
					if (ele.isJsonArray()) {
						JsonArray a = ele.getAsJsonArray();
						List<Object> list = new ArrayList<>();
						for(int i = 0; i < a.size(); i++) {
							JsonElement ae = a.get(i);
							if(ae != null) {
								Object o = this.cast(ae);
								list.add(o);
							}
						}
						
						pp.addProperty(key, list);
					} else {
						Object o = this.cast(ele);
						pp.addProperty(key, o);
					}
				}
			}

		}

		return pp;
	}
	
	private Object cast(JsonElement ele) {
		Object o = null;
		if(ele != null && !ele.isJsonNull()) {
			if(this.tryCast(ele, Long.class)) {
				o = ele.getAsLong();
			}
			else if(this.tryCast(ele, Double.class)) {
				o = ele.getAsDouble();
			}
			else if(this.tryCast(ele, String.class)) {
				o = ele.getAsString();
				
				try {
					Date d = DateUtils.parseDate((String)o);
					if(d != null) {
						o = d;
					}
				}
				catch(Exception e) { }
			}
		}
		
		return o;
	}
	
	private boolean tryCast(JsonElement ele, Class c) {
		boolean success = false;
		Object o = null;
		if(c == String.class) {
			try {
				o = ele.getAsString();
			}
			catch(Exception e) { }
		}
		else if(c == Long.class) {
			try {
				o = ele.getAsLong();
			}
			catch(Exception e) { }
		}
		else if(c == Boolean.class) {
			try {
				o = ele.getAsBoolean();
			}
			catch(Exception e) { }
		}
		else if(c == Double.class) {
			try {
				o = ele.getAsDouble();
			}
			catch(Exception e) { }
		}
		
		if(o != null) {
			success = true;
		}
		
		return success;
	}
	
}