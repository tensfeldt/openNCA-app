package com.pfizer.elasticsearch.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for reading and writing Query JSON instances of search query.
 * 
 * @author HeinemanWP
 *
 */
public class QueryAdapter extends TypeAdapter<Query> {

	@Override
	public Query read(JsonReader in) throws IOException {
		return null;
	}

	@Override
	public void write(JsonWriter out, Query value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.beginObject();
		out.name("query");
		PredicateAdapter pa = new PredicateAdapter();
		pa.write(out, value.getPredicate());
		out.name("from");
		out.value(value.getFrom());
		out.name("size");
		out.value(value.getSize());
		out.endObject();
	}

}
