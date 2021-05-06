package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

public class QCRequestBaseResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(QCRequestBaseResource.class);
	
	/**
	 * A {@link Route} that will fetch the qcrequests associated with the qcrequest
	 * ID.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noRequestError = "No QC Request was provided.";
			String returnJson = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			QCRequest qcrequest = null;
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					// String qcJson = " {'createdBy': 'HIRSCM08','createdDate':
					// '2018-01-24T18:18:46.991Z','equipId': 12345678,'dataframeId':
					// 1516817926991,'assemblyId': null, 'qcStatus' : 'QC
					// COMPLETE','RequestorUserId': 'HIRSCM08','qcDueDate':
					// '2018-02-28T18:50:16.333Z','ChecklistTemplateId' : '123ABC'}";
	
					String qcJson = request.body();
					if (qcJson != null) {
						List<QCRequest> list = unmarshalObject(qcJson, QCRequest.class);
						if (!list.isEmpty()) {
							qcrequest = list.get(0);
							if (qcrequest != null) {
								qcrequest.setEquipId(EquipIdCalculator.calculate("qc request"));
								QCRequestDAO dao = getQCRequestDAO();
								
								
								if (qcrequest.getCreated() == null) {
									qcrequest.setCreated(new Date());
								}
								
								if (qcrequest.getCreatedBy() == null) {
									userId = request.headers("IAMPFIZERUSERCN");
									if (userId != null) {
										qcrequest.setCreatedBy(userId);}
										else{
											Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
										}
											
									}
								
								ServiceBaseResource.setSubInfo(qcrequest, userId);
								
								// inserting a QC request requires generating children, this will be done in the
								// DAO
								qcrequest = dao.insertQCRequest(qcrequest);
								String qcrequestId = qcrequest.getId();
								
								if(qcrequest != null){
									/*asc.logAuditEntry(	"Creation of QC Request",
														qcrequest.getEquipId(),
														"QCRequest",
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														1L);	*/
									AuditDetails details = asc.new AuditDetails("Creation of QC Request", qcrequest, userId);
									
									asc.logAuditEntryAsync(details);
								}
								
								returnJson = qcrequestId;
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
								response.header(HTTPHeaders.LOCATION, "/qcrequests/" + qcrequestId);
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRequestError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRequestError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRequestError);
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			}
			catch(Exception ex){
				try{
					if(userId != null && qcrequest != null){
//						asc.logAuditEntry(	"Attempt to create QCRequestBaseResource failed with exception " + ex.getMessage(), 
//											qcrequest.getEquipId(),
//											"QCRequestBaseResource",
//											userId,
//											Props.isAudit(),
//											Const.AUDIT_FAILURE,
//											qcrequest.getVersionNumber());
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

}
