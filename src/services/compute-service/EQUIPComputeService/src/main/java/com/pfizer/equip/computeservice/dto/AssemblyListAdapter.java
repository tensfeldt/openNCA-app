package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;

public class AssemblyListAdapter extends TypeAdapter<List<Assembly>> {

	@Override
	public List<Assembly> read(JsonReader jReader) throws IOException {
		AssemblyAdapter assemblyAdapter = new AssemblyAdapter();
		List<Assembly> returnValue = new ArrayList<>();
		jReader.beginArray();
		while (jReader.hasNext()) {
			Assembly a = assemblyAdapter.read(jReader);
			returnValue.add(a);
		}
		jReader.endArray();
		return returnValue;
	}

	@Override
	public void write(JsonWriter jWriter, List<Assembly> assemblies) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
