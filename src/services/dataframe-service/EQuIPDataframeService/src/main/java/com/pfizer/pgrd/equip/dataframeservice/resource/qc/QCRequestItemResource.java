package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.Date;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequestItem;
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

public class QCRequestItemResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(QCRequestItemResource.class);
	
	/**
	 * A {@link Route} that will fetch the qcrequests associated with the qcrequest
	 * ID.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noRequestError = "No QC Request Item was provided.";
			String returnJson = null;
			QCRequestItem qcrequest = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
	
					String qcJson = request.body();
					if (qcJson != null) {
						List<QCRequestItem> list = unmarshalObject(qcJson, QCRequestItem.class);
						if (!list.isEmpty()) {
							qcrequest = list.get(0);
							if (qcrequest != null) {
								qcrequest.setEquipId(EquipIdCalculator.calculate("qc request item"));
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
								
								qcrequest = dao.insertQCRequestItem(qcrequest);
								String qcrequestId = qcrequest.getId();
				
								/*asc.logAuditEntry(	"Creation of QCRequestItem",
													qcrequest.getEquipId(),
													"QCRequestItem",
													userId,
													Props.isAudit(),
													Const.AUDIT_SUCCESS,
													1L);	*/					
								AuditDetails details = asc.new AuditDetails("Creation of QCRequestItem", qcrequest, userId);
								asc.logAuditEntry(details);
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
//					if(userId != null && qcrequest != null){
//						asc.logAuditEntry(	"Attempt to create QCRequestItemResource failed with exception " + ex.getMessage(), 
//											qcrequest.getEquipId(),
//											"QCRequestItemResource",
//											userId,
//											Props.isAudit(),
//											Const.AUDIT_FAILURE,
//											qcrequest.getVersionNumber());
//					}
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
