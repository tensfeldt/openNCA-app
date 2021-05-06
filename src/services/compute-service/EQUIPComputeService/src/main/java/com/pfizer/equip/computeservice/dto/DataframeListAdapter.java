package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class DataframeListAdapter extends TypeAdapter<List<Dataframe>> {

	@Override
	public List<Dataframe> read(JsonReader jReader) throws IOException {
		DataframeAdapter dataframeAdapter = new DataframeAdapter();
		List<Dataframe> returnValue = new ArrayList<>();
		jReader.beginArray();
		while (jReader.hasNext()) {
			Dataframe df = dataframeAdapter.read(jReader);
			returnValue.add(df);
		}
		jReader.endArray();
		return returnValue;
	}

	@Override
	public void write(JsonWriter jWriter, List<Dataframe> dataframes) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
