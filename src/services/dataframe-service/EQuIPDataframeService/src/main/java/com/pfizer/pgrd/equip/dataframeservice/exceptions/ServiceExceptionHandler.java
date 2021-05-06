package com.pfizer.pgrd.equip.dataframeservice.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.HaltException;
import spark.Spark;

public class ServiceExceptionHandler {
	private static Logger log = LoggerFactory.getLogger(ServiceExceptionHandler.class);

	public static void handleException(Exception ex) throws Exception {
		log.error("", ex);

		if (ex == null) {
			Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "exception reference was null");
		}

		if (ex.getClass().getName().equals("spark.HaltException") || ex instanceof HaltException) {
			throw ex;
		} else {
			Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
