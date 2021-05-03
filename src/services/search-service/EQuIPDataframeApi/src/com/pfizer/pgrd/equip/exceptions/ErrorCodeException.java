package com.pfizer.pgrd.equip.exceptions;

public class ErrorCodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int errorCode;
	
	public ErrorCodeException( int errorCode, String message ){
		super( message );
		this.errorCode = errorCode;
	}
	
	public int getErrorCode(){
		return errorCode;
	}
}
