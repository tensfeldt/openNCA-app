package com.pfizer.equip.searchservice.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.dto.Predicate;
import com.pfizer.elasticsearch.dto.PredicateAdapter;
import com.pfizer.elasticsearch.dto.PropertyValuePair;
import com.pfizer.elasticsearch.dto.PropertyValuePairAdapter;
import com.pfizer.elasticsearch.dto.Query;
import com.pfizer.equip.searchservice.Application;
import com.pfizer.equip.searchservice.dto.CommentsSearchRequest;
import com.pfizer.equip.searchservice.dto.CommentsSearchRequestAdapter;
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
 * Implements SparkJava Route for comments searches
 * 
 * @author HeinemanWP
 *
 */
public class CommentsSearchRoute implements Route {
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
		CommentsSearchRequest csrRequest = unmarshallCommentsRequest(request);
		SearchResponse searchResponse = initiateCommentsSearch(userId, csrRequest, idsOnly);
		response.header("Content-Type", "application/json");
		response.body((String) SearchResource.marshallSearchResponse(searchResponse));
		return response;
	}

	private CommentsSearchRequest unmarshallCommentsRequest(Request request) {
		Gson gson = getGson();
		return gson.fromJson(request.body(), CommentsSearchRequest.class);
	}

	private SearchResponse initiateCommentsSearch(String userId, CommentsSearchRequest mdsRequest, boolean idsOnly) throws SearchException {
		String[] sourcesIncluded = {"jcr:uuid", "jcr:primaryType"};
		String[] sourcesExcluded = {"jcr:uuid", "jcr:primaryType"};
		if (!idsOnly) {
			sourcesIncluded = ResourceCommon.COMMENTS_SEARCH_SOURCES_INCLUDE;
			sourcesExcluded = ResourceCommon.COMMENTS_SEARCH_SOURCES_EXCLUDE;
		}
		Predicate predicate = mdsRequest.toElasticSearch();
		Query query = new Query(sourcesIncluded, sourcesExcluded, predicate);
		Search search = new Search(authorizationServiceClient);
		return search.initiateSearch(
				userId,
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				ResourceCommon.COMMENTS_SEARCH_INDEX,
				ResourceCommon.SEARCH_TYPE,
				query);
	}

	private Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CommentsSearchRequest.class, new CommentsSearchRequestAdapter());
		gsonBuilder.registerTypeAdapter(PropertyValuePair.class, new PropertyValuePairAdapter());
		gsonBuilder.registerTypeAdapter(Predicate.class, new PredicateAdapter());
		return gsonBuilder.create();
	}

}
