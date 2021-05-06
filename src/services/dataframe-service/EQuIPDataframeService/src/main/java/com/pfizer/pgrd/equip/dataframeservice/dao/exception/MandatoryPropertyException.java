package com.pfizer.pgrd.equip.dataframeservice.dao.exception;

public class MandatoryPropertyException extends DAOException {	
	public MandatoryPropertyException(String property) {
		super("The object is missing the mandatory property '" + property + "'.");
	}
}