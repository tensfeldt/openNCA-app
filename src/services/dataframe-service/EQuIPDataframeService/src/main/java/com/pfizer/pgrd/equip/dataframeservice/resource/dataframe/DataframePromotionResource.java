package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.NotificationDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
public class DataframePromotionResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframePromotionResource.class);

	/**
	 * A {@link Route} that will fetch the promotion records associated with the
	 * dataframe ID.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String dataframeId = request.params(":id");
				if (dataframeId != null) {
					DataframeDAO dao = getDataframeDAO();
					Dataframe df = dao.getDataframe(dataframeId);

					if (df != null) {

						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.canViewDataframe(df, userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " is not authorized to view this dataframe");
						}

						isOk = auth.checkPrivileges(df.getDataframeType(), "GET", userId);

						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to view this type of dataframe");
						}

						if (Props.isAudit()) {
							/*asc.logAuditEntry("Access of dataframe via promotion", df.getEquipId(),
									df.getDataframeType(), userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									df.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Access of dataframe via promotion", df, userId);
							details.setContextEntity(df);
							asc.logAuditEntryAsync(details);
							
						}

						json = marshalObject(df.getPromotions());
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND,
								"No Dataframe with ID '" + dataframeId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noPromotionError = "No Promotion was provided.";
			String returnJson = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			boolean dataBlindingStatusIsBeingModified = false;
			boolean dataStatusIsBeingModified = false;
			boolean restrictionStatusIsBeingModified = false;
			boolean promotionStatusIsBeingModified = false;
			String dataframeId = null;
			String userId = null;
			Dataframe df = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String pJson = request.body();
					if (pJson != null) {
						System.out.println("<<<PROMOTION PAYLOAD>>> " + pJson);
						List<Promotion> list = unmarshalObject(pJson, Promotion.class);
						if (!list.isEmpty()) {
							Promotion p = list.get(0);
							if (p != null) {
								// check user permissions to create this promotion

								userId = request.headers("IAMPFIZERUSERCN");
								if (userId == null) {
									Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
								}

								dataframeId = request.params(":id");
								if (dataframeId != null) {
									DataframeDAO dao = getDataframeDAO();
									df = dao.getDataframe(dataframeId);
									
									AuthorizationDAO auth = new AuthorizationDAO();
									boolean isOk = auth.canViewDataframe(df, userId);
									if (!isOk) {
										Spark.halt(HTTPStatusCodes.FORBIDDEN,
												"User " + userId + " is not authorized to view this dataframe and so cannot create promotions for it");
									}

									List<String> promotionTypes = new ArrayList<>();
									if (p.getDataBlindingStatus() != null
											&& !p.getDataBlindingStatus().equals(df.getDataBlindingStatus())) {
										promotionTypes.add("data blinding");
										dataBlindingStatusIsBeingModified = true;
									}
									if (p.getDataStatus() != null && !p.getDataStatus().equals(df.getDataStatus())) {
										promotionTypes.add("data status");
										dataStatusIsBeingModified = true;
									}
									if (p.getRestrictionStatus() != null
											&& !p.getRestrictionStatus().equals(df.getRestrictionStatus())) {
										promotionTypes.add("data restriction");
										restrictionStatusIsBeingModified = true;
									}
									if (p.getPromotionStatus() != null
											&& !p.getPromotionStatus().equals(df.getPromotionStatus())) {
										promotionTypes.add("data promotion");
										promotionStatusIsBeingModified = true;
									}
									
									isOk = false;
									for (String promotionType : promotionTypes) {
										isOk = auth.checkPrivileges(promotionType, "POST", userId);
										if (!isOk) {
											Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userId
													+ " does not have privileges to modify " + promotionType);
										}
									}
//need to check promotion status first
									if (p.getPromotionStatus().equalsIgnoreCase("promoted")) {
										if (!df.getPromotionStatus().equalsIgnoreCase("revoke/fail")) 	{
											if (!p.getDataBlindingStatus().equalsIgnoreCase("Blinded")) {  //fix defect #302 if promoted need to check auth if not blinded
											try { // an exception will be thrown if blinding group does not exist or if
													// the call to auth fails, otherwise the call will return false
												if (!auth.hasBlindingAccess(df, userId)) {
													Spark.halt(HTTPStatusCodes.FORBIDDEN,
															"There are no users in the blinding group for "
																	+ df.getStudyIds().get(0));
												}
											} catch (ServiceCallerException ex) {
												LOGGER.error("Error when calling OpMeta blinding access - " + ex.getStatusCode() + " // " + ex.getMessage(), ex);
												
												if (ex.getStatusCode() != 404) {
													Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, ex.getMessage());
												} else {
													Spark.halt(HTTPStatusCodes.NOT_FOUND, ex.getMessage());
												}
												
											}
										}
										} else {
											Spark.halt(HTTPStatusCodes.FORBIDDEN,
													"Promoting a revoked dataframe is not allowed");
										}
									}

									if (p.getPromotionStatus().equalsIgnoreCase("revoke/fail")
											&& !dao.isPromotionRevokable(df)) {
										// only revoke if parent is not promoted or this a generated node
										Spark.halt(HTTPStatusCodes.FORBIDDEN,
												"This dataframe's promotion status cannot be revoked");
									}

									Date now = new Date();
									if (p.getCreated() == null) {
										p.setCreated(now);
									}
									p.setCreatedBy(userId);
									// p.setEquipId("P1209");
									
									ServiceBaseResource.setSubInfo(p, userId);
									p = dao.addPromotion(p, dataframeId);

									if (dataBlindingStatusIsBeingModified) {
										if (Props.isAudit()) {
											if (p.getDataBlindingStatus() != null) {
												/*asc.logAuditEntry(
														"Data blinding status was changed from "
																+ df.getDataBlindingStatus() + " to "
																+ p.getDataBlindingStatus(),
														df.getEquipId(), "Dataframe", userId, Props.isAudit(),
														Const.AUDIT_SUCCESS, df.getVersionNumber());*/
												AuditDetails details = asc.new AuditDetails("Data blinding status was changed from "+ df.getDataBlindingStatus() + " to "
														+ p.getDataBlindingStatus(), df, userId);
												details.setContextEntity(p);
												asc.logAuditEntryAsync(details);
											}
										}
									}

									if (dataStatusIsBeingModified) {
										if (Props.isAudit()) {
											if (p.getDataStatus() != null) {
												/*asc.logAuditEntry(
														"Data status was changed from " + df.getDataStatus() + " to "
																+ p.getDataStatus(),
														df.getEquipId(), "Dataframe", userId, Props.isAudit(),
														Const.AUDIT_SUCCESS, df.getVersionNumber());*/
												AuditDetails details = asc.new AuditDetails("Data status was changed from " + df.getDataStatus() + " to "
														+ p.getDataStatus(), df, userId);
												details.setContextEntity(p);
												asc.logAuditEntryAsync(details);
											}
										}
									}

									if (restrictionStatusIsBeingModified) {
										if (Props.isAudit()) {
											if (p.getRestrictionStatus() != null) {
												/*asc.logAuditEntry("Restriction status was changed from "
														+ df.getRestrictionStatus() + " to " + p.getRestrictionStatus(),
														df.getEquipId(), "Dataframe", userId, Props.isAudit(),
														Const.AUDIT_SUCCESS, df.getVersionNumber());*/
												AuditDetails details = asc.new AuditDetails("Restriction status was changed from "
														+ df.getRestrictionStatus() + " to " + p.getRestrictionStatus(), df, userId);
												details.setContextEntity(p);
												asc.logAuditEntryAsync(details);
											}
										}
									}

									if (promotionStatusIsBeingModified) {
										if (Props.isAudit()) {
											if (p.getPromotionStatus() != null) {
												/*asc.logAuditEntry(
														"Promotion status was changed from " + df.getPromotionStatus()
																+ " to " + p.getPromotionStatus(),
														df.getEquipId(), df.getDataframeType(), userId, Props.isAudit(),
														Const.AUDIT_SUCCESS, df.getVersionNumber());*/
												AuditDetails details = asc.new AuditDetails("Promotion status was changed from " + df.getPromotionStatus()
												+ " to " + p.getPromotionStatus(), df, userId);
												details.setContextEntity(p);
												asc.logAuditEntryAsync(details);
											}
										}
									}

									if (promotionStatusIsBeingModified
											&& p.getPromotionStatus().equalsIgnoreCase("promoted")) {
										NotificationDAO ndao = new NotificationDAO();
										NotificationRequestBody body = new NotificationRequestBody();
										event_detail detail = new event_detail();
										body.setEvent_type("data_promotion");
										body.setEntity_id(df.getEquipId() + " v." + df.getVersionNumber());
										detail.setUser_name(userId);
										List<String> studyIds = df.getStudyIds();
										body.setEntity_type(df.getDataframeType());
										detail.setData_status(df.getDataStatus());
										detail.setBlinding_status(df.getDataBlindingStatus());
										List<String> notifComments = new ArrayList();

										for (Comment comment : df.getComments()) {
											notifComments.add(comment.getBody());
										}
										detail.setComments(notifComments);
										body.setEventDetail(detail);
										boolean notifyFlag = ndao.notifyEvent(body, studyIds);
										if (!notifyFlag) {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"Dataframe promoted but notification failed");
										}
									}

									// call opmeta service to update modification time on associated protocol
									try {
										OpmetaServiceClient osc = new OpmetaServiceClient();
										osc.setHost(Props.getOpmetaServiceServer());
										osc.setPort(Props.getOpmetaSerivcePort());
										List<String> studyIds = df.getStudyIds();
										for(String studyId: studyIds) {
											LOGGER.info("DataframePromotionResource: update protocol for study id=" + studyId);
											osc.updateProtocolModifiedDate(userId, studyId);
										}
									}
									catch(Exception err) {
										LOGGER.warn("DataframePromotionResource: Error updating protocol modification time", err);
									}

									returnJson = p.getId();
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
									response.header(HTTPHeaders.LOCATION, "/dataframes/" + dataframeId + "/promotions");
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataframe ID was provided.");
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noPromotionError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noPromotionError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, noPromotionError);
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && df != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Promotion attempt failed with exception: " + ex.getMessage(),
									df.getEquipId(), "Dataframe", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									df.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Promotion attempt failed with exception: " + ex.getMessage(), df, userId);
							details.setActionStatus(Const.AUDIT_SUCCESS);
							asc.logAuditEntryAsync(details);
							
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}
	};

}
