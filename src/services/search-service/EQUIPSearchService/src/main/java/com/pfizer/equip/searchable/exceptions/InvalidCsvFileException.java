package com.pfizer.equip.searchable.exceptions;

/**
 * Exception for invalid csv files, 
 * that is files determined to be not csv files.
 * 
 * @author HeinemanWP
 *
 */
public class InvalidCsvFileException extends Exception {
	private static final long serialVersionUID = -7207965319606289907L;

	public InvalidCsvFileException() {
		super();
	}

	public InvalidCsvFileException(String message) {
		super(message);
	}

	public InvalidCsvFileException(Throwable cause) {
		super(cause);
	}

	public InvalidCsvFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCsvFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
