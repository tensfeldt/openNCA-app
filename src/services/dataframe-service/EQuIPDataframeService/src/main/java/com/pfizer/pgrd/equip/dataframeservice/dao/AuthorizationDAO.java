package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.util.FormattingUtils;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationRequestBody;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationResponseBody;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;
import com.pfizer.pgrd.equip.services.authorization.client.User;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

public class AuthorizationDAO extends ModeShapeDAO {

	AuthorizationServiceClient asc;

	public AuthorizationServiceClient getAsc() {
		return asc;
	}

	public void setAsc(AuthorizationServiceClient asc) {
		this.asc = asc;
	}

	public static final void maskDataframe(Dataframe df) {
		AuthorizationDAO.maskObject(df);
		df.setPromotions(new ArrayList<>());
		df.setProfileConfig(new ArrayList<>());
		df.setScript(null);
	}

	public static final void maskAssembly(Assembly a) {
		AuthorizationDAO.maskObject(a);
	}

	private static final void maskObject(EquipObject eo) {
		if (eo instanceof EquipMetadatable) {
			((EquipMetadatable) eo).setMetadata(new ArrayList<>());
		}
		if (eo instanceof EquipCommentable) {
			((EquipCommentable) eo).setComments(new ArrayList<>());
		}
	}

	public AuthorizationDAO() throws ServiceCallerException {
		this.setAsc(AuthorizationServiceClient.getClient(Props.getAuthorizationServiceServer(),
				Props.getAuthorizationSerivcePort(), Props.getAuthorizationServiceSystemId()));
	}

	/**
	 * Returns a {@link Map} of dataframe GUIDs and whether the provided user ID has
	 * access to view that particular dataframe.
	 * 
	 * @param dataframes
	 * @param userId
	 * @return {@link Map}<{@link String}, {@link Boolean}>
	 * @throws ServiceCallerException
	 */
	public Map<String, Boolean> canViewDataframe(List<Dataframe> dataframes, String userId)
			throws ServiceCallerException {
		Map<String, Boolean> map = new HashMap<>();
		if (dataframes != null && userId != null && !dataframes.isEmpty()) {
			List<AuthorizationRequestBody> list = this.createAuthRequestBody(dataframes, false);
			GsonBuilder gb = new GsonBuilder();
			gb.setPrettyPrinting();
			String body = gb.create().toJson(list);
			// System.out.println(body);
			AuthorizationResponseBody arb = this.asc.getMultipleDataframePrivileges(userId, body);
			map = arb.getPermissionsInfo();
		}

		return map;
	}

	public boolean canViewDataframe(Dataframe dataframe, String userId) throws ServiceCallerException {
		AuthorizationRequestBody body = this.createAuthRequestBody(dataframe, true);
		String authRequestBodyJson = new Gson().toJson(body);

		AuthorizationResponseBody arb = asc.getDataframePrivileges(userId, authRequestBodyJson);
		Map<String, Boolean> map = arb.getPermissionsInfo();
		if (map != null) {
			return map.get("canViewDataframe");
		} else {
			return false;
		}
	}

	public boolean canViewAssembly(Assembly a, String userId) throws ServiceCallerException {
		return this.canViewAssembly(a, userId, new HashMap<>());
	}

	/*
	 * public boolean canViewAssembly(Assembly a, String userId) throws
	 * ServiceCallerException { Map<String, Dataframe> map =
	 * getAllGrandDataframes(a); List<Dataframe> list = new ArrayList<>();
	 * for(Entry<String, Dataframe> e : map.entrySet()) { list.add(e.getValue()); }
	 * 
	 * Map<String, Boolean> viewMap = this.canViewDataframe(list, userId); return
	 * this.canViewAssembly(a, viewMap); }
	 */

