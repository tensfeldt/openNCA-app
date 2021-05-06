package com.pfizer.pgrd.equip.dataframeservice.resource.assembly;


import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
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

public class AssemblyIdResource extends AssemblyBaseResource {
	/**
	 * A {@link Route} that will fetch the assembly that matches the provided
	 * assembly ID.
	 */
	public static final Route get = new Route() {
		
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try{
				String id = request.params(":id");
				if (id != null) {
					AssemblyDAO dao = getAssemblyDAO();
					Assembly a = dao.getAssembly(id);
					
					if (a != null) {
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						
						if (a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
							//make sure the user can post an assembly
							AuthorizationDAO auth = new AuthorizationDAO();
							boolean isOk = auth.checkPrivileges(a.getAssemblyType(), "GET", userId);
							if (!isOk) {
								Spark.halt(HTTPStatusCodes.FORBIDDEN,
										"User " + userId + " does not have privileges to GET " + a.getAssemblyType());
							}
							
							boolean hasAccess = ServiceBaseResource.userHasAccess(a, userId);
							if(!hasAccess) {
								a.setComments(new ArrayList<>());
								a.setMetadata(new ArrayList<>());
							}
						}
						
						if(Props.isAudit()){
							/*asc.logAuditEntry(	"Access of Assembly by id",
												a.getEquipId(),
												"Assembly",
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												a.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Access of Assembly by id", a, userId);
							details.setContextEntity(a);
							details.setRequest(request);
							asc.logAuditEntryAsync(details);
						}
						
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
						json = marshalObject(a);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No Assembly with ID '" + id + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Assembly ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};
	
	public static final Route getAssemblyByEquipId = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try{
				String equipId = request.params(":equipId");
				if (equipId != null) {
					AssemblyDAO dao = getAssemblyDAO();
					List<Assembly> assemblies = dao.getAssemblysByEquipId(equipId);
	
					if (assemblies != null) {
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						
						for(Assembly a : assemblies) {
							if (a.getAssemblyType().equalsIgnoreCase("data load")){
								
								//make sure the user can post an assembly
								AuthorizationDAO auth = new AuthorizationDAO();
								boolean isOk = auth.checkPrivileges(a.getAssemblyType(), "GET", userId);
								if (!isOk) {
									Spark.halt(HTTPStatusCodes.FORBIDDEN,
											"User " + userId + " does not have privileges to post " + a.getAssemblyType());
								}
							}
							
							if(Props.isAudit()){
								/*asc.logAuditEntry(	"Access of Assembly by equip id", 
													a.getEquipId(),
													a.getAssemblyType(),
													userId,
													Props.isAudit(),
													Const.AUDIT_SUCCESS,
													a.getVersionNumber());		*/
								AuditDetails details = asc.new AuditDetails("Access of Assembly by equip id", a, userId);
								details.setContextEntity(a);
								asc.logAuditEntryAsync(details);
							}
						}
						
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
						json = marshalObject(assemblies);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No Assembly with EQUIP ID '" + equipId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Assembly EQUIP ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
		
	};
}