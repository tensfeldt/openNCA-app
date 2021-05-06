package com.pfizer.equip.searchservice.dto;

import com.google.gson.annotations.Expose;
import com.pfizer.elasticsearch.dto.Query;

/**
 * Stores the search response data.
 * 
 * @author HeinemanWP
 *
 */
public class SearchResponse {
	private String searchId;
	private String expires;
	private int count;
	@Expose(serialize = false, deserialize = false)
	private transient String index;
	@Expose(serialize = false, deserialize = false)
	private transient Query query;
	
	public SearchResponse() {}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}
	
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

}
