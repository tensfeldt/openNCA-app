package com.pfizer.equip.computeservice.dto;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;

public class LibraryReferenceAdapter extends TypeAdapter<LibraryReference> {
	private static Logger log = LoggerFactory.getLogger(LibraryReferenceAdapter.class);	

	@Override
	public LibraryReference read(JsonReader jReader) throws IOException {
		LibraryReference libraryReference = new LibraryReference();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "libraryRef":
				libraryReference.setLibraryRef(jReader.nextString());
				break;
			case "id":
				libraryReference.setId(jReader.nextString());
				break;
			case "entityType":
				libraryReference.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return libraryReference;
	}

	@Override
	public void write(JsonWriter arg0, LibraryReference jWriter) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
