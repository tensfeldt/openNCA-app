package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
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

public class EntityResource  extends ServiceBaseResource {
	
	public static final Route getReportingEvents = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try {
				String nodeId = request.params(":id");
				
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
				
				if(nodeId != null) {
					ModeShapeDAO mdao = new ModeShapeDAO();
					EquipObject eo = mdao.getEquipObject(nodeId);
					if(eo != null) {
						List<Assembly> reportingEvents = new ArrayList<>();
						
						// get the reporting events
						if( Props.isAudit() ){
							EquipID equipIdObject = (EquipID)eo;
							EquipVersion equipVersionObject = (EquipVersion)eo;
							if(Props.isAudit()){
								if(equipIdObject != null && equipVersionObject != null){
									/*asc.logAuditEntry(	"View access of reporting event", 
														equipIdObject.getEquipId(),
														"ReportingEvent",
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														equipVersionObject.getVersionNumber());*/
									
									AuditDetails details = asc.new AuditDetails("View access of reporting event", eo, userId);
									
									asc.logAuditEntryAsync(details);
								}
							}
						}
						
						json = ServiceBaseResource.marshalObject(reportingEvents);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					}
					else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No entity with ID '" + nodeId + "' could be found.");
					}
				}
				else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No entity ID was provided.");
				}
			}
			catch(Exception e) {
				ServiceExceptionHandler.handleException(e);
			}
			
			return json;
		}
		
	};
	
	public static final Route getByEquipId = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
}
