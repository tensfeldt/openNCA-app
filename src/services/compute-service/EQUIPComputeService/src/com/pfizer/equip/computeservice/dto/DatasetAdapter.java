package com.pfizer.equip.computeservice.dto;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class DatasetAdapter extends TypeAdapter<Dataset> {
	private static Logger log = LoggerFactory.getLogger(DatasetAdapter.class);	

	@Override
	public Dataset read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		ColumnAdapter columnAdapter = new ColumnAdapter();
		Dataset dataset = new Dataset();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "clientName":
				dataset.setClientName(jReader.nextString());
				break;
			case "clientVersion":
				dataset.setClientVersion(jReader.nextString());
				break;
			case "stdIn":
				dataset.setStdIn(jReader.nextString());
				break;
			case "stdOut":
				dataset.setStdOut(jReader.nextString());
				break;
			case "stdErr":
				dataset.setStdErr(jReader.nextString());
				break;
			case "parameters":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataset.getParameters().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "data":
				dataset.setData(jReader.nextString());
				break;
			case "dataSize":
				dataset.setDataSize(jReader.nextLong());
				break;
			case "mimeType":
				dataset.setMimeType(jReader.nextString());
				break;
			case "complexDataId":
				dataset.setComplexDataId(jReader.nextString());
				break;
			case "columns":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataset.getColumns().add(columnAdapter.read(jReader));
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					dataset.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "id":
				dataset.setId(jReader.nextString());
				break;
			case "entityType":
				dataset.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return dataset;
	}

	@Override
	public void write(JsonWriter jWriter, Dataset dataset) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
