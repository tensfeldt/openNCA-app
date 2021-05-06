package com.pfizer.pgrd.equip.services.audit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

import spark.Request;

public class AuditServiceClient extends BaseClient {
	public AuditServiceClient() throws ServiceCallerException {
		this(null, 0);
	}

	public AuditServiceClient(String host, int port) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
	}

	public void logAuditEntry(String action, EquipObject entity, String userId) throws ServiceCallerException {
		this.logAuditEntry(action, entity, userId, false);
	}

	public void logAuditEntryAsync(String action, EquipObject entity, String userId) throws ServiceCallerException {
		this.logAuditEntry(action, entity, userId, true);
	}

	public void logAuditEntryAsync(String action, EquipObject entity, String userId, EquipObject context)
			throws ServiceCallerException {
		this.logAuditEntry(action, entity, userId, context, true);
	}

	private void logAuditEntry(String action, EquipObject entity, String userId, boolean isAsync)
			throws ServiceCallerException {
		this.logAuditEntry(action, entity, userId, null, isAsync);
	}

	private void logAuditEntry(String action, EquipObject entity, String userId, EquipObject context, boolean isAsync)
			throws ServiceCallerException {
		AuditDetails details = new AuditDetails();
		details.setAction(action);
		details.setEntity(entity);
		details.setUser(userId);
		details.setContextEntity(context);

		this.logAuditEntry(details, isAsync);
	}

	public void logAuditEntry(AuditDetails details) throws ServiceCallerException {
		this.logAuditEntry(details, false);
	}

	public void logAuditEntryAsync(AuditDetails details) throws ServiceCallerException {
		this.logAuditEntry(details, true);
	}

	private void getAuditEntryContent(String user, String studyId, AuditEntryContent content) {
		String program = null;
		String protocol = null;
		
		String response = "";
		if (studyId != null) {
			String results[] = studyId.split(":");
			if (results.length > 1) {
				program = results[0];
				protocol = results[1];
			}
			// try to split if form is B5121002?
		}
		
		if (program != null && protocol != null) {
			String url = this.getBaseURI() + "/opmeta/nodes/programs/" + program + "/protocols/" + protocol;
			try {
				ServiceResponse sr = this.get(url);
				response = sr.getResponseAsString();
				Gson gson = new Gson();
				OpMetadataResponse r = gson.fromJson(response, OpMetadataResponse.class);
				Program pgm = r.getPrograms().get(0);
				Protocol p = pgm.getProtocols().get(0);
				content.setStudyBlindingStatus(p.getStudyBlindingStatus());
				content.setStudyRestrictionStatus(p.getStudyRestrictionStatus());
				content.setStudyId(p.getStudyId());
				List<ProtocolAlias> aliases = p.getProtocolAliases();
				String protocolId = "";
				for (ProtocolAlias pAlias : aliases) {
					if (pAlias.getAliasType().equalsIgnoreCase("PROTOCOL ID")) {
						protocolId = pAlias.getStudyAlias();
						break;
					}
				}
				
				content.setProtocolId(protocolId);
			} catch (Exception e) {
				String str = "GET:" + url + " failed: " + e.getMessage();
				
				e.printStackTrace();
				System.out.println(e.getMessage());
				throw new IllegalStateException(str, e);
			}
		}

	}

	private void logAuditEntry(AuditDetails details, boolean isAsync) throws ServiceCallerException {
		
		if (details != null) {
			if(this.getUser() == null || this.getUser().isEmpty())
				this.setUser(details.getUser());
			Gson gson = new Gson();
			System.out.println(gson.toJson(details));
			if (this.getUser() == null || this.getUser().isEmpty())
				this.setUser(details.getUser());
			HttpServletRequest request = details.getRequest();
			String reHeader = null;
			String actualUser = null;
			
			if (request != null && request.getHeaderNames() != null) {
				if (request.getHeader("EQUIP_CONTENTOBJ") != null) {
					reHeader = request.getHeader("EQUIP_CONTENTOBJ");

				}
				if (request.getHeader("EQUIP_ACTUALUSERID") != null)
					actualUser = request.getHeader("EQUIP_ACTUALUSERID");
			}
			

			AuditEntry entry = new AuditEntry();

			entry.action = details.getAction();
			entry.actionStatus = details.getActionStatus();
			entry.content = details.getContent();
			entry.scriptId = details.getScriptId();

			if (actualUser != null && !actualUser.isEmpty())
				entry.userId = actualUser;
			else
				entry.userId = details.getUser();

			AuditEntryContent content = new AuditEntryContent();
			if (details.getAuditContentEntity() != null)
				content = details.getAuditContentEntity();
			content.setOtherContent(details.getContent());

			if (details.getEntity() != null) {
				EquipObject entity = details.getEntity();
				
				entry.entityType = entity.getEntityType();
				if (entity instanceof Dataframe) {
					Dataframe df = (Dataframe) entity;
					content.setDataBlindingStatus(df.getDataBlindingStatus());
					content.setDataRestrictionStatus(df.getRestrictionStatus());
					entry.entityType = df.getDataframeType();
					getAuditEntryContent(this.getUser(), df.getStudyIds().get(0), content);
				} else if (entity instanceof Assembly) {
					Assembly a = (Assembly) entity;
					entry.entityType = a.getAssemblyType();
					
					// ReportingEvent
					
					if (entry.entityType.equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
						content.setEntityType(entry.entityType);
						content.setReportingEventType(a.getItemType());
						content.setReportingEventId(a.getEquipId());
						
					}
					
					getAuditEntryContent(this.getUser(), a.getStudyIds().get(0), content);
				} else if (entity instanceof ReportingEventItem) {
					//ReportingEventItem rei = (ReportingEventItem) entity;
					if (details.getContextEntity() != null && details.getContextEntity() instanceof Dataframe) {
						Dataframe dfre = (Dataframe) details.getContextEntity();
						getAuditEntryContent(this.getUser(), dfre.getStudyIds().get(0), content);
					}

				}

				if (entity instanceof EquipID) {
					EquipID ei = (EquipID) entity;
					entry.entityId = ei.getEquipId();
					if (entity instanceof EquipVersionable) {
						EquipVersionable ev = (EquipVersionable) entity;
						entry.entityVersion = ev.getVersionNumber() + "";
					}
					
				} else {
					entry.entityId = entity.getId();
				}
				if(entry.entityType == null || entry.entityType.isEmpty()) {
					entry.entityType =entity.getEntityType();
				}
			}

			if (reHeader != null && !reHeader.isEmpty()) {
				AuditEntryContent re = gson.fromJson(reHeader, AuditEntryContent.class);
				EquipObject obj = new EquipObject();
				obj.setId(re.getReportingEventId());
				obj.setEntityType(re.getReportingEventType());
				details.setContextEntity(obj);
				if (content.getReportingEventId() == null || content.getReportingEventId().isEmpty()) {
					content.setReportingEventId(re.getReportingEventId());
				}
				if (content.getReportingEventType() == null || content.getReportingEventType().isEmpty()) {
					content.setReportingEventType(re.getReportingEventType());
				}

			}

			if (details.getContextEntity() != null) {
				EquipObject contextEntity = details.getContextEntity();
				if (contextEntity instanceof EquipID) {
					EquipID ei = (EquipID) contextEntity;
					entry.contextEntityId = ei.getEquipId();
					if (contextEntity instanceof EquipVersionable) {
						EquipVersionable ev = (EquipVersionable) contextEntity;
						entry.contextEntityId += " v" + ev.getVersionNumber();
					}
				} else {
					entry.contextEntityId = contextEntity.getId();
				}
			}

			String strContent = gson.toJson(content);
			details.setContent(strContent);
			
			entry.content = strContent;
			System.out.println("Audit data sent below");
			System.out.println(gson.toJson(entry));
			this.logAuditEntry(entry, isAsync);
		}
	}

	public void logAuditEntry(String action, String id, String type, String user, String actionStatus, long version)
			throws ServiceCallerException {
		logAuditEntry(action, id, type, user, true, actionStatus, version);
	}

	public void logAuditEntryAsync(String action, String id, String type, String user, String actionStatus,
			long version) throws ServiceCallerException {
		logAuditEntryAsync(action, id, type, user, true, actionStatus, version);
	}

	public void logAuditEntry(String action, String id, String type, String user, boolean isAudit, String actionStatus,
			Long version) throws ServiceCallerException {
		logAuditEntry(action, id, type, user, isAudit, actionStatus, version, null, null);
	}

	public void logAuditEntryAsync(String action, String id, String type, String user, boolean isAudit,
			String actionStatus, Long version) throws ServiceCallerException {
		logAuditEntryAsync(action, id, type, user, isAudit, actionStatus, version, null, null);
	}

	public void logAuditEntry(String action, String id, String type, String user, boolean isAudit, String actionStatus,
			Long version, String contextEntityId, String scriptId) throws ServiceCallerException {
		logAuditEntry(action, id, type, user, isAudit, actionStatus, version, contextEntityId, scriptId, null);
	}

	public void logAuditEntryAsync(String action, String id, String type, String user, boolean isAudit,
			String actionStatus, Long version, String contextEntityId, String scriptId) throws ServiceCallerException {
		logAuditEntryAsync(action, id, type, user, isAudit, actionStatus, version, contextEntityId, scriptId, null);
	}

	public void logAuditEntry(String action, String id, String type, String user, boolean isAudit, String actionStatus,
			Long version, String contextEntityId, String scriptId, String content) throws ServiceCallerException {
		this.logAuditEntry(action, id, type, user, isAudit, actionStatus, version, contextEntityId, scriptId, content,
				false);
	}

	public void logAuditEntryAsync(String action, String id, String type, String user, boolean isAudit,
			String actionStatus, Long version, String contextEntityId, String scriptId, String content)
			throws ServiceCallerException {
		this.logAuditEntry(action, id, type, user, isAudit, actionStatus, version, contextEntityId, scriptId, content,
				true);
	}

	// isAudit allows us to turn auditing off in development if there is a problem
	// so we can continue working.
	// It should always be true in production.
	private void logAuditEntry(String action, String id, String type, String user, boolean isAudit, String actionStatus,
			Long version, String contextEntityId, String scriptId, String content, boolean isAsync)
			throws ServiceCallerException {
		if (isAudit) {
			AuditEntry entry = new AuditEntry();
			entry.action = action;
			if (id != null) {
				entry.entityId = id;
			}
			entry.entityVersion = version.toString();
			if (type != null) {
				entry.entityType = type;
			}
			entry.userId = user;
			entry.actionStatus = actionStatus;
			if (contextEntityId != null) {
				entry.contextEntityId = contextEntityId;
			}
			if (scriptId != null) {
				entry.scriptId = scriptId;
			}
			if (content != null) {
				entry.content = content;
			}

			this.logAuditEntry(entry, isAsync);
		}
	}

	private void logAuditEntry(AuditEntry entry, boolean isAsync) throws ServiceCallerException {
		Gson gson = this.getGson();
		String json = gson.toJson(entry);

		if (isAsync) {
			this.logAuditEntryAsJsonAsync(entry.userId, json);
		} else {
			this.logAuditEntryAsJson(entry.userId, json);
		}
	}

	public String addAuditEntry(String user, AuditEntryRequestBody auditEntryRequestBody)
			throws ServiceCallerException {
		String json = this.marshall(auditEntryRequestBody, "application/json");
		return logAuditEntryAsJson(user, json);
	}

	public void addAuditEntryAsync(String user, AuditEntryRequestBody auditEntryRequestBody)
			throws ServiceCallerException {
		String json = this.marshall(auditEntryRequestBody, "application/json");
		logAuditEntryAsJsonAsync(user, json);
	}

	public String logAuditEntryAsJson(String user, String auditEntryRequestBody) throws ServiceCallerException {
		String uri = this.getBaseURI() + "/audit-entry";
		if (this.getUser() == null || this.getUser().isEmpty()) {
			this.setUser(user);
		}
		
		ServiceResponse sr = this.post(uri, auditEntryRequestBody);
		
		return sr.getResponseAsString();
	}

	public void logAuditEntryAsJsonAsync(String user, String auditEntryRequestBody) throws ServiceCallerException {
		String uri = this.getBaseURI() + "/audit-entry";
		if (this.getUser() == null || this.getUser().isEmpty()) {
			this.setUser(user);
		}
		
		this.postAsync(uri, auditEntryRequestBody);
		
		
	}

	private String marshall(AuditEntryRequestBody auditEntryRequestBody, String contentType) {
		Gson gson = this.getGson();
		return gson.toJson(auditEntryRequestBody);
	}

	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/equip-services/" + this.getSystemId();
	}

	class AuditEntry {
		public String scriptId;
		public String contextEntityId;
		public String action;
		public String entityId;
		public String entityVersion;
		public String entityType;
		public String userId;
		public String actionStatus;
		public String content;
		public String hostname;
		public String operatingSystem;
		public String executionEngineName;
		public String executionEngineVersion;
		public String runtimeEnvironment;
		public String runtimePath;

		public AuditEntry() {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				// We don't have any logger defined for the EQuIPServiceAPI.
				// This will show up in the server logs, but should be
				// replaced when we add a logger to the project.
				e.printStackTrace();
			}
			operatingSystem = String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version"));
		}
	}

	public class ReportingEvent {
		private String equipId;
		private String type;

		public String getEquipId() {
			return equipId;
		}

		public void setEquipId(String equipId) {
			this.equipId = equipId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		private String itemType;

		public String getItemType() {
			return itemType;
		}

		public void setItemType(String itemType) {
			this.itemType = itemType;
		}
	}

	public class AuditEntryContent {

		private String studyId;
		private String protocolId;
		private String studyBlindingStatus;
		private String studyRestrictionStatus;
		private String dataBlindingStatus;
		private String dataRestrictionStatus;
		private String reportingEventId;
		private String reportingEventType;
		private String entityType;

		public String getEntityType() {
			return entityType;
		}

		public void setEntityType(String entityType) {
			this.entityType = entityType;
		}

		public String getStudyId() {
			return studyId;
		}

		public void setStudyId(String studyId) {
			this.studyId = studyId;
		}

		public String getProtocolId() {
			return protocolId;
		}

		public void setProtocolId(String protocolId) {
			this.protocolId = protocolId;
		}

		public String getStudyBlindingStatus() {
			return studyBlindingStatus;
		}

		public void setStudyBlindingStatus(String studyBlindingStatus) {
			this.studyBlindingStatus = studyBlindingStatus;
		}

		public String getStudyRestrictionStatus() {
			return studyRestrictionStatus;
		}

		public void setStudyRestrictionStatus(String studyRestrictionStatus) {
			this.studyRestrictionStatus = studyRestrictionStatus;
		}

		public String getDataBlindingStatus() {
			return dataBlindingStatus;
		}

		public void setDataBlindingStatus(String dataBlindingStatus) {
			this.dataBlindingStatus = dataBlindingStatus;
		}

		public String getDataRestrictionStatus() {
			return dataRestrictionStatus;
		}

		public void setDataRestrictionStatus(String dataRestrictionStatus) {
			this.dataRestrictionStatus = dataRestrictionStatus;
		}

		public String getReportingEventId() {
			return reportingEventId;
		}

		public void setReportingEventId(String reportingEventId) {
			this.reportingEventId = reportingEventId;
		}

		public String getReportingEventType() {
			return reportingEventType;
		}

		public void setReportingEventType(String reportingEventType) {
			this.reportingEventType = reportingEventType;
		}

		public String getOtherContent() {
			return otherContent;
		}

		public void setOtherContent(String otherContent) {
			this.otherContent = otherContent;
		}

		private String otherContent;

	}

	public class OpMetadataResponse {
		private List<Program> programs;

		public List<Program> getPrograms() {
			return programs;
		}

		public void setPrograms(List<Program> programs) {
			this.programs = programs;
		}
	}

	public class Program {
		private List<Protocol> protocols;

		public List<Protocol> getProtocols() {
			return protocols;
		}

		public void setProtocols(List<Protocol> protocols) {
			this.protocols = protocols;
		}
	}

	public class Protocol {
		private String studyId;
		private String studyBlindingStatus;
		private List<ProtocolAlias> protocolAliases;

		public String getStudyId() {
			return studyId;
		}

		public void setStudyId(String studyId) {
			this.studyId = studyId;
		}

		public String getStudyBlindingStatus() {
			return studyBlindingStatus;
		}

		public void setStudyBlindingStatus(String studyBlindingStatus) {
			this.studyBlindingStatus = studyBlindingStatus;
		}

		public List<ProtocolAlias> getProtocolAliases() {
			return protocolAliases;
		}

		public void setProtocolAliases(List<ProtocolAlias> protocolAliases) {
			this.protocolAliases = protocolAliases;
		}

		public String getStudyRestrictionStatus() {
			return studyRestrictionStatus;
		}

		public void setStudyRestrictionStatus(String studyRestrictionStatus) {
			this.studyRestrictionStatus = studyRestrictionStatus;
		}

		private String studyRestrictionStatus;
	}

	public class ProtocolAlias {
		private String aliasType;
		private String studyAlias;

		public String getAliasType() {
			return aliasType;
		}

		public void setAliasType(String aliasType) {
			this.aliasType = aliasType;
		}

		public String getStudyAlias() {
			return studyAlias;
		}

		public void setStudyAlias(String studyAlias) {
			this.studyAlias = studyAlias;
		}
	}

	public class AuditDetails {
		public static final String SUCCESS = "SUCCESS";
		public static final String FAILURE = "FAILURE";

		private EquipObject entity;
		private EquipObject contextEntity;
		private String action;
		private String actionStatus = AuditDetails.SUCCESS;
		private String scriptId;
		private String content;
		private String user;
		private HttpServletRequest request;
		private AuditEntryContent auditContentEntity;

		public AuditEntryContent getAuditContentEntity() {
			return auditContentEntity;
		}

		public void setAuditContentEntity(AuditEntryContent auditContentEntity) {
			this.auditContentEntity = auditContentEntity;
		}

		public AuditDetails() {
			this(null, null, null);
		}

		public HttpServletRequest getRequest() {
			return request;
		}

		public void setRequest(HttpServletRequest request) {
			this.request = request;
		}

		public void setRequest(spark.Request request) {
			this.request = request.raw();
		}

		public AuditDetails(String action, EquipObject entity, String userId) {
			this.action = action;
			this.entity = entity;
			this.user = userId;
		}

		public EquipObject getEntity() {
			return entity;
		}

		public void setEntity(EquipObject entity) {
			this.entity = entity;
		}

		public EquipObject getContextEntity() {
			return contextEntity;
		}

		public void setContextEntity(EquipObject contextEntity) {
			this.contextEntity = contextEntity;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getActionStatus() {
			return actionStatus;
		}

		public void setActionStatus(String actionStatus) {
			this.actionStatus = actionStatus;
		}

		public String getScriptId() {
			return scriptId;
		}

		public void setScriptId(String scriptId) {
			this.scriptId = scriptId;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}
	}
}