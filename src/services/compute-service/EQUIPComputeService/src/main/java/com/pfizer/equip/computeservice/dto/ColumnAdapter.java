package com.pfizer.equip.computeservice.dto;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.Row;

public class ColumnAdapter extends TypeAdapter<Column> {
	private static Logger log = LoggerFactory.getLogger(ColumnAdapter.class);	

	@Override
	public Column read(JsonReader jReader) throws IOException {
		RowAdapter rowAdapter = new RowAdapter();
		Column column = new Column();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "rows":
				jReader.beginArray();
				while (jReader.hasNext()) {
					column.getRows().add(rowAdapter.read(jReader));
				}
				jReader.endArray();
				break;
			case "id":
				column.setId(jReader.nextString());
				break;
			case "entityType":
				column.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return column;
	}

	@Override
	public void write(JsonWriter jWriter, Column column) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
