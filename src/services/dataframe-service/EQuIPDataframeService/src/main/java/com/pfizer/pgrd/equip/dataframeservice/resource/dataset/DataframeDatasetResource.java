package com.pfizer.pgrd.equip.dataframeservice.resource.dataset;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
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

public class DataframeDatasetResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeDatasetResource.class);

	/**
	 * A {@link Route} that will fetch the dataset json object, including the id of
	 * the file (if one exists)
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;

			try {
				String dataframeId = request.params(":id");
				if (dataframeId != null) {
					DatasetDAO dao = getDatasetDAO();
					DataframeDAO ddao = getDataframeDAO();
					Dataset dataset = dao.getDatasetByDataframe(dataframeId);
					
					if (dataset != null) {
						
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						// get the parent dataframe for this
						Dataframe df = ddao.getDataframe(dataframeId);
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.canViewDataframe(df, userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " is not authorized to view this dataset");
						}
						isOk = auth.checkPrivileges(df.getDataframeType(), "GET", userId);

						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to view this type of dataframe");
						}

						returnJson = marshalObject(dataset);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								"No Dataset was found for Dataframe '" + dataframeId + "'.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}

	};

	/**
	 * A {@link Route} that will post a dataset to an existing dataframe
	 */
	public static Route post = new Route() {
		@Override
		public String handle(Request request, Response response) throws Exception {
			String noDatasetError = "No Dataset was provided.";
			String datasetId = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

			try {
				String dataframeId = request.params(":id");
				if (dataframeId != null) {
					// check user privileges on this dataframe
					DataframeDAO dao = getDataframeDAO();
					Dataframe df = dao.getDataframe(dataframeId);

					userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}

					if (df != null) {
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.canViewDataframe(df, userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " is not authorized to view this dataframe");
						}

						isOk = auth.checkPrivileges(df.getDataframeType(), "POST", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to post a dataset to this dataframe");
						}
					}

					String dsJson = request.body();
					if (dsJson != null) {
						List<Dataset> list = unmarshalObject(dsJson, Dataset.class);
						if (!list.isEmpty()) {
							Dataset dataset = list.get(0);
							if (dataset != null) {
								DatasetDAO ddao = getDatasetDAO();
								ServiceBaseResource.setSubInfo(dataset, userId);
								dataset = ddao.insertDataset(dataframeId, dataset);
								datasetId = dataset.getId();
								
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
								response.header(HTTPHeaders.LOCATION, "/dataframes/" + dataframeId + "/data");
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDatasetError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDatasetError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDatasetError);
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return datasetId;
		}
	};

}
