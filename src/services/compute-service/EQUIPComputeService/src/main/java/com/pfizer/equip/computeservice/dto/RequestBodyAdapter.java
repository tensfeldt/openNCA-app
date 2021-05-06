package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pfizer.equip.utils.TypedValue;

public class RequestBodyAdapter extends TypeAdapter<RequestBody> {
	private static Logger log = LoggerFactory.getLogger(RequestBodyAdapter.class);	

	@Override
	public RequestBody read(JsonReader jReader) throws IOException {
		RequestBody rb = new RequestBody();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "user":
				rb.setUser(jReader.nextString());
				break;
			case "computeContainer":
				rb.setComputeContainer(jReader.nextString());
				break;
			case "environment":
				rb.setEnvironment(jReader.nextString());
				break;
			case "scriptId":
				rb.setScriptId(jReader.nextString());
				break;
			case "dataframeType":
				{
					List<String> ids = new ArrayList<>();
					JsonToken token = jReader.peek();
					if (token == JsonToken.STRING) {
						ids.add(jReader.nextString());
					} else {
						ids = extractListOfStrings(jReader);
					}
					rb.setDataframeType(ids);
				}
				break;
			case "subType":
				rb.setSubType(jReader.nextString());
				break;
			case "parameters":
				Map<String, TypedValue> parameters = extractParameters(jReader);
				rb.setParameters(parameters);
				break;
			case "dataframes":
				{
					List<String> ids = extractListOfStrings(jReader);
					rb.setDataframes(ids);
				}
				break;
			case "assemblies":
				{
					List<String> ids = extractListOfStrings(jReader);
					rb.setAssemblies(ids);
				}
				break;
			case "equipId":
			{
				List<String> ids = new ArrayList<>();
				JsonToken token = jReader.peek();
				if (token == JsonToken.STRING) {
					ids.add(jReader.nextString());
				} else {
					ids = extractListOfStrings(jReader);
				}
				rb.setEquipId(ids);
			}
			break;
			case "batch":
			{
				rb.setBatch(jReader.nextBoolean());
			}
			break;
			case "dontBatch":
			{
				rb.setDontBatch(jReader.nextBoolean());
			}
			break;
			case "updateScript":
			{
				// ignoring for now. This shouldn't be in the request body
				jReader.nextBoolean();
			}
			break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			
			}
			
		}
		jReader.endObject();
		return rb;
	}

	@Override
	public void write(JsonWriter jWriter, RequestBody rb) throws IOException {
		jWriter.beginObject();
		jWriter.name("user");
		jWriter.value(rb.getUser());
		jWriter.name("computeContainer");
		jWriter.value(rb.getComputeContainer());
		jWriter.name("environment");
		jWriter.value(rb.getEnvironment());
		jWriter.name("scriptId");
		jWriter.value(rb.getScriptId());
		jWriter.name("dataframeType");
		jWriter.beginArray();
		for (String id : rb.getDataframeType()) {
			jWriter.value(id);
		}
		jWriter.endArray();
		jWriter.name("subType");
		jWriter.value(rb.getSubType());
		jWriter.name("parameters");
		jWriter.beginArray();
		for (Map.Entry<String, TypedValue> entry : rb.getParameters().entrySet()) {
			jWriter.beginObject();
			jWriter.name("key");
			jWriter.value(entry.getKey());
			jWriter.name("type");
			jWriter.value(entry.getValue().getType());
			jWriter.name("value");
			jWriter.value(entry.getValue().getValue());
			jWriter.endObject();
		}
		jWriter.endArray();
		jWriter.name("dataframes");
		jWriter.beginArray();
		for (String id : rb.getDataframes()) {
			jWriter.value(id);
		}
		jWriter.endArray();
		jWriter.name("assemblies");
		jWriter.beginArray();
		for (String id : rb.getAssemblies()) {
			jWriter.value(id);
		}
		jWriter.endArray();
		jWriter.name("equipId");
		jWriter.beginArray();
		for (String id : rb.getEquipId()) {
			jWriter.value(id);
		}
		jWriter.endArray();
		jWriter.endObject();
	}

	private Map<String, TypedValue> extractParameters(JsonReader jReader) throws IOException {
		Map<String, TypedValue> parameters = new HashMap<>();
		jReader.beginArray();
		while (jReader.hasNext()) {
			TypedValue tv = new TypedValue();
			String key = null;
			jReader.beginObject();
			while (jReader.hasNext()) {
				String tvName = jReader.nextName();
				switch(tvName) {
				case "key":
					key = jReader.nextString();
					break;
				case "value":
					{
						JsonToken jt = jReader.peek();
						if (jt.equals(JsonToken.BEGIN_ARRAY)) {
							jReader.beginArray();
							tv.setValue(jReader.nextString());
							jReader.endArray();
						} else {
							tv.setValue(jReader.nextString());
						}
					}
					break;
				case "type":
					tv.setType(jReader.nextString());
					break;
				default:
					break;
				}
			}					
			jReader.endObject();
			parameters.put(key, tv);
		}
		jReader.endArray();
		return parameters;
	}

	private List<String> extractListOfStrings(JsonReader jReader) throws IOException {
		List<String> ids = new ArrayList<>();
		jReader.beginArray();
		int count = 0;
		while (jReader.hasNext() && count < 256) {
			if (jReader.peek().equals(JsonToken.NULL)) {
				ids.add(null);
				jReader.nextNull();
			} else {
				ids.add(jReader.nextString());
			}
			count += 1;
		}
		jReader.endArray();
		return ids;
	}

}
