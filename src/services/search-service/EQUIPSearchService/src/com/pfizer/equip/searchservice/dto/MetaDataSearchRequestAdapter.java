package com.pfizer.equip.searchservice.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for MetaDataSearchRequest JSON
 * 
 * @author HeinemanWP
 *
 */
public class MetaDataSearchRequestAdapter extends TypeAdapter<MetaDataSearchRequest> {

	@Override
	public MetaDataSearchRequest read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		MetaDataSearchRequest returnValue = new MetaDataSearchRequest();
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

	private void extractConditions(JsonReader in, MetaDataSearchRequest returnValue) throws IOException {
		in.beginArray();
		while(in.hasNext()) {
			MetaDataSearchCondition condition = new MetaDataSearchCondition();
			in.beginObject();
			while (in.hasNext()) {
				String cname = in.nextName();
				switch (cname) {
				case "condition":
					condition.setCondition(in.nextString());
					break;
				case "property":
					condition.setProperty(in.nextString());
					break;
				case "value":
					condition.setValue(in.nextString());
					break;
				case "conditions":
					while (in.hasNext()) {
						MetaDataSearchRequest searchRequest = new MetaDataSearchRequest();
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
			if ((condition != null) && (condition.getCondition() != null)) {
				returnValue.getConditions().add(condition);
				condition = null;
			}
		}
		in.endArray();
	}

	@Override
	public void write(JsonWriter out, MetaDataSearchRequest value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
	}

}
