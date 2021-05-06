package com.pfizer.pgrd.equip.dataframeservice.resource.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditEntryContent;

public class ReportingEventItemResource extends ServiceBaseResource {
	private static Logger LOGGER = LoggerFactory.getLogger(ReportingEventItemResource.class);

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
					ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
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
						List<ReportingEventItem> reportingItems = new ArrayList<>();

						for (String id : idsArray) {
							
							ReportingEventItem ri = dao.getReportingItem(id);

							if (userId != null) {
								// check for privileges to do this:
								AuthorizationDAO auth = new AuthorizationDAO();
								boolean isOk = auth.checkPrivileges("publishing", "POST", userId);
								
								if (ri != null && !ri.isDeleteFlag() && isOk) {
									reportingItems.add(ri);
								}
							} else {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "Modify User cannot be determined");
							}
						}

						if (Props.isAudit()) {
							for (ReportingEventItem ri : reportingItems) {
								/*asc.logAuditEntry("Access of Reporting Event Item", ri.getEquipId(),
										"ReportingEventItem", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
										ri.getVersionNumber());
								*/
								AuditDetails details = asc.new AuditDetails("Access of Reporting Event Item", ri, userId);
								
								AuditEntryContent detailsContent = asc.new AuditEntryContent();
								Assembly re = (Assembly)dao.getEquipObject(ri.getReportingEventId());
								Dataframe df = (Dataframe)dao.getEquipObject(ri.getDataFrameId());
								details.setContextEntity(df);
								detailsContent.setReportingEventId(re.getEquipId());
								detailsContent.setReportingEventType(re.getItemType());
								details.setAuditContentEntity(detailsContent);
								asc.logAuditEntryAsync(details);
							}
						}

						json = marshalObject(reportingItems);
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
			// NO WORKFLOW ITEM FOR REPORTING EVENT ITEM
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			List<ReportingEventItem> newReportingItems = null;
			Set<String> studyIds = new HashSet<String>();

			String userId = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();

					if (jsonBody != null) {
						// Note that by new is meant new version. That is, if they post to the same
						// equip id more than once
						// then we create a new version
						List<ReportingEventItem> reportingItems = unmarshalObject(jsonBody, ReportingEventItem.class);
						ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
						newReportingItems = new ArrayList<>();
						AssemblyDAO assemblyDAO = getAssemblyDAO();

						userId = request.headers(Const.IAMPFIZERUSERCN);
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						// Dont allow this function to proceed if the RE is locked or locked by user --
						// new requirement 12/18/18
						for (ReportingEventItem ri : reportingItems) {
							Assembly a = assemblyDAO.getAssembly(ri.getReportingEventId());
							if (a.getLockedByUser() != null) {
								if (!a.getLockedByUser().equalsIgnoreCase(userId)) {
									Spark.halt(HTTPStatusCodes.FORBIDDEN,
											"This reporting event is locked by user " + a.getLockedByUser());
								}
							}
						}
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges("reporting event", "POST", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to post a reporting event item");
						}

						Map<String, List<ReportingEventItem>> map = new HashMap<>();
						for (ReportingEventItem ri : reportingItems) {
							if (ri.getCreatedBy() == null) {
								ri.setCreatedBy(userId);
							}

							/*for (Comment comment : ri.getComments()) {
								if (comment.getCreated() == null) {
									comment.setCreated(ri.getCreated());
								}
								if (comment.getCreatedBy() == null) {
									comment.setCreatedBy(ri.getCreatedBy());
								}
							}*/
							ServiceBaseResource.setSubInfo(ri, userId);

							if (ri.getCreated() == null) {
								ri.setCreated(new Date());
							}

							// ri.getPublishItem().setPublishStatus(PUBLISH_STATUS_PUBLISHED);
							// ri.getPublishItem().setPublishedDate(new Date());

							ri.getPublishItem().setCreated(ri.getCreated());
							ri.getPublishItem().setCreatedBy(ri.getCreatedBy());
							ri.getPublishItem().setEquipId(EquipIdCalculator.calculate("publish item"));

							PublishItemPublishStatusChangeWorkflow scw = new PublishItemPublishStatusChangeWorkflow();
							scw.setCreated(ri.getCreated() == null ? new Date() : ri.getCreated());
							scw.setCreatedBy(ri.getCreatedBy() == null ? "" : ri.getCreatedBy());
							scw.setPublishItemPublishStatusChangeDescription("Item Published");
							scw.setPublishStatus(ri.getPublishItem().getPublishStatus());
							List<PublishItemPublishStatusChangeWorkflow> scws = new ArrayList<PublishItemPublishStatusChangeWorkflow>();
							scws.add(scw);
							ri.getPublishItem().setWorkflowItems(scws);

							applyVersionIncrementingLogic(ri, dao);
							ReportingEventItem newRi = dao.insertReportingItem(ri);
							newRi = dao.getReportingItem(newRi.getId());
							newReportingItems.add(newRi);

							// update the dataframe with the publish status of this ri
							ModeShapeDAO bdao = new ModeShapeDAO();
							PropertiesPayload pp = new PropertiesPayload();

							Assembly parentAssembly = assemblyDAO.getAssembly(ri.getReportingEventId());
							// Update the reporting event modified date
							bdao.updateModified(parentAssembly.getId(), userId);
							if (ri.getPublishItem() != null && ri.getPublishItem().getPublishStatus() != null) {
								if (ri.getPublishItem().getPublishStatus().equals(PUBLISH_STATUS_PUBLISHED)
										&& ri.isIncluded()) {
									pp.addProperty("equip:published", true);
								}
							}

							if (parentAssembly != null) {
								Metadatum metadatum = parentAssembly.getMetadatum("Reporting Event Release Status");
								if (metadatum != null) {
									if (metadatum.getValue().get(0).equals("Released") && ri.isIncluded()) {
										pp.addProperty("equip:released", true);
									}
								}
							}
							if (pp.properties.size() > 0) {
								bdao.updateNode(ri.getDataFrameId(), pp);
								if (parentAssembly != null) {
									bdao.updateNode(parentAssembly.getId(), pp);
								}
							}
							
							List<ReportingEventItem> list = !map.containsKey(ri.getReportingEventId()) ? new ArrayList<>()
									: map.get(ri.getReportingEventId());
							list.add(newRi);
							map.put(ri.getReportingEventId(), list);
						}
						
						DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
						for (Entry<String, List<ReportingEventItem>> e : map.entrySet()) {
							Assembly parentAssembly = assemblyDAO.getAssembly(e.getKey());
							if (parentAssembly != null) {
								boolean needsAtrUpdate = false;
								for(ReportingEventItem rei : e.getValue()) {
									parentAssembly.getReportingItemIds().add(rei.getId());
									if(rei.getDataFrameId() != null && !needsAtrUpdate) {
										Dataframe df = dDao.getDataframe(rei.getDataFrameId());
										if(df.getSubType() == null || !df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
											needsAtrUpdate = true;
										}
									}
								}
								
								if(needsAtrUpdate) {
									parentAssembly.setAtrIsCurrent(false);
								}
								
								assemblyDAO.updateAssembly(parentAssembly.getId(), parentAssembly);
								studyIds.addAll(parentAssembly.getStudyIds());
							}
						}
						
						if (Props.isAudit()) {
							for (ReportingEventItem rei : newReportingItems) {
								/*asc.logAuditEntry("Creation of Reporting Event Item", rei.getEquipId(),
										"ReportingEventItem", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
										rei.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Creation of Reporting Event Item", rei, userId);
								AuditEntryContent detailsContent = asc.new AuditEntryContent();
								Assembly re = (Assembly)dao.getEquipObject(rei.getReportingEventId());
								Dataframe df = (Dataframe)dao.getEquipObject(rei.getDataFrameId());
								details.setContextEntity(df);
								detailsContent.setReportingEventId(re.getEquipId());
								detailsContent.setReportingEventType(re.getItemType());
								details.setAuditContentEntity(detailsContent);
								asc.logAuditEntryAsync(details);
								DataframeDAO dfDAO = new DataframeDAOImpl();

								if (rei.getDataFrameId() != null) {
									Dataframe associatedDataframe = dfDAO.getDataframe(rei.getDataFrameId());
									if (associatedDataframe != null && rei.getAssemblyId() != null
											&& !rei.getAssemblyId().isEmpty()
											&& associatedDataframe.getDataframeType() != null
											&& associatedDataframe.getId() != null
											&& associatedDataframe.getEquipId() != null) {
										Assembly associatedAssembly = assemblyDAO.getAssembly(rei.getAssemblyId());

										if (associatedAssembly != null) {
											if (associatedDataframe.getDataframeType()
													.equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
												/*asc.logAuditEntry(
														Dataframe.PRIMARY_PARAMETERS_TYPE
																+ " were included in the Reporting Event",
														associatedDataframe.getEquipId(), "ReportingEventItem", userId,
														Props.isAudit(), Const.AUDIT_SUCCESS,
														associatedDataframe.getVersionNumber(),
														"" + associatedAssembly.getEquipId() + ".v"
																+ associatedAssembly.getVersionNumber(),
														null);*/
												AuditDetails details1 = asc.new AuditDetails(Dataframe.PRIMARY_PARAMETERS_TYPE
														+ " were included in the Reporting Event", associatedDataframe, userId);
												details1.setContextEntity(associatedAssembly);
												asc.logAuditEntryAsync(details1);

											} else if (associatedDataframe.getDataframeType()
													.equals(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
												/*asc.logAuditEntry(
														Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE
																+ " was included in the Reporting Event",
														associatedDataframe.getEquipId(), "ReportingEventItem", userId,
														Props.isAudit(), Const.AUDIT_SUCCESS,
														associatedDataframe.getVersionNumber(),
														"" + associatedAssembly.getEquipId() + ".v"
																+ associatedAssembly.getVersionNumber(),
														null);*/
												AuditDetails details1 = asc.new AuditDetails(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE
														+ " was included in the Reporting Event", associatedDataframe, userId);
												details1.setContextEntity(associatedAssembly);
												asc.logAuditEntryAsync(details1);

											} else if (associatedDataframe.getDataframeType()
													.equals(Dataframe.KEL_FLAGS_TYPE)) {
												/*asc.logAuditEntry(
														Dataframe.KEL_FLAGS_TYPE
																+ " were included in the Reporting Event",
														associatedDataframe.getEquipId(), "ReportingEventItem", userId,
														Props.isAudit(), Const.AUDIT_SUCCESS,
														associatedDataframe.getVersionNumber(),
														"" + associatedAssembly.getEquipId() + ".v"
																+ associatedAssembly.getVersionNumber(),
														null);*/
												AuditDetails details1 = asc.new AuditDetails(Dataframe.KEL_FLAGS_TYPE
														+ " were included in the Reporting Event", associatedDataframe, userId);
												details1.setContextEntity(associatedAssembly);
												asc.logAuditEntryAsync(details1);
											} else if (associatedDataframe.getDataframeType()
													.equals(Dataframe.DATA_TRANSFORMATION_TYPE)
													&& rei.getAssemblyId() != null
													&& assemblyDAO
															.getAssemblyByMemberDataframeId(associatedDataframe.getId())
															.size() > 0
													&& assemblyDAO
															.getAssemblyByMemberDataframeId(associatedDataframe.getId())
															.get(0).getAssemblyType().equals(Assembly.ANALYSIS_TYPE)) {
												/*asc.logAuditEntry(
														"Concentration data was included in the Reporting Event",
														associatedDataframe.getEquipId(), "ReportingEventItem", userId,
														Props.isAudit(), Const.AUDIT_SUCCESS,
														associatedDataframe.getVersionNumber(),
														"" + associatedAssembly.getEquipId() + ".v"
																+ associatedAssembly.getVersionNumber(),
														null);*/
												AuditDetails details1 = asc.new AuditDetails("Concentration data was included in the Reporting Event"
														, associatedDataframe, userId);
												details1.setContextEntity(associatedAssembly);
												asc.logAuditEntryAsync(details1);
											}
										}
									}
								}
							}
						}

						// call opmeta service to update modification time on associated protocol
						try {
							OpmetaServiceClient osc = new OpmetaServiceClient();
							osc.setHost(Props.getOpmetaServiceServer());
							osc.setPort(Props.getOpmetaSerivcePort());
							for (String studyId : studyIds) {
								LOGGER.info("ReportingEventItemResource: update protocol for study id=" + studyId);
								osc.updateProtocolModifiedDate(userId, studyId);
							}
						} catch (Exception err) {
							LOGGER.warn("ReportingEventItemResource: Error updating protocol modification time", err);
						}

						json = marshalObject(newReportingItems);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Reporting Items were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && newReportingItems != null) {
						if (Props.isAudit()) {
							for (ReportingEventItem rei : newReportingItems) {
								/*asc.logAuditEntry(
										"Attempt to create ReportingEventItem failed with exception " + ex.getMessage(),
										rei.getEquipId(), "ReportingEventItem", userId, Props.isAudit(),
										Const.AUDIT_SUCCESS, rei.getVersionNumber());*/
								AuditDetails details1 = asc.new AuditDetails("Attempt to create ReportingEventItem failed with exception " + ex.getMessage()
										, rei, userId);
								//details1.setContextEntity(associatedAssembly);
								asc.logAuditEntryAsync(details1);
							}
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

		private void applyVersionIncrementingLogic(ReportingEventItem ri, ReportingAndPublishingDAO dao) {
			EquipVersionableListGetter reportingEventItemSiblingGetter = equipId -> {
				List<ReportingEventItem> items = dao.getReportingEventItemsByEquipId(equipId);
				List<EquipVersionable> ev = new ArrayList<>();
				for (ReportingEventItem item : items) {
					ev.add(item);
				}

				return ev;
			};

			new VersioningDAO().applyVersionIncrementingLogic(ri, "reporting event item",
					reportingEventItemSiblingGetter);
		}
	};

	public static final Route getByDataframeId = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String dataframeId = request.params(":id");
				if (dataframeId != null) {
					String userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}

					List<ReportingEventItem> reportingItems = new ArrayList<>();
					ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
					List<ReportingEventItem> reis = dao.getReportingEventItemByDataframeId(dataframeId);
					AuthorizationDAO auth = new AuthorizationDAO();
					for (ReportingEventItem ri : reis) {
						boolean isOk = auth.checkPrivileges("publishing", "POST", userId);
						if (ri != null && !ri.isDeleteFlag() && isOk) {
							reportingItems.add(ri);
							if (Props.isAudit()) {
								/*asc.logAuditEntry("Access of Reporting Event Item via dataframe", ri.getEquipId(),
										"ReportingEventItem", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
										ri.getVersionNumber());*/
								AuditDetails details1 = asc.new AuditDetails("Access of Reporting Event Item via dataframe"
								, ri, userId);
								AuditEntryContent detailsContent = asc.new AuditEntryContent();
								Assembly re = (Assembly)dao.getEquipObject(ri.getReportingEventId());
								Dataframe df = (Dataframe)dao.getEquipObject(ri.getDataFrameId());
								details1.setContextEntity(df);
								detailsContent.setReportingEventId(re.getEquipId());
								detailsContent.setReportingEventType(re.getItemType());
								details1.setAuditContentEntity(detailsContent);
								//details1.setContextEntity(associatedAssembly);
								asc.logAuditEntryAsync(details1);
							}
						}
					}

					if (Props.isAudit()) {
						for (ReportingEventItem ri : reportingItems) {
							/*asc.logAuditEntry("Access of Reporting Event Item via Dataframe id", ri.getEquipId(),
									"ReportingEventItem", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									ri.getVersionNumber(), dataframeId, null);*/
							AuditDetails details1 = asc.new AuditDetails("Access of Reporting Event Item via Dataframe id"
									, ri, userId);
							AuditEntryContent detailsContent = asc.new AuditEntryContent();
							Assembly re = (Assembly)dao.getEquipObject(ri.getReportingEventId());
							Dataframe df = (Dataframe)dao.getEquipObject(ri.getDataFrameId());
							details1.setContextEntity(df);
							detailsContent.setReportingEventId(re.getEquipId());
							detailsContent.setReportingEventType(re.getItemType());
							details1.setAuditContentEntity(detailsContent);
									//details1.setContextEntity(ri.);
									asc.logAuditEntryAsync(details1);
						}
					}

					json = marshalObject(reportingItems);
					response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No dataframe ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};
}
