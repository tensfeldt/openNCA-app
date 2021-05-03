package com.pfizer.pgrd.equip.services.client;

public class ServiceCallerException extends Exception {
	private static final long serialVersionUID = -3779315885473040353L;
	private String serviceName;
	private int statusCode;
	private ServiceResponse response;

	public ServiceCallerException() {
		super();
	}

	public ServiceCallerException(String serviceName, int statusCode) {
		this();
		this.serviceName = serviceName;
		this.statusCode = statusCode;
	}
	
	public ServiceCallerException(String serviceName, int statusCode, String message) {
		super(message);
		this.serviceName = serviceName;
		this.statusCode = statusCode;
	}
	
	public ServiceCallerException(String serviceName, int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.serviceName = serviceName;
		this.statusCode = statusCode;
	}
	
	public ServiceCallerException(String serviceName, int statusCode, Throwable cause) {
		super(cause);
		this.serviceName = serviceName;
		this.statusCode = statusCode;
	}
	
	public ServiceCallerException(String serviceName, Throwable cause) {
		super(cause);
		this.serviceName = serviceName;
	}
	
	public ServiceCallerException(Throwable cause) {
		super(cause);
	}
	
	public ServiceCallerException(Throwable cause, ServiceResponse response) {
		super(cause);
		this.response = response;
	}
	
	public ServiceCallerException(String serviceName, int statusCode, Throwable cause, ServiceResponse response) {
		super(cause);
		this.serviceName = serviceName;
		this.statusCode = statusCode;
		this.response = response;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getMessage() {
		String r = "No response";
		try {
			if(this.response != null) {
				r = response.getResponseAsString();
			}
		}
		catch(Exception e) { }
		
		if (statusCode > 0) {
			if (serviceName != null && !serviceName.isEmpty()) {
				return String.format("Received %d when calling %s : %s : %s", statusCode, serviceName, super.getMessage(), r);
			}
			return String.format("Received %d : %s : %s", statusCode, super.getMessage(), r);
		}
		if (serviceName != null && !serviceName.isEmpty()) {
			return String.format("Received when calling %s : %s : %s", serviceName, super.getMessage(), r);
		}
		
		return r + " // " + super.getMessage();
	}

}
