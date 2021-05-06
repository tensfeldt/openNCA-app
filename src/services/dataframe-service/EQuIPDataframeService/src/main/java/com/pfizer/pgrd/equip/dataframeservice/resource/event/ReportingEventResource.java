package com.pfizer.pgrd.equip.dataframeservice.resource.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.PublishStatus;
import com.pfizer.pgrd.equip.dataframe.dto.ReleaseStatus;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.MetadataDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.NotificationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;
import com.pfizer.pgrd.equip.services.search.SearchServiceClient;
import com.pfizer.pgrd.equip.utils.ConstAPI;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

public class ReportingEventResource extends ServiceBaseResource {
	private static Logger LOGGER = LoggerFactory.getLogger(ReportingEventResource.class);

	private static final String REPORTING_EVENT_NAME = "reportingEventName";
	private static final String REPORTING_EVENT_TYPE_ID = "reportingEventTypeId";
	private static final String REPORTING_EVENT_TEMPLATE_ID = "reportingEventTemplateId";
	private static final String REPORTING_EVENT_RELEASE_STATUS_KEY = "reportingEventReleaseStatusKey";
	private static final String REPORTING_EVENT_RELEASE_DATE = "reportingEventReleaseDate";

	// private static final String RELEASE_STATUS_UNRELEASED = "Unreleased";
	// private static final String RELEASE_STATUS_RELEASED = "Released";
	// private static final String RELEASE_STATUS_REOPENED = "Re-opened";

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

						// List<String> ids = new ArrayList<>();
						// String[] idsArray = jsonBody.replace("[", "").replace("]",
						// "").replace("\"","").split(",");
						// Collections.addAll(ids, idsArray);
						List<ReportingEvent> reportingEvents = new ArrayList<>();
						
