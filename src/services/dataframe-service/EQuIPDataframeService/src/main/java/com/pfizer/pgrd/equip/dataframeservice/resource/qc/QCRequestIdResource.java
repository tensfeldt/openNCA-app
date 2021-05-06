package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCRequestDTO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class QCRequestIdResource extends QCRequestBaseResource {
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
					
					QCRequestDAO dao = getQCRequestDAO();
					QCRequest qcr = dao.getQCRequest(id);
					if (qcr != null) {
						/*asc.logAuditEntry(	"Access of QC Request by Id", 
											qcr.getId(),
											"QCRequest",
											userId,
											Props.isAudit(),
											Const.AUDIT_SUCCESS,
											0L);*/						
						AuditDetails details = asc.new AuditDetails("Access of QC Request by Id", qcr, userId);
						asc.logAuditEntryAsync(details);
						json = marshalObject(qcr);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Request with ID '" + id + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Request ID was provided.");
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
			QCRequest qcr = null;
			String returnJson = null;
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equals(HTTPContentTypes.JSON)) {
					String id = request.params(":id");
					if (id != null) {
						QCRequestDAO dao = getQCRequestDAO();
						qcr = dao.getQCRequest(id);
						if (qcr != null) {
							String status = request.body();
							if (status != null) {
								String key = status.split(":")[0];
								String value = status.split(":")[1];
	
								if (key.equalsIgnoreCase("qcDueDate")) {
									Date d = DateUtils.parseDate(value);
									qcr.setQcDueDate(d);
								} else if (key.equalsIgnoreCase("qcStatus")) {
									qcr.setQcStatus(value);
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "'" + key + "' is not a modifiable property.");
								}
	
								qcr.setModifiedBy(System.getProperty("username"));
								qcr.setModifiedDate(new Date());
	
								ModeShapeDAO bdao = new ModeShapeDAO();
								QCRequestDTO dto = new QCRequestDTO(qcr);
								bdao.updateNode(dto);
	
								qcr = dao.getQCRequest(id);
								returnJson = marshalObject(qcr);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No properties were provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Request with ID '" + id + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Request ID was provided.");
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