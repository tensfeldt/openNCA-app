package com.pfizer.equip.searchservice.dto;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for UnifiedSearchRequest JSON
 * 
 * @author HeinemanWP
 *
 */
public class UnifiedSearchRequestAdapter extends TypeAdapter<UnifiedSearchRequest> {

	@Override
	public UnifiedSearchRequest read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		UnifiedSearchRequest returnValue = new UnifiedSearchRequest();
		in.beginObject();
		String name = in.peek().name();
		while ((name != null) && name.equals("NAME")) {
			name = in.nextName();
			switch(name) {
			case "metadataConditions":
			case "metaDataConditions":
				readMetaDataConditions(in, returnValue);
				break;
			case "filedataConditions":
			case "fileDataConditions":
				readFileDataConditions(in, returnValue);
				break;
			case "filetextConditions":
			case "fileTextConditions":
				readFileTextConditions(in, returnValue);
				break;
			default:
				break;
			}
			name = in.peek().name();
		}
		in.endObject();
		return returnValue;
	}

	private void readMetaDataConditions(JsonReader in, UnifiedSearchRequest returnValue) throws IOException {
		MetaDataSearchRequestAdapter mdsra = new MetaDataSearchRequestAdapter();
		MetaDataSearchRequest mdsr = mdsra.read(in);
		returnValue.setMetadataConditions(mdsr);
	}

	private void readFileDataConditions(JsonReader in, UnifiedSearchRequest returnValue) throws IOException {
		FileDataSearchRequestAdapter fdsra = new FileDataSearchRequestAdapter();
		FileDataSearchRequest fdsr = fdsra.read(in);
		returnValue.setFileDataConditions(fdsr);
	}

	private void readFileTextConditions(JsonReader in, UnifiedSearchRequest returnValue) {
		Gson gson = new Gson();
		List<String> texts =  gson.fromJson(in, List.class);
		FileTextSearchRequest ftsr = new FileTextSearchRequest();
		ftsr.setTexts(texts);
		returnValue.setFileTextConditions(ftsr);
	}

	@Override
	public void write(JsonWriter out, UnifiedSearchRequest value) throws IOException {
		if (value == null) {
			out.nullValue();
		}
	}

}
