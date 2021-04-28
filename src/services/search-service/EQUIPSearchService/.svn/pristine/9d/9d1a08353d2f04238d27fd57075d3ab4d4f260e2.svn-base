package com.pfizer.equip.lineage.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MetadataAdapter extends TypeAdapter<Metadata> {

	@Override
	public Metadata read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		Metadata returnValue = new Metadata();
		reader.beginObject();
		String name = reader.peek().name();
		while (reader.hasNext()) {
			name = reader.nextName();
			switch(name) {
			case "self":
			case "up":
			case "jcr:primaryType":
				reader.skipValue();
				break;
			case "id":
				returnValue.setId(reader.nextString());
				break;
			case "equip:value":
				reader.beginArray();
				try {
					List<String> values = new ArrayList<>();
					while (reader.hasNext()) {
						values.add(reader.nextString());
					}
					returnValue.setValue(values.toArray(new String[values.size()]));
				} finally {
					reader.endArray();
				}
				break;
			case "equip:key":
				returnValue.setKey(reader.nextString());
				break;
			case "equip:deleteFlag":
				returnValue.setDeleted(Boolean.parseBoolean(reader.nextString()));
				break;
			default:
				reader.skipValue();
				break;
			}
		}
		reader.endObject();
		return returnValue;
	}

	@Override
	public void write(JsonWriter writer, Metadata metadata) throws IOException {
		throw new UnsupportedOperationException();
	}

}
