package com.pfizer.equip.filedata.exceptions;

/**
 * Exception for file data storage and retrieval errors
 * 
 * @author HeinemanWP
 *
 */
public class FileDataException extends Exception {
	private static final long serialVersionUID = -6338094796156410524L;

	public FileDataException() {
		super();
	}

	public FileDataException(String message) {
		super(message);
	}

	public FileDataException(Throwable cause) {
		super(cause);
	}

	public FileDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileDataException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
