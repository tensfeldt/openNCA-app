package com.pfizer.pgrd.equip.dataframeservice.dao.exception;

public class DAOException extends Exception {
	public DAOException() {
		this(null);
	}
	
	public DAOException(String message) {
		super(message);
	}
}
