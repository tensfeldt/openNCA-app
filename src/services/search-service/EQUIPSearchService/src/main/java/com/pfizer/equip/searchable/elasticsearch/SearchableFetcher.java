package com.pfizer.equip.searchable.elasticsearch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.InstantSerializer;
import com.pfizer.equip.searchable.dto.SearchableData;

/**
 * Retrieves data for equip:searchable nodes from elasticsearch.
 * 
 * @author HeinemanWP
 *
 */
public class SearchableFetcher {
	private static final String ES_INDEX_PREFIX = "162d9619a77200";
	private static String INDEX = "esvalndx-nca";
	private static String CONTENT_INDEX = "contentndx-nca";
	private static String METADATA_INDEX = "ekvp1ndx-nca";
	private static String TYPE = "nca";
	private static String ID_QUERY = "{ \"_source\":[\"_id\"], \"query\" : {\"match_all\": {}}, \"from\" : %d, \"size\" : 1 }";
	// private static String ID_REGEX = "(\\\"_id\\\":\\\")([a-f 0-9 -]+)";
	private static String ID_REGEX = "([a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";	
	private static Pattern ID_PATTERN = Pattern.compile(ID_REGEX, Pattern.CASE_INSENSITIVE);
	private static String COUNT_REGEX = "^(.*\"hits\":.*\\{\"total\":)([0-9]*)(.*)";
	private static Pattern COUNT_PATTERN = Pattern.compile(COUNT_REGEX, Pattern.CASE_INSENSITIVE);
	private static String EQUIPID_REGEX = "(\\\"equip:equipId\\\":\\\")(.+)(\\\".+)";
	private static Pattern EQUIPID_PATTERN = Pattern.compile(EQUIPID_REGEX, Pattern.CASE_INSENSITIVE);
	private static String JCRPATH_REGEX = "(\\{\\\"jcr:path\\\":\\\")(.+)(\\\"\\}\\})";
	private static Pattern JCRPATH_PATTERN = Pattern.compile(JCRPATH_REGEX, Pattern.CASE_INSENSITIVE);
	private static String SOURCE_REGEX = "(\\\"_source\\\":)(\\{.+\\})(.+)";
	private static Pattern SOURCE_PATTERN = Pattern.compile(SOURCE_REGEX, Pattern.CASE_INSENSITIVE);
	
	private static String UPDATE_QUERY = "{ \"_source\":[\"_id\"], " + 
			"  \"query\" :  { \"bool\" : { \"must\" : [" + 
			"                                    { \"bool\" : { \"should\" : [{\"query_string\":{\"default_field\" : \"jcr:created\", \"query\" : \">%d\"}}," + 
			"						                                     {\"query_string\":{\"default_field\" : \"jcr:lastModified\", \"query\" : \">%d\"}}]}}]}}," + 
			"  \"sort\" : [{\"jcr:lastModified\" : {\"order\" : \"asc\"}}, {\"jcr:created\" : {\"order\" : \"asc\"}}],	" + 
			"  \"from\" : %d, \"size\" : %d }";
	private static String STUDY_QUERY = "{ \"_source\": [\"_id\"], "
			+ "\"query\" : { \"match_phrase\": {\"equip:studyId\": \"%s\"} }, "
			+ "\"from\" : 0, \"size\" : 5000 }";
	
	private static String METADATA_KEY_VALUE_QUERY = "{" + 
			" \"_source\" : \"equip:value\"," + 
			" \"query\": { \"bool\": { \"must\" : [" + 
			" 	{\"match_phrase\" : {\"jcr:path\" : \"%s/*\"}}," + 
			" 	{\"match_phrase\" : {\"equip:key\": \"%s\"}}" + 
			" ]}}, \"from\" : 0, \"size\" : 10000}";

	private static String EQUIPID_QUERY = "%s?_source=equip:equipId";
	private static String SEACHABLE_QUERY = "%s?_source=_id,jcr:*,equip:*";
	
	private ElasticSearchClient esc;
	private Gson gson;
	private Gson decodeGson;

	public SearchableFetcher(ElasticSearchClient esc) {
		this.esc = esc;
		gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
		decodeGson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
	}
	
