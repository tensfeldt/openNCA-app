package com.pfizer.pgrd.equip.dataframeservice.resource.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.MultipartConfig;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

@MultipartConfig
public class AssemblyListResource extends AssemblyBaseResource {

	/**
	 * A {@link Route} that will insert any JSON represented dataframes found in the
	 * request body.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
				AuthorizationDAO auth = new AuthorizationDAO();

				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String assemblyList = request.body();
					List<String> assemblyArray = unmarshalObject(assemblyList, String.class);
					// String[] assemblyArray = assemblyList.replace("[", "").replace("]",
					// "").replace("\"", "").split(",");

					AssemblyDAO dao = getAssemblyDAO();
					List<Assembly> list = dao.getAssembly(assemblyArray);
					List<Assembly> returnList = new ArrayList<>();
					Map<String, Boolean> privs = new HashMap<>();
					
					// check each assembly and only return the ones the user can see
					for(Assembly a : list) {
						if(a.getAssemblyType() != null && !a.getAssemblyType().isEmpty()) {
							Boolean isOk = privs.get(a.getAssemblyType());
							if(isOk == null) {
								isOk = true;
								if(a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
									isOk = auth.checkPrivileges(a.getAssemblyType(), "POST", userId);
								}
								
								privs.put(a.getAssemblyType(), isOk);
							}
							
							if(isOk) {
								returnList.add(a);
							}
						}
					}
					
					/*
					// check each assembly and only return the ones the user can see
					for (Assembly a : list) {
						if (a.getAssemblyType() != null && !a.getAssemblyType().isEmpty()) {
							boolean isOk = true;
							if (a.getAssemblyType().equalsIgnoreCase("data load")) {
								
								// make sure the user can post this assembly
								isOk = auth.checkPrivileges(a.getAssemblyType(), "POST", userId);
							}
							
							if (isOk) {
								returnList.add(a);
							}
						}
					}
					*/
					
					if (!list.isEmpty() && returnList.isEmpty()) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN,
								"User " + userId + " does not have privileges to view any of these assemblies");
					}

					for (Assembly a : returnList) {
						if (Props.isAudit()) {
							if (a != null) {
								/*asc.logAuditEntry("Creation of Assembly by id", a.getEquipId(), a.getAssemblyType(),
										userId, Props.isAudit(), Const.AUDIT_SUCCESS, a.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Creation of Assembly by id", a, userId);
								details.setContextEntity(a);
								asc.logAuditEntryAsync(details);
							}
						}
					}

					json = marshalObject(list);
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON + ".");
				}

				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

}
