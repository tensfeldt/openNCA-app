package com.pfizer.equip.computeservice.resource;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * A base class for EQUIP service resources. All methods return a 405 (Method Not Allowed) status.
 * @author QUINTJ16
 *
 */
public class BaseEquipResource {
	private static final int METHOD_NOT_ALLOWED = 405;

	/**
	 * Default GET handling.
	 */
	public static Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default POST handling.
	 */
	public static Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default PUT handling.
	 */
	public static Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default DELETE handling.
	 */
	public static Route delete = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};
}
