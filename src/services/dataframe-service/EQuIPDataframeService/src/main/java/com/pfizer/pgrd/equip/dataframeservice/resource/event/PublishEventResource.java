package com.pfizer.pgrd.equip.dataframeservice.resource.event;

import java.util.ArrayList;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishEvent;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class PublishEventResource extends ServiceBaseResource {
	private static Logger log = LoggerFactory.getLogger(PublishEventResource.class);

	protected static final String PUBLISHING_EVENT_PUBLISH_STATUS_KEY = "PUBLISHING_EVENT_PUBLISH_STATUS_KEY";
	protected static final String PUBLISHING_EVENT_EXPIRATION_DATE = "PUBLISHING_EVENT_EXPIRATION_DATE";
	protected static final String PUBLISHING_EVENT_NAME = "PUBLISHING_EVENT_NAME";
	protected static final String PUBLISHING_EVENT_PUBLISH_DATE = "PUBLISHING_EVENT_PUBLISH_DATE";

	public static final String PUBLISHING_EVENT_ASSEMBLY_TYPE = "Publishing Event";

	public static final Route getList = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

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
						
						List<PublishEvent> publishingEvents = new ArrayList<>();
						for (String id : idsArray) {
							AssemblyDAO dao = getAssemblyDAO();
							Assembly a = dao.getAssembly(id);

							if (a != null) {
								if(Props.isAudit()){
									/*asc.logAuditEntry(	"Access of Publish Event by list",
														a.getEquipId(),
														"PublishingEvent",
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														a.getVersionNumber());*/
									AuditDetails details = asc.new AuditDetails("Access of Publish Event by list", a, userId);
									details.setContextEntity(a);
									asc.logAuditEntryAsync(details);
								}

								PublishEvent re = convertAssemblyToPublishingEvent(a);
								publishingEvents.add(re);
							}
						}

						json = marshalObject(publishingEvents);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No event IDs were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			}
			catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

	public static final Route post = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();
					if (jsonBody != null) {
						List<PublishEvent> newPublishingEvents = unmarshalObject(jsonBody, PublishEvent.class);
						AssemblyDAO dao = getAssemblyDAO();
						List<Assembly> assemblies = new ArrayList<>();

						for (PublishEvent pe : newPublishingEvents) {
							if (pe.getCreatedBy() == null) {
								String userId = request.headers(Const.IAMPFIZERUSERCN);
								if (userId != null) {
									pe.setCreatedBy(userId);
								} else {
									Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
								}
							}

							Assembly a = new Assembly();

							a.setAssemblyType(PUBLISHING_EVENT_ASSEMBLY_TYPE);
							a.setComments(pe.getComments());
							a.setCreated(pe.getCreated());
							a.setCreatedBy(pe.getCreatedBy());
							a.setEquipId(EquipIdCalculator.calculate("publishing event"));
							a.setName(pe.getName());

							List<Metadatum> metadatum = new ArrayList<>();

							metadatum.add(new Metadatum(PUBLISHING_EVENT_PUBLISH_STATUS_KEY,
									pe.getPublishingEventPublishStatusKey()));
							metadatum.add(new Metadatum(PUBLISHING_EVENT_EXPIRATION_DATE,
									pe.getPublishingEventExpirationDate()));
							metadatum.add(new Metadatum(PUBLISHING_EVENT_NAME, pe.getPublishingEventName()));
							metadatum.add(
									new Metadatum(PUBLISHING_EVENT_PUBLISH_DATE, DateUtils.stringifyDate(new Date())));

							a.setMetadata(metadatum);
							
							ServiceBaseResource.setSubInfo(a, pe.getCreatedBy());

							// this code to be removed when a folder decision is finally made
							// List<String> studyIds = new ArrayList<String>();
							// studyIds.add("B085:B0851001");
							// a.setStudyIds(studyIds);
							
							Assembly outputAssembly = dao.insertAssembly(a);
							assemblies.add(outputAssembly);
						}

						List<PublishEvent> publishingEvents = new ArrayList<>();

						for (Assembly assembly : assemblies) {
							PublishEvent publishingEvent = PublishEventResource
									.convertAssemblyToPublishingEvent(assembly);
							// *********************************************
							// THERE IS NO WORKFLOW ITEM FOR A PUBLISH EVENT
							// *********************************************
							publishingEvents.add(publishingEvent);
						}

						json = marshalObject(publishingEvents);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No events were provided.");
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

	private static PublishEvent convertAssemblyToPublishingEvent(Assembly a) {
		PublishEvent pe = new PublishEvent();

		pe.setComments(a.getComments());
		pe.setCreated(a.getCreated());
		pe.setCreatedBy(a.getCreatedBy());
		pe.setEquipId(a.getEquipId());
		pe.setId(a.getId());
		pe.setName(a.getName());

		// metadatum
		Map<String, Metadatum> map = new HashMap<>();
		for (Metadatum metadatum : a.getMetadata()) {
			map.put(metadatum.getKey(), metadatum);
		}

		String expirationDate = map.get(PUBLISHING_EVENT_EXPIRATION_DATE) == null ? null
				: map.get(PUBLISHING_EVENT_EXPIRATION_DATE).getValue().get(0);
		pe.setPublishingEventExpirationDate(expirationDate);

		String name = map.get(PUBLISHING_EVENT_NAME) == null ? null : map.get(PUBLISHING_EVENT_NAME).getValue().get(0);
		pe.setPublishingEventName(name);

		String publishDate = map.get(PUBLISHING_EVENT_PUBLISH_DATE) == null ? null
				: map.get(PUBLISHING_EVENT_PUBLISH_DATE).getValue().get(0);
		pe.setPublishingEventPublishedDate(publishDate);

		String publishStatusKey = map.get(PUBLISHING_EVENT_PUBLISH_STATUS_KEY) == null ? null
				: map.get(PUBLISHING_EVENT_PUBLISH_STATUS_KEY).getValue().get(0);
		pe.setPublishingEventPublishStatusKey(publishStatusKey);

		pe.setPublishItemIds(a.getPublishItemIds());

		return pe;
	}
}
