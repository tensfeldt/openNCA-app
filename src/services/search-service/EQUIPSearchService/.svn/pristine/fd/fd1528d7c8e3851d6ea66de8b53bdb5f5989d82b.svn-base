package com.pfizer.elasticsearch.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Query class for search queries. 
 * Search queries consist of sources and a predicate.
 * 
 * @author HeinemanWP
 *
 */
public class Query {
	private String[] sourcesIncluded = {};
	private String[] sourcesExcluded = {};
	private String sort = "";
	private Predicate predicate;
	private long from = 0;
	private long size = 100;
	
	public Query() {}
	
	public Query(String[] sourcesIncluded, String[] sourcesExcluded, Predicate predicate) {
		this();
		this.sourcesIncluded = sourcesIncluded;
		this.sourcesExcluded = sourcesExcluded;
		this.predicate = predicate;
	}

	public Query(String[] sourcesIncluded, String[] sourcesExcluded, Predicate predicate, long from, long size) {
		this(sourcesIncluded, sourcesExcluded, predicate);
		this.from = from;
		this.size = size;
	}

	public String[] getSourcesIncluded() {
		return sourcesIncluded;
	}

	public void setSourcesIncluded(String[] sources) {
		this.sourcesIncluded = sources;
	}

	public String[] getSourcesExcluded() {
		return sourcesExcluded;
	}

	public void setSourcesExcluded(String[] sourcesExcluded) {
		this.sourcesExcluded = sourcesExcluded;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String toJson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Predicate.class, new PredicateAdapter());
		Gson gson = gsonBuilder.create();
		return String.format("{ \"_source\":{\"include\":%s,\"exclude\":%s}, \"query\" : %s, \"sort\" : [%s], \"from\" : %d, \"size\" : %d }",
				gson.toJson(sourcesIncluded),
				gson.toJson(sourcesExcluded),
				gson.toJson(predicate),
				(!sort.isEmpty() ? gson.toJson(sort) : ""),
				from,
				size);
	}

}
