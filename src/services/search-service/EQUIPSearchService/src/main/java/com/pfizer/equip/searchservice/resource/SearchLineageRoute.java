package com.pfizer.equip.searchservice.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchservice.Application;
import com.pfizer.equip.searchservice.indexer.Indexer;
import com.pfizer.equip.searchservice.indexer.IndexerException;
import com.pfizer.equip.searchservice.util.HTTPStatusCodes;
import com.pfizer.equip.service.client.ServiceCallerException;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class SearchLineageRoute implements Route {
	private static Logger log = LoggerFactory.getLogger(SearchLineageRoute.class);
	private static final String[] equipTypes = {"assemblyType", "dataframeType", "subType"};
	private Indexer indexer;

	@Override
	public Object handle(Request request, Response response) throws Exception {
		if (indexer == null) {
			indexer = new Indexer(Application.getAppProperties());
		}
		String studyId = request.params(":studyId");
		String userId = request.headers(ResourceCommon.IAMPFIZERUSERCN);
		Map<String, String[]> queryParams = request.queryMap().toMap();
		if (userId == null) {
			Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
		}
		response.header("Content-Type", "application/json");
		response.body(extractLineageFromSearchResult(
				searchLineages(
						ResourceCommon.ELASTICSEARCH_SERVER,
						ResourceCommon.ELASTICSEARCH_USERNAME,
						ResourceCommon.ELASTICSEARCH_PASSWORD,
						studyId,
						queryParams)));
		return response;
	}

	public String searchLineages(
			String esServer, 
			String esUsername, 
			String esPassword, 
			String studyId,
			Map<String, String[]> queryParams) throws ServiceCallerException, ElasticSearchClientException, IndexerException {
		if (indexer != null) {
			indexer.indexForLineageChanges();
		}
		String subQueries = constructQueries(studyId, queryParams);
		ElasticSearchClient esc = new ElasticSearchClient(esServer, esUsername, esPassword);
		return esc.searchIndex(
				"lineagendx-nca", 
				"nca", 
				String.format("{\"sort\": [\"created\"], \"query\" : %s, \"from\" : 0, \"size\" : 10000}",
						subQueries));
	}
	
	private String constructQueries(String studyId, Map<String, String[]> queryParams) {
		List<Map.Entry<String, String[]>> mustEntries = new ArrayList<>();
		List<Map.Entry<String, String[]>> shouldEntries = new ArrayList<>();
		for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
			String[] values = entry.getValue();
			if (values.length > 1) {
				shouldEntries.add(entry);
			} else {
				mustEntries.add(entry);
			}
		}		
		StringBuilder sb = new StringBuilder();
		sb.append("{\"bool\":{\"must\":[");
		boolean appendComma = false;
		if (!studyId.equalsIgnoreCase("all")) {
			sb.append(String.format("{\"match_phrase\":{\"studyIds\":\"%s\"}}", studyId));
			appendComma = true;
		}
		for (Map.Entry<String, String[]> entry : mustEntries) {
			String k = entry.getKey();
			String[] values = entry.getValue();
			if (appendComma) {
				sb.append(",");
			} else {
				appendComma = true;
			}
			if (k.equals("equipType")) {
				sb.append(constructEquipTypeQuery(values[0]));
			} else {
				sb.append(String.format("{\"match_phrase\":{\"%s\":\"%s\"}}", k, values[0]));
			}
		}
		if (!shouldEntries.isEmpty()) {
			for (Map.Entry<String, String[]> entry :shouldEntries) {
				String k = entry.getKey();
				String[] values = entry.getValue();
				if (appendComma) {
					sb.append(",");
				} else {
					appendComma = true;
				}
				sb.append("{\"bool\":{\"should\":[");
				int i = 0;
				for (String value: values) {
					if (i > 0) {
						sb.append(",");
					}
					if (k.equals("equipType")) {
						sb.append(constructEquipTypeQuery(value));
					} else {
						sb.append(String.format("{\"match_phrase\":{\"%s\":\"%s\"}}", k, value));
					}
					i += 1;
				}
				sb.append("]}}");
			}
		}
		sb.append("]}}");
		return sb.toString();
	}

	/**
	 * Expand the query parameter "equipType" to an or'd expression over all the parameters
	 * defined in equipTypes.
	 * @param searchValue
	 * @return
	 */
	private String constructEquipTypeQuery(String searchValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"bool\":{\"should\":[");
		int i = 0;
		for (String equipType : equipTypes) {
			sb.append(String.format("{\"match_phrase\":{\"%s\":\"%s\"}}", equipType, searchValue));
			if (i < (equipTypes.length - 1)) {
				sb.append(",");
			}
			i += 1;
		}
		sb.append("]}}");
		return sb.toString();
	}
	
	public String extractLineageFromSearchResultOld(String searchResult) {
		if ((searchResult == null) || searchResult.isEmpty()) {
			return new JsonArray().toString();
		}
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(searchResult);
		JsonElement resultsElem = jelem.getAsJsonObject().get("hits");
		JsonArray hits = resultsElem.getAsJsonObject().get("hits").getAsJsonArray();
		JsonArray objects = new JsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			JsonObject obj = hits.get(i).getAsJsonObject().get("_source").getAsJsonObject();
			objects.add(obj);
		}
		return objects.toString();
	}

	public String extractLineageFromSearchResult(String searchResult) {
		if ((searchResult == null) || searchResult.isEmpty()) {
			return "[]";
		}
		// JsonIterator.parse(in, bufSize)
		Any any = JsonIterator.deserialize(searchResult);
		Any results = any.get("hits").get("hits", '*', "_source");
		return results.toString();
	}

}
