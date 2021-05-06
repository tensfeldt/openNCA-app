package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
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

public class QCRequestSummaryResource extends QCRequestBaseResource {
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
						/*asc.logAuditEntry(	"Access of QC Request Summary", 
											qcr.getEquipId(),
											"QCRequest",
											userId,
											Props.isAudit(),
											Const.AUDIT_SUCCESS,
											1L);*/						
						AuditDetails details = asc.new AuditDetails("Access of QC Request Summary", qcr, userId);
						
						asc.logAuditEntryAsync(details);
						json = marshalObject(qcr.getQcChecklistSummaryItems());
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No QC Request with ID '" + id + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No QC Rquest ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

}