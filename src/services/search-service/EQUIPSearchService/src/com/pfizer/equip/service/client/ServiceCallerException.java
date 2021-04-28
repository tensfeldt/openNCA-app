package com.pfizer.equip.service.client;

/**
 * Exception for ServiceCaller errors
 * 
 * @author HeinemanWP
 *
 */
public class ServiceCallerException extends Exception {
	private static final long serialVersionUID = -3779315885473040353L;

	public ServiceCallerException() {
		super();
	}

	public ServiceCallerException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ServiceCallerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServiceCallerException(String arg0) {
		super(arg0);
	}

	public ServiceCallerException(Throwable arg0) {
		super(arg0);
	}

}
