package com.pfizer.pgrd.equip.dataframeservice.dao.exception;

public class IllegalReferenceException extends DAOException {
	public IllegalReferenceException() {
		super("An illegal reference was made in one or more of the reference-type properties.");
	}
}