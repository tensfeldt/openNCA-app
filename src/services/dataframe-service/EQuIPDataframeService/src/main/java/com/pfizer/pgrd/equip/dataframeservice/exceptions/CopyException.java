package com.pfizer.pgrd.equip.dataframeservice.exceptions;

public class CopyException extends Exception {
	public CopyException() {
		this(null);
	}

	public CopyException(String reason) {
		super(reason);
	}
}
