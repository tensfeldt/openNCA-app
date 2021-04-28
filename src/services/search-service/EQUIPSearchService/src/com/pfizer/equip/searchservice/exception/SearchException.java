package com.pfizer.equip.searchservice.exception;

/**
 * Exception for search errors
 * 
 * @author HeinemanWP
 *
 */
public class SearchException extends Exception {
	private static final long serialVersionUID = -4467080174287906360L;

	public SearchException() {
		super();
	}

	public SearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchException(String message) {
		super(message);
	}

	public SearchException(Throwable cause) {
		super(cause);
	}

}
