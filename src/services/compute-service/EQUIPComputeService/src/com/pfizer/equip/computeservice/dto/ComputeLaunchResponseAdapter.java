package com.pfizer.equip.computeservice.dto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ComputeLaunchResponseAdapter extends TypeAdapter<ComputeLaunchResponse> {

	@Override
	public ComputeLaunchResponse read(JsonReader jReader) throws IOException {
		return null;
	}

	@Override
	public void write(JsonWriter jWriter, ComputeLaunchResponse clr) throws IOException {
		jWriter.beginObject();
		jWriter.name("id");
		jWriter.value(clr.getId());
		jWriter.name("url");
		jWriter.value(clr.getUrl());
		jWriter.name("environment");
		jWriter.value(clr.getEnvironment());
		jWriter.name("started");
		jWriter.value(clr.getStarted().toString());
		jWriter.name("status");
		jWriter.value(clr.getStatus());
		jWriter.endObject();
	}

}
