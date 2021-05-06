package com.pfizer.modeshape.api.client;

/**
 * Exception for ModeshapeClient errors
 * 
 * @author HeinemanWP
 *
 */
public class ModeshapeClientException extends Exception {
	private static final long serialVersionUID = -6300556498695363889L;

	public ModeshapeClientException() {
		super();
	}

	public ModeshapeClientException(String message) {
		super(message);
	}

	public ModeshapeClientException(Throwable cause) {
		super(cause);
	}

	public ModeshapeClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModeshapeClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
