package com.pfizer.equip.searchable.exceptions;

/**
 * Exception for errors resulting from retrieving
 * equip:searchable nodes.
 * 
 * @author HeinemanWP
 *
 */
public class SearchableDataException extends Exception {
	private static final long serialVersionUID = -6338094796156410524L;

	public SearchableDataException() {
		super();
	}

	public SearchableDataException(String message) {
		super(message);
	}

	public SearchableDataException(Throwable cause) {
		super(cause);
	}

	public SearchableDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchableDataException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