	private boolean canViewAssembly(Assembly a, Map<String, Boolean> dataframeViewMap) {
		boolean canView = true;
		for (String dfId : a.getDataframeIds()) {
			canView = canView && (dataframeViewMap.get(dfId));
		}

		if (canView) {
			if (a instanceof Analysis) {
				Analysis an = (Analysis) a;
				if (an.getKelFlagsDataframeId() != null) {
					canView = canView && (dataframeViewMap.get(an.getKelFlagsDataframeId()));
				}
				if (an.getModelConfigurationDataframeId() != null) {
					canView = canView && (dataframeViewMap.get(an.getModelConfigurationDataframeId()));
				}
				if (an.getParametersDataframeId() != null) {
					canView = canView && (dataframeViewMap.get(an.getParametersDataframeId()));
				}
				if (an.getEstimatedConcDataframeId() != null) {
					canView = canView && (dataframeViewMap.get(an.getEstimatedConcDataframeId()));
				}
			} else {
				List<ReportingEventItem> items = a.getReportingItems();
				if (items == null || items.isEmpty()) {
					List<String> reportingEventItemIds = a.getReportingItemIds();
					items = new ReportingAndPublishingDAO().getReportingItem(reportingEventItemIds);
				}

				for (ReportingEventItem item : items) {
					if (item.getDataFrameId() != null && !item.getDataFrameId().trim().isEmpty()) {
						canView = canView && (dataframeViewMap.get(item.getDataFrameId()));
					} else if (item.getAssemblyId() != null && !item.getAssemblyId().trim().isEmpty()) {
						AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
						Assembly as = aDao.getAssembly(item.getAssemblyId());
						canView = canView && this.canViewAssembly(as, dataframeViewMap);
						if (!canView) {
							break;
						}
					}
				}
			}
		}

		return canView;
	}

	private Map<String, Dataframe> getAllGrandDataframes(Assembly a) {
		Map<String, Dataframe> map = new HashMap<>();
		if (a != null) {
			this.populateGrandDataframes(a, map);
		}

		return map;
	}

	private void populateGrandDataframes(Assembly a, Map<String, Dataframe> map) {
		if (a != null) {
			List<String> dfIds = new ArrayList<>();
			List<String> assemblyIds = new ArrayList<>();
			dfIds.addAll(a.getDataframeIds());
			assemblyIds.addAll(a.getAssemblyIds());

			if (a instanceof Analysis) {
				Analysis an = (Analysis) a;
				if (an.getKelFlagsDataframeId() != null) {
					dfIds.add(an.getKelFlagsDataframeId());
				}
				if (an.getModelConfigurationDataframeId() != null) {
					dfIds.add(an.getModelConfigurationDataframeId());
				}
				if (an.getParametersDataframeId() != null) {
					dfIds.add(an.getParametersDataframeId());
				}
				if (an.getEstimatedConcDataframeId() != null) {
					dfIds.add(an.getEstimatedConcDataframeId());
				}
			} else {
				List<ReportingEventItem> items = a.getReportingItems();
				if (items == null || items.isEmpty()) {
					List<String> reportingEventItemIds = a.getReportingItemIds();
					items = new ReportingAndPublishingDAO().getReportingItem(reportingEventItemIds);
				}

				for (ReportingEventItem item : items) {
					if (item.getDataFrameId() != null && !item.getDataFrameId().trim().isEmpty()) {
						dfIds.add(item.getDataFrameId());
					} else if (item.getAssemblyId() != null && !item.getAssemblyId().trim().isEmpty()) {
						assemblyIds.add(item.getAssemblyId());
					}
				}
			}

			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			for (String dfId : dfIds) {
				if (map.get(dfId) == null) {
					Dataframe df = dDao.getDataframe(dfId);
					if (df != null) {
						map.put(df.getId(), df);
					}
				}
			}

			AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
			List<Assembly> assemblies = aDao.getAssembly(assemblyIds);
			for (Assembly ca : assemblies) {
				this.populateGrandDataframes(ca, map);
			}
		}
	}

