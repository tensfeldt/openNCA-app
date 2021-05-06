
package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipLockable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.EquipIDDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.NotificationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.AnalysisDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.AssemblyDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.BatchDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.CommentDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.DataframeDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.MetadatumDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.dto.ReportingEventItemDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipLock;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.computeservice.dto.Parameter;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class EntityVersioningResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityVersioningResource.class);
	public static final String DELETE_ACTION = "delete";
	public static final String COMMIT_ACTION = "commit";
	public static final String SUPERSEDE_ACTION = "supersede";
	public static final String LOCK_ACTION = "setlock";
	public static final String UPDATE_ACTION = "update";

	public interface EntityVersioningActionHandler extends Route {
		public Object handle(String nodeId, String action, String userCN, String requestBody, Response response)
				throws Exception;
	}
	
	public static final boolean lockedByUser(String userId, EquipObject object) {
		if(userId != null && object != null && object instanceof EquipLockable) {
			return EntityVersioningResource.lockedByUser(userId, (EquipLockable) object);
		}
		
		return true;
	}
	
	public static final boolean lockedByUser(String userId, EquipLockable object) {
		if(object != null && object.isLocked()) {
			if(object.getLockedByUser() == null || !object.getLockedByUser().equalsIgnoreCase(userId)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static final boolean lockedByUser(String userId, EquipLock object) {
		if(object != null && object.isLocked()) {
			if(object.getLockedByUser() == null || !object.getLockedByUser().equalsIgnoreCase(userId)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static final boolean lockedByUser(String userId, ModeShapeNode node) {
		if(node != null && userId != null) {
			if(node instanceof EquipLock) {
				return EntityVersioningResource.lockedByUser(userId, (EquipLock) node);
			}
		}
		
		return true;
	}

	public static final Route deleteMultiple = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String userId = request.headers("IAMPFIZERUSERCN");

			try {
				if (userId != null) {
					if (request.body() != null) {
						String body = request.body();
						List<Result> results = new ArrayList<>();

						List<String> ids = ServiceBaseResource.unmarshalObject(body, String.class);
						for (String id : ids) {
							if (id != null) {
								Result r = new Result();
								r.id = id;

								try {
									EntityVersioningResource.put.handle(id, "delete", userId, null, null);
									r.statusCode = HTTPStatusCodes.OK;
									r.deleted = true;
								} catch (HaltException e) {
									r.statusCode = e.statusCode();
									r.error = e.body();
								}

								results.add(r);
							}
						}

						json = EntityVersioningResource.returnJSON(results, response);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No request body was provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}

		class Result {
			public String id;
			public int statusCode;
			public boolean deleted;
			public String error;
		}
	};

	/**
	 * A {@link Route} that will commit, supersede, delete or obsolete an existing
	 * entity
	 */
	public static final EntityVersioningActionHandler put = new EntityVersioningActionHandler() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			return this.handle(request.params(":id"), request.params(":action"), request.headers("IAMPFIZERUSERCN"),
					request.body(), response);
		}
		
		@Override
		public Object handle(String nodeId, String action, String userCN, String requestBody, Response response) throws Exception {
			return this.handle(nodeId, action, userCN, requestBody, response, null);
		}

		private Object handle(String nodeId, String action, String userCN, String requestBody, Response response, EquipObject context)
				throws Exception {
			String outcome = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			boolean deleteAction = false;
			boolean updateAction = false;

			boolean obsoleteIsOccurring = false;
			boolean deleteIsOccurring = false;
			boolean unDeleteIsOccurring = false;
			boolean updateIsOccurring = false;
			boolean commitIsOccurring = false;
			
			boolean nominalDataChangeIsOccuring = false;
			Map<String, Object> mapUdatesForAudit = new HashMap<String, Object>();
			String forAuditingQCStatusThatIsBeingSet = null;
			NotificationDAO ndao = new NotificationDAO();
			boolean qcNotifNeeded = false;
			boolean dataloadNotifNeeded = false;
			boolean analysisParamNotifNeeded = false;
			boolean dfParamNotifNeeded = false;
			boolean analysisChangeNotifNeeded = false;
			List<String> studyIds = new ArrayList<String>(); // this is needed for the notifications
			String notifId = ""; // needed for notification
			boolean notifyFlag = true;
			Boolean lock = null;

			EquipObject entity = null;
			com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID equipIdObject = null;
			EquipVersionable equipVersionableObject = null;

			try {
				if (nodeId != null && action != null) {
					String equipId = "";

					ModeShapeDAO dao = new ModeShapeDAO();
					ModeShapeNode node = dao.getNode(nodeId);
					AuthorizationDAO auth = new AuthorizationDAO();

					if (node != null) {
						if (Props.isAudit()) {
							entity = dao.getEquipObject(nodeId);
							if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
									&& entity instanceof EquipVersionable) {
								equipIdObject = (com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID) entity;
								equipVersionableObject = (EquipVersionable) entity;
							}
						}
						
						deleteAction = action.equalsIgnoreCase(DELETE_ACTION);
						boolean obsoleteAction = action.equalsIgnoreCase("obsolete");
						boolean commitAction = action.equalsIgnoreCase(COMMIT_ACTION);
						boolean supersedeAction = action.equalsIgnoreCase(SUPERSEDE_ACTION);
						boolean lockAction = action.equalsIgnoreCase(LOCK_ACTION);
						boolean qcChange = false;
						updateAction = action.equalsIgnoreCase(UPDATE_ACTION);

						if (node instanceof EquipID) {
							equipId = ((EquipID) node).getEquipId();
						}
						
						PropertiesPayload payload = new PropertiesPayload();
						// check authentication
						if (userCN == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						} else {
							ServiceBaseResource.handleUserAccess(node, userCN);
						}
						
						// Check to make sure the parent entity is not locked.
						boolean parentLocked = this.parentIsLocked(node, userCN);
						if(parentLocked) {
							Spark.halt(HTTPStatusCodes.CONFLICT, "The parent entity is locked by another user.");
						}

						// payload object to get the properties to update (if any)
						Boolean qcOnly = false;
						Boolean lockOnly = false;
						PropertiesPayload props = PropertiesPayload.unmarshal(requestBody);
						if (props != null) {
							if (props.properties.size() == 1 && props.properties.containsKey(("equip:qcStatus"))) {
								qcOnly = true;
							}
							if (props.properties.size() == 1 && props.properties.containsKey(ModeShapeNode.CommonProperties.LOCKED_BY_USER)) {
								lockOnly = true;
							}
							
						}

						// now check authorization
						boolean isOk = false;
						if (deleteAction) {
							isOk = auth.checkPrivileges("delete", "PUT", userCN);
							
							// If this user is not allowed to delete, but the item he/she is trying to delete 
							// is a reporting event or reporting event item that he/she created, he/she should be allowed to
							if(!isOk) {
								String createdBy = null;
								EquipObject eo = node.toEquipObject();
								if(eo instanceof ReportingEventItem) {
									ReportingEventItem rei = (ReportingEventItem) eo;
									createdBy = rei.getCreatedBy();
								}
								else if(eo instanceof Assembly) {
									Assembly a = (Assembly) eo;
									if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
										createdBy = a.getCreatedBy();
									}
								}
								
								if(createdBy != null && createdBy.equalsIgnoreCase(userCN)) {
									isOk = auth.checkPrivileges("reporting event", "POST", userCN);
								}
							}
						} else {
							if (!qcOnly && !lockOnly) {
								if (node.getPrimaryType().equalsIgnoreCase(DataframeDTO.PRIMARY_TYPE)) {
									Dataframe df = (Dataframe) node.toEquipObject();
									isOk = auth.checkPrivileges(df.getDataframeType(), "PUT", userCN);
								} else if (node.getPrimaryType().equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)) {
									Assembly a = (Assembly) node.toEquipObject();
									isOk = auth.checkPrivileges(a.getAssemblyType(), "PUT", userCN);
								} else if (node.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
									isOk = auth.checkPrivileges("analysis", "PUT", userCN);
								} else {
									isOk = true;
								}
							} else {
								isOk = true; // if this is an update to qcstatus, those privileges will be checked later
							}
						}
						
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userCN + " does not have privileges to " + action + " this object");
						}

						if (deleteAction || obsoleteAction) {
							if (node instanceof EquipDelete) {
								if (deleteAction) {
									// check first
									EquipDelete edn = (EquipDelete) node;

									if (!edn.isDeleted()) {
										LOGGER.info("Performing checks for deleting node " + node.getJcrId() + ".");
										long start = System.currentTimeMillis();
										EquipObject equipObject = node.toEquipObject();
										EquipObject parent = null;
										if(equipObject instanceof Comment || equipObject instanceof Metadatum) {
											String parentPath = node.getUp();
											ModeShapeNode pn = dao.getNodeByPath(parentPath);
											if(pn != null) {
												parent = pn.toEquipObject();
											}
										}
										else if(equipObject instanceof Dataframe) {
											Dataframe df = (Dataframe) equipObject;
											String dfType = df.getDataframeType();
											if(dfType != null && dfType.equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
												String parentId = df.getParentIds().get(0);
												parent = dao.getEquipObject(parentId);
											}
										}
										
										if(parent != null) {
											AuthorizationDAO authDao = new AuthorizationDAO();
											if(!authDao.canModifyExtras(parent, userCN)) {
												Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userCN + " may not remove this entity from node " + parent.getId() + ".");
											}
										}
										
										String blockingId = EntityVersioningResource.canDeleteItem(equipObject);
										if (blockingId == null) {
											deleteIsOccurring = true;
											String primaryType = node.getPrimaryType();
											if (primaryType.equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)) {
												Assembly a = (Assembly) node.toEquipObject();
												if (a.getAssemblyType().equalsIgnoreCase("Data Load")) {
													// delete associated dataframes
													VersioningDAO vdao = new VersioningDAO();
													vdao.deleteAssociatedDataframes(a);
												} else if (a.getAssemblyType()
														.equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
													EntityVersioningResource.handleReportingEvent(a, true, userCN);
												}
											} else if (primaryType.equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
												EntityVersioningResource.handleAnalysis(node, true, userCN);
											}
											
											// need to check that node is not new version of a superseded node
											// if yes we need to roll back on delete
											if (node.getPrimaryType().equalsIgnoreCase(DataframeDTO.PRIMARY_TYPE)) {
												DataframeDAO ddao = getDataframeDAO();

												Dataframe df = ddao.getDataframe(node.getJcrId());
												VersioningDAO vdao = new VersioningDAO(df);
												if (!df.isCommitted()) {
													vdao.unSupersedePrevious(df);
												}

												// Handle ATR and Analysis QC reports
												if (df.getSubType() != null) {
													String subType = df.getSubType();
													if (subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
														// Handle ATR
														ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
														List<ReportingEventItem> items = rpDao
																.getReportingEventItemByDataframeId(df.getId());
														if (items.size() == 1) {
															AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
															ReportingEventItem item = items.get(0);
															Assembly reportingEvent = aDao
																	.getAssembly(item.getReportingEventId());
															if (reportingEvent != null) {
																String failureReason = "The ATR report cannot be deleted because either the Reporting Event has been Released or one or more Reporting Event Items have been Published.";
																String releaseStatus = reportingEvent
																		.getReleaseStatus();
																if (releaseStatus != null && releaseStatus
																		.equalsIgnoreCase(Const.RELEASED_STATUS)) {
																	Spark.halt(HTTPStatusCodes.CONFLICT, failureReason);
																}

																items = rpDao.getReportingItem(
																		reportingEvent.getReportingItemIds());
																for (ReportingEventItem rei : items) {
																	PublishItem pi = rei.getPublishItem();
																	if (pi != null && pi.getPublishStatus() != null
																			&& pi.getPublishStatus().equalsIgnoreCase(
																					Const.PUBLISHED_STATUS)) {
																		Spark.halt(HTTPStatusCodes.CONFLICT,
																				failureReason);
																	}
																}
															}
															
															// Delete the REI
															ModeShapeDAO msDao = new ModeShapeDAO();
															PropertiesPayload ppl = new PropertiesPayload();
															ppl.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG,
																	true);
															ppl.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY,
																	userCN);
															ppl.addProperty(
																	ModeShapeNode.CommonProperties.MODIFIED_DATE,
																	new Date());
															msDao.updateNode(item.getId(), ppl);
															
															// Update the reporting event modified date
															msDao.updateModified(reportingEvent.getId(), userCN);
															EntityVersioningResource.updateAtrFlag(reportingEvent, false, userCN);
														}
													} else if (subType
															.equalsIgnoreCase(Dataframe.ANALYSIS_QC_REPORT_SUB_TYPE)) {
														// Handle Analysis QC
														AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
														List<Assembly> assemblies = aDao
																.getAssembly(df.getAssemblyIds());
														for (Assembly a : assemblies) {
															if (a instanceof Analysis) {
																Analysis an = (Analysis) a;
																String paramsId = an.getParametersDataframeId();
																if (paramsId != null) {
																	ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
																	List<ReportingEventItem> items = rpDao
																			.getReportingEventItemByDataframeId(
																					paramsId);
																	for (ReportingEventItem item : items) {
																		PublishItem pi = item.getPublishItem();
																		if (pi != null && pi.getPublishStatus() != null
																				&& pi.getPublishStatus()
																						.equalsIgnoreCase(
																								Const.PUBLISHED_STATUS)) {
																			Spark.halt(HTTPStatusCodes.CONFLICT,
																					"Analysis QC Report cannot be deleted because Parameters have been published.");
																		}
																	}
																}
																
																break;
															}
														}
													}
												}
											}
											
											// Update the reporting event modified fields if deleting a REI.
											if(node.getPrimaryType().equalsIgnoreCase(ReportingEventItemDTO.PRIMARY_TYPE)) {
												ReportingEventItem rei = (ReportingEventItem) node.toEquipObject();
												ModeShapeDAO msDao = new ModeShapeDAO();
												msDao.updateModified(rei.getReportingEventId(), userCN);
												
												// If the dataframe reference by the REI is not an ATR or if it is an ATR and its not superseded,
												// update the ATR flag of the RE.
												if(rei.getDataFrameId() != null) {
													DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
													Dataframe df = dDao.getDataframe(rei.getDataFrameId());
													if(df != null) {
														String subType = df.getSubType();
														if(subType == null || (subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE) && !df.getVersionSuperSeded())) {
															EntityVersioningResource.updateAtrFlag(rei.getReportingEventId(), false, userCN);
														}
													}
												}
											}
											
											payload.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, true);
										} else {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"The object is used by " + blockingId + ", which is not deleted.");
										}

										long end = System.currentTimeMillis();
										long dt = end - start;
										LOGGER.info("Took " + dt + "ms to perform checks for deleting "
												+ node.getJcrId() + ".");
									} else {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"The object is already marked for deletion.");
									}

								} else {
									obsoleteIsOccurring = true;
									payload.addProperty("equip:obsoleteFlag", true);
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not deletable.");
							}
						}

						else if (commitAction || supersedeAction) {
							if (node instanceof EquipVersion) {
								EquipVersion version = (EquipVersion) node;
								if (commitAction) {
									EquipCreated ec = (EquipCreated) node;
									if(ec.getCreatedBy() != null && !ec.getCreatedBy().equalsIgnoreCase(userCN)) {
										Spark.halt(HTTPStatusCodes.CONFLICT, "Entity " + ((EquipID) node).getEquipId() + " v" + version.getVersionNumber() + " was created by another user and can only be committed by that user.");
									}
									
									// check first
									if (version.isCommitted() || version.isSuperseded() || ((EquipDelete) node).isDeleted()) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Node " + ((EquipID) node).getEquipId()
												+ " v" + version.getVersionNumber()
												+ " cannot be committed. It is already committed, superseded or marked for deletion.");
									} else {
										version.setCommitted(true);
										
										payload.addProperty("equip:versionCommitted", true);
										//payload.addProperty(ModeShapeNode.CommonProperties.LOCKED_BY_USER, ""); // removed lockedByUser name when committing
										//payload.addProperty(ModeShapeNode.CommonProperties.LOCK_FLAG, false);
										
										// do we need to notify?
										if (node.getPrimaryType().equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)) {
											AssemblyDAO adao = getAssemblyDAO();
											Assembly a = adao.getAssembly(node.getJcrId());
											studyIds = a.getStudyIds();
											notifId = a.getVersionNumber() > 1 ? equipId + " v." + a.getVersionNumber()
													: equipId;
											dataloadNotifNeeded = a.getAssemblyType().equalsIgnoreCase("data load");

											analysisChangeNotifNeeded = a.getAssemblyType().equalsIgnoreCase("analysis")
													&& a.getVersionNumber() > 1;
										} else if (node.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
											AnalysisDAO adao = getAnalysisDAO();
											Analysis an = adao.getAnalysis(node.getJcrId());
											studyIds = an.getStudyIds();
											notifId = equipId + " v." + an.getVersionNumber();
											analysisParamNotifNeeded = (an.getParametersDataframeId() != null)
													&& (!an.getParametersDataframeId().isEmpty());
											analysisChangeNotifNeeded = an.getVersionNumber() > 1;

										} else if (node.getPrimaryType().equalsIgnoreCase(DataframeDTO.PRIMARY_TYPE)) {
											DataframeDAO ddao = getDataframeDAO();
											Dataframe df = ddao.getDataframe(node.getJcrId());
											studyIds = df.getStudyIds();
											notifId = equipId + " v." + df.getVersionNumber();
											dfParamNotifNeeded = (df.getDataframeType()
													.equalsIgnoreCase("derived parameters"));

										}

										// need to delete the other nodes with this equipId

										VersioningDAO vdao = new VersioningDAO((EquipVersion) node, equipId);
										// only siblings that are dataframes will be returned and deleted by this
										// routine
										if (!vdao.deleteSiblings(((EquipVersion) node).getVersionNumber(),
												node.getJcrId())) {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"Unable to delete related nodes for " + equipId + ".");
										}
										commitIsOccurring = true;

									}
								} else { // supersede
									// check first
									EquipObject equipObject = node.toEquipObject();
									EquipObject parent = null;
									if(equipObject instanceof Dataframe) {
										Dataframe df = (Dataframe) equipObject;
										String dfType = df.getDataframeType();
										if(dfType != null && dfType.equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
											String parentId = df.getParentIds().get(0);
											parent = dao.getEquipObject(parentId);
										}
									}
									
									if(parent != null) {
										AuthorizationDAO authDao = new AuthorizationDAO();
										if(!authDao.canModifyExtras(parent, userCN)) {
											Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userCN + " may not supersede attachments.");
										}
									}
									
									boolean locked = false;
									if (node instanceof EquipLock) {
										EquipLock el = (EquipLock) node;
										locked = el.isLocked() && !el.getLockedByUser().equalsIgnoreCase(userCN);
									}

									if (version.isSuperseded() && response != null) {
										response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
										outcome = "Node " + action + " was successful.";
										return (outcome);
									} else if (!version.isCommitted() || ((EquipDelete) node).isDeleted() || locked) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object cannot be superseded.");
									} else {
										// supersede downstream nodes for dataframe or assembly
										// will also check if its associated with released re
										VersioningDAO vdao = new VersioningDAO(version, equipId);
										String primaryType = node.getPrimaryType();
										if (primaryType.equalsIgnoreCase(DataframeDTO.PRIMARY_TYPE)) {
											Dataframe dataframe = ((DataframeDTO) node).toDataframe();
											// first check if referenced by any released REs
											if (dataframe.getSubType() == null || !dataframe.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
												if (vdao.isREReleased(dataframe.getId())) {
													Spark.halt(HTTPStatusCodes.BAD_REQUEST,
															"Unable to supersede dataframe for " + equipId
																	+ " because it references released Reporting Events.");
												}
											}

											if (!vdao.supersedeDownstream(dataframe, userCN)) {
												Spark.halt(HTTPStatusCodes.BAD_REQUEST,
														"Unable to supersede downstream nodes for " + equipId + ".");
											}
										} else if (primaryType.equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)
												|| primaryType.equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)
												|| primaryType.equalsIgnoreCase(BatchDTO.PRIMARY_TYPE)) {
											Assembly assembly = null;
											if (primaryType.equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)) {
												assembly = ((AssemblyDTO) node).toAssembly();
											} else if (primaryType.equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
												AnalysisDAO adao = ModeShapeDAO.getAnalysisDAO();
												assembly = adao.getAnalysis(node.getJcrId());
											}
											else if (primaryType.equalsIgnoreCase(BatchDTO.PRIMARY_TYPE)) {
												assembly = ((BatchDTO) node).toBatch();
											}
											
											if (!vdao.supersedeDownstream(assembly, userCN)) {
												Spark.halt(HTTPStatusCodes.BAD_REQUEST,
														"Unable to supersede downstream nodes for " + equipId + ".");
											}
										} else {
											// not a dataframe or assembly
											version.setSuperseded(true);
											payload.addProperty("equip:versionSuperSeded", true);
										}
									}
								}

							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not versionable.");
							}
						} else if (lockAction) {
							if (node instanceof EquipLock) {
								EquipLock el = (EquipLock) node;
								EquipVersion version = (EquipVersion) node;
								if (!version.isCommitted() || version.isSuperseded()
										|| ((EquipDelete) node).isDeleted()) {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST,
											"The object cannot be locked in its current state.");
								}
								
								if(el.getLockedByUser() == null || el.getLockedByUser().trim().isEmpty() ||  el.getLockedByUser().equalsIgnoreCase(userCN)) {
									payload.addProperty(ModeShapeNode.CommonProperties.LOCK_FLAG, true);
									payload.addProperty(ModeShapeNode.CommonProperties.LOCKED_BY_USER, userCN);
									lock = true;
								}
								else {
									Spark.halt(HTTPStatusCodes.CONFLICT, "The object is already locked by another user.");
								}
								
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not lockable.");
							}
						} else if (updateAction) {
							updateIsOccurring = true;
							// new payload object to get the properties to update
							props = PropertiesPayload.unmarshal(requestBody);
							// check if each property is allowed, if it is then add it to the existing
							// payload
							if (props != null) {
								// List<String> allowedUpdates = allowedPropertyUpdates();
								
								EquipLock equipLock = null;
								if(node instanceof EquipLock) {
									equipLock = (EquipLock) node;
								}
								
								// now check authorization for special cases
								if (props.properties.containsKey("equip:qcStatus")) {
									isOk = auth.checkPrivileges("qcstatus", "PUT", userCN);
									if (!isOk) {
										Spark.halt(HTTPStatusCodes.FORBIDDEN,
												"User " + userCN + " does not have privileges to update qc status");
									}
									forAuditingQCStatusThatIsBeingSet = props.properties.get("equip:qcStatus")
											.toString();
									// will need to set a notification for qc status complete
									if (props.getString("equip:qcStatus").equalsIgnoreCase("qc'd")) {
										qcNotifNeeded = true;
									}
									
									qcChange = true;
								}
								
								// If the node is locked, forbid any property except unlock
								if(!qcChange) {
									if(!props.properties.containsKey(ModeShapeNode.CommonProperties.LOCK_FLAG)) {
										if(equipLock != null) {
											if (equipLock.isLocked() && !equipLock.getLockedByUser().equalsIgnoreCase(userCN)) {
												Spark.halt(HTTPStatusCodes.FORBIDDEN, "This object is locked and cannot be modified");
											}
										}
									}
									else {
										if(equipLock != null && equipLock.getLockedByUser() != null && !equipLock.getLockedByUser().trim().isEmpty() && !equipLock.getLockedByUser().equalsIgnoreCase(userCN)) {
											Spark.halt(HTTPStatusCodes.CONFLICT, "The object is locked by another user.");
										}
										
										lock = Boolean.parseBoolean(props.properties.get(ModeShapeNode.CommonProperties.LOCK_FLAG).toString());
										props.properties.put(ModeShapeNode.CommonProperties.LOCK_FLAG, lock);
										String lbu = userCN;
										if(!lock) {
											lbu = "";
										}
										
										props.properties.put(ModeShapeNode.CommonProperties.LOCKED_BY_USER, lbu);
										
										lockAction = true;
										payload = props;
									}
								}
								
								// if we are changing the lock status we dont want to add a modified date and modified by
								if (props.properties.containsKey(ModeShapeNode.CommonProperties.LOCKED_BY_USER)) {
									// check auth if its a reporting event
									if (node.getPrimaryType().equalsIgnoreCase("equip:assembly")) {
										Assembly a = (Assembly) node.toEquipObject();
										isOk = true;
										if (a.getAssemblyType().toLowerCase().equals("reporting event")) {
											isOk = auth.checkPrivileges("re lock", "LOCK", userCN);
										}
										if (!isOk) {
											// end the code here = do not update payload
											payload = null;
											return ("Node was not locked because user lacks privileges");
										}
									}
									
									String lbu = (String) props.properties.get(ModeShapeNode.CommonProperties.LOCKED_BY_USER);
									lock = lbu != null && !lbu.trim().isEmpty();
									props.properties.put(ModeShapeNode.CommonProperties.LOCK_FLAG, lock);
									
									lockAction = true;
									payload = props;
								}
								
								for (Map.Entry<String, Object> prop : props.properties.entrySet()) {
									// allowing all properties for now
									// if (allowedUpdates.contains(prop.getKey().toLowerCase())) {
									payload.addProperty(prop.getKey(), prop.getValue());
									mapUdatesForAudit.put(prop.getKey(), prop.getValue());
									// }
								}
								
								// check for operational metadata
								if ((props.properties.containsKey("equip:protocolIds"))
										|| (props.properties.containsKey("equip:programIds"))
										|| (props.properties.containsKey("equip:projectIds"))) {
									isOk = auth.checkPrivileges("operational data", "PUT", userCN);
									if (!isOk) {
										Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userCN
												+ " does not have privileges to update operational metadata");
									}
									// also check here for downstream dependencies
									// is there a publishing or reporting event for this entity?
									VersioningDAO vdao = new VersioningDAO();
									
									if (!vdao.checkDownstreamReporting(nodeId)) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"This entity has downstream dependencies so its operational metadata cannot be changed");
									}
								}

								// check for parametersDataframeId - this is an audit trigger
								if (props.properties.containsKey("equip:parametersDataframeId")) {
									nominalDataChangeIsOccuring = true;
								}
								
								// check for undelete
								if (props.properties.containsKey(ModeShapeNode.CommonProperties.DELETE_FLAG)) {
									// need to check that node is not new version of a supercede node
									
									// need to check that the value is set to false
									// this comes across as a string check if this will affect release or publish status
									Boolean deleteFlag = Boolean.parseBoolean(props.getString(ModeShapeNode.CommonProperties.DELETE_FLAG)); 

									Boolean existingDeleteFlag = ((EquipDelete) node).isDeleted();
									if (!deleteFlag && existingDeleteFlag) {
										// only the original user can undelete unless the user has the "any entity"
										// privilege
										// String createdBy = ((EquipCreated) node).getCreatedBy();

										String userId = ((EquipCreated) node).getModifiedBy();
										if (userId == null) {
											userId = ((EquipCreated) node).getCreatedBy();
										}
										
										if (userId.equalsIgnoreCase(userCN)) {
											isOk = auth.checkPrivileges("undelete", "USER", userCN);
										} else {
											isOk = auth.checkPrivileges("undelete", "ANY", userCN);
										}
										if (!isOk) {
											Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userCN
													+ " does not have privileges to undelete this object");
										}

										unDeleteIsOccurring = true;
									} else if (deleteFlag && existingDeleteFlag) {
										String createdBy = ((EquipCreated) node).getCreatedBy();
										if (createdBy.equalsIgnoreCase(userCN)) {
											isOk = auth.checkPrivileges("undelete", "USER", userCN);
										} else {
											isOk = auth.checkPrivileges("undelete", "ANY", userCN);
										}
										if (!isOk) {
											Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userCN
													+ " does not have privileges to undelete this object");
										}

										deleteIsOccurring = true;
									}

									if (node.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
										boolean deleted = true;
										if (unDeleteIsOccurring) {
											deleted = false;
										}

										EntityVersioningResource.handleAnalysis(node, deleted, userCN);
									} else if (node.getPrimaryType().equalsIgnoreCase(AssemblyDTO.PRIMARY_TYPE)) {
										Assembly a = (Assembly) node.toEquipObject();
										if (a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
											EntityVersioningResource.handleReportingEvent(a, !unDeleteIsOccurring, userCN);
										}
									}
								}
							}
							
							
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Incorrect command entered.");
						}

						// check payload contents to see if we need to update other objects
						// payload contents are removed when teh node is updated
						Boolean checkUpdate = false;
						if (payload.properties.containsKey("equip:included")
								|| payload.properties.containsKey(ModeShapeNode.CommonProperties.DELETE_FLAG)) {
							checkUpdate = true;
						}
						
						if (!supersedeAction && !lockAction && !qcChange) {
							payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, userCN);
							payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());
						}
						
						dao.updateNode(nodeId, payload);
						
						// BATCH HANDLING
						EquipObject eobj = node.toEquipObject();
						if(eobj instanceof Batch) {
							// The node's dataframe IDs are all URI's. We are just going to fetch the batch fresh to have them swapped to IDs.
							AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
							Batch b = (Batch) aDao.getAssembly(eobj.getId());
							List<String> ids = b.getDataframeIds();
							ids.addAll(b.getAssemblyIds());
							for(String id : ids) {
								this.handle(id, action, userCN, requestBody, null, b);
							}
						}

						if (forAuditingQCStatusThatIsBeingSet != null) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
										&& entity instanceof EquipVersionable) {
									String entityType = "";
									if (entity instanceof Dataframe) {
										entityType = ((Dataframe) entity).getDataframeType();
									}
									if (entity instanceof Assembly) {
										entityType = ((Assembly) entity).getAssemblyType();
									}
									if (entity instanceof ReportingEventItem) {
										entityType = "Reporting Event Item";
									}

									/*asc.logAuditEntry("QCStatus updated to " + forAuditingQCStatusThatIsBeingSet,
											equipIdObject.getEquipId(), entityType, userCN, Props.isAudit(),
											Const.AUDIT_SUCCESS, equipVersionableObject.getVersionNumber());*/
									
									asc.logAuditEntryAsync("QCStatus updated to " + forAuditingQCStatusThatIsBeingSet, entity, userCN, context);
								}
							}
						}

						if (commitIsOccurring) {
							if (Props.isAudit()) {
								if (node.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
									AnalysisDAO adao = getAnalysisDAO();
									Analysis a = adao.getAnalysis(node.getJcrId());
									DataframeDAO dfDao = new DataframeDAOImpl();
									if (a.getDataframeIds().size() > 0) {
										Dataframe df = dfDao.getDataframe(a.getDataframeIds().get(0));
										/*asc.logAuditEntry("Concentration data selected for Analysis", df.getEquipId(),
												df.getDataframeType(), userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
												df.getVersionNumber(),
												"" + a.getEquipId() + " v." + a.getVersionNumber(), null);*/
										
										AuditDetails details = asc.new AuditDetails("Concentration data selected for Analysis", df, userCN);
										details.setContextEntity(a);
										asc.logAuditEntryAsync(details);
									}

									Dataframe df2 = dfDao.getDataframe(a.getModelConfigurationDataframeId());
									DatasetDAO ddao = new DatasetDAOImpl();
									if (df2 != null && df2.getDataset() != null
											&& df2.getDataset().getComplexDataId() != null) {
										ComplexData complexData = ddao.getData(df2.getDataset().getComplexDataId());
										String content = new String(complexData.getBytes());
										
										/*asc.logAuditEntry("MCT settings selected", a.getEquipId(), a.getAssemblyType(),
												userCN, Props.isAudit(), Const.AUDIT_SUCCESS, a.getVersionNumber(),
												"" + a.getEquipId() + " v." + a.getVersionNumber(), null, content);*/

										AuditDetails details = asc.new AuditDetails("MCT settings selected", df2, userCN);
										details.setContextEntity(a);
										details.setContent(content);
										asc.logAuditEntryAsync(details);
									}
								}
							}

							if (dataloadNotifNeeded) {
								NotificationRequestBody body = createBody("data_loading", notifId, userCN);
								body.setEntity_type("Data Load");
								notifyFlag = ndao.notifyEvent(body, studyIds);
							}
							if (analysisParamNotifNeeded || dfParamNotifNeeded) {
								NotificationRequestBody body = createBody("parameter_data_change", notifId, userCN);
								body.setEntity_type("Derived Parameters");
								notifyFlag = ndao.notifyEvent(body, studyIds);
							}
							if (analysisChangeNotifNeeded) {
								NotificationRequestBody body = createBody("analysis_change", notifId, userCN);
								body.setEntity_type("Analysis");
								notifyFlag = ndao.notifyEvent(body, studyIds);
							}
						}

						if (obsoleteIsOccurring) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
										&& entity instanceof EquipVersionable) {
									/*asc.logAuditEntry("Obsolete occurred", equipIdObject.getEquipId(),
											equipIdObject.getClass().getName(), userCN, Props.isAudit(),
											Const.AUDIT_SUCCESS, equipVersionableObject.getVersionNumber());*/
									
									asc.logAuditEntryAsync("Obsolete occurred", entity, userCN);
								}
							}
						}

						if (deleteIsOccurring) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
										&& entity instanceof EquipVersionable) {
									/*asc.logAuditEntry("Soft delete of entity occurred", equipIdObject.getEquipId(), "",
											userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
											equipVersionableObject.getVersionNumber());*/
									
									asc.logAuditEntryAsync("Soft delete of entity occurred", entity, userCN, context);
								}
							}
							if (node.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
								deleteAnalysisMembers(node);
							}

						}

						if (deleteIsOccurring || unDeleteIsOccurring || updateIsOccurring || commitIsOccurring) {

							// call opmeta service to update modification time on associated protocol
							try {
								OpmetaServiceClient osc = new OpmetaServiceClient();
								osc.setHost(Props.getOpmetaServiceServer());
								osc.setPort(Props.getOpmetaSerivcePort());
								EquipObject eo = node.toEquipObject();
								// for Assembly or Analysis
								if (eo instanceof Assembly) {
									Assembly a = (Assembly) eo;
									notifId = equipId + " v." + a.getVersionNumber();
									studyIds = a.getStudyIds();
								} else if (eo instanceof Dataframe) {
									Dataframe df = (Dataframe) eo;
									notifId = equipId + " v." + df.getVersionNumber();
									studyIds = df.getStudyIds();
								}

								for (String studyId : studyIds) {
									LOGGER.info("EntityVersioningResource: update protocol for study id=" + studyId);
									osc.updateProtocolModifiedDateAsync(userCN, studyId);
								}
							} catch (Exception err) {
								LOGGER.warn(
										"EntityVersioningResource: Error updating protocol modification time for node "
												+ node.getJcrId(),
										err);
							}
						}

						if (unDeleteIsOccurring) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
										&& entity instanceof EquipVersionable) {
									/*asc.logAuditEntry("Soft UNdelete of entity occurred", equipIdObject.getEquipId(),
											equipIdObject.getClass().getName(), userCN, Props.isAudit(),
											Const.AUDIT_SUCCESS, equipVersionableObject.getVersionNumber());*/
									
									asc.logAuditEntryAsync("Soft UNdelete of entity occurred", entity, userCN, context);
								}
							}
						}

						if (updateIsOccurring || lockAction) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID && entity instanceof EquipVersionable) {
									String a = "Update of entity occurred with action(s) in content column.";
									if(lockAction) {
										if(lock != null) {
											a = "Entity ";
											if(lock) {
												a += "locked.";
											}
											else {
												a += "unlocked.";
											}
										}
									}
									
									/*asc.logAuditEntry(a,
											equipIdObject.getEquipId(), equipIdObject.getClass().getName(), userCN,
											Props.isAudit(), Const.AUDIT_SUCCESS,
											equipVersionableObject.getVersionNumber(), null,
											mapUdatesForAudit.toString());*/
									
									AuditDetails details = asc.new AuditDetails(a, entity, userCN);
									details.setContent(mapUdatesForAudit.toString());
									details.setContextEntity(context);
									asc.logAuditEntryAsync(details);
								}
							}

							if (qcNotifNeeded) {
								String entityType = "";
								if (node.getPrimaryType().equals("equip:assembly")) {
									Assembly a = (Assembly) node.toEquipObject();
									notifId = equipId + " v." + a.getVersionNumber();
									studyIds = a.getStudyIds();
									entityType = a.getAssemblyType();
								}
								if (node.getPrimaryType().equals("equip:dataframe")) {
									Dataframe df = (Dataframe) node.toEquipObject();
									notifId = equipId + " v." + df.getVersionNumber();
									studyIds = df.getStudyIds();
									entityType = df.getDataframeType();
								}

								NotificationRequestBody body = createBody("qc_complete", notifId, userCN);
								body.setEntity_type(entityType);
								body.getEventDetail().setQc_status("QC Complete");
								body.getEventDetail().setAnalyst_name(userCN);
								notifyFlag = ndao.notifyEvent(body, studyIds);
							}
						}

						// TO BE REMOVED ONCE VALIDATION LOGIC IS FINALIZED
						checkUpdate = false;

						// check for Reporting Event Item inclusion status or deletion change -- update
						// df and assembly node if needed
						if (checkUpdate) {
							updateOtherEntities(node, dao, userCN);
							// deleting some types of nodes might require update to df published or released
							// status
							if (node.getPrimaryType().equals("equip:assembly")) {
								AssemblyDAO aDAO = getAssemblyDAO();
								Assembly a = aDAO.getAssembly(node.getJcrId());
								if (a.getAssemblyType().equals("Reporting Event")) {
									VersioningDAO vdao = new VersioningDAO();
									// if the assembly was released the status may need to change
									if (a.isReleased()) {
										vdao.updateDFReleaseStatus(a);
									}
								}
							}
						}

						if (commitIsOccurring) {
							if (Props.isAudit()) {
								if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
										&& entity instanceof EquipVersionable) {
									/*equipVersionableObject = (EquipVersionable) entity;
									asc.logAuditEntry("Entity committed", equipIdObject.getEquipId(),
											entity.getClass().getName(), userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
											equipVersionableObject.getVersionNumber());*/
									
									asc.logAuditEntryAsync("Entity committed", entity, userCN, context);
								}
							}
						}

						if (nominalDataChangeIsOccuring && Props.isAudit()) {
							auditNominalDataChanges(node.getJcrId(), userCN, asc);
						}

						// Update the modified properties of the parent node.
						try {
							if ((node instanceof CommentDTO || node instanceof MetadatumDTO) && node.getUp() != null) {
								ModeShapeNode parent = dao.getNodeByPath(node.getUp());
								if (parent != null) {
									dao.updateModified(parent.getJcrId(), userCN);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							LOGGER.error("Attempted to update the modified properties of the parent of "
									+ node.getJcrId() + ".", e);
						}

						if (response != null) {
							response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
						}
						outcome = "Node " + action + " was successful.";
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No object with ID '" + nodeId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No object ID and/or action was provided.");
				}
			} catch (Exception ex) {
				try {
					if (userCN != null && equipIdObject != null && equipVersionableObject != null) {
						if (deleteAction) {
							if (Props.isAudit()) {
								/*asc.logAuditEntry("Attempt to soft delete failed with exception " + ex.getMessage(),
										equipIdObject.getEquipId(), "", userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
										equipVersionableObject.getVersionNumber());*/
								
								asc.logAuditEntryAsync("Attempt to soft delete failed with exception " + ex.getMessage(), entity, userCN, context);
							}
						} else if (updateAction && forAuditingQCStatusThatIsBeingSet != null) {
							if (Props.isAudit()) {
								/*asc.logAuditEntry(
										"Attempt to update qc status failed with exception " + ex.getMessage(),
										equipIdObject.getEquipId(), "", userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
										equipVersionableObject.getVersionNumber());*/
								
								asc.logAuditEntryAsync("Attempt to update qc status failed with exception " + ex.getMessage(), entity, userCN, context);
							}
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return outcome;
		}

		private boolean parentIsLocked(ModeShapeNode node, String user) {
			String primaryType = node.getPrimaryType();
			if (primaryType.equalsIgnoreCase(CommentDTO.PRIMARY_TYPE) || primaryType.equalsIgnoreCase(MetadatumDTO.PRIMARY_TYPE)) {
				ModeShapeDAO dao = new ModeShapeDAO();
				String parentPath = node.getUp();
				ModeShapeNode parentNode = dao.getNodeByPath(parentPath, false);
				if(parentNode != null) {
					EquipObject parent = parentNode.toEquipObject();
					if(parent instanceof EquipLockable) {
						EquipLockable lock = (EquipLockable) parent;
						String lbu = lock.getLockedByUser();
						if(lock.isLocked() && lbu != null && !lbu.equalsIgnoreCase(user)) {
							return true;
						}
					}
				}
			}
			
			return false;
		}

		private void updateOtherEntities(ModeShapeNode node, ModeShapeDAO dao, String userId) {
			if (node.getPrimaryType().equals("equip:reportingEventItem")
					|| node.getPrimaryType().equals("equip:publishedItem")) {
				ReportingAndPublishingDAO pdao = getReportingAndPublishingDAO();
				VersioningDAO vdao = new VersioningDAO();
				DataframeDAO ddao = getDataframeDAO();
				AssemblyDAO adao = getAssemblyDAO();
				PropertiesPayload newProps = new PropertiesPayload();
				ReportingEventItem rei = null;

				if (node.getPrimaryType().equals("equip:reportingEventItem")) {
					rei = pdao.getReportingItem(node.getJcrId());
				} else {
					rei = pdao.getReportingEventItemFromPublishItem(pdao.getPublishItem(node.getJcrId()));
				}
				Boolean isREReleasedDF = vdao.isREReleased(rei.getDataFrameId());
				Boolean isREPublishedDF = vdao.isREIPublished(rei.getDataFrameId());
				Boolean isREPublishedA = vdao.isREPublished(rei.getReportingEventId());

				Assembly a = adao.getAssembly(rei.getReportingEventId());
				Dataframe df = ddao.getDataframe(rei.getDataFrameId());

				// update df if there is a mismatch
				if (df.isReleased() != isREReleasedDF) {
					newProps.addProperty("equip:released", isREReleasedDF);
					dao.updateNode(df.getId(), newProps);
				}
				if (df.isPublished() != isREPublishedDF) {
					newProps.addProperty("equip:published", isREPublishedDF);
					dao.updateNode(df.getId(), newProps);
				}
				// update assembly if there is a mismatch
				if (a.isPublished() != isREPublishedA) {
					newProps.addProperty("equip:published", isREPublishedA);
					dao.updateNode(a.getId(), newProps);
					EntityVersioningResource.updateAtrFlag(a, false, userId);
				}

			}
		}

		private NotificationRequestBody createBody(String eventType, String notifId, String userCN) {
			NotificationRequestBody body = new NotificationRequestBody();
			event_detail detail = new event_detail();
			body.setEvent_type(eventType);
			body.setEntity_id(notifId);
			detail.setUser_name(userCN);
			body.setEventDetail(detail);
			return body;
		}
	};

	/**
	 * Supersedes the provided {@link EquipObject} and all children following the
	 * supersede rules and provided user ID. Returns any error messages encountered
	 * during the supersede, {@code null} otherwise.
	 * 
	 * @param object
	 * @param userId
	 * @return {@link String} any error message encountered
	 */
	public static final String supersedeAction(EquipObject object, String userId) {
		String error = null;
		if (object != null && object instanceof EquipVersionable) {
			EquipVersionable version = (EquipVersionable) object;
			if (!version.getVersionSuperSeded()) {
				boolean locked = false;
				if (object instanceof EquipLockable) {
					EquipLockable el = (EquipLockable) object;
					if(el.isLocked()) {
						if(el.getLockedByUser() != null && !el.getLockedByUser().trim().isEmpty() && !el.getLockedByUser().equalsIgnoreCase(userId)) {
							locked = true;
						}
					}
				}
				
				if(locked) {
					return "The object is locked by another user and cannot be superseded.";
				}
				
				if (version.isCommitted() && !version.isDeleteFlag() && !locked) {
					String equipId = ((com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID) object).getEquipId();
					VersioningDAO vdao = new VersioningDAO(equipId);
					
					if (object instanceof Dataframe) {
						Dataframe dataframe = (Dataframe) object;
						if (vdao.isREReleased(dataframe.getId())) {
							error = "Unable to supersede dataframe for " + equipId
									+ " because it references released Reporting Events.";
						}
						if (!vdao.supersedeDownstream(dataframe, userId)) {
							error = "Unable to supersede downstream nodes for " + equipId + ".";
						}
					} else if (object instanceof Assembly) {
						Assembly assembly = (Assembly) object;
						if (!vdao.supersedeDownstream(assembly, userId)) {
							error = "Unable to supersede downstream nodes for " + equipId + ".";
						}
					}
					
					version.setVersionSuperSeded(true);

					PropertiesPayload payload = new PropertiesPayload();
					payload.addProperty("equip:versionSuperSeded", true);
					payload.addProperty("equip:modified", new Date());
					payload.addProperty("equip:modifiedBy", userId);

					ModeShapeDAO msDao = new ModeShapeDAO();
					msDao.updateNode(object.getId(), payload);
				}
			}
		}

		return error;
	}

	public static final void commitAction(EquipObject eo, String userId) {

	}

	public static final Route putByEquipId = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String outcome = null;
			String userCN = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			;
			String equipId = null;
			Set<String> studyIds = new HashSet<String>();

			try {
				equipId = request.params(":equipId");
				String action = request.params(":action");

				if (equipId != null && action != null) {
					if (action.equalsIgnoreCase("delete")) {
						// check that user has permissions first
						userCN = request.headers("IAMPFIZERUSERCN");
						if (userCN == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges("delete", "PUT", userCN);

						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userCN + " does not have privileges to delete nodes");
						}

						EquipIDDAO eidao = getEquipIDDAO();
						List<EquipObject> nodes = eidao.getItem(equipId);

						if (nodes != null && !nodes.isEmpty()) {
							String blockingId = EntityVersioningResource.canDeleteItem(nodes);
							if (blockingId == null) {
								ModeShapeDAO bdao = new ModeShapeDAO();
								for (EquipObject node : nodes) {
									ServiceBaseResource.handleUserAccess(node, userCN);

									PropertiesPayload pp = new PropertiesPayload();
									pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, true);

									bdao.updateNode(node.getId(), pp);
									ModeShapeNode modeshapeNode = bdao.getNode(node.getId());
									if (modeshapeNode.getPrimaryType().equalsIgnoreCase(AnalysisDTO.PRIMARY_TYPE)) {
										deleteAnalysisMembers(modeshapeNode);
									}

									if (Props.isAudit()) {
										if (node instanceof EquipID && node instanceof EquipVersionable) {
											/*EquipVersion equipVersionableObject = (EquipVersion) node;
											asc.logAuditEntry("Soft delete of entity by equip id successful", equipId,
													"", userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
													equipVersionableObject.getVersionNumber());*/
											
											asc.logAuditEntryAsync("Soft delete of entity by equip id successful", node, userCN);
										}
									}

									// get all study ids for protocol timestamp updates
									if (modeshapeNode.getPrimaryType().equals(AssemblyDTO.PRIMARY_TYPE)) {
										Assembly a = (Assembly) modeshapeNode.toEquipObject();
										studyIds.addAll(a.getStudyIds());
									}
									if (modeshapeNode.getPrimaryType().equals(DataframeDTO.PRIMARY_TYPE)) {
										Dataframe df = (Dataframe) modeshapeNode.toEquipObject();
										studyIds.addAll(df.getStudyIds());
									}
								}
								if (Props.isAudit()) {
									for (EquipObject node : nodes) {
										if (node instanceof EquipID && node instanceof EquipVersionable) {
											/*EquipVersion equipVersionableObject = (EquipVersion) node;
											asc.logAuditEntry("Access of putByEquipId", equipId, "", userCN,
													Props.isAudit(), Const.AUDIT_SUCCESS,
													equipVersionableObject.getVersionNumber());*/
											
											asc.logAuditEntryAsync("Access of putByEquipId", node, userCN);
										}
									}
								}

								// call opmeta service to update modification time on associated protocol
								try {
									OpmetaServiceClient osc = new OpmetaServiceClient();
									osc.setHost(Props.getOpmetaServiceServer());
									osc.setPort(Props.getOpmetaSerivcePort());
									for (String studyId : studyIds) {
										LOGGER.info(
												"EntityVersioningResource: update protocol for study id=" + studyId);
										osc.updateProtocolModifiedDate(userCN, studyId);
									}
								} catch (Exception err) {
									LOGGER.warn(
											"EntityVersioningResource: Error updating protocol modification time for equipId "
													+ equipId,
											err);
								}

								outcome = "Deleted all versions of " + equipId + ".";
							} else {
								Spark.halt(HTTPStatusCodes.CONFLICT,
										"One or more versions are used by " + blockingId + ", which is not deleted.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No items with EQuIP ID '" + equipId + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								"Action '" + action + "' is not allowed; must be 'delete'.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No EQuIP ID or action was provided.");
				}
			} catch (Exception ex) {
				try {
					if (userCN != null && equipId != null) {
						if (Props.isAudit()) {
							asc.logAuditEntry(
									"Attempt to soft delete or get failed with exception, stack trace will give more details "
											+ ex.getMessage(),
									equipId, "", userCN, Props.isAudit(), Const.AUDIT_FAILURE, 0L); // 0L means all
																									// versions were
																									// attempted for the
																									// given equipId
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return outcome;
		}

	};

	private static String getUserId(Request request) {
		String userId = null;
		if (request != null) {
			String q = request.queryParams("userId");
			if (q != null) {
				userId = q.trim();
			}
		}

		return userId;
	}
	
	protected static final void deleteAnalysisMembers(ModeShapeNode node) {
		AnalysisDAO adao = getAnalysisDAO();
		Analysis an = adao.getAnalysis(node.getJcrId());
		EntityVersioningResource.deleteAnalysisMembers(an);
	}

	protected static final void deleteAnalysisMembers(Analysis an) {
		// sets the delete flag on dfs referenced by an analysis
		ModeShapeDAO bdao = new ModeShapeDAO();
		List<String> dfList = Arrays.asList(an.getKelFlagsDataframeId(), an.getParametersDataframeId(),
				an.getModelConfigurationDataframeId());

		for (String dfId : dfList) {
			if (dfId != null && !dfId.equals("")) {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, true);
				bdao.updateNode(dfId, pp);

			}
		}
	}

	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String equipId = request.params(":equipId");
				String userId = EntityVersioningResource.getUserId(request);
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());
				;

				if (equipId != null) {
					// get all dataframe or assembly versions
					EquipIDDAO eidao = getEquipIDDAO();
					VersioningDAO vdao = new VersioningDAO();
					List<EquipObject> nodes = eidao.getItem(equipId);
					List<EquipObject> returnNodes = new ArrayList<>();
					List<EquipVersionable> userNodes = new ArrayList<>();
					String currentUser = request.headers("IAMPFIZERUSERCN");
					AuthorizationDAO auth = new AuthorizationDAO();

					if (nodes != null && !nodes.isEmpty()) {
						// get all committed nodes with this id
						Boolean committed = false;
						Boolean deleted = false;
						ModeShapeDAO mDao = new ModeShapeDAO();
						
						boolean isKel = false;
						EquipObject typeTest = nodes.get(0);
						if(typeTest instanceof Dataframe) {
							Dataframe df = (Dataframe) typeTest;
							if(df.getDataframeType().equalsIgnoreCase(Dataframe.KEL_FLAGS_TYPE)) {
								isKel = true;
							}
						}
						
						if(isKel) {
							nodes.sort(new Comparator<EquipObject>() {
	
								@Override
								public int compare(EquipObject eo1, EquipObject eo2) {
									Dataframe df1 = (Dataframe) eo1;
									Dataframe df2 = (Dataframe) eo2;
									
									return df2.getCreated().compareTo(df1.getCreated());
								}
								
							});
							
							List<Long> handledVersions = new ArrayList<>();
							for(EquipObject node : nodes) {
								Dataframe df = (Dataframe) node;
								if(!handledVersions.contains(df.getVersionNumber())) {
									handledVersions.add(df.getVersionNumber());
									returnNodes.add(df);
								}
							}
						}
						else {
							for (EquipObject node : nodes) {
								EquipObject eo = mDao.getEquipObject(node.getId());
								ServiceBaseResource.handleUserAccess(eo, currentUser);
								
								if (eo instanceof EquipVersionable) {
									EquipVersionable v = (EquipVersionable) eo;
									committed = v.isCommitted();
									deleted = v.isDeleteFlag();
								}
	
								// if (committed & !deleted) {
								if (true) {
									// if this is a dataframe, does the person making the call have permissions to
									// see this dataframe?
									if (node instanceof Dataframe) {
										if (auth.canViewDataframe((Dataframe) node, currentUser)) {
											returnNodes.add(node);
										}
									} else {
										returnNodes.add(node);
									}
								} else {
									if (userId != null && !userId.isEmpty()) {
										// add uncommitted for this user to a separate array
										EquipCreatable c = (EquipCreatable) node;
										EquipVersionable v = (EquipVersionable) node;
										if (c.getCreatedBy().equals(userId) && !v.isDeleteFlag())
											userNodes.add((EquipVersionable) node);
									}
								}
							}
						}
						
						if (!userNodes.isEmpty()) {
							// this is the highest uncommitted node -- make sure current user can see it
							EquipVersionable maxVersion = vdao.getMaxVersion(userNodes);
							if (maxVersion instanceof Dataframe) {

								if (auth.canViewDataframe((Dataframe) maxVersion, currentUser)) {
									returnNodes.add((EquipObject) maxVersion);
								} else
									returnNodes.add((EquipObject) maxVersion);
							}
							
						}

						if (!returnNodes.isEmpty()) {
							if (Props.isAudit()) {
								for (EquipObject obj : nodes) {
									if (obj instanceof EquipID && obj instanceof EquipVersionable) {
										/*EquipID equipIdObject = (EquipID) obj;
										EquipVersionable equipVersionableObject = (EquipVersionable) obj;
										
										asc.logAuditEntry("Access of EquipVersionable via equipId",
												equipIdObject.getEquipId(), "EquipVersionable", userId, Props.isAudit(),
												Const.AUDIT_SUCCESS, equipVersionableObject.getVersionNumber());*/
										
										asc.logAuditEntryAsync("Access of EquipVersionable via equipId", obj, userId);
									}
								}
							}

							json = ServiceBaseResource.marshalObject(returnNodes);
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST,
									"No qualifying objects were found with Equip Id: " + equipId);
						}
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No objects were found with Equip Id: " + equipId);
					}

				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No EQuIP ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}
			response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			return json;
		}

	};
	
	public static final void commit(String entityId, String user) {
		EntityVersioningResource.commit(entityId, user, null);
	}
	
	public static final void commit(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.commit(entity, user, context);
		}
	}
	
	public static final void commit(EquipObject entity, String user) {
		EntityVersioningResource.commit(entity, user, null);
	}
	
	public static final void commit(EquipObject entity, String user, EquipObject context) {
		if(entity != null && user != null && entity instanceof EquipID && entity instanceof EquipVersionable) {
			PropertiesPayload payload = new PropertiesPayload();
			
			EquipVersionable version = (EquipVersionable) entity;
			String equipId = ((EquipID) entity).getEquipId();
			// check first
			if (version.isCommitted() || version.getVersionSuperSeded() || version.isDeleteFlag()) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Node " + equipId
						+ " v" + version.getVersionNumber()
						+ " cannot be committed. It is already committed, superseded or marked for deletion.");
			}
			
			EquipCreatable ec = (EquipCreatable) entity;
			if(!ec.getCreatedBy().equalsIgnoreCase(user)) {
				Spark.halt(HTTPStatusCodes.CONFLICT, "Entity " + equipId + " v" + version.getVersionNumber() + " was created by another user and can only be committed by that user.");
			}
			
			version.setCommitted(true);
			
			payload.addProperty(ModeShapeNode.CommonProperties.COMMIT_FLAG, true);
			payload.addProperty(ModeShapeNode.CommonProperties.LOCKED_BY_USER, "");
			payload.addProperty(ModeShapeNode.CommonProperties.LOCK_FLAG, false);
			payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, user);
			payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());
			
			// do we need to notify?
			List<String> studyIds = new ArrayList<>();
			NotificationRequestBody notificationBody = null;
			boolean paramDataChanged = false;
			if(entity instanceof Analysis) {
				Analysis an = (Analysis) entity;
				studyIds = an.getStudyIds();
				
				if(an.getVersionNumber() > 1) {
					notificationBody = EntityVersioningResource.createBody("analysis_change", equipId + " v." + an.getVersionNumber(), user);
					notificationBody.setEntity_type("Analysis");
				}
				
				paramDataChanged = (an.getParametersDataframeId() != null && !an.getParametersDataframeId().isEmpty());
			}
			else if(entity instanceof Batch) {
				// If this entity is a batch, commit it and all of its members.
				Batch b = (Batch) entity;
				for(String dfId : b.getDataframeIds()) {
					EntityVersioningResource.commit(dfId, user, b);
				}
				for(String aId : b.getAssemblyIds()) {
					EntityVersioningResource.commit(aId, user, b);
				}
			}
			else if(entity instanceof Assembly) {
				Assembly a = (Assembly) entity;
				
				// If we are committing a data load, we need to notify.
				if(a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
					studyIds = a.getStudyIds();
					String id = equipId;
					if(a.getVersionNumber() > 1) {
						id += " v." + a.getVersionNumber();
					}
					
					notificationBody = EntityVersioningResource.createBody("data_loading", id, user);
					notificationBody.setEntity_type("Data Load");
				}
			}
			else if(entity instanceof Dataframe) {
				Dataframe df = (Dataframe) entity;
				studyIds = df.getStudyIds();
				paramDataChanged = df.getDataframeType().equalsIgnoreCase(Dataframe.DERIVED_PARAMETERS_TYPE);
			}
			
			// Delete all siblings
			VersioningDAO vdao = new VersioningDAO(equipId);
			boolean siblingsDeleted = vdao.deleteSiblings(version.getVersionNumber(), entity.getId());
			if (!siblingsDeleted) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Unable to delete related nodes for " + equipId + ".");
			}
			
			// Update the node
			ModeShapeDAO msDao = new ModeShapeDAO();
			msDao.updateNode(entity.getId(), payload);
			
			// Audit
			if (Props.isAudit()) {
				try {
					AuditServiceClient asc = EntityVersioningResource.getAuditServiceClient();
					if (entity instanceof Analysis) {
						Analysis a = (Analysis) entity;
						DataframeDAO dfDao = new DataframeDAOImpl();
						if (a.getDataframeIds().size() > 0) {
							Dataframe df = dfDao.getDataframe(a.getDataframeIds().get(0));
							/*asc.logAuditEntry("Concentration data selected for Analysis", df.getEquipId(),
									df.getDataframeType(), userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
									df.getVersionNumber(),
									"" + a.getEquipId() + " v." + a.getVersionNumber(), null);*/
							
							AuditDetails details = asc.new AuditDetails("Concentration data selected for Analysis", df, user);
							details.setContextEntity(a);
							asc.logAuditEntryAsync(details);
						}
						
						Dataframe mct = dfDao.getDataframe(a.getModelConfigurationDataframeId());
						DatasetDAO ddao = new DatasetDAOImpl();
						if (mct != null && mct.getDataset() != null
								&& mct.getDataset().getComplexDataId() != null) {
							ComplexData complexData = ddao.getData(mct.getDataset().getComplexDataId());
							String content = new String(complexData.getBytes());
							
							/*asc.logAuditEntry("MCT settings selected", a.getEquipId(), a.getAssemblyType(),
									userCN, Props.isAudit(), Const.AUDIT_SUCCESS, a.getVersionNumber(),
									"" + a.getEquipId() + " v." + a.getVersionNumber(), null, content);*/
	
							AuditDetails details = asc.new AuditDetails("MCT settings selected", mct, user);
							details.setContextEntity(a);
							details.setContent(content);
							asc.logAuditEntryAsync(details);
						}
					}
					
					AuditDetails details = asc.new AuditDetails("Entity committed", entity, user);
					details.setContextEntity(context);
					asc.logAuditEntryAsync(details);
				}
				catch(Exception e) {
					e.printStackTrace();
					LOGGER.error("Error when creating audit entry during commit.", e);
				}
			}
			
			// Update opmeta
			EntityVersioningResource.updateOpmeta(entity, user);
			
			// Send any notifications
			if(notificationBody != null && !studyIds.isEmpty()) {
				try {
					NotificationDAO nDao = new NotificationDAO();
					nDao.notifyEvent(notificationBody, studyIds);
					
					if(paramDataChanged) {
						notificationBody = createBody("parameter_data_change", equipId + " v." + version.getVersionNumber(), user);
						notificationBody.setEntity_type("Derived Parameters");
						nDao.notifyEvent(notificationBody, studyIds);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					LOGGER.error("Error when trying to send notifications during commit.", e);
				}
			}
		}
	}
	
	public static final void supersede(String entityId, String user) {
		EntityVersioningResource.supersede(entityId, user, null);
	}
	
	public static final void supersede(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.supersede(entity, user, context);
		}
	}
	
	public static final void supersede(EquipObject entity, String user) {
		EntityVersioningResource.supersede(entity, user, null);
	}
	
	public static final void supersede(EquipObject entity, String user, EquipObject context) {
		if(entity != null && user != null) {
			if(!(entity instanceof EquipVersionable) || !(entity instanceof EquipID)) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not versionable.");
			}
			
			EquipVersionable version = (EquipVersionable) entity;
			// If the entity is already superseded, we're done.
			if(version.getVersionSuperSeded()) {
				return;
			}
			
			if(version.isDeleteFlag()) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is deleted and so cannot be superseded.");
			}
			if(!version.isCommitted()) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not committed and so cannot be superseded.");
			}
			
			if(entity instanceof EquipLockable) {
				EquipLockable el = (EquipLockable) entity;
				if(el.isLocked() && !el.getLockedByUser().equalsIgnoreCase(user)) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is locked and so cannot be superseded.");
				}
			}
			
			// We need to supersede downstream nodes for dataframes and assemblies.
			// The supersedeDownstream method will also supersede the entity passed as a parameter.
			String equipId = ((EquipID) entity).getEquipId();
			VersioningDAO vdao = new VersioningDAO(equipId);
			if(entity instanceof Dataframe) {
				Dataframe dataframe = (Dataframe) entity;
				if (dataframe.getSubType() == null || !dataframe.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
					boolean isReleased = vdao.isREReleased(dataframe.getId());
					if (isReleased) {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								"Unable to supersede dataframe " + equipId + " v" + version.getVersionNumber() + " because it references released Reporting Events.");
					}
				}
				
				if (!vdao.supersedeDownstream(dataframe, user)) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							"Unable to supersede downstream nodes for " + equipId + " v" + version.getVersionNumber() + ".");
				}
			}
			else if(entity instanceof Assembly) {
				Assembly assembly = (Assembly) entity;
				if (!vdao.supersedeDownstream(assembly, user)) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							"Unable to supersede downstream nodes for " + equipId + " v" + version.getVersionNumber() + ".");
				}
				
				if(entity instanceof Batch) {
					// If this is a batch, we need to supersede all of its members.
					Batch b = (Batch) entity;
					for(String dfId : b.getDataframeIds()) {
						EntityVersioningResource.supersede(dfId, user, context);
					}
					for(String aId : b.getAssemblyIds()) {
						EntityVersioningResource.supersede(aId, user, context);
					}
				}
			}
			else {
				PropertiesPayload payload = new PropertiesPayload();
				payload.addProperty(ModeShapeNode.CommonProperties.SUPERSEDE_FLAG, true);
				version.setVersionSuperSeded(true);
				
				ModeShapeDAO msDao = new ModeShapeDAO();
				msDao.updateNode(entity.getId(), payload);
			}
		}
	}
	
	public static final void delete(String entityId, String user) {
		EntityVersioningResource.delete(entityId, user, null);
	}
	
	public static final void delete(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.delete(entity, user, true, context);
		}
	}
	
	public static final void delete(EquipObject entity, String user) {
		EntityVersioningResource.delete(entity, user, true, null);
	}
	
	public static final void restore(String entityId, String user) {
		EntityVersioningResource.restore(entityId, user, null);
	}
	
	public static final void restore(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.delete(entity, user, false, context);
		}
	}
	
	public static final void restore(EquipObject entity, String user) {
		EntityVersioningResource.delete(entity, user, false, null);
	}
	
	private static final void delete(EquipObject entity, String user, boolean deleted, EquipObject context) {
		if(entity != null && user != null) {
			if(!(entity instanceof EquipVersionable)) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not deletable.");
			}
			
			EquipVersionable version = (EquipVersionable) entity;
			if(version.isDeleteFlag()) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is already deleted.");
			}
			
			String blockingId = EntityVersioningResource.canDeleteItem(entity);
			if(blockingId != null) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is referenced by " + blockingId + ", which is not deleted.");
			}
			
			// Begin deletion
			if(entity instanceof Analysis) {
				Analysis an = (Analysis) entity;
				EntityVersioningResource.handleAnalysis(an, deleted, user);
				EntityVersioningResource.deleteAnalysisMembers(an);
			}
			else if(entity instanceof Batch) {
				Batch b = (Batch) entity;
				
				// Delete the members of the batch.
				for(String dfId : b.getDataframeIds()) {
					EntityVersioningResource.delete(dfId, user, b);
				}
				for(String aId : b.getAssemblyIds()) {
					EntityVersioningResource.delete(aId, user, b);
				}
			}
			else if(entity instanceof Assembly) {
				Assembly a = (Assembly) entity;
				if(a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
					VersioningDAO vdao = new VersioningDAO();
					vdao.deleteAssociatedDataframes(a);
				}
				else if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
					EntityVersioningResource.handleReportingEvent(a, deleted, user);
				}
			}
			else if(entity instanceof Dataframe) {
				Dataframe df = (Dataframe) entity;
				VersioningDAO vdao = new VersioningDAO(df);
				if (!df.isCommitted()) {
					vdao.unSupersedePrevious(df);
				}
				
				String subType = df.getSubType();
				if(subType != null) {
					if (subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
						// Handle ATR
						ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
						List<ReportingEventItem> items = rpDao
								.getReportingEventItemByDataframeId(df.getId());
						if (items.size() == 1) {
							AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
							ReportingEventItem item = items.get(0);
							Assembly reportingEvent = aDao
									.getAssembly(item.getReportingEventId());
							if (reportingEvent != null) {
								String failureReason = "The ATR report cannot be deleted because either the Reporting Event has been Released or one or more Reporting Event Items have been Published.";
								String releaseStatus = reportingEvent
										.getReleaseStatus();
								if (releaseStatus != null && releaseStatus
										.equalsIgnoreCase(Const.RELEASED_STATUS)) {
									Spark.halt(HTTPStatusCodes.CONFLICT, failureReason);
								}

								items = rpDao.getReportingItem(
										reportingEvent.getReportingItemIds());
								for (ReportingEventItem rei : items) {
									PublishItem pi = rei.getPublishItem();
									if (pi != null && pi.getPublishStatus() != null
											&& pi.getPublishStatus().equalsIgnoreCase(
													Const.PUBLISHED_STATUS)) {
										Spark.halt(HTTPStatusCodes.CONFLICT,
												failureReason);
									}
								}
							}

							// Delete the REI
							ModeShapeDAO msDao = new ModeShapeDAO();
							PropertiesPayload ppl = new PropertiesPayload();
							ppl.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG,
									true);
							ppl.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY,
									user);
							ppl.addProperty(
									ModeShapeNode.CommonProperties.MODIFIED_DATE,
									new Date());
							msDao.updateNode(item.getId(), ppl);
							
							// Update the reporting event modified date
							msDao.updateModified(reportingEvent.getId(), user);
							EntityVersioningResource.updateAtrFlag(reportingEvent, false, user);
						}
					} else if (subType
							.equalsIgnoreCase(Dataframe.ANALYSIS_QC_REPORT_SUB_TYPE)) {
						// Handle Analysis QC
						AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
						List<Assembly> assemblies = aDao
								.getAssembly(df.getAssemblyIds());
						for (Assembly a : assemblies) {
							if (a instanceof Analysis) {
								Analysis an = (Analysis) a;
								String paramsId = an.getParametersDataframeId();
								if (paramsId != null) {
									ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
									List<ReportingEventItem> items = rpDao
											.getReportingEventItemByDataframeId(
													paramsId);
									for (ReportingEventItem item : items) {
										PublishItem pi = item.getPublishItem();
										if (pi != null && pi.getPublishStatus() != null
												&& pi.getPublishStatus()
														.equalsIgnoreCase(
																Const.PUBLISHED_STATUS)) {
											Spark.halt(HTTPStatusCodes.CONFLICT,
													"Analysis QC Report cannot be deleted because Parameters have been published.");
										}
									}
								}
								
								break;
							}
						}
					}
				}
			}
			
			PropertiesPayload payload = new PropertiesPayload();
			payload.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, true);
			payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, user);
			payload.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());
			
			ModeShapeDAO msDao = new ModeShapeDAO();
			msDao.updateNode(entity.getId(), payload);
			
			if (Props.isAudit()) {
				try {
					AuditServiceClient asc = EntityVersioningResource.getAuditServiceClient();
					if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID
							&& entity instanceof EquipVersionable) {
						/*asc.logAuditEntry("Soft delete of entity occurred", equipIdObject.getEquipId(), "",
								userCN, Props.isAudit(), Const.AUDIT_SUCCESS,
								equipVersionableObject.getVersionNumber());*/
						
						AuditDetails details = asc.new AuditDetails("Soft delete of entity occurred", entity, user);
						details.setContextEntity(context);
						asc.logAuditEntryAsync(details);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					LOGGER.error("Error when auditing deletion of entity.", e);
				}
			}
			
			// Update opmeta
			EntityVersioningResource.updateOpmeta(entity, user);
		}
	}
	
	public static final void lock(String entityId, String user) {
		EntityVersioningResource.lock(entityId, user, null);
	}
	
	public static final void lock(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.lock(entity, user, true, context);
		}
	}
	
	public static final void lock(EquipObject entity, String user) {
		EntityVersioningResource.lock(entity, user, true, null);
	}
	
	public static final void unlock(String entityId, String user) {
		EntityVersioningResource.unlock(entityId, user, null);
	}
	
	public static final void unlock(String entityId, String user, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.lock(entity, user, false, context);
		}
	}
	
	public static final void unlock(EquipObject entity, String user) {
		EntityVersioningResource.lock(entity, user, false, null);
	}
	
	private static final void lock(EquipObject entity, String user, boolean lock, EquipObject context) {
		if(entity != null && user != null) {
			if(!(entity instanceof EquipLockable)) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is not lockable.");
			}
			
			if(entity instanceof EquipVersionable) {
				EquipVersionable ev = (EquipVersionable) entity;
				if(ev.isDeleteFlag()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is deleted and cannot be locked.");
				}
				if(!ev.isCommitted() || ev.getVersionSuperSeded()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The object is either not committed or is superseded and so cannot be locked.");
				}
			}
			
			EquipLockable el = (EquipLockable) entity;
			PropertiesPayload payload = new PropertiesPayload();
			if(el.getLockedByUser() == null || el.getLockedByUser().trim().isEmpty() ||  el.getLockedByUser().equalsIgnoreCase(user)) {
				payload.addProperty(ModeShapeNode.CommonProperties.LOCK_FLAG, true);
				payload.addProperty(ModeShapeNode.CommonProperties.LOCKED_BY_USER, user);
			}
			else {
				Spark.halt(HTTPStatusCodes.CONFLICT, "The object is already locked by another user.");
			}
			
			ModeShapeDAO msDao = new ModeShapeDAO();
			msDao.updateNode(entity.getId(), payload);
			
			if(entity instanceof Batch) {
				Batch b = (Batch) entity;
				for(String dfId : b.getDataframeIds()) {
					EntityVersioningResource.lock(dfId, user, b);
				}
				for(String aId : b.getAssemblyIds()) {
					EntityVersioningResource.lock(aId, user, b);
				}
			}
			
			if (Props.isAudit()) {
				try {
					AuditServiceClient asc = EntityVersioningResource.getAuditServiceClient();
					if (entity instanceof com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID && entity instanceof EquipVersionable) {
						String a = "Update of entity occurred with action(s) in content column.";
						a = " Entity ";
						if(lock) {
							a += "locked.";
						}
						else {
							a += "unlocked.";
						}
						
						AuditDetails details = asc.new AuditDetails(a, entity, user);
						//details.setContent(mapUdatesForAudit.toString());
						asc.logAuditEntryAsync(details);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final void update(String entityId, String user, PropertiesPayload payload) {
		EntityVersioningResource.update(entityId, user, payload, null);
	}
	
	public static final void update(String entityId, String user, PropertiesPayload payload, EquipObject context) {
		if(entityId != null && user != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			EquipObject entity = msDao.getEquipObject(entityId);
			EntityVersioningResource.update(entity, user, payload, context);
		}
	}
	
	public static final void update(EquipObject entity, String user, PropertiesPayload payload) {
		EntityVersioningResource.update(entity, user, payload, null);
	}
	
	public static final void update(EquipObject entity, String user, PropertiesPayload props, EquipObject context) {
		if(entity != null && user != null && props != null && !props.properties.isEmpty()) {
			try {
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean qcChange = false;
				boolean qcNotifNeeded = false;
				String forAuditingQCStatusThatIsBeingSet;
				
				// Check if this is a QC status change.
				String QC_STATUS_PROP = "equip:qcStatus";
				if (props.properties.containsKey(QC_STATUS_PROP)) {
					boolean isOk = auth.checkPrivileges("qcstatus", "PUT", user);
					if (!isOk) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + user + " does not have privileges to update qc status");
					}
					
					String qcStatus = props.getString(QC_STATUS_PROP);
					forAuditingQCStatusThatIsBeingSet = qcStatus;
					
					// Will need to set a notification for qc status complete
					if (qcStatus.equalsIgnoreCase("qc'd")) {
						qcNotifNeeded = true;
					}
					
					qcChange = true;
				}
				
				// If this is not a QC status change, we need to check if the entity is locked.
				if(!qcChange && entity instanceof EquipLockable) {
					EquipLockable equipLock = (EquipLockable) entity;
					if (equipLock.isLocked() && !equipLock.getLockedByUser().equalsIgnoreCase(user)) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "This object is locked and cannot be modified");
					}
				}
				
				// If this is a lock/unlock or a delete/restore, call the appropriate methods.
				if(props.properties.containsKey(ModeShapeNode.CommonProperties.LOCK_FLAG)) {
					
				}
				else if (props.properties.containsKey(ModeShapeNode.CommonProperties.DELETE_FLAG)) {
					
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates the {@code atrIsCurrent} property of the provided reporting event to the specified value. {@code true} means there is an up-to-date ATR for the 
	 * provided reporting event. {@code false} means there is no up-to-date ATR for the provided reporting event.
	 * @param reId
	 * @param newValue
	 * @param user
	 */
	public static final void updateAtrFlag(String reId, boolean newValue, String user) {
		if(reId != null && user != null) {
			Assembly re = null;
			if(reId.startsWith("http")) {
				ModeShapeDAO msDao = new ModeShapeDAO();
				ModeShapeNode node = msDao.getNodeByPath(reId);
				if(node != null) {
					re = (Assembly) node.toEquipObject();
				}
			}
			else {
				AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
				re = aDao.getAssembly(reId);
			}
			
			EntityVersioningResource.updateAtrFlag(re, newValue, user);
		}
	}
	
	/**
	 * Updates the {@code atrIsCurrent} property of the provided reporting event to the specified value. {@code true} means there is an up-to-date ATR for the 
	 * provided reporting event. {@code false} means there is no up-to-date ATR for the provided reporting event.
	 * @param reportingEvent
	 * @param newValue
	 * @param user
	 */
	public static final void updateAtrFlag(Assembly reportingEvent, boolean newValue, String user) {
		if(reportingEvent != null && user != null && reportingEvent.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
			//if(reportingEvent.atrIsCurrent() == null || reportingEvent.atrIsCurrent() != newValue) {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:atrIsCurrent", newValue);
				
				LOGGER.info("Setting atrIsCurrent of " + reportingEvent.getId() + " to " + newValue + " // stack: ");
				Thread.dumpStack();
				
				ModeShapeDAO msDao = new ModeShapeDAO();
				msDao.updateNode(reportingEvent.getId(), pp);
				reportingEvent.setAtrIsCurrent(newValue);
			//}
		}
	}
	
	private static final AuditServiceClient getAuditServiceClient() throws ServiceCallerException {
		return new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
	}
	
	/**
	 * Returns a {@link NotificationRequestBody} created from the provided information.
	 * @param eventType
	 * @param notifId
	 * @param userCN
	 * @return {@link NotificationRequestBody}
	 */
	private static final NotificationRequestBody createBody(String eventType, String notifId, String userCN) {
		NotificationRequestBody body = new NotificationRequestBody();
		event_detail detail = new event_detail();
		body.setEvent_type(eventType);
		body.setEntity_id(notifId);
		detail.setUser_name(userCN);
		body.setEventDetail(detail);
		return body;
	}
	
	private static final void updateOpmeta(EquipObject eo, String user) {
		// call opmeta service to update modification time on associated protocol
		try {
			OpmetaServiceClient osc = new OpmetaServiceClient();
			osc.setHost(Props.getOpmetaServiceServer());
			osc.setPort(Props.getOpmetaSerivcePort());
			
			// for Assembly or Analysis
			List<String> studyIds = new ArrayList<>();
			if (eo instanceof Assembly) {
				Assembly a = (Assembly) eo;
				studyIds = a.getStudyIds();
			} else if (eo instanceof Dataframe) {
				Dataframe df = (Dataframe) eo;
				studyIds = df.getStudyIds();
			}

			for (String studyId : studyIds) {
				LOGGER.info("EntityVersioningResource: update protocol for study id=" + studyId);
				osc.updateProtocolModifiedDateAsync(user, studyId);
			}
		} catch (Exception err) {
			LOGGER.warn("EntityVersioningResource: Error updating protocol modification time for node " + eo.getId(), err);
		}
	}
	
	private static final void handleReportingEvent(Assembly reportingEvent, boolean deleted, String user) {
		if (reportingEvent != null) {
			String lockedBy = reportingEvent.getLockedByUser();
			if (lockedBy != null) {
				lockedBy = lockedBy.trim();
				if (!lockedBy.isEmpty() && !lockedBy.equalsIgnoreCase(user)) {
					Spark.halt(HTTPStatusCodes.CONFLICT, "Reporting event " + reportingEvent.getEquipId() + " v"
							+ reportingEvent.getVersionNumber() + " is locked and cannot be deleted.");
				}
			}

			ModeShapeDAO mDao = new ModeShapeDAO();
			reportingEvent.setDeleteFlag(deleted);

			// ReportingAndPublishingDAO rDao = new ReportingAndPublishingDAO();
			// List<ReportingEventItem> items =
			// rDao.getReportingEventItemByAssemblyId(reportingEvent.getId());
			List<ReportingEventItem> items = new ArrayList<>();
			for (String reId : reportingEvent.getReportingItemIds()) {
				ModeShapeNode node = null;
				if (reId.contains("http")) {
					node = mDao.getNodeByPath(reId);
				} else {
					node = mDao.getNode(reId);
				}

				if (node != null) {
					ReportingEventItem item = (ReportingEventItem) node.toEquipObject();
					items.add(item);
				}
			}

			// If we're deleting the reporting event, there are some things we have to check
			// first.
			if (deleted) {
				if (reportingEvent.getReleaseStatus() != null
						&& reportingEvent.getReleaseStatus().equalsIgnoreCase(Const.RELEASED_STATUS)) {
					Spark.halt(HTTPStatusCodes.CONFLICT, "Reporting event " + reportingEvent.getEquipId() + " v"
							+ reportingEvent.getVersionNumber() + " is released and cannot be deleted.");
				}

				// Prevent deletion if any of the reporting event items are published.
				for (ReportingEventItem item : items) {
					PublishItem pi = item.getPublishItem();
					if (pi != null) {
						if (pi.getPublishStatus().equalsIgnoreCase(Const.PUBLISHED_STATUS)) {
							Spark.halt(HTTPStatusCodes.CONFLICT,
									"Reporting item " + item.getEquipId() + " v" + item.getVersionNumber()
											+ " is published. Reporting event " + reportingEvent.getEquipId() + " v"
											+ reportingEvent.getVersionNumber() + " cannot be deleted.");
						}
					}
				}
			}

			// Handle the reporting event items.
			for (ReportingEventItem item : items) {
				item.setDeleteFlag(deleted);
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, deleted);
				pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, user);
				pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());

				mDao.updateNode(item.getId(), pp);
			}

			// Handle child reports.
			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			List<Dataframe> children = dDao.getDataframeByAssemblyId(reportingEvent.getId());
			for (Dataframe child : children) {
				if (child.getSubType() != null && child.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
					child.setDeleteFlag(deleted);
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, deleted);
					pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, user);
					pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());

					mDao.updateNode(child.getId(), pp);
				}
			}
		}
	}

	private static final void handleAnalysis(ModeShapeNode node, boolean delete, String user) {
		AnalysisDAO anDao = ModeShapeDAO.getAnalysisDAO();
		Analysis an = anDao.getAnalysis(node.getJcrId());
		EntityVersioningResource.handleAnalysis(an, delete, user);
	}
	
	private static final void handleAnalysis(Analysis an, boolean delete, String user) {
		ModeShapeDAO dao = new ModeShapeDAO();

		// Delete/Undelete PPRM, KEL, MCT, and CEST of an Analysis
		String[] ids = { an.getParametersDataframeId(), an.getKelFlagsDataframeId(),
				an.getModelConfigurationDataframeId(), an.getEstimatedConcDataframeId() };
		for (String id : ids) {
			if (id != null) {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, delete);
				pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_BY, user);
				pp.addProperty(ModeShapeNode.CommonProperties.MODIFIED_DATE, new Date());

				dao.updateNode(id, pp);
			}
		}
	}

	public static final List<String> allowedPropertyUpdates() throws Exception {

		return Props.getAllowedUpdates();

	}

	private static final <T extends EquipObject> String canDeleteItem(T item) {
		List<T> list = new ArrayList<>();
		list.add(item);
		return EntityVersioningResource.canDeleteItem(list);
	}

	private static final <T extends EquipObject> String canDeleteItem(List<T> items) {
		String blockingId = null;
		if (items != null) {
			VersioningDAO vdao = new VersioningDAO();
			for (EquipObject item : items) {
				if (item != null) {
					if (item instanceof Dataframe && !vdao.canDeleteDataframe((Dataframe) item)) {
						blockingId = vdao.getLastBlockingId();
						break;
					} else if (item instanceof Assembly && !vdao.canDeleteAssembly(item.getId())) {
						blockingId = vdao.getLastBlockingId();
						break;
					}
				}
			}
		}

		return blockingId;
	}

	private static String getDataFromDataframe(String dataframeId) {
		DataframeDAO ddao = getDataframeDAO();
		DatasetDAO dsdao = getDatasetDAO();

		Dataframe inputDf = ddao.getDataframe(dataframeId);
		ComplexData inputData = dsdao.getData(inputDf.getDataset().getComplexDataId());
		return new String(inputData.getBytes());

	}

	public static void auditNominalDataChanges(String nodeId, String userCN, AuditServiceClient asc)
			throws ServiceCallerException {
		if(asc == null) {
			asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
		}
		
		// get values from this analysis
		AnalysisDAO adao = getAnalysisDAO();
		Analysis an = adao.getAnalysis(nodeId);
		String df0 = an.getDataframeIds().get(0);
		List<String> dfIds = new ArrayList<>();
		dfIds.add(df0);

		String flagsJson = getDataFromDataframe(an.getKelFlagsDataframeId());
		String mctJson = getDataFromDataframe(an.getModelConfigurationDataframeId());

		// call compute service
		ComputeServiceClient csc = new ComputeServiceClient();
		csc.setHost(Props.getComputeServiceServer());
		csc.setPort(Props.getComputeServicePort());
		csc.setUser(userCN);
		ComputeParameters params = new ComputeParameters();

		params.setDataframeIds(dfIds);
		params.setUser(csc.getUser());
		params.setComputeContainer("equip-r-base");
		params.setEnvironment("Server");
		params.setDataframeType("Data Transformation");
		params.getParameters().add(new Parameter("FLAGS", flagsJson, "string"));
		params.getParameters().add(new Parameter("MCT", mctJson, "string"));

		LibraryServiceClient lsc = new LibraryServiceClient();
		lsc.setHost(Props.getLibraryServiceServer());
		lsc.setPort(Props.getLibraryServicePort());
		lsc.setUser(userCN);
		LibraryResponse lr = lsc.getGlobalSystemScriptByName(Props.getAuditNominalDataChangesScriptName());
		// below line is for testing
//	LibraryResponse lr = lsc.getScriptByName("/users/hirscm08", "audit-nominal-data-changes8.R");

		params.setScriptId(lr.getArtifactId());

		ComputeResult result = csc.computeVirtual(params);
		List<String> list = result.getDatasetData();
		String standardCSV = new String(Base64.decodeBase64(list.get(0)), StandardCharsets.UTF_8);
		
		/*asc.logAuditEntry("Nominal Data Changes", an.getEquipId(), "Analysis", userCN, Props.isAudit(),
				Const.AUDIT_SUCCESS, an.getVersionNumber(), an.getId(), lr.getArtifactId(), standardCSV);*/
		
		AuditDetails details = asc.new AuditDetails("Nominal Data Changes", an, userCN);
		details.setScriptId(lr.getArtifactId());
		details.setContent(standardCSV);
		details.setContextEntity(an);
		asc.logAuditEntryAsync(details);
	}
}
