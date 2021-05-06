package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistSummaryItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCChecklistSummaryItemDTO;
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

public class QCRequestSummaryIdResource extends QCRequestBaseResource {

	public static final Route put = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			QCChecklistSummaryItem qccsi = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equals(HTTPContentTypes.JSON)) {
					String id = request.params(":id");
					if(id != null) {
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						ModeShapeDAO dao = new ModeShapeDAO();
						QCChecklistSummaryItemDTO dto = dao.getNode(id, QCChecklistSummaryItemDTO.class);
						if(dto != null) {
							String status = request.body();
							if(status != null) {
								String key = status.split(":")[0];
								String value = status.split(":")[1];
								
								if (key.equalsIgnoreCase("checklistSummaryItemAnswer")) {
									dto.setAnswer(value);
									// TODO validation
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "'" + key + "' is not a modifiable property.");
								}
								
								dto.setModified(new Date());
								dto.setModifiedBy(System.getProperty("username"));
								
								dao.updateNode(dto);
								dto = dao.getNode(id, QCChecklistSummaryItemDTO.class);
								qccsi = dto.toQCChecklistSummaryItem();
						
//								asc.logAuditEntry(	"Access of QC Request Summary by id", 
//													equipId,
//													"QCRequestSummary",
//													userId,
//													Props.isAudit(),
//													Const.AUDIT_SUCCESS,
//													versionNumber );						
								
								returnJson = marshalObject(qccsi);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
							}
							else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No properties were provided.");
							}
						}
						else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Summary Item with ID '" + id + "' could be found.");
						}
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Summary Item ID was provided.");
					}
				}
				else {
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