	public boolean canViewAssembly(Assembly a, String userId, Map<String, EquipObject> dataMap)
			throws ServiceCallerException {
		if (a != null && userId != null) {
			List<String> dfIds = new ArrayList<>();
			dfIds.addAll(a.getDataframeIds());
			List<String> assemblyIds = new ArrayList<>();
			assemblyIds.addAll(a.getAssemblyIds());

			if (a instanceof Analysis) {
				Analysis an = (Analysis) a;
				if (an.getKelFlagsDataframeId() != null) {
					dfIds.add(an.getKelFlagsDataframeId());
				}
				if (an.getModelConfigurationDataframeId() != null) {
					dfIds.add(an.getModelConfigurationDataframeId());
				}
				if (an.getParametersDataframeId() != null) {
					dfIds.add(an.getParametersDataframeId());
				}
				if (an.getEstimatedConcDataframeId() != null) {
					dfIds.add(an.getEstimatedConcDataframeId());
				}
			} else {
				List<ReportingEventItem> items = a.getReportingItems();
				if (items == null || items.isEmpty()) {
					List<String> reportingEventItemIds = a.getReportingItemIds();
					items = new ReportingAndPublishingDAO().getReportingItem(reportingEventItemIds);
				}

				for (ReportingEventItem item : items) {
					if (item.getDataFrameId() != null && !item.getDataFrameId().trim().isEmpty()) {
						dfIds.add(item.getDataFrameId());
					} else if (item.getAssemblyId() != null && !item.getAssemblyId().trim().isEmpty()) {
						assemblyIds.add(item.getAssemblyId());
					}
				}
			}

			List<Dataframe> dataframes = new ArrayList<>();
			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			for (String dfId : dfIds) {
				Dataframe df = (Dataframe) dataMap.get(dfId);
				if (df == null) {
					df = dDao.getDataframe(dfId);
					if (df != null) {
						dataMap.put(df.getId(), df);
					}
				}

				if (df != null) {
					dataframes.add(df);
				}
			}

			if (!dataframes.isEmpty()) {
				Map<String, Boolean> accessMap = this.canViewDataframe(dataframes, userId);
				for (Entry<String, Boolean> e : accessMap.entrySet()) {
					if (e.getValue() == false) {
						return false;
					}
				}
			}

			AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
			List<Assembly> assemblies = new ArrayList<>();
			for (String aId : assemblyIds) {
				Assembly ca = (Assembly) dataMap.get(aId);
				if (ca == null) {
					ca = aDao.getAssembly(aId);
					if (ca != null) {
						dataMap.put(ca.getId(), ca);
					}
				}

				if (ca != null) {
					assemblies.add(ca);
				}
			}

			for (Assembly assembly : assemblies) {
				boolean canView = this.canViewAssembly(assembly, userId, dataMap);
				if (!canView) {
					return false;
				}
			}
		}

		return true;
	}

	private List<AuthorizationRequestBody> createAuthRequestBody(List<Dataframe> dataframes, boolean useEquipId) {
		List<AuthorizationRequestBody> list = new ArrayList<>();
		if (dataframes != null) {
			for (Dataframe dataframe : dataframes) {
				AuthorizationRequestBody body = this.createAuthRequestBody(dataframe, useEquipId);
				if (body != null) {
					list.add(body);
				}
			}
		}

		return list;
	}

	private AuthorizationRequestBody createAuthRequestBody(Dataframe dataframe, boolean useEquipId) {
		AuthorizationRequestBody body = null;
		if (dataframe != null) {
			body = new AuthorizationRequestBody();
			body.setDataBlindingStatus(dataframe.getDataBlindingStatus());
			body.setDataframeType(dataframe.getDataframeType());
			body.setPromotionStatus(dataframe.getPromotionStatus());
			body.setRestrictionStatus(dataframe.getRestrictionStatus());
			body.setstudyIds(dataframe.getStudyIds());

			if (useEquipId) {
				body.setDataframeId(dataframe.getEquipId()); // change 10/10/18 mh per EQ-1039
			} else {
				body.setDataframeId(dataframe.getId());
			}
		}

		return body;
	}

	public Boolean checkPrivileges(String objectType, String action, String username) throws ServiceCallerException {
		// check the user privileges for viewing based on what type of object
		List<String> privileges = getPrivileges(objectType, action);
		return asc.checkAuthorization(privileges, username);
	}

