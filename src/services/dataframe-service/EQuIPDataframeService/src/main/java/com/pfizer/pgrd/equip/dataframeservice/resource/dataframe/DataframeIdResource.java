package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
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
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
public class DataframeIdResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeIdResource.class);

	/**
	 * A {@link Route} that will fetch the dataframe that matches the provided
	 * dataframe ID.
	 */
	public static final Route get = new Route() {
		
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			long fetchDataframe = 0;
			long authTime = 0;
			long privTime = 0;
			long totalTime = System.currentTimeMillis();
			try {
				String dataframeId = request.params(":id");
				if (dataframeId != null) {
					DataframeDAO dao = getDataframeDAO();
					
					fetchDataframe = System.currentTimeMillis();
					Dataframe df = dao.getDataframe(dataframeId);
					fetchDataframe = System.currentTimeMillis() - fetchDataframe;
					
					String userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}
									
					if (df != null) {
						AuthorizationDAO auth = new AuthorizationDAO();
						
						authTime = System.currentTimeMillis();
						boolean isOk = auth.canViewDataframe(df, userId);
						authTime = System.currentTimeMillis() - authTime;
						
						if (!isOk) {
							//Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userId + " is not authorized to view this dataframe");
							
							// If user does not have access, hide anything that may contain information on the data.
							AuthorizationDAO.maskDataframe(df);
						}
						
						isOk = auth.checkPrivileges(df.getDataframeType(), "GET", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to view this type of dataframe");
						}
						
						if(Props.isAudit()){
							/*asc.logAuditEntry(	"Access of dataframe by id", 
												df.getEquipId(),
												df.getDataframeType(),
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												df.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Access of dataframe by id", df, userId);
							details.setRequest(request);
							asc.logAuditEntryAsync(details);
						}
						
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
						json = marshalObject(df);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "Dataframe not found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}
			totalTime = System.currentTimeMillis() - totalTime;
			
			return json;
		}

	};
}