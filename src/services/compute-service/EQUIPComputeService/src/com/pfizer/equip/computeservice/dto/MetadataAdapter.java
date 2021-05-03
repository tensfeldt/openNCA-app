package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class MetadataAdapter extends TypeAdapter<Metadatum> {
	private static Logger log = LoggerFactory.getLogger(MetadataAdapter.class);	

	@Override
	public Metadatum read(JsonReader jReader) throws IOException {
		Metadatum metadata = new Metadatum();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "isDeleted":
				metadata.setDeleted(jReader.nextBoolean());
				break;
			case "key":
				metadata.setKey(jReader.nextString());
				break;
			case "value":
				List<String> values = new ArrayList<>();
				JsonToken token = jReader.peek();
				if (token == JsonToken.STRING) {
					values.add(jReader.nextString());
				} else {
					values = extractListOfStrings(jReader);
				}
				metadata.setValue(values);
				break;
			case "valueType":
				metadata.setValueType(jReader.nextString());
				break;
			case "complexValue":
				List<Byte> cv = new ArrayList<>();
				jReader.beginArray();
				while (jReader.hasNext()) {
					cv.add(Byte.parseByte(jReader.nextString()));
				}
				jReader.endArray();
				byte[] cvb = new byte[cv.size()];
				int n = 0;
				for (Byte b : cv) {
					cvb[n] = b;
					n += 1;
				}
				metadata.setComplexValue(cvb);
				break;
			case "complexValueId":
				metadata.setComplexValueId(jReader.nextString());
				break;
			case "created": {
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					metadata.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				metadata.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate": {
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					metadata.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				metadata.setModifiedBy(jReader.nextString());
				break;
			case "id":
				metadata.setId(jReader.nextString());
				break;
			case "entityType":
				metadata.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return metadata;
	}

	@Override
	public void write(JsonWriter jWriter, Metadatum metadata) throws IOException {
		// TODO Auto-generated method stub
	}

	private List<String> extractListOfStrings(JsonReader jReader) throws IOException {
		List<String> strings = new ArrayList<>();
		jReader.beginArray();
		int count = 0;
		while (jReader.hasNext() && count < 256) {
			if (jReader.peek().equals(JsonToken.NULL)) {
				strings.add(null);
				jReader.nextNull();
			} else {
				strings.add(jReader.nextString());
			}
			count += 1;
		}
		jReader.endArray();
		return strings;
	}
}
