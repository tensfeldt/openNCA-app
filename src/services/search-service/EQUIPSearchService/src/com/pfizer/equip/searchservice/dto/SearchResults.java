package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the search results.
 * 
 * @author HeinemanWP
 *
 */
public class SearchResults {
	private String searchId;
	private String expires;
	private int count;
	List<Object> results = new ArrayList<>();
	
	public SearchResults() {}
	
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
	public List<Object> getResults() {
		return results;
	}
	public void setResults(List<Object> results) {
		this.results = results;
	}
}