	public int getCount() throws ElasticSearchClientException {
		int returnValue = 0;
		String result = esc.searchIndex(INDEX, TYPE, String.format(ID_QUERY, 0));
		Matcher m = COUNT_PATTERN.matcher(result);
		if (m.matches()) {
			returnValue = Integer.parseInt(m.group(2));
		}
		return returnValue;
	}
	
	public String getId(int offset) throws ElasticSearchClientException {
		return getId(esc.searchIndex(INDEX, TYPE, String.format(ID_QUERY, offset)));
	}
	
	public List<String> getIdsUpdatedSince(Instant lastUpdate, int count) throws ElasticSearchClientException {
		long lastUpdateMillis = lastUpdate.toEpochMilli();
		esc.refreshIndex(INDEX);
		return getIds(esc.searchIndex(INDEX, TYPE, String.format(UPDATE_QUERY, lastUpdateMillis, lastUpdateMillis, 0, count)));
	}
	
	public List<String> getIdsInStudy(String study) throws ElasticSearchClientException {
		esc.refreshIndex(INDEX);
		return getIds(esc.searchIndex(INDEX, TYPE, String.format(STUDY_QUERY, study)));
	}

	public String getPathForId(String id) throws ElasticSearchClientException {
		if (id.length() < 50) {
			id = ES_INDEX_PREFIX + id;
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", "jcr:path");
		String json = esc.getIndexValue(INDEX, TYPE, id, parameters);
		String returnValue = null;
		Matcher m = JCRPATH_PATTERN.matcher(json);
		if (m.find()) {
			returnValue = m.group(2);
		}
		return returnValue;
	}
	
	public String getEquipIdForId(String id) throws ElasticSearchClientException {
		if (id.length() < 50) {
			id = ES_INDEX_PREFIX + id;
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", "equip:equipId");
		String json = esc.getIndexValue(INDEX, TYPE, id, parameters);
		String returnValue = null;
		Matcher m = EQUIPID_PATTERN.matcher(json);
		if (m.find()) {
			returnValue = m.group(2);
		}
		return returnValue;
	}

	public SearchableData getSearchableDataForId(String id) throws ElasticSearchClientException {
		if (id.length() < 50) {
			id = ES_INDEX_PREFIX + id;
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", "_id,jcr:*,equip:*");
		String json = esc.getIndexValue(INDEX, TYPE, id, parameters);
		Matcher m = SOURCE_PATTERN.matcher(json);
		if (m.find()) {
			String searchableJson = m.group(2);
			return decodeGson.fromJson(searchableJson, SearchableData.class);
		}
		return null;
	}
	
	public List<String> getMetadataValuesForParentPathAndKey(String parentPath, String key) throws ElasticSearchClientException {
		List<String> returnValue = new ArrayList<>();
		String json = esc.searchIndex(METADATA_INDEX, TYPE, String.format(METADATA_KEY_VALUE_QUERY, parentPath, key));
		JsonParser jp = new JsonParser();
		JsonObject jo = jp.parse(json).getAsJsonObject();
		JsonArray ja = jo.getAsJsonObject("hits").getAsJsonArray("hits");
		for (int i = 0, n = ja.size(); i < n; i++) {
			JsonObject hitSource = ja.get(i).getAsJsonObject().getAsJsonObject("_source");
			JsonArray valuesArray = hitSource.getAsJsonArray("equip:value");
			for (int j = 0, m = valuesArray.size(); j < m; j++) {
				returnValue.add(valuesArray.get(j).getAsString());
			}
		}
		return returnValue;
	}
	
	private List<String> getIds(String nodeData) {
		List<String> returnValue = new ArrayList<>();
		Matcher m = ID_PATTERN.matcher(nodeData);
		while (m.find()) {
			String id = m.group();
			if (id.startsWith("\"")) {
				id = id.substring(1);
			}
			returnValue.add(id);
		}
		return returnValue;
	}
	
	private String getId(String nodeData) {
		Matcher m = ID_PATTERN.matcher(nodeData);
		String returnValue = null;
		if (m.find()) {
			returnValue = m.group();
			if (returnValue.startsWith("\"")) {
				returnValue = returnValue.substring(1);
			}
		}
		return returnValue;
	}

}
