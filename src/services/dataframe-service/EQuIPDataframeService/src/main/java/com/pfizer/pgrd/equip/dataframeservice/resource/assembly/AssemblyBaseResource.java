package com.pfizer.pgrd.equip.dataframeservice.resource.assembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class AssemblyBaseResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyBaseResource.class);
	
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = "";
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			Assembly assembly = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null) {
					if (contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
						String assemblyJson = request.body();
						List<Assembly> list = unmarshalObject(assemblyJson, Assembly.class);
						if (!list.isEmpty()) {
							assembly = list.get(0);
							
							userId = request.headers("IAMPFIZERUSERCN");
							if (userId == null) {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
							}

							if (assembly != null) {
								if (assembly.getAssemblyType() == null || assembly.getAssemblyType().isEmpty()){
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Assembly must include an Assembly Type");
								}
								if (assembly.getAssemblyType().equalsIgnoreCase("data load")){							
									//make sure the user can post an assembly
									AuthorizationDAO auth = new AuthorizationDAO();
									boolean isOk = auth.checkPrivileges(assembly.getAssemblyType(), "POST", userId);
									if (!isOk) {
										Spark.halt(HTTPStatusCodes.FORBIDDEN,
												"User " + userId + " does not have privileges to post " + assembly.getAssemblyType());
									}
								}
								
								if (assembly.getStudyIds() != null && !assembly.getStudyIds().isEmpty()) {
									if (assembly.getCreated() == null) {
										assembly.setCreated(new Date());
									}
									if (assembly.getCreatedBy() == null) {
										if (userId != null) {
											assembly.setCreatedBy(userId);
										} else {
											Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
										}

									}
									
									/*for(Comment c : assembly.getComments()) {
										if(c.getCreated() == null) {
											c.setCreated(new Date());
										}
										if(c.getCreatedBy() == null) {
											c.setCreatedBy(userId);
										}
									}*/
									ServiceBaseResource.setSubInfo(assembly, userId);
									
									AssemblyDAO dao = getAssemblyDAO();
									applyVersionIncrementingLogic(assembly, dao);
									
									String aType = assembly.getAssemblyType();
									if(aType != null && aType.equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
										assembly.setAtrIsCurrent(false);
									}
									
									assembly = dao.insertAssembly(assembly);
									returnJson = assembly.getId();
									
									if(Props.isAudit()){
										/*asc.logAuditEntry(	"Creation of assembly",
															assembly.getEquipId(),
															"Assembly",
															userId,
															Props.isAudit(),
															Const.AUDIT_SUCCESS,
															assembly.getVersionNumber());*/
										AuditDetails details = asc.new AuditDetails("Creation of assembly", assembly, userId);
										details.setContextEntity(assembly);
										asc.logAuditEntryAsync(details);
									}
									
									// call opmeta service to update modification time on associated protocol
									try {
										OpmetaServiceClient osc = new OpmetaServiceClient();
										osc.setHost(Props.getOpmetaServiceServer());
										osc.setPort(Props.getOpmetaSerivcePort());
										List<String> studyIds = assembly.getStudyIds();
										for(String studyId: studyIds) {
											LOGGER.info("AssemblyBaseResource: update protocol for study id=" + studyId);
											osc.updateProtocolModifiedDate(userId, studyId);
										}
									}
									catch(Exception err) {
										LOGGER.warn("AssemblyBaseResource: Error updating protocol modification time for node " + assembly.id, err);
									}
									
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
									response.header(HTTPHeaders.LOCATION, "/assemblies/" + assembly.getId());
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "At least one study ID must be provided.");
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Assembly was provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Assembly was provided.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON + ".");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "A " + HTTPHeaders.CONTENT_TYPE + " must be provided.");
				}
			} catch (Exception ex) {
				try{
					if(userId != null && assembly != null){
						if(Props.isAudit()){
							/*asc.logAuditEntry(	"Attempt to create assembly failed with exception " + ex.getMessage(), 
												assembly.getEquipId(),
												"Assembly",
												userId,
												Props.isAudit(),
												Const.AUDIT_FAILURE,
												assembly.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Attempt to create assembly failed with exception " + ex.getMessage(), assembly, userId);
							details.setActionStatus(AuditDetails.FAILURE);
							details.setContextEntity(assembly);
							asc.logAuditEntryAsync(details);
						}
					}
				}
				catch(Exception ex2){
					LOGGER.error("", ex2); //intentionally swallowing exception, we want the original exception to be reported.
				}
				
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}
	};
	
	public static final Assembly fullInsert(Assembly assembly, String userId) throws ServiceCallerException {
		if (assembly.getCreated() == null) {
			assembly.setCreated(new Date());
		}
		if (assembly.getCreatedBy() == null) {
			if (userId != null) {
				assembly.setCreatedBy(userId);
			} else {
				Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
			}
		}
		
		for(Comment c : assembly.getComments()) {
			if(c.getCreated() == null) {
				c.setCreated(new Date());
			}
			if(c.getCreatedBy() == null) {
				c.setCreatedBy(userId);
			}
		}
		
		// Insert the assembly.
		AssemblyDAO dao = getAssemblyDAO();
		applyVersionIncrementingLogic(assembly, dao);
		assembly = dao.insertAssembly(assembly);
		
		// Create audit entry.
		if(Props.isAudit()){
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			/*asc.logAuditEntryAsync(	"Creation of assembly",
								assembly.getEquipId(),
								"Assembly",
								userId,
								Props.isAudit(),
								Const.AUDIT_SUCCESS,
								assembly.getVersionNumber());*/
			AuditDetails details = asc.new AuditDetails("Creation of assembly", assembly, userId);
			details.setContextEntity(assembly);
			asc.logAuditEntryAsync(details);
		}
		
		// Update opmeta.
		try {
			OpmetaServiceClient osc = new OpmetaServiceClient();
			osc.setHost(Props.getOpmetaServiceServer());
			osc.setPort(Props.getOpmetaSerivcePort());
			List<String> studyIds = assembly.getStudyIds();
			for(String studyId: studyIds) {
				LOGGER.info("AssemblyBaseResource: update protocol for study id=" + studyId);
				osc.updateProtocolModifiedDate(userId, studyId);
			}
		}
		catch(Exception err) {
			LOGGER.warn("AssemblyBaseResource: Error updating protocol modification time for node " + assembly.id, err);
		}
		
		return assembly;
	}
	
	public static final String validate(Assembly assembly) {
		if(assembly != null) {
			if(assembly.getAssemblyType() == null || assembly.getAssemblyType().isEmpty()) {
				return  "Assembly does not have assembly type.";
			}
			if(assembly.getStudyIds() == null || assembly.getStudyIds().isEmpty()) {
				return "Assembly has no study IDs.";
			}
		}
		
		return null;
	}
	
	public static void applyVersionIncrementingLogic(Assembly assembly) {
		AssemblyBaseResource.applyVersionIncrementingLogic(assembly, ModeShapeDAO.getAssemblyDAO());
	}
	
	public static void applyVersionIncrementingLogic(Assembly assembly, AssemblyDAO dao) {
		try {
			EquipVersionableListGetter assemblySiblingGetter = equipId -> {
				List<Assembly> assemblies = dao.getAssemblysByEquipId(equipId);
				List<EquipVersionable> retVal = new ArrayList<>();
				for (Assembly a : assemblies) {
					retVal.add(a);
				}
				
				return retVal;
			};
			new VersioningDAO().applyVersionIncrementingLogic(assembly, assembly.getAssemblyType(),
					assemblySiblingGetter);
		} catch (ErrorCodeException ex) {
			Spark.halt(ex.getErrorCode(), ex.getMessage());
		}
	}
}