	private List<String> getPrivileges(String nodeType, String action) {
		// check the user privileges for posting based on what type of dataframe
		String which = "RUN";
		if (action.equals("GET")) {
			which = "VIEW";
		}
		if (action.equals("USER") || action.equals("ANY")) {
			which = action;
		}

		List<String> privileges = new ArrayList<>();

		switch (nodeType.toLowerCase()) {
		case "dataset":
		case "data load":
			privileges.add(which + "_DATALOAD");
			break;
		case "analysis":
		case "kel flags":
		case "model configuration template":
			privileges.add(which + "_ANALYSIS");
			break;
		case "primary parameters":
		case "derived parameters":
			if(which.equalsIgnoreCase("run")) {
				privileges.add("RUN_PK_PARAM_AND_CONC_TIME");
			}
			privileges.add(which + "_ANALYSIS");
			break;
		case "data transformation":
			privileges.add(which + "_DATA_TRANSFORM");
			break;
		case "qcstatus":
			privileges.add("ALTER_QC_STATUS");
			break;
		case "delete":
			privileges.add("DROP_USER_ENTITY");
			break;
		case "undelete":
			privileges.add("RESTORE_" + which + "_ENTITY");
			break;
		case "data blinding":
			privileges.add("ALTER_DATA_BLINDING");
			break;
		case "data promotion":
			privileges.add("ALTER_DATA_PROMOTION");
			break;
		case "data restriction":
			privileges.add("ALTER_DATA_RESTRICTION");
			break;
		case "data status":
			privileges.add("ALTER_DATA_STATUS");
			break;
		case "operational data":
			privileges.add("ALTER_DATA_OPMETA_REFERENCE");
			break;
		case "re lock":
			privileges.add("ALTER_REPORTING_EVENT_LOCK");
			break;
		case "release reopened":
			privileges.add("ALTER_REPORTING_EVENT_LOCK");
			privileges.add("ALTER_REPORTING_EVENT");
			privileges.add("ALTER_REPORTING_EVENT_SELF");
			break;
		case "publishing":
			privileges.add("PUBLISH_DATA");
			privileges.add("ALTER_REPORTING_EVENT");
			privileges.add("ALTER_REPORTING_EVENT_SELF");
			break;
		case "reporting event":
			privileges.add("ALTER_REPORTING_EVENT");
			privileges.add("ALTER_REPORTING_EVENT_SELF");
			break;
		case "lineage reexecute":
		case "lineage copy":
			privileges.add("VIEW_ANY_DEPENDENTS");
			privileges.add("VIEW_ANY_PROVENANCE");
			privileges.add("VIEW_DATA_TRANSFORM");
			privileges.add("RUN_DATA_TRANSFORM");
			privileges.add("RUN_PK_PARAM_AND_CONC_TIME");
			privileges.add("RUN_ANALYSIS");
			privileges.add("VIEW_ANALYSIS");
			break;
		case "equip id":
			privileges.add("VIEW_ANY_DEPENDENTS");
			break;
		case "lineage":
			privileges.add("VIEW_ANY_PROVENANCE");
			break;
		default:

			// will allow other types of dataframe/assembly not listed
		}
		return privileges;
	}

	public boolean hasBlindingAccess(Dataframe df, String userId) throws ServiceCallerException {
		boolean isOk = true;
		OpmetaServiceClient osc = new OpmetaServiceClient(Props.getOpmetaServiceServer(), Props.getOpmetaSerivcePort());
		for (String studyId : df.getStudyIds()) {
			String program = studyId.split(":")[0];
			String protocol = studyId.split(":")[1];
			boolean blinded = osc.isStudyBlinded(userId, program, protocol);
			if (!blinded)
				return true; // blinded data has been modified to remove personal info, it can be open to
								// everyone
			try {
				if (!inBlindingGroup(studyId, userId))
					return false;
			} catch (ServiceCallerException ex) {
				throw (ex); // need to throw exception if blinding group does not exist
			}
		}

		return isOk;
	}

