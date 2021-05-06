package com.pfizer.equip.searchservice.search;

import java.util.Date;

import com.pfizer.equip.searchservice.dto.SearchResults;
import com.pfizer.equip.searchservice.exception.SearchException;

/**
 * Abstract base class for searches.
 * 
 * @author HeinemanWP
 *
 */
public abstract class BaseSearch {
	protected String searchId;
	protected Date expires;

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public abstract int getCount();

	public abstract void setCount(int count);
	
	public boolean isExpired() {
		return (new Date()).getTime() > expires.getTime();
	}

	public abstract SearchResults searchResults(
			String searchUser,
			String server, 
			String username, 
			String password, 
			String type, 
			int offset,
			int count) throws SearchException;

}
