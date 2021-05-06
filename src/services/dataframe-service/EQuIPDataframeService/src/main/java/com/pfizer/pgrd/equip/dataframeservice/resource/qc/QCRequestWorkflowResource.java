package com.pfizer.pgrd.equip.dataframeservice.resource.qc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCRequestDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCWorkflowItemDTO;
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

public class QCRequestWorkflowResource extends QCRequestBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(QCRequestWorkflowResource.class);

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
						/*asc.logAuditEntry(	"Access of QC Request Workflow Resource", 
											qcr.getEquipId(),
											"QCRequest",
											userId,
											Props.isAudit(),
											Const.AUDIT_SUCCESS,
											1L);	*/
						AuditDetails details = asc.new AuditDetails("Access of QC Request Workflow Resource", qcr, userId);
						
						asc.logAuditEntryAsync(details);

						json = marshalObject(qcr.getQcWorkflowItems());
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

	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noRepError = "No QC Workflow Item representation was provided.";
			String returnJson = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

			try{
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equals(HTTPContentTypes.JSON)) {
					userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}

					String parentId = request.params(":id");
					if (parentId != null) {
						ModeShapeDAO dao = new ModeShapeDAO();
						QCRequestDTO dto = dao.getNode(parentId, QCRequestDTO.class);
						if (dto != null) {
							String workflowJson = request.body();
							if (workflowJson != null) {
								List<QCWorkflowItem> list = unmarshalObject(workflowJson, QCWorkflowItem.class);
								if (list != null && !list.isEmpty()) {
									QCWorkflowItem workflowItem = list.get(0);
									if (workflowItem != null) {
										String path = dto.getSelf() + "/" + QCWorkflowItemDTO.PRIMARY_TYPE;
	
										workflowItem.setEquipId(EquipIdCalculator.calculate("qc workflow item"));
										ServiceBaseResource.setSubInfo(workflowItem, userId);
	
										QCWorkflowItemDTO wdto = new QCWorkflowItemDTO(workflowItem);
										wdto = dao.postNode(wdto, path, true);
										if (wdto != null) {
											workflowItem = wdto.toQCWorkflowItem();
											
											if(workflowItem != null){
												/*asc.logAuditEntry(	"Creation of QCWorkflowItem",
																	workflowItem.getEquipId(),
																	"QCWorkflowItem",
																	userId,
																	Props.isAudit(),
																	Const.AUDIT_SUCCESS,
																	1L,
																	parentId,
																	null);*/
												AuditDetails details = asc.new AuditDetails("Creation of QCWorkflowItem", workflowItem, userId);
												details.setContextEntity(dto.toEquipObject());
												asc.logAuditEntryAsync(details);
											}
											
											returnJson = workflowItem.getId();
											response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
											response.header(HTTPHeaders.LOCATION,
													"/qcrequests/" + parentId + "/qcworkflowitems/" + workflowItem.getId());
										} else {
											Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
													"The QC Workflow Item was not created.");
										}
									} else {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRepError);
									}
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRepError);
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noRepError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No QC Request with ID '" + parentId + "' could be found.");
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