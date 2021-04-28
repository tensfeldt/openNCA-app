package com.pfizer.elasticsearch.dto;

import java.io.IOException;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for reading and writing ProperyValuePair 
 * JSON instances of search query.
 * 
 * @author HeinemanWP
 *
 */
public class PropertyValuePairAdapter extends TypeAdapter<PropertyValuePair> {

	@Override
	public PropertyValuePair read(JsonReader in) throws IOException {
		return null;
	}

	@Override
	public void write(JsonWriter out, PropertyValuePair value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		if (value.getProperty() != null) {
			out.beginObject();
			out.name(value.getProperty());
		}
		if (value.getValue() instanceof String) {
			out.value((String) value.getValue());
		} else if (value.getValue() instanceof Number) {
			out.value((Number) value.getValue());
		} else if (value.getValue() instanceof Boolean) {
			out.value((Boolean) value.getValue());
		} else if (value.getValue() instanceof List<?>) {
			out.beginArray();
			for (Object obj : (List<?>) value.getValue()) {
				if (obj instanceof Predicate) {
					PredicateAdapter pa = new PredicateAdapter();
					pa.write(out, (Predicate) obj);
				}
			}
			out.endArray();
		} else if (value.getValue() instanceof Predicate) {
			PredicateAdapter pa = new PredicateAdapter();
			pa.write(out, (Predicate) value.getValue());
		}
		if (value.getProperty() != null) {
			out.endObject();
		}
	}

}
