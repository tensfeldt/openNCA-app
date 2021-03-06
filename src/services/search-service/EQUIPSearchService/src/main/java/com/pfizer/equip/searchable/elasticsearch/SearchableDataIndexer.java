package com.pfizer.equip.searchable.elasticsearch;

import java.time.Instant;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchable.dto.InstantSerializer;
import com.pfizer.equip.searchable.dto.SearchableData;

/**
 * Stores and retrieves data from search index.
 * 
 * @author HeinemanWP
 *
 */
public class SearchableDataIndexer {
	private static final String ES_INDEX_PREFIX = "162d9619a77200";
	public static String INDEX = "contentndx-nca";
	public static String TYPE = "nca";
	private ElasticSearchClient esc;
	private Gson gson;


	public SearchableDataIndexer(ElasticSearchClient esc) {
		this.esc = esc;
		gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
	}
	
	public String jsonForIndex(SearchableData indexDatum) {
		String json = gson.toJson(indexDatum);
		if (json.contains(",\"rowValues\":{")) {
			int startNdx = json.indexOf(",\"rowValues\":{");
			int endNdx = json.indexOf("}}", startNdx) + 1;
			String values = json.substring(startNdx + ",\"rowValues\":{".length(), endNdx - 1);
			if (values.length() > 0) {
				values = "," + values;
			}
			json = json.substring(0, startNdx) + values + json.substring(endNdx);
		}
		return json;
	}
	
	public void putIndex(SearchableData indexData) throws ElasticSearchClientException {
		indexData.setLastIndexed(Instant.now());
		String json = jsonForIndex(indexData);
		esc.putIndex(INDEX, TYPE, indexData.getIndexKey(), json);
	}
	
	public void updateContentNode(String id, SearchableData contentData) throws ElasticSearchClientException {
		if ((contentData.getJcrPath() != null) && contentData.getJcrPath().endsWith("/equip:complexData/jcr:content")) {
			contentData.setJcrPath(contentData.getJcrPath().substring(contentData.getJcrPath().indexOf("/equip:complexData/jcr:content")));
		}
		contentData.setLastIndexed(Instant.now());
		contentData.setFileId(id);
		String json = jsonForIndex(contentData);
		if (id.length() <= 36) {
			id = ES_INDEX_PREFIX + id;
		}
		esc.updateIndex(INDEX, TYPE, id, json);
	}

	public String insertContentNode(String id, SearchableData contentData) throws ElasticSearchClientException {
		contentData.setLastIndexed(Instant.now());
		String json = jsonForIndex(contentData);
		if (id.length() <= 36) {
			id = ES_INDEX_PREFIX + id;
		}
		esc.updateIndex(INDEX, TYPE, id, json);
		return id;
	}

	public void deleteIndices(List<SearchableData> indexData) throws ElasticSearchClientException {
		for (SearchableData indexDatum : indexData) {
			esc.deleteIndex(INDEX, TYPE, indexDatum.getIndexKey());
		}
	}

	public void refresh() throws ElasticSearchClientException {
		esc.refreshIndex(INDEX);
	}

}
