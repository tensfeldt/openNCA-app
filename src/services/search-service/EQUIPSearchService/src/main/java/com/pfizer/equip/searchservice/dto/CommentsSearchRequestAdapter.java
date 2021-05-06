package com.pfizer.equip.searchservice.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for CommentsSearchRequest JSON
 * 
 * @author HeinemanWP
 *
 */
public class CommentsSearchRequestAdapter extends TypeAdapter<CommentsSearchRequest> {

	@Override
	public CommentsSearchRequest read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		CommentsSearchRequest returnValue = new CommentsSearchRequest();
		in.beginObject();
		while(in.hasNext()) {
			String name = in.nextName();
			switch (name) {
			case "conditions":
				extractConditions(in, returnValue);
				break;
			case "operator":
				returnValue.setOperator(in.nextString());
				break;
			default:
				break;
			}
		}
		in.endObject();
		return returnValue;
	}

	private void extractConditions(JsonReader in, CommentsSearchRequest returnValue) throws IOException {
		CommentsSearchCondition condition = null;
		in.beginArray();
		while(in.hasNext()) {
			in.beginObject();
			while (in.hasNext()) {
				String cname = in.nextName();
				switch (cname) {
				case "condition":
					condition = new CommentsSearchCondition();
					condition.setCondition(in.nextString());
					break;
				case "property":
					condition.setProperty(in.nextString());
					break;
				case "value":
					condition.setValue(in.nextString());
					returnValue.getConditions().add(condition);
					break;
				case "conditions":
					while (in.hasNext()) {
						CommentsSearchRequest searchRequest = new CommentsSearchRequest();
						extractConditions(in, searchRequest);
						in.nextName();
						searchRequest.setOperator(in.nextString());
						returnValue.getConditions().add(searchRequest);
					}
					break;
				case "operator":
					returnValue.setOperator(in.nextString());					
					break;
				default:
					break;
				}
			}
			in.endObject();
		}
		in.endArray();
	}

	@Override
	public void write(JsonWriter out, CommentsSearchRequest value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
	}

}
