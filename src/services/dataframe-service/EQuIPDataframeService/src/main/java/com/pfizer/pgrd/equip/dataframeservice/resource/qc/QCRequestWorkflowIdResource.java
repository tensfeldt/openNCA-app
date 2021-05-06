package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCWorkflowItemDTO;
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

public class QCRequestWorkflowIdResource extends QCRequestBaseResource {
	/**
	 * A {@link Route} that will fetch the qcworkflowitem that matches the provided
	 * ID.
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
					QCWorkflowItemDTO dto = dao.getNode(id, QCWorkflowItemDTO.class);
					if (dto != null) {
						QCWorkflowItem item = dto.toQCWorkflowItem();

						/*asc.logAuditEntry(	"Access of QC Workflow by Id", 
											item.getEquipId(),
											"QCWorkflowItem",
											userId,
											Props.isAudit(),
											Const.AUDIT_SUCCESS,
											1L);		*/				
						AuditDetails details = asc.new AuditDetails("Access of QC Workflow by Id", item, userId);
						
						asc.logAuditEntryAsync(details);
						json = marshalObject(item);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Workflow Item with ID '" + id + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Workflow Item ID was provided.");
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
			QCWorkflowItem qcwi = null;
			String returnJson = null;
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equals(HTTPContentTypes.JSON)) {
					String id = request.params(":id");
					if (id != null) {
						ModeShapeDAO dao = new ModeShapeDAO();
						QCWorkflowItemDTO dto = dao.getNode(id, QCWorkflowItemDTO.class);
						if (dto != null) {
							String status = request.body();
							if (status != null) {
								String key = status.split(":")[0];
								String value = status.split(":")[1];
	
								if (key.equalsIgnoreCase("qcworkflowstatus")) {
									dto.setQcWorkflowStatus(value);
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "'" + key + "' is not a modifiable property.");
								}
	
								dto.setModified(new Date());
								dto.setModifiedBy(System.getProperty("username"));
	
								dao.updateNode(dto);
								dto = dao.getNode(id, QCWorkflowItemDTO.class);
								qcwi = dto.toQCWorkflowItem();
	
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
								returnJson = marshalObject(qcwi);
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No properties were provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No QC Workflow Item with ID '" + id + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Workflow Item ID was provided.");
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