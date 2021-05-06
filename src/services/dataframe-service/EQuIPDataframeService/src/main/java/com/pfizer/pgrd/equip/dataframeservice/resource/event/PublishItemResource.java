package com.pfizer.pgrd.equip.dataframeservice.resource.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.PublishStatus;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.NotificationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditEntryContent;

public class PublishItemResource extends ServiceBaseResource {
	private static Logger log = LoggerFactory.getLogger(PublishItemResource.class);

	protected static final String PUBLISH_STATUS_UNPUBLISHED = "Unpublished";
	protected static final String PUBLISH_STATUS_PUBLISHED = "Published";

	public static final Route getList = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();
					if (jsonBody != null) {
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						List<String> idsArray = ServiceBaseResource.unmarshalObject(jsonBody, String.class);

						// List<String> ids = new ArrayList<String>();
						// String[] idsArray = jsonBody.replace("[", "").replace("]",
						// "").replace("\"","").split(",");
						// Collections.addAll(ids, idsArray);
						List<PublishItem> publishItems = new ArrayList<>();
						for (String id : idsArray) {
							ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
							PublishItem pe = dao.getPublishItem(id);
							if (pe != null) {
								if (!pe.isDeleteFlag()) {
									publishItems.add(pe);
								} else {
									Spark.halt(HTTPStatusCodes.CONFLICT,
											"Publish Item was previously deleted (id=" + id + ")");
								}
							} else {
								Spark.halt(HTTPStatusCodes.NOT_FOUND, "Publish Item does not exist (id=" + id + ")");
							}
						}

						if (Props.isAudit()) {
							for (PublishItem pi : publishItems) {
								/*asc.logAuditEntry("Access of a Publish Item", pi.getEquipId(), "PublishItem", userId,
										Props.isAudit(), Const.AUDIT_SUCCESS, pi.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Access of a Publish Item", pi, userId);
								details.setContextEntity(pi);
								asc.logAuditEntryAsync(details);
							}
						}

						json = marshalObject(publishItems);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No IDs were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
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
			String json = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			List<PublishItem> newPublishItems = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();
					if (jsonBody != null) {
						List<PublishItem> publishItems = unmarshalObject(jsonBody, PublishItem.class);

						ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
						newPublishItems = new ArrayList<>();
						Map<String, List<String>> map = new HashMap<String, List<String>>();
						// check authorization and authentication
						userId = request.headers(Const.IAMPFIZERUSERCN);
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges("publishing", "POST", userId);

						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to publish reporting event items");
						}

						for (PublishItem pi : publishItems) {

							if (pi.getCreatedBy() == null) {
								pi.setCreatedBy(userId);
							}

							List<String> list = map.get(pi.getPublishEventId()) == null ? new ArrayList<>()
									: map.get(pi.getPublishEventId());
							list.add(pi.getId());
							map.put(pi.getPublishEventId(), list);

							pi.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
							pi.setPublishedDate(new Date());

							applyVersionIncrementingLogic(pi, dao);
							ServiceBaseResource.setSubInfo(pi, userId);
							PublishItem newPi = dao.addPublishItemToReportingEventItem(pi.getReportingEventItemId(),
									pi);

							// now update the dataframe
							ReportingEventItem rei = dao.getReportingItem(pi.getReportingEventItemId());
							PropertiesPayload pp = new PropertiesPayload();
							pp.addProperty("equip:published", true);
							
							Date md = new Date();
							
							// Added 5-7-2020, Justin Quintanilla
							pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, md);
							pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, userId);
							ModeShapeDAO bdao = new ModeShapeDAO();
							bdao.updateNode(rei.getDataFrameId(), pp);
							
							// Added 11-9-2020, Justin Quintanilla
							// This helps the indexer find when the REI's publish status changes
							pp = new PropertiesPayload();
							pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, md);
							pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, userId);
							bdao.updateNode(rei.getId(), pp);
							
							// Update the reporting event
							bdao.updateModified(rei.getReportingEventId(), userId);
							EntityVersioningResource.updateAtrFlag(rei.getReportingEventId(), false, userId);
							
							PublishItemPublishStatusChangeWorkflow scw = new PublishItemPublishStatusChangeWorkflow();
							scw.setCreated(pi.getCreated() == null ? new Date() : pi.getCreated());
							scw.setCreatedBy(pi.getCreatedBy() == null ? "" : pi.getCreatedBy());
							scw.setPublishItemPublishStatusChangeDescription("Item Published");
							scw.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
							scw.setPublishItemId(newPi.getId());
							
							PublishItemPublishStatusChangeWorkflow scwNew = dao.addStatusChangeWorkflow(newPi.getId(), scw);
							newPi.getWorkflowItems().add(scwNew);

							newPublishItems.add(newPi);
						}

						for (String publishEventId : map.keySet()) {
							AssemblyDAO assemblyDAO = new AssemblyDAOImpl();
							Assembly assembly = assemblyDAO.getAssembly(publishEventId);
							assembly.setPublished(true);
							assembly.getPublishItemIds().addAll(map.get(publishEventId));
							assemblyDAO.updateAssembly(publishEventId, assembly);
						}

						if (Props.isAudit()) {
							for (PublishItem pi : newPublishItems) {
								/*asc.logAuditEntry("Creation of Publish Item", pi.getEquipId(), "PublishItem", userId,
										Props.isAudit(), Const.AUDIT_SUCCESS, pi.getVersionNumber());*/
								
								AuditDetails details = asc.new AuditDetails("Creation of Publish Item", pi, userId);
								details.setContextEntity(pi);
								asc.logAuditEntryAsync(details);
							}
						}

						json = marshalObject(newPublishItems);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Published Items were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && newPublishItems != null) {
						if (Props.isAudit()) {
							for (PublishItem pi : newPublishItems) {
								/*asc.logAuditEntry(
										"Attempt to create PublishItem failed with exception " + ex.getMessage(),
										pi.getEquipId(), "PublishItem", userId, Props.isAudit(), Const.AUDIT_FAILURE,
										pi.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Attempt to create PublishItem failed with exception " + ex.getMessage(), pi, userId);
								details.setContextEntity(pi);
								details.setActionStatus(Const.AUDIT_FAILURE);
								asc.logAuditEntryAsync(details);
							}
						}
					}
				} catch (Exception ex2) {
					log.error("", ex2); // intentionally swallowing exception, we want the original exception to be
										// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

		private void applyVersionIncrementingLogic(PublishItem pi, ReportingAndPublishingDAO dao) {
			EquipVersionableListGetter publishItemSiblingGetter = equipId -> {
				List<PublishItem> items = dao.getPublishItemsByEquipId(equipId);
				List<EquipVersionable> ev = new ArrayList<>();
				for (PublishItem item : items) {
					ev.add(item);
				}

				return ev;
			};

			new VersioningDAO().applyVersionIncrementingLogic(pi, "publishing event item", publishItemSiblingGetter);
		}
	};

	public static final Route putMultiplePublishStatus = new Route() {
		class StatusResult {
			public PublishStatus publishStatus;
			public PublishItem newPublishItem;
			public int statusCode;
			public String error;
		}
		
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String userId = request.headers(Const.IAMPFIZERUSERCN);
				if (userId != null) {
					String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
					if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
						String commentJson = request.body();
						if (commentJson != null) {
							AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
									Props.getExternalServicesPort());
							List<StatusResult> results = new ArrayList<>();
							List<PublishStatus> list = unmarshalObject(commentJson, PublishStatus.class);
							for (PublishStatus status : list) {
								if (status != null) {
									StatusResult result = new StatusResult();
									result.publishStatus = status;
									
									PublishItem newPi = null;
									try {
										newPi = PublishItemResource.putPublishStatus.handle(status, userId, false);
										result.newPublishItem = newPi;
										result.statusCode = HTTPStatusCodes.OK;
									} catch (Exception ex) {
										if(ex instanceof HaltException) {
											HaltException he = (HaltException) ex;
											result.statusCode = he.statusCode();
											result.error = he.body();
										}
										else {
											result.statusCode = HTTPStatusCodes.INTERNAL_SERVER_ERROR;
											result.error = ex.getMessage();
										}
										
										try {
											if (newPi != null && userId != null) {
												if (Props.isAudit()) {
													/*asc.logAuditEntry(
															"Attempt to change publish status to "
																	+ status.getPublishStatus()
																	+ " failed with exception: " + ex.getMessage(),
															newPi.getEquipId(), "ReportingEvent", userId,
															Props.isAudit(), Const.AUDIT_SUCCESS,
															newPi.getVersionNumber());*/
													AuditDetails details = asc.new AuditDetails("Attempt to change publish status to "
															+ status.getPublishStatus()
															+ " failed with exception: " + ex.getMessage(), newPi, userId);
													details.setContextEntity(newPi);
													
													asc.logAuditEntryAsync(details);
												}
											}
										} catch (Exception ex2) {
											log.error("", ex2); // intentionally swallowing exception, we want the
																// original exception to be reported.
										}
									}
									
									results.add(result);
								}
							}
							
							json = PublishItemResource.returnJSON(results, response);
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No comment was provided.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

	interface StatusHandler extends Route {
		public PublishItem handle(PublishStatus ps, String userId, boolean updateAdditionals) throws HaltException, ServiceCallerException;
	}

	// just set the flag, compute service will enforce locking of child dataframes
	public static final StatusHandler putPublishStatus = new StatusHandler() {
		String noCommentError = "No comment was provided.";

		@Override
		public Object handle(Request request, Response response) throws Exception {

			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			NotificationDAO ndao = new NotificationDAO();
			PublishStatus ps = null;
			String userId = null;
			PublishItem newPi = null;

			try {
				userId = request.headers(Const.IAMPFIZERUSERCN);
				if (userId != null) {
					String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
					if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
						String commentJson = request.body();
						if (commentJson != null) {
							List<PublishStatus> list = unmarshalObject(commentJson, PublishStatus.class);
							if (!list.isEmpty()) {
								ps = list.get(0);
								newPi = this.handle(ps, userId, false);
								json = marshalObject(newPi);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
								response.header(HTTPHeaders.LOCATION, "/publishitems/" + newPi.getId());
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noCommentError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noCommentError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID provided.");
				}
			} catch (Exception ex) {
				try {
					if (newPi != null && userId != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry(
									"Attempt to change publish status to " + ps.getPublishStatus()
											+ " failed with exception: " + ex.getMessage(),
									newPi.getEquipId(), "ReportingEvent", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									newPi.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Attempt to change publish status to " + ps.getPublishStatus()
							+ " failed with exception: " + ex.getMessage(), newPi, userId);
							details.setContextEntity(newPi);
							asc.logAuditEntryAsync(details);
						}
					}
				} catch (Exception ex2) {
					log.error("", ex2); // intentionally swallowing exception, we want the original exception to be
										// reported.
				}
				
				ServiceExceptionHandler.handleException(ex);
			}
			
			return json;
		}
		
		public PublishItem handle(PublishStatus ps, String userId, boolean updateAdditionals) throws HaltException, ServiceCallerException {
			PublishItem newPi = null;
			if (ps != null) {
				NotificationDAO ndao = new NotificationDAO();
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());

				ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
				PublishItem pi = dao.getPublishItem(ps.getPublishItemId());
				if (pi != null) {
					if (!pi.isDeleteFlag()) {
						if (isValidPublishStatusKey(ps.getPublishStatus())) {
							pi.setPublishStatus(ps.getPublishStatus());

							if (ps.getPublishStatus().equals(PUBLISH_STATUS_PUBLISHED)) {
								pi.setPublishedDate(ps.getModifiedDate());
							} else {
								pi.setPublishedDate(null);
							}
							if (ps.getModifiedBy() == null) {
								if (userId != null) {
									pi.setModifiedBy(userId);
								} else {
									Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "Modify User cannot be determined");
								}
							}

							// check for privileges to do this:
							AuthorizationDAO auth = new AuthorizationDAO();
							boolean isOk;
							if (ps.getPublishStatus().equals(PUBLISH_STATUS_PUBLISHED)) {
								isOk = auth.checkPrivileges("publishing", "PUT", userId);
							} else {
								isOk = auth.checkPrivileges("reporting event", "PUT", userId);
							}
							// check for lock of reporting event
							AssemblyDAO adao = getAssemblyDAO();
							ReportingEventItem rei = dao.getReportingEventItemFromPublishItem(pi);
							Assembly a = adao.getAssembly(rei.getReportingEventId());
							if (a.getLockedByUser() != null) {
								if (!a.getLockedByUser().equalsIgnoreCase(userId)) {
									Spark.halt(HTTPStatusCodes.FORBIDDEN, "This reporting event is locked by user "
											+ a.getLockedByUser() + " so statuses cannot be updated");
								}
							}

							if (!isOk) {
								Spark.halt(HTTPStatusCodes.FORBIDDEN,
										"User " + userId + " does not have privileges to update the publish status");
							}

							if (!(ps.getPublishedDate() == null) && !ps.getPublishedDate().trim().isEmpty()) {
								pi.setPublishedDate(DateUtils.parseDate(ps.getPublishedDate()));
							}
							
							if(updateAdditionals) {
								pi.setExpirationDate(ps.getExpirationDate());
								pi.setPublishedViewFilterCriteria(ps.getPublishedViewFilterCriteria());
								pi.getComments().addAll(ps.getComments());
								
								CommentDAO cDao = ModeShapeDAO.getCommentDAO();
								for(Comment c : ps.getComments()) {
									cDao.insertComment(c, pi.getId());
								}
							}
							
							pi = dao.updatePublishItem(ps.getPublishItemId(), pi);

							PublishItemPublishStatusChangeWorkflow scw = new PublishItemPublishStatusChangeWorkflow();
							scw.setCreated(ps.getModifiedDate());
							scw.setCreatedBy(ps.getModifiedBy());
							// passed across the wire
							scw.setPublishItemPublishStatusChangeDescription(
									"Publish status changed to " + ps.getPublishStatus());
							scw.setPublishStatus(ps.getPublishStatus());
							scw.setPublishItemId(ps.getPublishItemId());
							List<Comment> comments = scw.getComments();
							comments.addAll(ps.getComments());
							
							dao.insertPublishItemPublishStatusChangeWorkflow(pi.getId(), scw);

							updateDataframeStatus(rei, userId);
							updateAssemblyStatus(rei, userId);
							
							// Added 11-9-2020, Justin Quintanilla
							// Support search finding updated REIs
							ModeShapeDAO msDao = new ModeShapeDAO();
							msDao.updateModified(rei.getId(), userId);
							
							EntityVersioningResource.updateAtrFlag(a, false, userId);
							
							String dfEquipId = null;
							long dfv = 0L;
							Dataframe df =null;
							if(rei.getDataFrameId() != null) {
								DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
								df = ddao.getDataframe(rei.getDataFrameId());
								if(df != null) {
									dfEquipId = df.getEquipId();
									dfv = df.getVersionNumber();
								}
							}

							if (Props.isAudit()) {
								if (rei != null) {
									/*asc.logAuditEntry(
											"Changed publish status to " + ps.getPublishStatus()
													+ "for reporting event item id=" + rei.getId(),
											dfEquipId, "Dataframe", userId, Props.isAudit(),
											Const.AUDIT_SUCCESS, dfv);*/
									AuditDetails details = asc.new AuditDetails("Changed publish status to " + ps.getPublishStatus()
									+ "for reporting event item id=" + rei.getId(), df, userId);
									AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
									
									Assembly reportingEvent = aDao
											.getAssembly(rei.getReportingEventId());
									
									AuditEntryContent detailsContent = asc.new AuditEntryContent();
									detailsContent.setReportingEventId(reportingEvent.getEquipId());
									detailsContent.setReportingEventType(reportingEvent.getItemType());
									details.setAuditContentEntity(detailsContent);
									//details.setContextEntity(rei);
									asc.logAuditEntryAsync(details);
								}
							}

							// notify if publish status = published
							if (ps.getPublishStatus().equals(PUBLISH_STATUS_PUBLISHED)) {
								if (Props.isAudit()) {
									if (Props.isAuditParametersPublished()) {
										if (pi.getPublishedViewFilterCriteria() != null
												&& !pi.getPublishedViewFilterCriteria().isEmpty()) {

											DataframeDAO dfDAO = new DataframeDAOImpl();
											df = dfDAO.getDataframe(rei.getDataFrameId());

											if (df != null) {
												if (df.getDataframeType().equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
													AssemblyDAO assemblyDAO = new AssemblyDAOImpl();
													List<Assembly> assemblies = assemblyDAO
															.getAssemblyByMemberDataframeId(df.getId());

													String assemblyId = null;
													if (assemblies != null && assemblies.size() > 0) {
														assemblyId = assemblies.get(0).getId();
														/*asc.logAuditEntry("Primary parameters published",
																df.getEquipId(), "Dataframe", userId, Props.isAudit(),
																Const.AUDIT_SUCCESS, df.getVersionNumber(), assemblyId,
																null, pi.getPublishedViewFilterCriteria());*/
														AuditDetails details = asc.new AuditDetails("Primary parameters published", df, userId);
														details.setContextEntity(assemblies.get(0));
														details.setContent(pi.getPublishedViewFilterCriteria());
														asc.logAuditEntryAsync(details);
													}
												}
											}
										}
									}
								}

								NotificationRequestBody body = new NotificationRequestBody();
								notifyPublishStatusPublished(rei, ndao, userId, dao, pi, comments, body);
							}

							// call opmeta service to update modification time on associated protocol
							try {
								OpmetaServiceClient osc = new OpmetaServiceClient();
								osc.setHost(Props.getOpmetaServiceServer());
								osc.setPort(Props.getOpmetaSerivcePort());
								List<String> studyIds = a.getStudyIds();
								for (String studyId : studyIds) {
									log.info("PublishItemResource: update protocol for study id=" + studyId);
									osc.updateProtocolModifiedDate(userId, studyId);
								}
							} catch (Exception err) {
								log.warn("PublishItemResource: Error updating protocol modification time for node "
										+ a.id, err);
							}

							newPi = dao.getPublishItem(pi.getId());
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST,
									"Invalid publish status (" + ps.getPublishStatus() + ") for id = "
											+ ps.getPublishItemId() + ".  Valid publish statuses are: "
											+ PUBLISH_STATUS_PUBLISHED + ", " + PUBLISH_STATUS_UNPUBLISHED);
						}
					} else {
						Spark.halt(HTTPStatusCodes.CONFLICT,
								"Publish Item '" + ps.getPublishItemId() + "' is deleted.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.NOT_FOUND,
							"No Publish Item with ID '" + ps.getPublishItemId() + "' could be found.");
				}
			} else {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, noCommentError);
			}

			return newPi;
		}

		private void notifyPublishStatusPublished(ReportingEventItem rei, NotificationDAO ndao, String userId,
				ReportingAndPublishingDAO dao, PublishItem pi, List<Comment> comments, NotificationRequestBody body) {
			event_detail detail = new event_detail();
			body.setEvent_type("reporting_event_published");

			DataframeDAO ddao = getDataframeDAO();
			Dataframe df = ddao.getDataframe(rei.getDataFrameId());
			AssemblyDAO adao = getAssemblyDAO();
			List<Assembly> aList = adao.getAssemblyByReportingEventItemId(rei.getId());
			Assembly a = aList.get(0); // there should only be one assembly returned
			body.setEntity_id(df.getEquipId() + " v." + df.getVersionNumber());
			detail.setUser_name(userId);
			detail.setReporting_event_id(a.getEquipId());
			detail.setReporting_event_type(a.getAssemblyType());
			detail.setPublishing_event_expiration_date(pi.getExpirationDate());
			detail.setConcentration_data_status(df.getDataStatus());
			detail.setData_status(df.getDataStatus());
			List<String> studyIds = df.getStudyIds();
			body.setEntity_type(df.getDataframeType());

			if (df.getDataframeType().equalsIgnoreCase("primary parameters")) {
				detail.setParameter_data_qc_status(df.getQcStatus());
			}
			detail.setData_status(df.getDataStatus());
			detail.setPublishing_event_expiration_date(pi.getExpirationDate());
			List<String> notifComments = new ArrayList<>();
			
			for (Comment comment : comments) {
				notifComments.add(comment.getBody());
			}
			detail.setComments(notifComments);
			body.setEventDetail(detail);
			
			boolean notifyFlag = false;
			try {
				notifyFlag = ndao.notifyEvent(body, studyIds);
			}
			catch(Exception e) {
				e.printStackTrace();
				log.error("Error when notifying about reporting publish status.", e);
			}
			
			if (!notifyFlag) {
				log.error("Publish status updated but notification failed");
			}
		}

		private boolean isValidPublishStatusKey(String publishStatus) {
			return publishStatus.equals(PUBLISH_STATUS_PUBLISHED) || publishStatus.equals(PUBLISH_STATUS_UNPUBLISHED);
		}

		private void updateDataframeStatus(ReportingEventItem rei, String userId) {
			// after status change is complete, see if the df needs updating
			DataframeDAO ddao = getDataframeDAO();
			VersioningDAO vdao = new VersioningDAO();
			Dataframe df = ddao.getDataframe(rei.getDataFrameId());
			Boolean isPublished = vdao.isREIPublished(df.getId()); // is the status still published?
			PropertiesPayload pp = new PropertiesPayload();
			pp.addProperty("equip:published", isPublished);
			//pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());
			//pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, userId);
			// do we want to add user name and date here?
			ModeShapeDAO bdao = new ModeShapeDAO();
			bdao.updateNode(rei.getDataFrameId(), pp);
		}

		private void updateAssemblyStatus(ReportingEventItem rei ,String userId) {
			// after status change is complete, see if the assembly needs updating
			AssemblyDAO adao = getAssemblyDAO();

			// ReportingAndPublishingDAO rdao = getReportingAndPublishingDAO();
			List<Assembly> aList = adao.getAssemblyByReportingEventItemId(rei.getId());

			// Assembly a = adao.getAssembly(rei.getReportingEventId());
			if (!aList.isEmpty()) {
				for (Assembly a : aList) {
					VersioningDAO vdao = new VersioningDAO();
					Boolean isPublished = vdao.isREPublished(a.getId());
					
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty("equip:published", isPublished);
					//pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());
					//pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, userId);
					ModeShapeDAO bdao = new ModeShapeDAO();
					bdao.updateNode(a.getId(), pp);
				}
			}
		}

	};

	// no longer necessary but here as an example of a call into the dao to make a
	// mode shape query
	// public static Route getPublishItemViaReportingEventId = new Route(){
	// @Override
	// public Object handle(Request request, Response response) throws Exception {
	// String reportingEventItemId = request.params(":rid");
	//
	// ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
	// ReportingItem ri = dao.getReportingItem(reportingEventItemId);
	//
	// if(ri == null){
	// Spark.halt(400, "Reporting Event item with id " + reportingEventItemId + "
	// does not exist.");
	// }
	//
	// if(ri.isDeleteFlag()){
	// Spark.halt(400, "Reporting Event item with id " + reportingEventItemId + "
	// was previously deleted" );
	// }
	//
	// PublishItem pi =
	// dao.getPublishItemViaReportingEventItemId(reportingEventItemId);
	//
	// if(pi == null){
	// Spark.halt(400, "Publish item for reporting event id " + reportingEventItemId
	// + " does not exist.");
	// }
	//
	// if(pi.isDeleteFlag()){
	// Spark.halt(400, "Publish item for reporting event id " + reportingEventItemId
	// + " was previously deleted" );
	// }
	//
	// return marshalObject(pi);
	// }
	// };
}
