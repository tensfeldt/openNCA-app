package com.pfizer.equip.computeservice.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Row;

public class RowAdapter extends TypeAdapter<Row> {

	@Override
	public Row read(JsonReader jReader) throws IOException { 
		Row row = new Row();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "data":
				row.setData(jReader.nextString());
				break;
			case "id":
				row.setId(jReader.nextString());
				break;
			case "entityType":
				row.setEntityType(jReader.nextString());
				break;
			default:
				break;
			}
		}
		jReader.endObject();
		return row;
	}

	@Override
	public void write(JsonWriter jWriter, Row row) throws IOException {
		// TODO Auto-generated method stub

	}

}
