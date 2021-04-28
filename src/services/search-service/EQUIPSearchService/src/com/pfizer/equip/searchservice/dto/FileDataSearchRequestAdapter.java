package com.pfizer.equip.searchservice.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for FileDataSearchRequest JSON
 * 
 * @author HeinemanWP
 *
 */
public class FileDataSearchRequestAdapter extends TypeAdapter<FileDataSearchRequest> {

	@Override
	public FileDataSearchRequest read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		FileDataSearchRequest returnValue = new FileDataSearchRequest();
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

	private void extractConditions(JsonReader in, FileDataSearchRequest returnValue) throws IOException {
		FileDataSearchCondition condition = null;
		in.beginArray();
		while(in.hasNext()) {
			in.beginObject();
			while (in.hasNext()) {
				String cname = in.nextName();
				switch (cname) {
				case "condition":
					condition = new FileDataSearchCondition();
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
						FileDataSearchRequest searchRequest = new FileDataSearchRequest();
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
	public void write(JsonWriter out, FileDataSearchRequest value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
	}

}
