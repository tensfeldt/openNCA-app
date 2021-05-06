package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.MultipartConfig;

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
@MultipartConfig
public class DataframeListResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeListResource.class);

	/**
	 * A {@link Route} that will insert any JSON represented dataframes found in the
	 * request body.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String userId = null;
			List<Dataframe> list = null;
			List<Dataframe> returnList = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String dfList = request.body();
					if (dfList != null) {
						List<String> dfArray = ServiceBaseResource.unmarshalObject(dfList, String.class);
						//String[] dfArray = dfList.replace("[", "").replace("]", "").replace("\"", "").split(",");
	
						userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						
						DataframeDAO dao = getDataframeDAO();
						list = dao.getDataframe(dfArray, true);
						
						AuthorizationDAO auth = new AuthorizationDAO();
						Map<String, Boolean> privs = new HashMap<>();
						List<Dataframe> checkView = new ArrayList<>();
						for(Dataframe df : list) {
							Boolean hasPrivilege = privs.get(df.getDataframeType());
							if(hasPrivilege == null) {
								hasPrivilege = auth.checkPrivileges(df.getDataframeType(), "GET", userId);
								privs.put(df.getDataframeType(), hasPrivilege);
							}
							
							if(hasPrivilege) {
								checkView.add(df);
							}
						}
						
						Map<String, Boolean> viewMap = auth.canViewDataframe(checkView, userId);
						returnList = new ArrayList<>();
						for(Dataframe df : checkView) {
							boolean canView = viewMap.get(df.getId());
							if(!canView) {
								AuthorizationDAO.maskDataframe(df);
							}
							
							returnList.add(df);
						}
						
						/*
						for (Dataframe df:list) {
							if(this.notNullOrEmpty(df.getPromotionStatus()) && this.notNullOrEmpty(df.getDataStatus())
									&& this.notNullOrEmpty(df.getDataBlindingStatus()) && this.notNullOrEmpty(df.getDataframeType())) {
								
								boolean hasPrivilege = auth.checkPrivileges(df.getDataframeType(), "GET", userId);
								
								if(hasPrivilege) {
									boolean canView = auth.canViewDataframe(df, userId);
									if(!canView) {
										AuthorizationDAO.maskDataframe(df);
									}
									
									returnList.add(df);
								}
							}
							
							if (df.getPromotionStatus() == null || df.getPromotionStatus().isEmpty()) {
								list.remove(df);
								
							}
							
							if (df.getDataStatus() == null || df.getDataStatus().isEmpty()) {
								list.remove(df);
								
							}
							
							if (df.getDataBlindingStatus() == null || df.getDataBlindingStatus().isEmpty()) {
								list.remove(df);
								
							}
							
							if (df.getDataframeType() == null || df.getDataframeType().isEmpty()) {
								list.remove(df);
							}
							
							boolean isOk = auth.canViewDataframe(df, userId);
							if (!isOk) {
								list.remove(df);
							}
							
							isOk = auth.checkPrivileges(df.getDataframeType(), "GET", userId);
							
							if (!isOk) {
								list.remove(df);
							}
						}
						*/
						
						if (!returnList.isEmpty()) {
							for(Dataframe df : returnList){
								if(Props.isAudit()){
									/*asc.logAuditEntry(	"Creation of dataframe via list",
														df.getEquipId(),
														df.getDataframeType(),
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														df.getVersionNumber());*/
									AuditDetails details = asc.new AuditDetails("Creation of dataframe via list", df, userId);
									//details.setContextEntity(df);
									details.setRequest(request);
									asc.logAuditEntryAsync(details);
								}
							}
							
							json = marshalObject(returnList);
						}
						else if(!list.isEmpty() && returnList.isEmpty()) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"None of these dataframes are allowed. Please check that the user has privileges and that statuses and dataframe type have been set.");
						}
						
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe IDs were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON + ".");
				}
			}
			catch(Exception ex){
				try{
					if(userId != null && returnList != null){
						for( Dataframe df:returnList){
							if( df.getId() != null){
								if(Props.isAudit()){
									/*asc.logAuditEntry(	"Access of dataframe failed with exception " + ex.getMessage(), 
														df.getEquipId(),
														"Dataframe",
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														df.getVersionNumber());*/
									AuditDetails details = asc.new AuditDetails("Access of dataframe failed with exception " + ex.getMessage(), df, userId);
									details.setContextEntity(df);
									asc.logAuditEntryAsync(details);
								}
							}
						}
					}
				}
				catch(Exception ex2){
					LOGGER.error("", ex2); //intentionally swallowing exception, we want the original exception to be reported.
				}
				
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
		
		private boolean notNullOrEmpty(String s) {
			boolean good = (s != null) && (!s.isEmpty());
			return good;
		}

	};
}