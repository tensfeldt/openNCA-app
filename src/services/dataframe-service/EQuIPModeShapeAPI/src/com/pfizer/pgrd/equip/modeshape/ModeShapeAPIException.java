package com.pfizer.pgrd.equip.modeshape;

public class ModeShapeAPIException extends Exception {
	private String httpMethod;
	private String uri;
	private String modeShapeErrorMessage;
	private int statusCode;
	
	public ModeShapeAPIException() {
		this(null);
	}
	
	public ModeShapeAPIException(String message) {
		super(message);
	}

	public String getModeShapeErrorMessage() {
		return modeShapeErrorMessage;
	}

	public void setModeShapeErrorMessage(String modeShapeErrorMessage) {
		this.modeShapeErrorMessage = modeShapeErrorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}