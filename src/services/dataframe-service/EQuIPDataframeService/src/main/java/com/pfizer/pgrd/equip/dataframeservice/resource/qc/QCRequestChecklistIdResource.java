package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCChecklistItemDTO;
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

public class QCRequestChecklistIdResource extends QCRequestBaseResource {
	/**
	 * A {@link Route} that will fetch the qcrequest that matches the provided
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
					String userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}
					
					ModeShapeDAO dao = new ModeShapeDAO();
					QCChecklistItemDTO dto = dao.getNode(id, QCChecklistItemDTO.class);
					if (dto != null) {
						QCChecklistItem item = dto.toQCChecklistItem();

						if(item != null){
							/*asc.logAuditEntry(	"Access of QC Checklist Item", 
												item.getEquipId(),
												"QCChecklistItem",
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												1L);	*/
							AuditDetails details = asc.new AuditDetails("Access of QC Checklist Item", item, userId);
							
							asc.logAuditEntryAsync(details);
						}
						
						json = marshalObject(item);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Checklist Item with ID '" + id + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC checklist item ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

	public static final Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			QCChecklistItem qcci = null;
			String returnJson = null;
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equals(HTTPContentTypes.JSON)) {
					String id = request.params(":id");
					if (id != null) {
						ModeShapeDAO dao = new ModeShapeDAO();
						QCChecklistItemDTO dto = dao.getNode(id, QCChecklistItemDTO.class);
						if (dto != null) {
							String status = request.body();
							if (status != null) {
								String key = status.split(":")[0];
								String value = status.split(":")[1];
	
								if (key.equals("sourceComment")) {
									dto.setSourceComment(value);
								} else if (key.equals("qcComment")) {
									dto.setQcComment(value);
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "'" + key + "' is not a modifiable property.");
								}
	
								dto.setModified(new Date());
								dto.setModifiedBy(System.getProperty("username"));
	
								dao.updateNode(dto);
								dto = dao.getNode(id, QCChecklistItemDTO.class);
								qcci = dto.toQCChecklistItem();
	
								returnJson = marshalObject(qcci);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No properties were provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No QC Checklist Item with ID '" + id + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
					}
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}
	};

}