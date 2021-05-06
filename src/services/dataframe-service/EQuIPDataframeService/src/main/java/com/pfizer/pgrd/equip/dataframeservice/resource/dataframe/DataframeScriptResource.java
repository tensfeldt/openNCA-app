package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ScriptDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class DataframeScriptResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeScriptResource.class);

	/**
	 * A {@link Route} that will fetch a reference to the script object associated
	 * with the dataframe ID.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;

			try{
				String dataframeId = request.params(":id");

				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				if (dataframeId != null) {
					DataframeDAO dao = getDataframeDAO();
					Dataframe df = dao.getDataframe(dataframeId);
	
					if (df != null) {
						AuthorizationDAO aDao = new AuthorizationDAO();
						boolean hasAccess = aDao.canViewDataframe(df, userId);
						if(hasAccess) {
							if(df.getScript() != null) {
								
								json = marshalObject(df.getScript());
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
							}
							else {
								Spark.halt(HTTPStatusCodes.NOT_FOUND, "Dataframe " + dataframeId + " has no script.");
							}
						}
						else {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User does not have permission to view dataframe " + dataframeId + " and so cannot view its script.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No Dataframe with ID '" + dataframeId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

	/**
	 * A {@link Route} that will post a new comment to the node ID.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noScriptError = "No Script was provided.";
			String returnJson = null;
			String parentId = null;
			String userId = null;

			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					parentId = request.params(":id");
					if (parentId != null) {
						userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						String scriptJson = request.body();
						if (scriptJson != null) {
							List<Script> list = unmarshalObject(scriptJson, Script.class);
							if (!list.isEmpty()) {
								Script script = list.get(0);
								if (script != null) {
									ScriptDAO dao = getScriptDAO();
									ServiceBaseResource.setCreatedInfo(script, userId);
									script = dao.insertScript(script, parentId);
									
									returnJson = script.getId();
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
									response.header(HTTPHeaders.LOCATION, "/dataframes/" + parentId + "/script");
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, noScriptError);
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noScriptError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noScriptError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No parent ID was provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}

	};

}
