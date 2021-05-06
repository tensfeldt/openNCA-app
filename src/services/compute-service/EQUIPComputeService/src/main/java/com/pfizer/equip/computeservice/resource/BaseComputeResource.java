package com.pfizer.equip.computeservice.resource;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.equip.computeservice.dto.ByteArraySerializer;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.RequestBody;
import com.pfizer.equip.computeservice.dto.RequestBodyAdapter;

import spark.Request;
import spark.Spark;

public class BaseComputeResource {
	private static final String APPLICATION_JSON = "application/json";

	protected static RequestBody unmarshallComputeRequest(Request request) throws IOException {
		String contentType = request.headers("Content-Type");
		if (contentType != null) {
			if (contentType.equalsIgnoreCase(APPLICATION_JSON)) {
				String requestJson = request.body();
				RequestBodyAdapter rba = new RequestBodyAdapter();
				return rba.fromJson(requestJson);
			} else {
				Spark.halt(400, "Content-Type must be application/json ");
			}
		} else {
			Spark.halt(400, "A Content-Type must be provided.");
		}
		return null;
	}
		
	protected static String marshallComputeResponse(ComputeResponse cr, String contentType) {
		if (contentType != null) {
			if (contentType.equalsIgnoreCase(APPLICATION_JSON)) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(byte[].class, new ByteArraySerializer()).create();
				return gson.toJson(cr, ComputeResponse.class);
			} else {
				Spark.halt(400, "Content-Type must be application/json ");
			}
		} else {
			Spark.halt(400, "A Content-Type must be provided.");
		}
		return null;
	}
	
}
