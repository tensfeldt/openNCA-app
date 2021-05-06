package com.pfizer.equip.computeservice.exception;

public class ComputeException extends Exception {
	private static final long serialVersionUID = -4467080174287906360L;

	public ComputeException() {
		super();
	}

	public ComputeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComputeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComputeException(String message) {
		super(message);
	}

	public ComputeException(Throwable cause) {
		super(cause);
	}

}