						for (String id : idsArray) {
							AssemblyDAO dao = getAssemblyDAO();
							Assembly a = dao.getAssembly(id);
							
							if (Props.isAudit()) {
								/*asc.logAuditEntry("Access of Reporting Event by list", a.getEquipId(), "ReportingEvent",
										userId, Props.isAudit(), Const.AUDIT_SUCCESS, a.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Access of Reporting Event by list", a, userId);
								//details.setContextEntity(a);
								asc.logAuditEntryAsync(details);
							}

							if (a != null && !a.isDeleteFlag()) {
								ServiceBaseResource.handleUserAccess(a, userId);
								ReportingEvent re = convertAssemblyToReportingEvent(a);
								reportingEvents.add(re);
							}
						}
						
						json = ServiceBaseResource.marshalObject(reportingEvents);
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

	// TODO move this function into the DAO???
	private static ReportingEvent convertAssemblyToReportingEvent(Assembly a) {
		ReportingEvent re = null;
		if (a != null) {
			re = new ReportingEvent();
			re.setId(a.getId());
			re.setVersionNumber(a.getVersionNumber());
			re.setVersionSuperSeded(a.getVersionSuperSeded());
			re.setCommitted((a.isCommitted()));
			re.setComments(a.getComments());
			re.setCreated(a.getCreated());
			re.setCreatedBy(a.getCreatedBy());
			re.setLocked(a.isLocked());
			re.setLockedByUser(a.getLockedByUser());
			re.setModifiedDate(a.getModifiedDate());
			re.setModifiedBy(a.getModifiedBy());
			re.setEquipId(a.getEquipId());
			re.setObsoleteFlag(a.isObsoleteFlag());
			re.setReportingEventName(a.getName());
			re.setReportingEventTypeId(a.getItemType());
			re.setDescription(a.getDescription());
			re.setPublished(a.isPublished());
			re.setReleased(a.isReleased());
			re.setQcStatus(a.getQcStatus());
			re.setAtrIsCurrent(a.atrIsCurrent());
			
			// TODO, remove this from the outward facing DTO
			// re.setName(a.getName());
			// re.setItemType(a.getItemType());

			List<Metadatum> reMetadatum = new ArrayList<>();
			
			// metadatum
			for (Metadatum metadatum : a.getMetadata()) {
				if (metadatum.getKey().equalsIgnoreCase(REPORTING_EVENT_TEMPLATE_ID)) {
					re.setReportingEventTemplateId(metadatum.getValue().get(0));
				} else if (metadatum.getKey().equalsIgnoreCase(REPORTING_EVENT_RELEASE_STATUS_KEY)) {
					re.setReportingEventReleaseStatusKey(metadatum.getValue().get(0));
				} else if (metadatum.getKey().equalsIgnoreCase(REPORTING_EVENT_RELEASE_DATE)) {
					if (metadatum.getValue().isEmpty()) {
						re.setReportingEventReleaseDate("");
					} else {
						re.setReportingEventReleaseDate(metadatum.getValue().get(0));
					}
				} else {
					reMetadatum.add(metadatum);
				}
			}

			re.setMetadata(reMetadatum);

			// String name = map.get(REPORTING_EVENT_NAME) == null ? null :
			// map.get(REPORTING_EVENT_NAME).getValue().get(0);
			// re.setReportingEventName(name);
			//
			// String typeId = map.get(REPORTING_EVENT_TYPE_ID) == null ? null
			// : map.get(REPORTING_EVENT_TYPE_ID).getValue().get(0);
			// re.setReportingEventTypeId(typeId);

			// String templateId = map.get(REPORTING_EVENT_TEMPLATE_ID) == null ? null
			// : map.get(REPORTING_EVENT_TEMPLATE_ID).getValue().get(0);
			// re.setReportingEventTemplateId(templateId);
			//
			// String releaseStatus = map.get(REPORTING_EVENT_RELEASE_STATUS_KEY) == null ?
			// null
			// : map.get(REPORTING_EVENT_RELEASE_STATUS_KEY).getValue().get(0);
			// re.setReportingEventReleaseStatusKey(releaseStatus);
			//
			// String releaseDate = "";
			// Metadatum releaseDateMetadatum = map.get(REPORTING_EVENT_RELEASE_DATE);
			//
			// if(releaseDateMetadatum != null){
			// if(releaseDateMetadatum.getValue() != null){
			// if(releaseDateMetadatum.getValue().size() > 0){
			// releaseDate = map.get(REPORTING_EVENT_RELEASE_DATE).getValue().get(0);
			// }
			// }
			// }
			//
			// re.setReportingEventReleaseDate(releaseDate);

			// reopen reason is only in the workflow object.

			re.setReportingEventItemIds(a.getReportingItemIds());

			re.setReportingEventStatusChangeWorkflows(a.getReportingEventStatusChangeWorkflows());

			re.setObsoleteFlag(a.isObsoleteFlag());
			re.setDeleteFlag(a.isDeleteFlag());

			re.setVersionNumber(a.getVersionNumber());
			re.setVersionSuperSeded(a.getVersionSuperSeded());

			re.setVersionNumber(a.getVersionNumber());
			re.setObsoleteFlag(a.isObsoleteFlag());
			re.setCommitted(a.isCommitted());

			re.setStudyIds(a.getStudyIds());
		}

		return re;
	}

	public static final Route post = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			List<Assembly> assemblies = null;
			Set<String> studyIds = new HashSet<String>();
			
			LOGGER.info("creating reporting event... test");

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();
					if (jsonBody != null) {
						// Note that by new is meant new version. That is, if they post to the same
						// equip id more than once then we create a new version
						List<ReportingEvent> newReportingEvents = unmarshalObject(jsonBody, ReportingEvent.class);
						AssemblyDAO dao = getAssemblyDAO();
						assemblies = new ArrayList<>();
						userId = request.headers(Const.IAMPFIZERUSERCN);
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}

						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges("reporting event", "POST", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to post a reporting event");
						}
						
						for (ReportingEvent re : newReportingEvents) {
							if (re.getCreatedBy() == null || re.getCreatedBy().isEmpty()) {
								re.setCreatedBy(userId);
							}

							for (Comment comment : re.getComments()) {
								if (comment.getCreated() == null) {
									comment.setCreated(re.getCreated());
								}
								if (comment.getCreatedBy() == null) {
									comment.setCreatedBy(re.getCreatedBy());
								}
							}

							Assembly a = new Assembly();
							fillAssemblyFromReportingEvent(re, a);
							
							ServiceBaseResource.setSubInfo(a, userId);
							ServiceBaseResource.handleUserAccess(a, userId);
							
							assemblies.add(a);
						}

						List<ReportingEvent> reportingEvents = new ArrayList<>();
						for (Assembly assembly : assemblies) {
							ReportingEventStatusChangeWorkflow scw = new ReportingEventStatusChangeWorkflow();
							scw.setCreatedBy(assembly.getCreatedBy());
							scw.setCreated(assembly.getCreated());
							scw.setReportingEventReleaseStatusKey(Const.UNRELEASED_STATUS);
							// scw.setReportingEventReleaseStatusKey(RELEASE_STATUS_UNRELEASED);
							scw.setReportingEventReopenReasonKey("");
							// scw.setReportingEventReopenReasonAttachmentId("");
							scw.setReportingEventStatusWorkflowDescription("Reporting Event Created");

							List<ReportingEventStatusChangeWorkflow> scws = new ArrayList<ReportingEventStatusChangeWorkflow>();
							scws.add(scw);
							
							assembly.setReportingEventStatusChangeWorkflows(scws);
							assembly.setAssemblyType(ConstAPI.REPORTING_EVENT_ASSEMBLY_TYPE);
							assembly.setAtrIsCurrent(false);

							applyVersionIncrementingLogic(assembly, dao);
							Assembly outputAssembly1 = dao.insertAssembly(assembly);

							if (Props.isAudit()) {
								LOGGER.info("created reporting event");
								/*asc.logAuditEntry("Creation of Reporting Event (Assembly)",
										outputAssembly1.getEquipId(), "ReportingEvent", userId, Props.isAudit(),
										Const.AUDIT_SUCCESS, outputAssembly1.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Creation of Reporting Event (Assembly)", outputAssembly1, userId);
								//details.setContextEntity(a);
								asc.logAuditEntryAsync(details);
								ReportingEventResource.LOGGER.info("created reporting event... end test");
							}

							ReportingEvent reportingEvent = ReportingEventResource
									.convertAssemblyToReportingEvent(outputAssembly1);

							reportingEvents.add(reportingEvent);

							studyIds.addAll(assembly.getStudyIds());
						}

						// call opmeta service to update modification time on associated protocol
						try {
							OpmetaServiceClient osc = new OpmetaServiceClient();
							osc.setHost(Props.getOpmetaServiceServer());
							osc.setPort(Props.getOpmetaSerivcePort());
							for (String studyId : studyIds) {
								LOGGER.info("ReportingEventResource: update protocol for study id=" + studyId);
								osc.updateProtocolModifiedDate(userId, studyId);
							}
						} catch (Exception err) {
							LOGGER.warn("ReportingEventResource: Error updating protocol modification time", err);
						}

						json = marshalObject(reportingEvents);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Reporting Event representations were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && assemblies != null) {
						if (Props.isAudit()) {
							for (Assembly assembly : assemblies) {
								/*asc.logAuditEntry(
										"Attempt to create ReportingEvent failed with exception " + ex.getMessage(),
										assembly.getEquipId(), "ReportingEvent", userId, Props.isAudit(),
										Const.AUDIT_FAILURE, assembly.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Attempt to create ReportingEvent failed with exception " + ex.getMessage()
								, assembly, userId);
								details.setActionStatus(Const.AUDIT_FAILURE);
								//details.setContextEntity(a);
								asc.logAuditEntryAsync(details);
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

		// TODO move this function into the DAO
		private void fillAssemblyFromReportingEvent(ReportingEvent re, Assembly a) {
			a.setAssemblyType(ConstAPI.REPORTING_EVENT_ASSEMBLY_TYPE);

			for (Comment comment : re.getComments()) {
				a.setCreated(comment.getCreated());
				a.setCreatedBy(comment.getCreatedBy());
			}

			a.setComments(re.getComments());
			a.setCreated(re.getCreated() == null ? new Date() : re.getCreated());

			a.setCreatedBy(re.getCreatedBy());
			a.setModifiedBy(null);
			a.setModifiedDate(null);
			a.setDeleteFlag(false);
			a.setEquipId(re.getEquipId()); // equip id is calculated further down the line in the VersioningDAO
			a.setLocked(re.isLocked());
			a.setLockedByUser(re.getLockedByUser());
			a.setObsoleteFlag(false);
			a.setReportingItemIds(new ArrayList<String>());
			a.setVersionNumber(re.getVersionNumber());
			a.setVersionSuperSeded(false);
			a.setStudyIds(re.getStudyIds());
			a.setItemType(re.getItemType());
			a.setReleased(false);
			a.setPublished(false);
			a.setDescription(re.getDescription());
			a.setAtrIsCurrent(re.atrIsCurrent());
			if(a.atrIsCurrent() == null) {
				a.setAtrIsCurrent(false);
			}
			
			List<Metadatum> metadatum = new ArrayList<>();
			
			a.setName(re.getReportingEventName());
			a.setItemType(re.getReportingEventTypeId());
			metadatum.add(new Metadatum(REPORTING_EVENT_TEMPLATE_ID, re.getReportingEventTemplateId()));
			metadatum.add(new Metadatum(REPORTING_EVENT_RELEASE_STATUS_KEY, Const.UNRELEASED_STATUS));
			// metadatum.add(new Metadatum(REPORTING_EVENT_RELEASE_STATUS_KEY,
			// RELEASE_STATUS_UNRELEASED)); // For
			// blank
			// properties,
			// we
			// simply
			// do
			// not
			// add
			// them
			// to
			// metadata
			// initially
			metadatum.add(new Metadatum(REPORTING_EVENT_RELEASE_DATE, re.getReportingEventReleaseDate()));

			if (re.getMetadata() != null) {
				metadatum.addAll(re.getMetadata());
			}

			a.setMetadata(metadatum);
		}

		private void applyVersionIncrementingLogic(Assembly assembly, AssemblyDAO dao) {
			EquipVersionableListGetter assemblySiblingGetter = equipId -> {
				List<Assembly> assemblies = dao.getAssemblysByEquipId(equipId);
				List<EquipVersionable> retVal = new ArrayList<EquipVersionable>();
				for (Assembly a : assemblies) {
					retVal.add(a);
				}
				return retVal;
			};
			
			new VersioningDAO().applyVersionIncrementingLogic(assembly, assembly.getAssemblyType(),
					assemblySiblingGetter);
		}
	};

	// just set the flag, compute service will enforce locking of child dataframes
	public static final Route putReleaseStatus = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noRepError = "No Release Status representation was provided.";
			String returnJson = null;
			ReportingEvent re = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			ReleaseStatus rs = null;
			String userId = null;
			Assembly assembly = null;

			// test - remove eventually
			// AuditServiceClient asc2 = new AuditServiceClient();
			// asc2.logAuditEntry( "Release Status Changed to Published" ,
			// "12",
			// "Type",
			// "mcconkeyma",
			// "Status=Published" );

			// example of how to get a file, must specify multi-part form in yaml too
			// Part part = request.raw().getPart("file");
			// File attachment = BaseResource.readFile(part);

			try {
				userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();
					if (jsonBody != null) {
						List<ReleaseStatus> list = unmarshalObject(jsonBody, ReleaseStatus.class);
						if (list != null && !list.isEmpty()) {
							rs = list.get(0);
							if (rs != null) {
								AssemblyDAO assemblyDAO = new AssemblyDAOImpl();
								assembly = assemblyDAO.getAssembly(rs.getReportingEventId());
								if (assembly != null) {
									if (assembly.isDeleteFlag()) {
										Spark.halt(HTTPStatusCodes.CONFLICT, "Reporting Event with id "
												+ rs.getReportingEventId() + " was previously deleted");
									}
									// check for lock of reporting event
									if (assembly.getLockedByUser() != null) {
										if (!assembly.getLockedByUser().equalsIgnoreCase(userId)) {
											Spark.halt(HTTPStatusCodes.FORBIDDEN,
													"This reporting event is locked by user "
															+ assembly.getLockedByUser()
															+ " so statuses cannot be updated");
										}
									}
									
									ServiceBaseResource.handleUserAccess(assembly, userId);

									if (!isValidReleaseStatus(rs.getReportingEventReleaseStatusKey())) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"Invalid release status (" + rs.getReportingEventReleaseStatusKey()
														+ ") for id = " + rs.getReportingEventReleaseStatusKey()
														+ ".  Valid release statuses are: " + Const.RELEASED_STATUS
														+ ", " + Const.REOPEN_STATUS);
									}
									
									if(!rs.getReportingEventReleaseStatusKey().equalsIgnoreCase(Const.REOPEN_STATUS)) {
										List<String> reiIds = assembly.getReportingItemIds();
										ReportingAndPublishingDAO reiDAO = new ReportingAndPublishingDAO();
										DataframeDAO dfDAO = new DataframeDAOImpl();
										for (String reiId : reiIds) {
											ReportingEventItem rei = reiDAO.getReportingItem(reiId);
											if (rei.isIncluded() && !rei.isDeleteFlag()) {
												String dfId = rei.getDataFrameId();
												Dataframe df = dfDAO.getDataframe(dfId);
												//For ALM Defect 702 remove supersede check || df.getVersionSuperSeded()
												if (!df.isCommitted()  || df.isDeleteFlag()) {
													Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Dataframe " + dfId
															+ " must be committed, and cannot be deleted.\n"
															+ "For this dataframe:\nisCommitted = " + df.isCommitted()
															//+ "\nversionSuperSeded = " + df.getVersionSuperSeded()
															+ "\ndeleteFlag = " + df.isDeleteFlag());
												}
											}
										}
									}
									
									if (rs.getReportingEventReleaseStatusKey().equals(Const.REOPEN_STATUS)) {
										if (rs.getReopenReason() == null || rs.getReopenReason().equals("")) {
											// need to check authorization here
											AuthorizationDAO auth = new AuthorizationDAO();
											boolean isOk = auth.checkPrivileges("release reopened", "POST", userId);

											if (!isOk) {
												Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userId
														+ " does not have privileges to reopen release status");
											}

											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"When reopening a reporting event a reopen reason must be supplied.  Reporting Event Id: "
															+ rs.getReportingEventId());
										}
									}

									if (rs.getModifiedBy() == null || rs.getModifiedBy().trim().equals("")) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"When modifying release status must specify modifiedBy.  Reporting Event Id: "
														+ rs.getReportingEventId());
									}

									// This should probably be removed given the above if statement. Don't want to
									// muck with it right now though.
									if (rs.getModifiedBy() == null) {
										assembly.setModifiedBy(userId);
									}

									updateMetadatumValue(REPORTING_EVENT_RELEASE_STATUS_KEY,
											rs.getReportingEventReleaseStatusKey(), assembly);
									assembly.setModifiedDate(rs.getModifiedDate());
									if (rs.getReportingEventReleaseStatusKey().equals(Const.RELEASED_STATUS)) {
										updateMetadatumValue(REPORTING_EVENT_RELEASE_DATE,
												DateUtils.stringifyDate(new Date()), assembly);
										assembly.setReleased(true); // need to set property to support equip:searchable
									} else {
										updateMetadatumValue(REPORTING_EVENT_RELEASE_DATE, "", assembly);
										assembly.setReleased(false); // need to set property to support
																		// equip:searchable.
									}
									
									assemblyDAO.updateAssembly(assembly.getId(), assembly, false);
									EntityVersioningResource.updateAtrFlag(assembly, false, userId);

									// AuditServiceClient asc = new AuditServiceClient();
									// asc.logAuditEntry( "Release Status Changed to " +
									// rs.getReportingEventReleaseStatusKey() ,
									// rs.getReportingEventId(),
									// "",
									// rs.getModifiedBy(),
									// "Status=" + rs.getReportingEventReleaseStatusKey() );

									ReportingEventStatusChangeWorkflow scw = new ReportingEventStatusChangeWorkflow();
									scw.setCreatedBy(rs.getModifiedBy());
									scw.setCreated(rs.getModifiedDate());
									scw.setReportingEventReleaseStatusKey(rs.getReportingEventReleaseStatusKey());

									scw.setReportingEventStatusWorkflowDescription("Release Status Changed");
									scw.setReportingEventId(rs.getReportingEventId());
									scw.setReportingEventReopenReasonKey(rs.getReopenReason());
									scw.setReportingEventReopenReasonAttachmentId(rs.getReopenReasonAttachmentId());
									List<Comment> listComments = scw.getComments();
									listComments.addAll(rs.getComments());

									ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
									ReportingEventStatusChangeWorkflow newSCW = dao
											.addReportingEventStatusChangeWorkflow(assembly, scw);

									if (newSCW == null) {
										throw new ErrorCodeException(400, "assembly not found");
									}
									updateDataframeStatus(assembly);

									if (Props.isAudit()) {
										/*asc.logAuditEntry(
												"Changed release status to " + rs.getReportingEventReleaseStatusKey(),
												assembly.getEquipId(), "ReportingEvent", userId, Props.isAudit(),
												Const.AUDIT_SUCCESS, assembly.getVersionNumber());*/
										AuditDetails details = asc.new AuditDetails("Changed release status to " + rs.getReportingEventReleaseStatusKey()
										, assembly, userId);
										
										//details.setContextEntity(a);
										asc.logAuditEntryAsync(details);
									}

									Assembly newAssembly = assemblyDAO.getAssembly(rs.getReportingEventId());

									re = convertAssemblyToReportingEvent(newAssembly);
									// notify whenever this is called
									NotificationDAO ndao = new NotificationDAO();
									NotificationRequestBody body = new NotificationRequestBody();
									event_detail detail = new event_detail();

									body.setEntity_id(re.getEquipId());
									detail.setUser_name(userId);
									List<String> studyIds = re.getStudyIds();
									body.setEntity_type("Reporting Event");
									detail.setReporting_event_id(re.getEquipId());
									detail.setReporting_event_type(re.getItemType());
									List<String> notifComments = new ArrayList<>();

									for (Comment comment : listComments) {
										notifComments.add(comment.getBody());
									}
									detail.setComments(notifComments);
									body.setEventDetail(detail);

									boolean notifyFlag = true;

									if (rs.getReportingEventReleaseStatusKey().equalsIgnoreCase(Const.REOPEN_STATUS)) {
										body.setEvent_type("reopen");
										notifyFlag = ndao.notifyEvent(body, studyIds);
									}
									if (rs.getReportingEventReleaseStatusKey()
											.equalsIgnoreCase(Const.RELEASED_STATUS)) {
										body.setEvent_type("release");
										notifyFlag = ndao.notifyEvent(body, studyIds);
									}

									if (!notifyFlag) {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"Reporting event updated but notification failed");
									}

