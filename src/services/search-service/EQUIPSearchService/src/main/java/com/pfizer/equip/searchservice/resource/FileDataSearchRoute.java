package com.pfizer.equip.searchservice.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.dto.Predicate;
import com.pfizer.elasticsearch.dto.PredicateAdapter;
import com.pfizer.elasticsearch.dto.PropertyValuePair;
import com.pfizer.elasticsearch.dto.PropertyValuePairAdapter;
import com.pfizer.elasticsearch.dto.Query;
import com.pfizer.equip.searchservice.Application;
import com.pfizer.equip.searchservice.dto.FileDataSearchRequest;
import com.pfizer.equip.searchservice.dto.FileDataSearchRequestAdapter;
import com.pfizer.equip.searchservice.dto.SearchResponse;
import com.pfizer.equip.searchservice.exception.SearchException;
import com.pfizer.equip.searchservice.search.Search;
import com.pfizer.equip.searchservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Implements SparkJava Route for file data searches
 * 
 * @author HeinemanWP
 *
 */
public class FileDataSearchRoute implements Route {
	private AuthorizationServiceClient authorizationServiceClient;

	@Override
	public Object handle(Request request, Response response) throws Exception {
		if (authorizationServiceClient == null) {
			authorizationServiceClient = new AuthorizationServiceClient(
					ResourceCommon.AUTH_SERVER,
					Integer.parseInt(ResourceCommon.AUTH_PORT),
					ResourceCommon.AUTH_SYSTEM);
		}
		String userId = request.headers(ResourceCommon.IAMPFIZERUSERCN);
		if (userId == null) {
			Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
		}
		String idsOnlyString = request.queryParams("idsOnly");
		boolean idsOnly = false;
		if ((idsOnlyString != null) && !idsOnlyString.isEmpty()) {
			idsOnly = Boolean.parseBoolean(idsOnlyString);
		}
		FileDataSearchRequest fdsRequest = unmarshallSearchFileDataRequest(request);
		SearchResponse searchResponse = initiateFileDataSearch(userId, fdsRequest, idsOnly);
		response.header("Content-Type", "application/json");
		response.body((String) SearchResource.marshallSearchResponse(searchResponse));
		return response;
	}

	private FileDataSearchRequest unmarshallSearchFileDataRequest(Request request) {
		Gson gson = getGson();
		return gson.fromJson(request.body(), FileDataSearchRequest.class);
	}

	private SearchResponse initiateFileDataSearch(String userId, FileDataSearchRequest fdsRequest, boolean idsOnly) throws SearchException {
		String[] sourcesIncluded = {"jcr:uuid", "jcr:primaryType"};
		String[] sourcesExcluded = {};
		if (!idsOnly) {
			sourcesIncluded = ResourceCommon.FILEDATA_SEARCH_SOURCES_INCLUDE;
			sourcesExcluded = ResourceCommon.FILEDATA_SEARCH_SOURCES_EXCLUDE;
		}
		Predicate predicate = fdsRequest.toElasticSearch();
		Query query = new Query(sourcesIncluded, sourcesExcluded, predicate);
		Search search = new Search(authorizationServiceClient);
		return search.initiateSearch(
				userId,
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				ResourceCommon.FILETEXT_SEARCH_INDEX,
				ResourceCommon.SEARCH_TYPE,
				query);
	}

	private Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(FileDataSearchRequest.class, new FileDataSearchRequestAdapter());
		gsonBuilder.registerTypeAdapter(PropertyValuePair.class, new PropertyValuePairAdapter());
		gsonBuilder.registerTypeAdapter(Predicate.class, new PredicateAdapter());
		return gsonBuilder.create();
	}
}
