package com.pfizer.equip.lineage.elasticsearch;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchable.dto.InstantSerializer;

public class LineageIndexer {
	public static String INDEX = "lineagendx-nca";
	public static String TYPE = "nca";
	private static String DELETE_QUERY = "{\r\n" + 
			"  \"query\": {" + 
			"  	\"bool\" : {" + 
			"  		\"must\" : {" + 
			"  			\"match_phrase\": {" + 
			"  				\"studyIds\": \"%s\"" + 
			"  			}" + 
			"  		}" + 
			"  	}" + 
			"  }" + 
			"}";
	private ElasticSearchClient esc;
	private Gson gson;

	public LineageIndexer(ElasticSearchClient esc) {
		this.esc = esc;
		gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
	}

	public void insertNode(String uuid, Object obj) throws ElasticSearchClientException {
		String json = gson.toJson(obj);
		esc.putIndex(INDEX, TYPE, uuid, json);
	}

	public void refresh() throws ElasticSearchClientException {
		esc.refreshIndex(INDEX);
	}
	
	public void deleteItemsInLineage(String study) throws ElasticSearchClientException {
		esc.deleteByQuery(INDEX, String.format(DELETE_QUERY, study));
	}

}
