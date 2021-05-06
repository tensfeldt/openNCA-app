package com.pfizer.pgrd.equip.dataframeservice.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * This resource only exists in order to be able to test the EquipIdCalculator locally,
 * outside of the other places in the service where the "calculate" method is called.
 * 
 * In order to test this locally, you need to go into the Application.java class and enable
 * the lines which add this specific URI to the list of resources.

 * @author MeccaRA
 *
 */
public class EquipIdCalculatorTestResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EquipIdCalculatorTestResource.class);
	private static final String NO_OBJECT_TYPE_ERROR = "No Object Type was provided.";

	/**
	 * A {@link Route} that will fetch the comments associated with the node ID.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			
			try{
				String objType = request.params(":objectType");
				LOGGER.debug(":objectType value set: " + objType);
				if (objType != null) {					
					String equipId = EquipIdCalculator.calculate(objType);
					LOGGER.debug("equipId value fetched from the calculator: " + equipId);
					json = marshalObject(equipId);
					response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_OBJECT_TYPE_ERROR);
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

}