									// call opmeta service to update modification time on associated protocol
									try {
										OpmetaServiceClient osc = new OpmetaServiceClient();
										osc.setHost(Props.getOpmetaServiceServer());
										osc.setPort(Props.getOpmetaSerivcePort());
										for (String studyId : studyIds) {
											LOGGER.info(
													"ReportingEventResource: update protocol for study id=" + studyId);
											osc.updateProtocolModifiedDate(userId, studyId);
										}
									} catch (Exception err) {
										LOGGER.warn("ReportingEventResource: Error updating protocol modification time",
												err);
									}

									returnJson = marshalObject(re);
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
								} else {
									Spark.halt(HTTPStatusCodes.NOT_FOUND,
											"Reporting Event with id " + rs.getReportingEventId() + " does not exist");
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
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (assembly != null && userId != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry(
									"Attempt to change release status to " + rs.getReportingEventReleaseStatusKey()
											+ " failed with exception: " + ex.getMessage(),
									assembly.getEquipId(), "ReportingEvent", userId, Props.isAudit(),
									Const.AUDIT_FAILURE, assembly.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Attempt to change release status to " + rs.getReportingEventReleaseStatusKey()
							+ " failed with exception: " + ex.getMessage()
							, assembly, userId);
							details.setActionStatus(Const.AUDIT_FAILURE);
							//details.setContextEntity(a);
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

		private void updateMetadatumValue(String key, String value, Assembly assembly) {
			Map<String, Metadatum> map = new HashMap<>();
			for (Metadatum metadatum : assembly.getMetadata()) {
				map.put(metadatum.getKey(), metadatum);
			}
			Metadatum releaseStatusMetadatum = map.get(key);
			List<String> values = new ArrayList<String>();
			values.add(value);
			releaseStatusMetadatum.setValue(values);
			MetadataDAO dao = getMetadataDAO();
			dao.updateMetadata(releaseStatusMetadatum, releaseStatusMetadatum.getId());
		}

		private boolean isValidReleaseStatus(String releaseStatus) {
			return releaseStatus.equals(Const.RELEASED_STATUS) || releaseStatus.equals(Const.REOPEN_STATUS);
		}

		private void updateDataframeStatus(Assembly a) {
			// this needs to run after update has been made
			// it calculates teh status then updates the df if nessary.
			List<String> reiIds = a.getReportingItemIds();
			ReportingAndPublishingDAO reiDAO = new ReportingAndPublishingDAO();
			VersioningDAO vdao = new VersioningDAO();
			DataframeDAO ddao = getDataframeDAO();

			for (String reiId : reiIds) {
				ReportingEventItem rei = reiDAO.getReportingItem(reiId);

				if (rei.isIncluded() && !rei.isDeleteFlag()) {
					Boolean isReleased = vdao.isREReleased(rei.getDataFrameId());
					Dataframe df = ddao.getDataframe(rei.getDataFrameId());
					if (df.isReleased() != isReleased) {
						PropertiesPayload pp = new PropertiesPayload();
						ModeShapeDAO bdao = new ModeShapeDAO();
						pp.addProperty("equip:released", isReleased);
						bdao.updateNode(df.getId(), pp);
					}
				}
			}
		}

	};

	
	public static final Route publish = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String userId = request.headers(Const.IAMPFIZERUSERCN);
				if(userId == null || userId.isEmpty()) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
				
				String reId = request.params(":id");
				if(reId == null || reId.isEmpty()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No reporting event ID was provided.");
				}
				
				AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
				Assembly reportingEvent = aDao.getAssembly(reId);
				if(reportingEvent == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "No reporting event with ID '" + reId + "' could be found.");
				}
				if(!reportingEvent.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Assembly " + reId + " is not a reporting event.");
				}
				
				ServiceBaseResource.handleUserAccess(reportingEvent, userId);
				
				String body = request.body();
				if(body == null || body.isEmpty()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No publishing criteria was provided.");
				}
				
				List<PublishStatus> parameters = ReportingEventResource.unmarshalObject(body, PublishStatus.class);
				List<PublishOutcome> results = new ArrayList<>();
				for(PublishStatus pp : parameters) {
					PublishOutcome result = new PublishOutcome();
					try {
						// This call also handles updating the RE's ATR flag
						PublishItem newPi = PublishItemResource.putPublishStatus.handle(pp, userId, true);
						result.publishedItemId = pp.getPublishItemId();
						result.publishedItem = newPi;
						result.statusCode = HTTPStatusCodes.OK;
					}
					catch(Exception e) {
						int statusCode = 500;
						String error = e.getMessage();
						if(e instanceof HaltException) {
							HaltException he = (HaltException) e;
							statusCode = he.statusCode();
							error = he.body();
						}
						
						result.error = error;
						result.statusCode = statusCode;
					}
					
					results.add(result);
				}
				
				
				
				json = ReportingEventResource.returnJSON(results, response);
			}
			catch(Exception e) {
				ServiceExceptionHandler.handleException(e);
			}
			
			return json;
		}
		
		class PublishOutcome {
			public String publishedItemId;
			public PublishItem publishedItem;
			public int statusCode;
			public String error;
		}
	};
	
	public static final Route getReportingEventsByStudyIds = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				checkContentTypeIsJSON(request); // move this function into an HTTP utility class where it can be
													// reused.
				checkBodyExists(request); // move this function into an HTTP utility class where it can be reused.

				// Should not be manually parsing JSON
				String jsonBody = request.body();
				List<String> studyIds = new ArrayList<String>();
				String[] studyIdsArray = jsonBody.replace("[", "").replace("]", "").replace("\"", "").split(",");

				String message = "Retrieving reporting events for study IDs " + jsonBody + ".\nHEADERS";
				HttpServletRequest hsr = request.raw();
				Enumeration<String> headers = hsr.getHeaderNames();
				while (headers.hasMoreElements()) {
					String header = headers.nextElement();
					String value = request.headers(header);
					message += "\n" + header + ": " + value;
				}

				LOGGER.info(message);
				System.out.println(message);

				Collections.addAll(studyIds, studyIdsArray);
				
				//ReportingAndPublishingDAO dao = new ReportingAndPublishingDAO();
				SearchServiceClient ssClient = ModeShapeDAO.getSearchServiceClient();
				
				long time = System.currentTimeMillis();
				//List<Assembly> assemblies1 = dao.getReportingEventAssembliesByStudyIds(studyIds);
				List<Assembly> assemblies1 = new ArrayList<>();
				//List<EquipObject> objects = ssClient.searchObjectsByStudyId(studyIds, "Assembly", Assembly.REPORTING_EVENT_TYPE);
				List<EquipObject> objects = ssClient.searchObjectsByStudyId(studyIds);
				Map<String, EquipObject> dataMap = new HashMap<>();
				for(EquipObject eo : objects) {
					dataMap.put(eo.getId(), eo);
				}
				
				time = System.currentTimeMillis() - time;
				System.out.println("Took " + time + "ms to fetch reporting events.");
				
				// AUTHORIZATION
				time = System.currentTimeMillis();
				Map<String, Boolean> accessMap = new HashMap<>();
				for(EquipObject eo : objects) {
					if(eo instanceof Assembly) {
						Assembly a = (Assembly)eo;
						if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
							//boolean canView = ServiceBaseResource.userHasAccess(a, userId);
							boolean canView = ServiceBaseResource.userHasAccess(a, userId, dataMap);
							if(!canView) {
								AuthorizationDAO.maskAssembly(a);
							}
							
							assemblies1.add(a);
							accessMap.put(a.getId(), canView);
						}
					}
				}
				time = System.currentTimeMillis() - time;
				System.out.println("Took " + time + "ms to authorize all items.");
				
				time = System.currentTimeMillis();
				List<ReportingEvent> reportingEvents = reFetchAssembliesToAlsoGetCommentsAndMetadata(assemblies1, accessMap);
				//List<Assembly> reportingEvents = assemblies1;
				time = System.currentTimeMillis() - time;
				System.out
						.println("Took " + time + "ms to refetch reporting events to populate comments and metadata.");

				if (Props.isAudit()) {
					if (!assemblies1.isEmpty()) {
						long avg = 0;
						for (Assembly reAssembly : assemblies1) {
							long t = System.currentTimeMillis();
							/*asc.logAuditEntryAsync("Access of Reporting Event by study ID.", reAssembly.getEquipId(),
									"ReportingEvent", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									reAssembly.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Access of Reporting Event by study ID."
							, reAssembly, userId);
							
							asc.logAuditEntryAsync(details);
							t = System.currentTimeMillis() - t;
							avg += t;
						}
						avg = (avg / assemblies1.size());
						System.out.println("Took an average of " + avg + "ms to audit an entry (" + assemblies1.size()
								+ " item(s)).");
					}
				}

				json = marshalObject(reportingEvents);
				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
		
		private void populateAuthTable(EquipObject eo, Map<String, EquipObject> map) {
			if(map == null) {
				map = new HashMap<>();
			}
			
			if(eo instanceof Dataframe) {
				map.put(eo.getId(), eo);
			}
			else if(eo instanceof Assembly) {
				Assembly a = (Assembly) eo;
			}
		}
		
		private List<ReportingEvent> reFetchAssembliesToAlsoGetCommentsAndMetadata(List<Assembly> assemblies1, Map<String, Boolean> accessMap) {
			List<ReportingEvent> reportingEvents = new ArrayList<ReportingEvent>();

			if (!assemblies1.isEmpty()) {
				long avg = 0;
				for (Assembly assembly : assemblies1) {
					long t = System.currentTimeMillis();
					AssemblyDAO dao = getAssemblyDAO();
					//Assembly a = dao.getAssembly(assembly.getId());
					Assembly a = assembly;

					if (a != null && !a.isDeleteFlag()) {
						ReportingEvent re = convertAssemblyToReportingEvent(a);
						re.setUserHasAccess(accessMap.get(a.getId()));
						reportingEvents.add(re);
					}
					t = System.currentTimeMillis() - t;
					avg += t;
				}
				avg = (avg / assemblies1.size());
				System.out.println("\tTook an average of " + avg + "ms to convert an Assembly to ReportingEvent ("
						+ assemblies1.size() + " item(s)).");
			}

			return reportingEvents;
		}

		// move this function into an HTTP utility class where it can be reused.
		private void checkBodyExists(Request request) {
			String jsonBody = request.body();
			if (jsonBody == null || jsonBody.trim().equals("")) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Body provided.");
			}
		}

		// move this function into an HTTP utility class where it can be reused.
		private void checkContentTypeIsJSON(Request request) {
			String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);

			if (contentType == null || !contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
			}
		}

	};
}
