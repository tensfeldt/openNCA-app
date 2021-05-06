package com.pfizer.pgrd.equip.exceptions;

public class UnableToPersistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnableToPersistException( String msg ){
		super(msg);
	}
}
