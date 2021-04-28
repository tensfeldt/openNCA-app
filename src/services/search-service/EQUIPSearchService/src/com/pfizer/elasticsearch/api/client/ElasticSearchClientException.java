package com.pfizer.elasticsearch.api.client;

/**
 * Elasticsearch client exception 
 * 
 * @author HeinemanWP
 *
 */
public class ElasticSearchClientException extends Exception {
	private static final long serialVersionUID = -6300556498695363889L;

	public ElasticSearchClientException() {
		super();
	}

	public ElasticSearchClientException(String message) {
		super(message);
	}

	public ElasticSearchClientException(Throwable cause) {
		super(cause);
	}

	public ElasticSearchClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElasticSearchClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