	public boolean inBlindingGroup(String studyId, String userId) throws ServiceCallerException {
		boolean isOk = false;
		// get the access group -- need to throw exception if group does not exist
		String group = asc.getAccessGroupName(studyId, userId);
		if (group.equalsIgnoreCase("Not Found"))
			throw new ServiceCallerException("/entities/PROTOCOL/" + studyId + "/access", 404,
					"No blinding group exists for " + studyId);
//is user in the group?
		try {
			List<User> users = asc.getAccessGroupMembers(group, userId);
			// for (User user : users) {
			// if (user.getUserId().equalsIgnoreCase(userId)) {
			// return true;
			// }
			// }
			if (users.size() >= 1)
				return true;
			else
				return isOk;

		} catch (ServiceCallerException ex) {
			throw new ServiceCallerException("Authorization Service Error", 500, ex.getMessage());
		}
	}

	public boolean copyGroupAccess(Dataframe dataframe, String parentId, String userId) {
		AuthorizationRequestBody body = new AuthorizationRequestBody();
		body.setDataframeId(parentId);
		String authRequestBodyJson = new Gson().toJson(body);

		return asc.copyDataframeGroupAccess(dataframe.getEquipId(), userId, authRequestBodyJson);
	}
	
	public boolean canModifyExtras(EquipObject parent, String user) {
		if(parent == null) {
			return true;
		}
		if(user == null) {
			return false;
		}
		
		List<String> userPrivs = this.asc.getPrivileges(user);
		if(userPrivs == null || userPrivs.isEmpty()) {
			return false;
		}
		
		if(parent instanceof Dataframe) {
			Dataframe df = (Dataframe) parent;
			String dfType = df.getDataframeType();
			if(dfType != null) {
				List<String> analysisTypes = Arrays.asList(Dataframe.PRIMARY_PARAMETERS_TYPE, Dataframe.DERIVED_PARAMETERS_TYPE, Dataframe.KEL_FLAGS_TYPE, Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE, Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE);
				if(dfType.equalsIgnoreCase(Dataframe.DATA_TRANSFORMATION_TYPE)) {
					return FormattingUtils.caseInsensitiveContains("RUN_DATA_TRANSFORM", userPrivs);
				}
				else if(dfType.equalsIgnoreCase(Dataframe.DATASET_TYPE)) {
					return FormattingUtils.caseInsensitiveContains("RUN_DATALOAD", userPrivs);
				}
				else if(dfType.equalsIgnoreCase(Dataframe.REPORT_TYPE)) {
					String subType = df.getSubType();
					if(subType == null || !subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
						return FormattingUtils.caseInsensitiveContains("RUN_REPORT", userPrivs);
					}
					else {
						return FormattingUtils.caseInsensitiveContains("RUN_PUBLISH_DATA_AUDIT_REPORT", userPrivs);
					}
				}
				else if(dfType.equalsIgnoreCase(Dataframe.REPORT_ITEM_TYPE)) {
					return FormattingUtils.caseInsensitiveContains("RUN_REPORT", userPrivs);
				}
				else if(analysisTypes.contains(dfType)) {
					return FormattingUtils.caseInsensitiveContains("RUN_ANALYSIS", userPrivs);
				}
			}
		}
		else if(parent instanceof Assembly) {
			Assembly a = (Assembly) parent;
			String aType = a.getAssemblyType();
			if(aType != null) {
				if(aType.equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
					return FormattingUtils.caseInsensitiveContains("RUN_ANALYSIS", userPrivs);
				}
				else if(aType.equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
					boolean are = FormattingUtils.caseInsensitiveContains("ALTER_REPORTING_EVENT", userPrivs);
					boolean ares = FormattingUtils.caseInsensitiveContains("ALTER_REPORTING_EVENT_SELF", userPrivs) && a.getCreatedBy().equalsIgnoreCase(user);
					
					return are || ares;
				}
				else if(aType.equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
					return FormattingUtils.caseInsensitiveContains("RUN_DATALOAD", userPrivs);
				}
				else if(aType.equalsIgnoreCase(Assembly.BATCH_TYPE)) {
					if(a.getDataframeIds() != null && !a.getDataframeIds().isEmpty()) {
						String dfId = a.getDataframeIds().get(0);
						DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
						Dataframe df = ddao.getDataframe(dfId);
						return this.canModifyExtras(df, user);
					}
				}
			}
		}
		
		return true;
	}
}
