package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;

import com.pfizer.pgrd.equip.dataframeservice.application.Props;

import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyUtils;
import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyValidation;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.CopyException;

import com.pfizer.pgrd.equip.dataframeservice.util.FormattingUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.search.SearchServiceClient;

public class LineageDAOImpl extends ModeShapeDAO implements LineageDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(LineageDAOImpl.class);

	private List<Assembly> assemblyTable = new ArrayList<>();
	private List<Dataframe> dataframeTable = new ArrayList<>();
	private LineageBuilder lineageBuilder = this.createLineageBuilder();
	private List<LibraryResponse> excludedBreadcrumbScripts = new ArrayList<>();
	private DataframeDAO dataframeDAO;
	private AssemblyDAO assemblyDAO;
	private List<String> copyLog = new ArrayList<>();
	private String authUserId;

	private static final int DATA_LOAD_MODE = LineageBuilder.DATA_LOAD_MODE,
			ANALYSIS_PREP_MODE = LineageBuilder.ANALYSIS_PREP_MODE, PROMOTION_MODE = LineageBuilder.PROMOTION_MODE,
			FULL_MODE = LineageBuilder.FULL_MODE;

	private static final int DOWN = 0, UP = 1;
	
	public List<String> getCopyLog() {
		return this.copyLog;
	}

	// +-------------------+
	// | FULL LINEAGE |
	// +-------------------+
	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(String studyId) {
		return this.getFullStudyLineage(studyId, false);
	}

	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(String studyId, boolean includeDeleted) {
		return this.getFullStudyLineage(new String[] { studyId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(List<String> studyIds) {
		return this.getFullStudyLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(List<String> studyIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getFullStudyLineage(studyIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(String[] studyIds) {
		return this.gatherLineage(studyIds, null, FULL_MODE, false);
	}

	@Override
	public List<AssemblyLineageItem> getFullStudyLineage(String[] studyIds, boolean includeDeleted) {
		return this.gatherLineage(studyIds, null, FULL_MODE, includeDeleted);
	}
	// ----------------

	// +----------------------+
	// | GENERIC LINEAGE |
	// +----------------------+
	@Override
	public List<LineageItem> getLineage(String startId) {
		return this.getLineage(startId, false);
	}

	@Override
	public List<LineageItem> getLineage(String startId, boolean includeDeleted) {
		List<LineageItem> lineage = this.getLineage(new String[] { startId }, includeDeleted);
		return lineage;
	}

	@Override
	public List<LineageItem> getLineage(List<String> startIds) {
		return this.getLineage(startIds, false);
	}

	@Override
	public List<LineageItem> getLineage(List<String> startIds, boolean includeDeleted) {
		List<LineageItem> lineages = new ArrayList<>();
		if (startIds != null) {
			lineages = this.getLineage(startIds.toArray(new String[0]), includeDeleted);
		}

		return lineages;
	}

	@Override
	public List<LineageItem> getLineage(String[] startIds) {
		return this.getLineage(startIds, false);
	}

	@Override
	public List<LineageItem> getLineage(String[] startIds, boolean includeDeleted) {
		List<LineageItem> lineages = new ArrayList<>();
		if (startIds != null) {
			for (String id : startIds) {
				if (id != null) {
					LineageItem lineage = this.down(id, FULL_MODE, null, includeDeleted);
					if (lineage != null) {
						lineages.add(lineage);
					}
				}
			}
		}

		return lineages;
	}
	// ----------------

	// +------------------------+
	// | DATA LOAD LINEAGE |
	// +------------------------+
	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId) {
		return this.getDataLoadLineage(studyId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, boolean includeDeleted) {
		return this.getDataLoadLineage(new String[] { studyId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds) {
		return this.getDataLoadLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getDataLoadLineage(studyIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds) {
		return this.getDataLoadLineage(studyIds, null, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, boolean includeDeleted) {
		return this.getDataLoadLineage(studyIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, String userId) {
		return this.getDataLoadLineage(studyId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, String userId, boolean includeDeleted) {
		return this.getDataLoadLineage(new String[] { studyId }, userId, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, String userId) {
		return this.getDataLoadLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, String userId, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getDataLoadLineage(studyIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, String userId) {
		return this.getDataLoadLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, String userId, boolean includeDeleted) {
		return this.gatherLineage(studyIds, userId, DATA_LOAD_MODE, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId) {
		return this.getDataLoadLineageByEquipId(equipId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, boolean includeDeleted) {
		return this.getDataLoadLineageByEquipId(new String[] { equipId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds) {
		return this.getDataLoadLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getDataLoadLineageByEquipId(equipIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds) {
		return this.getDataLoadLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, boolean includeDeleted) {
		return this.getDataLoadLineageByEquipId(equipIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, String userId) {
		return this.getDataLoadLineageByEquipId(equipId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, String userId,
			boolean includeDeleted) {
		return this.getDataLoadLineageByEquipId(new String[] { equipId }, userId, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, String userId) {
		return this.getDataLoadLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, String userId,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getDataLoadLineageByEquipId(equipIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, String userId) {
		return this.getDataLoadLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, String userId,
			boolean includeDeleted) {
		return this.gatherLineageByEquipId(equipIds, userId, DATA_LOAD_MODE, includeDeleted);
	}
	// ----------------

	// +------------------------+
	// | PROMOTION LINEAGE |
	// +------------------------+
	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String studyId) {
		return this.getPromotionLineage(studyId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, boolean includeDeleted) {
		return this.getPromotionLineage(new String[] { studyId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds) {
		return this.getPromotionLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getPromotionLineage(studyIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds) {
		return this.getPromotionLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, boolean includeDeleted) {
		return this.getPromotionLineage(studyIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, String userId) {
		return this.getPromotionLineage(studyId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, String userId, boolean includeDeleted) {
		return this.getPromotionLineage(new String[] { studyId }, userId, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, String userId) {
		return this.getPromotionLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, String userId, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getPromotionLineage(studyIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, String userId) {
		return this.getPromotionLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, String userId, boolean includeDeleted) {
		return this.gatherLineage(studyIds, userId, PROMOTION_MODE, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId) {
		return this.getPromotionLineageByEquipId(equipId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, boolean includeDeleted) {
		return this.getPromotionLineageByEquipId(new String[] { equipId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds) {
		return this.getPromotionLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getPromotionLineageByEquipId(equipIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds) {
		return this.getPromotionLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, boolean includeDeleted) {
		return this.getPromotionLineageByEquipId(equipIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, String userId) {
		return this.getPromotionLineageByEquipId(equipId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, String userId,
			boolean includeDeleted) {
		return this.getPromotionLineageByEquipId(new String[] { equipId }, userId, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, String userId) {
		return this.getPromotionLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, String userId,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getPromotionLineageByEquipId(equipIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, String userId) {
		return this.getPromotionLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, String userId,
			boolean includeDeleted) {
		return this.gatherLineageByEquipId(equipIds, userId, PROMOTION_MODE, includeDeleted);
	}
	// ----------------

	// +----------------------------+
	// | ANALYSIS PREP LINEAGE |
	// +----------------------------+
	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId) {
		return this.getAnalysisPrepLineage(studyId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, boolean includeDeleted) {
		return this.getAnalysisPrepLineage(new String[] { studyId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds) {
		return this.getAnalysisPrepLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getAnalysisPrepLineage(studyIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds) {
		return this.getAnalysisPrepLineage(studyIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds, boolean includeDeleted) {
		return this.getAnalysisPrepLineage(studyIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, String userId) {
		return this.getAnalysisPrepLineage(studyId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, String userId, boolean includeDeleted) {
		return this.getAnalysisPrepLineage(new String[] { studyId }, userId, includeDeleted);
	}

	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, String userId) {
		return this.getAnalysisPrepLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, String userId,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			lineage = this.getAnalysisPrepLineage(studyIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds, String userId) {
		return this.getAnalysisPrepLineage(studyIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds, String userId, boolean includeDeleted) {
		return this.gatherLineage(studyIds, userId, ANALYSIS_PREP_MODE, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId) {
		return this.getAnalysisPrepLineageByEquipId(equipId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, boolean includeDeleted) {
		return this.getAnalysisPrepLineageByEquipId(new String[] { equipId }, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds) {
		return this.getAnalysisPrepLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getAnalysisPrepLineageByEquipId(equipIds.toArray(new String[0]), includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds) {
		return this.getAnalysisPrepLineageByEquipId(equipIds, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, boolean includeDeleted) {
		return this.getAnalysisPrepLineageByEquipId(equipIds, null, includeDeleted);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, String userId) {
		return this.getAnalysisPrepLineageByEquipId(equipId, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, String userId,
			boolean includeDeleted) {
		return this.getAnalysisPrepLineageByEquipId(new String[] { equipId }, userId);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, String userId) {
		return this.getAnalysisPrepLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, String userId,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (equipIds != null) {
			lineage = this.getAnalysisPrepLineageByEquipId(equipIds.toArray(new String[0]), userId, includeDeleted);
		}

		return lineage;
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, String userId) {
		return this.getAnalysisPrepLineageByEquipId(equipIds, userId, false);
	}

	@Override
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, String userId,
			boolean includeDeleted) {
		return this.gatherLineageByEquipId(equipIds, userId, ANALYSIS_PREP_MODE, includeDeleted);
	}
	// ----------------

	// WORKER METHODS
	private void populateTables(List<String> studyIds, boolean includeDeleted) {
		if (studyIds != null) {
			this.populateTables(studyIds.toArray(new String[0]), includeDeleted);
		}
	}

	private void populateTables(String[] studyIds, boolean includeDeleted) {
		/*
		AssemblyDAO adao = ModeShapeDAO.getAssemblyDAO();
		DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();

		List<Assembly> tempAssembly = adao.getAssemblyByStudyId(studyIds);
		List<Dataframe> tempDataframe = ddao.getDataframeByStudyId(studyIds);
		*/
		
		this.assemblyTable = new ArrayList<>();
		this.dataframeTable = new ArrayList<>();
		
		try {
			SearchServiceClient ssClient = ModeShapeDAO.getSearchServiceClient();
			GsonBuilder gb = new GsonBuilder();
			gb.registerTypeHierarchyAdapter(EquipObject.class,
					new SearchServiceClient.SearchServiceResultAdapter());
			Gson gson = gb.create();
			
			List<String> dataframeIds = new ArrayList<>();
			for(String studyId : studyIds) {
				String json = null;
				ServiceResponse response = ssClient.searchByStudyId(studyId);
				if (response != null && response.getCode() == HTTPStatusCodes.OK) {
					json = response.getResponseAsString();
				} else {
					LOGGER.error("An error ocurred when calling the Search Service for study ID '" + studyId + "'. "
							+ response.getCode() + ": " + response.getResponseAsString());
				}
				
				EquipObject[] equipObjects = gson.fromJson(json, EquipObject[].class);
				for (EquipObject eo : equipObjects) {
					if (eo instanceof Analysis) {
						Analysis an = (Analysis) eo;
						if (!an.isDeleteFlag() || includeDeleted) {
							this.assemblyTable.add(an);
						}
					} else if (eo instanceof Assembly) {
						Assembly a = (Assembly) eo;
						if (!a.isDeleteFlag() || includeDeleted) {
							this.assemblyTable.add(a);
						}
					} else if (eo instanceof Dataframe) {
						Dataframe df = (Dataframe) eo;
						if ((!df.isDeleteFlag() || includeDeleted) && (df.getSubType() == null || !df.getDataframeType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE))) {
							this.dataframeTable.add(df);
							dataframeIds.add(df.getId());
						}
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		

		/*this.assemblyTable = new ArrayList<>();
		for (Assembly a : tempAssembly) {
			if (includeDeleted || !a.isDeleteFlag()) {
				this.assemblyTable.add(a);
			}
		}

		this.dataframeTable = new ArrayList<>();
		for (Dataframe df : tempDataframe) {
			if (!df.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)
					&& (includeDeleted || !df.isDeleteFlag())) {
				this.dataframeTable.add(df);
			}
		}*/

		try {
			List<String> scriptNames = Props.getExcludedBreadcrumbScriptNames();
			LibraryServiceClient lsc = new LibraryServiceClient(Props.getLibraryServiceServer(),
					Props.getLibraryServicePort());
			lsc.setUser(Props.getServiceAccountName());
			this.excludedBreadcrumbScripts = lsc.getGlobalSystemScriptByName(scriptNames);
		} catch (ServiceCallerException sce) {
			LOGGER.info(
					"Unable to retrieve excluded breadcrumb scripts. " + sce.getStatusCode() + ": " + sce.getMessage());
		}
	}

	private List<AssemblyLineageItem> gatherLineageByEquipId(String[] equipIds, String userId, int mode,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineages = new ArrayList<>();
		if (equipIds != null) {
			EquipIDDAO eiDao = getEquipIDDAO();
			List<String> handledIds = new ArrayList<>();
			for (String equipId : equipIds) {
				if (!handledIds.contains(equipId)) {
					List<EquipObject> versions = eiDao.getItem(equipId);
					EquipObject node = this.getLatest(versions, equipId, userId, includeDeleted);
					if (node != null) {
						List<AssemblyLineageItem> lineage = this.gatherLineage(node, userId, mode, includeDeleted);
						lineages.addAll(lineage);
					}

					handledIds.add(equipId);
				}
			}
		}

		return lineages;
	}

	private List<AssemblyLineageItem> gatherLineage(EquipObject object, String userId, int mode,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (object != null && (object instanceof Assembly || object instanceof Dataframe)) {
			long dlTime = System.currentTimeMillis();
			if (object instanceof Assembly) {
				this.populateTables(((Assembly) object).getStudyIds(), includeDeleted);
			} else {
				this.populateTables(((Dataframe) object).getStudyIds(), includeDeleted);
			}

			AssemblyDAO aDao = getAssemblyDAO();
			List<Assembly> dataLoads = aDao.getRootDataLoads(object.getId());
			// List<Assembly> dataLoads = this.getRootDataLoads(object.getId());
			// List<Assembly> dataLoads = LineageResource.getRootDataLoads(object);
			dlTime = System.currentTimeMillis() - dlTime;
			LOGGER.info("Time taken to fetch root data loads based on ID " + object.getId() + ": " + dlTime + "ms.");

			long lTime = System.currentTimeMillis();
			lineage = this.gatherLineage(dataLoads, userId, mode, includeDeleted);
			lTime = System.currentTimeMillis() - lTime;
			LOGGER.info("Time taken to fetch lineage based on ID " + object.getId() + " in " + getModeName(mode)
					+ " mode: " + lTime + "ms.");
		}

		return lineage;
	}

	private List<AssemblyLineageItem> gatherLineage(String[] studyIds, String userId, int mode,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (studyIds != null) {
			LineageBuilder builder = this.createLineageBuilder(includeDeleted, userId, mode);
			/*
			 * builder.setIncludeDeleted(includeDeleted); builder.setUserId(userId);
			 * builder.setMode(mode);
			 * 
			 * try { List<String> scriptNames = Props.getExcludedBreadcrumbScriptNames();
			 * LibraryServiceClient lsc = new
			 * LibraryServiceClient(Props.getLibraryServiceServer(),
			 * Props.getLibraryServicePort()); lsc.setUser(Props.getServiceAccountName());
			 * this.excludedBreadcrumbScripts =
			 * lsc.getGlobalSystemScriptByName(scriptNames);
			 * builder.setExcludedBreadcrumbScripts(this.excludedBreadcrumbScripts); }
			 * catch(ServiceCallerException sce) {
			 * LOGGER.info("Unable to retrieve excluded breadcrumb scripts. " +
			 * sce.getStatusCode() + ": " + sce.getMessage()); }
			 */

			Map<String, List<AssemblyLineageItem>> map = builder.getLineage(studyIds);
			for (Entry<String, List<AssemblyLineageItem>> entry : map.entrySet()) {
				lineage.addAll(entry.getValue());
			}

			/*
			 * this.populateTables(studyIds, includeDeleted);
			 * 
			 * List<Assembly> assemblies = this.filterAssemblies(studyIds, userId,
			 * includeDeleted); lineage = this.gatherLineage(assemblies, userId, mode,
			 * includeDeleted);
			 */
		}

		return lineage;
	}

	private List<AssemblyLineageItem> gatherLineage(List<Assembly> assemblies, String userId, int mode,
			boolean includeDeleted) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (assemblies != null) {
			long bigTime = System.currentTimeMillis();
			for (Assembly a : assemblies) {
				long time = System.currentTimeMillis();
				if (a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)
						&& (mode != DATA_LOAD_MODE || (userId == null || a.getCreatedBy().equalsIgnoreCase(userId)))) {
					AssemblyLineageItem ali = this.down(a, mode, userId, includeDeleted);
					lineage.add(ali);
				}
				time = System.currentTimeMillis() - time;
				LOGGER.info("Time taken to gather lineage for assembly " + a.getId() + ": " + time + "ms.");
			}

			bigTime = System.currentTimeMillis() - bigTime;
			LOGGER.info("Time taken to gather lineage for " + assemblies.size() + " assembly(ies): " + bigTime + "ms.");
		}

		return lineage;
	}

	private <T extends EquipObject> T getLatest(List<T> items, String equipId, String userId, boolean includeDeleted) {
		T latestItem = null;
		List<T> subSorted = new ArrayList<>();
		for (T item : items) {
			boolean isDeleted = false;
			if (item instanceof EquipVersionable) {
				isDeleted = ((EquipVersionable) item).isDeleteFlag();
			}

			if (includeDeleted || !isDeleted) {
				String eid = ((EquipID) item).getEquipId();
				if (eid != null && eid.equals(equipId)) {
					subSorted.add(item);
				}
			}
		}
		subSorted.sort(
				(T a, T b) -> (((EquipCreatable) a).getCreated().after(((EquipCreatable) b).getCreated())) ? -1 : 1);

		for (T i : subSorted) {
			boolean isCommitted = ((EquipVersionable) i).isCommitted();
			String createdBy = ((EquipCreatable) i).getCreatedBy();
			if (isCommitted || (userId != null && createdBy != null && createdBy.equals(userId))) {
				latestItem = i;
				break;
			}
		}

		/*
		 * if(latestItem != null) { System.out.println(equipId + ": using " +
		 * latestItem.getId()); } else { System.out.println(equipId +
		 * ": no valid item found"); }
		 */

		return latestItem;
	}

	private List<Assembly> filterAssemblies(String[] studyIds, String userId, boolean includeDeleted) {
		List<Assembly> assemblies = new ArrayList<>();

		long time = System.currentTimeMillis();
		AssemblyDAO aDao = getAssemblyDAO();
		if (studyIds != null && studyIds.length > 0) {
			List<Assembly> temp = this.assemblyTable;
			// List<Assembly> temp = aDao.getAssemblyByStudyId(studyIds);
			List<String> handledIds = new ArrayList<>();
			for (Assembly a : temp) {
				String equipId = a.getEquipId();
				if ((a.getParentIds() == null || a.getParentIds().isEmpty()) && !handledIds.contains(equipId)) {
					Assembly latest = this.getLatest(temp, equipId, userId, includeDeleted);
					handledIds.add(equipId);

					if (latest != null) {
						assemblies.add(latest);
					}
				}
			}
			time = System.currentTimeMillis() - time;
			LOGGER.info("Time taken to get root assemblies by study ID(s) " + FormattingUtils.asList(studyIds) + ": "
					+ time + "ms.");
		} else {
			assemblies = aDao.getAssemblyByUserId(userId);
			time = System.currentTimeMillis() - time;
			LOGGER.info("Time taken to get root assemblies by user ID " + userId + ": " + time + "ms.");
		}

		return assemblies;
	}

	private LineageItem down(String objectId) {
		return this.down(objectId, false);
	}

	private LineageItem down(String objectId, boolean includeDeleted) {
		return this.down(objectId, FULL_MODE, null, includeDeleted);
	}

	private LineageItem down(String objectId, int mode, String userId, boolean includeDeleted) {
		LineageItem lineage = null;
		if (objectId != null) {
			ModeShapeDAO bdao = new ModeShapeDAO();
			EquipObject object = bdao.getEquipObject(objectId);
			if (object != null) {
				List<String> studyIds = new ArrayList<>();
				if (object instanceof Assembly) {
					Assembly a = (Assembly) object;
					studyIds = a.getStudyIds();

					this.populateTables(studyIds, includeDeleted);
					lineage = this.down(a, mode, userId, includeDeleted);
				} else if (object instanceof Dataframe) {
					Dataframe df = (Dataframe) object;
					studyIds = df.getStudyIds();

					this.populateTables(studyIds, includeDeleted);
					lineage = this.down(df, mode, userId, includeDeleted);
				}
			}
		}

		return lineage;
	}

	private Assembly getAssembly(String assemblyId) {
		// AssemblyDAO aDao = this.getAssemblyDAO();
		// Assembly assembly = aDao.getAssembly(assemblyId);

		/*
		 * Assembly assembly = null; for (Assembly a : this.assemblyTable) { if
		 * (a.getId().equalsIgnoreCase(assemblyId)) { assembly = a; break; } }
		 * 
		 * return assembly;
		 */
		return this.lineageBuilder.getAssemblyTable().get(assemblyId);
	}

	private Dataframe getDataframe(String dataframeId) {
		// DataframeDAO dfDao = this.getDataframeDAO();
		// Dataframe dataframe = dfDao.getDataframe(dfId);

		/*
		 * Dataframe dataframe = null; for (Dataframe df : this.dataframeTable) { if
		 * (df.getId().equalsIgnoreCase(dataframeId)) { dataframe = df; break; } }
		 * 
		 * return dataframe;
		 */
		return this.lineageBuilder.getDataframeTable().get(dataframeId);
	}

	private List<Assembly> getAssemblyByMemberDataframeId(String dataframeId) {
		// AssemblyDAO aDao = this.getAssemblyDAO();
		// List<Assembly> list = aDao.getAssemblyByMemberDataframeId(dfId);

		List<Assembly> list = new ArrayList<>();
		for (Assembly a : this.assemblyTable) {
			if (a.getDataframeIds().contains(dataframeId)) {
				list.add(a);
			}
		}

		return list;
	}

	private List<Dataframe> getDataframeByAssemblyId(String assemblyId) {
		// DataframeDAO dfDao = this.getDataframeDAO();
		// List<Dataframe> list = dfDao.getDataframeByAssemblyId(assembly.getId());

		List<Dataframe> list = new ArrayList<>();
		for (Dataframe df : this.dataframeTable) {
			if (df.getAssemblyIds().contains(assemblyId)) {
				list.add(df);
			}
		}

		return list;
	}

	private List<Assembly> getAssemblyByParentId(String parentId) {
		// AssemblyDAO aDao = this.getAssemblyDAO();
		// List<Assembly> list = aDao.getAssemblyByParentId(parentId);

		List<Assembly> list = new ArrayList<>();
		for (Assembly a : this.assemblyTable) {
			if (a.getParentIds().contains(parentId)) {
				list.add(a);
			}
		}

		return list;
	}

	private List<Dataframe> getDataframeByParentDataframeId(String dataframeId) {
		// DataframeDAO dDao = this.getDataframeDAO();
		// List<Dataframe> list = dDao.getDataframeByParentDataframeId(dataframeId);

		List<Dataframe> list = new ArrayList<>();
		for (Dataframe df : this.dataframeTable) {
			if (df.getDataframeIds().contains(dataframeId)) {
				list.add(df);
			}
		}

		return list;
	}

	private AssemblyLineageItem down(Assembly assembly, int mode, String userId, boolean includeDeleted) {
		return this.down(assembly, mode, userId, includeDeleted, null, null);
	}

	private AssemblyLineageItem down(Assembly assembly, int mode, String userId, boolean includeDeleted,
			String breadcrumb, String legacyBreadcrumb) {
		return this.traverse(DOWN, assembly, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
	}

	private AssemblyLineageItem up(Assembly assembly, int mode, String userId, boolean includeDeleted,
			String breadcrumb, String legacyBreadcrumb) {
		return this.traverse(UP, assembly, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
	}

	private AssemblyLineageItem traverse(int direction, Assembly assembly, int mode, String userId,
			boolean includeDeleted, String breadcrumb, String legacyBreadcrumb) {
		AssemblyLineageItem ali = null;
		if (assembly != null) {
			long totalTime = System.currentTimeMillis();

			AssemblyDAO adao = getAssemblyDAO();
			assembly = adao.getAssembly(assembly.getId());
			ali = AssemblyLineageItem.fromAssembly(assembly);

			if (assembly.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
				ali.setQcStatus(assembly.getQcStatus());
			}

			if (assembly.getAssemblyType().equals(Assembly.DATA_LOAD_TYPE)) {
				breadcrumb = assembly.getEquipId();
				ali.setBreadcrumb(breadcrumb);

				legacyBreadcrumb = assembly.getName();
				ali.setLegacyBreadcrumb(legacyBreadcrumb);
			} else if (assembly.getAssemblyType().equals(Assembly.ANALYSIS_TYPE)) {
				if (breadcrumb != null) {
					breadcrumb += Props.getLineageBreadcrumbSeparator() + assembly.getEquipId();
					ali.setBreadcrumb(breadcrumb);
				}
				if (legacyBreadcrumb != null && assembly.getName() != null) {
					legacyBreadcrumb += Props.getLineageBreadcrumbSeparator() + assembly.getName();
					ali.setLegacyBreadcrumb(legacyBreadcrumb);
				}
			}

			if (direction == DOWN) {
				// HANDLE MEMBER DATAFRAMES
				// We only want dataframes that are datasets
				long memTime = System.currentTimeMillis();
				for (String dfId : assembly.getDataframeIds()) {
					Dataframe memberDataframe = this.getDataframe(dfId);
					if (memberDataframe != null
							&& (memberDataframe.getDataframeType().equalsIgnoreCase(Dataframe.DATASET_TYPE)
									|| (assembly.getAssemblyType().equalsIgnoreCase("analysis")))) {
						DataframeLineageItem dli = DataframeLineageItem.fromDataframe(memberDataframe);

						List<Assembly> memberOfs = this.getAssemblyByMemberDataframeId(dfId);
						for (Assembly owner : memberOfs) {
							dli.getMemberOfAssemblyIds().add(owner.getId());
						}

						ali.getMemberDataframes().add(dli);
					}
				}
				memTime = System.currentTimeMillis() - memTime;

				if (assembly instanceof Analysis) {
					Analysis an = (Analysis) assembly;
					String ppmId = an.getParametersDataframeId();
					if (ppmId != null) {
						DataframeDAO ddao = getDataframeDAO();
						Dataframe ppm = ddao.getDataframe(ppmId);
						if (ppm != null) {
							ali.setParametersEquipId(ppm.getEquipId());
							if (ppm.isPublished()) {
								ali.setPublishStatus("Published");
							}
						}
					}
				}

				// HANDLE CHILD DATAFRAMES
				// The children we add depends on the mode we are in.
				long cdfTime = 0;

				List<Dataframe> childDataframes = this.getDataframeByAssemblyId(assembly.getId());
				List<String> handledEquipIds = new ArrayList<>();
				for (Dataframe childDataframe : childDataframes) {
					Dataframe latest = childDataframe;
					if (!childDataframe.isDeleteFlag() || includeDeleted) {
						latest = null;
						String equipId = childDataframe.getEquipId();
						if (!handledEquipIds.contains(equipId)) {
							latest = childDataframe;
							if (!latest.isDeleteFlag() || includeDeleted) {
								latest = this.getLatest(childDataframes, equipId, userId, includeDeleted);
								handledEquipIds.add(equipId);
							}
						}
					}

					if (latest != null) {
						DataframeDAO ddao = getDataframeDAO();
						latest = ddao.getDataframe(latest.getId(), false);
						boolean stop = this.stopWithDataframe(latest, mode);

						DataframeLineageItem dli = DataframeLineageItem.fromDataframe(latest);
						if (!stop) {
							dli = this.down(latest, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
						}

						ali.getChildDataframes().add(dli);
					}
				}

				cdfTime = System.currentTimeMillis() - cdfTime;

				// HANDLE CHILD ASSEMBLIES
				if (mode == ANALYSIS_PREP_MODE) {
					List<Assembly> childAssemblies = this.getAssemblyByParentId(assembly.getId());
					handledEquipIds = new ArrayList<>();
					for (Assembly a : childAssemblies) {
						Assembly latest = a;
						if (!a.isDeleteFlag() || includeDeleted) {
							latest = null;
							String equipId = a.getEquipId();
							if (!handledEquipIds.contains(equipId)) {
								latest = this.getLatest(childAssemblies, equipId, userId, includeDeleted);
								handledEquipIds.add(equipId);
							}
						}

						if (latest != null) {
							AssemblyLineageItem cli = this.down(latest, mode, userId, includeDeleted, breadcrumb,
									legacyBreadcrumb);
							ali.getChildAssemblies().add(cli);
						}
					}
				}

				totalTime = System.currentTimeMillis() - totalTime;
				LOGGER.info("Total time taken to fetch lineage for assembly " + assembly.getId() + " in "
						+ getModeName(mode) + " mode: " + totalTime + "ms. " + "Time taken to fetch member dataframes: "
						+ memTime + "ms (" + FormattingUtils.percentage(totalTime, memTime) + "%); "
						+ "Time taken to fetch child dataframe lineage: " + cdfTime + "ms ("
						+ FormattingUtils.percentage(totalTime, cdfTime) + "%);");
			} else {
				ModeShapeDAO msDao = new ModeShapeDAO();
				for (String id : assembly.getParentIds()) {
					LineageItem handled = this.getHandledUpItem(id);
					if (handled == null) {
						EquipObject eo = msDao.getEquipObject(id);
						if (eo instanceof Assembly) {
							Assembly parent = (Assembly) eo;
							handled = this.up(parent, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
							this.handledUp.add(handled);
						} else if (eo instanceof Dataframe) {
							Dataframe parent = (Dataframe) eo;
							handled = this.up(parent, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
							this.handledUp.add(handled);
						}
					}

					if (handled != null) {
						handled.getChildAssemblies().add(ali);
					}
				}
			}
		}

		return ali;
	}

	private boolean stopWithDataframe(Dataframe dataframe, int mode) {
		boolean stop = true;
		if (mode == PROMOTION_MODE) {
			// If we're in PROMOTION_MODE, we want to stop once we hit a dataframe that has
			// a promotion status
			// of Promoted, Revoked, or Fail.
			String promotionStatus = dataframe.getPromotionStatus();
			stop = promotionStatus != null && (promotionStatus.equalsIgnoreCase("Promoted")
					|| promotionStatus.equalsIgnoreCase("Revoked") || promotionStatus.equalsIgnoreCase("Fail"));
		} else if (mode == ANALYSIS_PREP_MODE) {
			stop = false;
		}

		return stop;
	}

	private DataframeLineageItem down(Dataframe dataframe, int mode, String userId, boolean includeDeleted) {
		return this.down(dataframe, mode, userId, includeDeleted, null, null);
	}

	private DataframeLineageItem down(Dataframe dataframe, int mode, String userId, boolean includeDeleted,
			String breadcrumb, String legacyBreadcrumb) {
		return this.traverse(DOWN, dataframe, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
	}

	private DataframeLineageItem up(Dataframe dataframe, int mode, String userId, boolean includeDeleted,
			String breadcrumb, String legacyBreadcrumb) {
		return this.traverse(UP, dataframe, mode, userId, includeDeleted, breadcrumb, legacyBreadcrumb);
	}

	private DataframeLineageItem traverse(int direction, Dataframe dataframe, int mode, String userId,
			boolean includeDeleted, String breadcrumb, String legacyBreadcrumb) {
		DataframeLineageItem dli = null;
		if (dataframe != null) {
			long totalTime = System.currentTimeMillis();

			DataframeDAO ddao = getDataframeDAO();
			dataframe = ddao.getDataframe(dataframe.getId(), false);
			dli = DataframeLineageItem.fromDataframe(dataframe);

			if (!dataframe.getDataframeType().equals(Dataframe.REPORT_TYPE)
					&& !dataframe.getDataframeType().equals(Dataframe.REPORT_ITEM_TYPE)) {
				Script script = dataframe.getScript();
				if (script != null && !this.isExcludedBreadcrumbScript(script.getId())) {
					if (breadcrumb != null) {
						breadcrumb += Props.getLineageBreadcrumbSeparator() + dataframe.getEquipId();
						dli.setBreadcrumb(breadcrumb);
					}

					String name = dataframe.getName();
					if (name != null) {
						boolean endsWithSuffix = name.endsWith("_reviewed");
						boolean startsWithAnalysisData = name.startsWith(Dataframe.ANALYSIS_DATA_PREFIX);
						if (name.startsWith(Dataframe.DATA_PREP_PREFIX) || (startsWithAnalysisData && endsWithSuffix)) {
							name = name.split("_")[0];
							dli.setName(name);
						}

						if (legacyBreadcrumb != null && (!startsWithAnalysisData || endsWithSuffix)) {
							legacyBreadcrumb += Props.getLineageBreadcrumbSeparator() + name;
							dli.setLegacyBreadcrumb(legacyBreadcrumb);
						}
					}
				}
			}

			List<String> handledEquipIds = new ArrayList<>();
			long memTime = System.currentTimeMillis();
			if (direction == DOWN) {
				// GET MEMBER ASSEMBLIES
				// We need to retrieve all Assemblies that this dataframe is a member of
				List<Assembly> memberOf = this.getAssemblyByMemberDataframeId(dataframe.getId());
				for (Assembly a : memberOf) {
					Assembly latest = a;
					if (!a.isDeleteFlag() || includeDeleted) {
						latest = null;
						String equipId = a.getEquipId();
						if (!handledEquipIds.contains(equipId)) {
							latest = this.getLatest(memberOf, equipId, userId, includeDeleted);
							handledEquipIds.add(equipId);
						}
					}

					if (latest != null) {
						dli.getMemberOfAssemblyIds().add(latest.getId());
					}
				}
				memTime = System.currentTimeMillis() - memTime;
			}

			// HANDLE CHILD ASSEMBLIES
			// If we are not in DATA_LOAD_MODE, we get all Assemblies that are children of
			// this dataframe
			long caTime = System.currentTimeMillis();
			if (mode != DATA_LOAD_MODE && mode != PROMOTION_MODE) {
				List<Assembly> assemblies = new ArrayList<>();
				if (direction == DOWN) {
					assemblies = this.getAssemblyByParentId(dataframe.getId());
				} else {
					for (String id : dataframe.getAssemblyIds()) {

					}
				}

				handledEquipIds = new ArrayList<>();
				for (Assembly a : assemblies) {
					Assembly latest = a;
					if (!a.isDeleteFlag() || includeDeleted) {
						latest = null;
						String equipId = a.getEquipId();
						if (!handledEquipIds.contains(equipId)) {
							latest = this.getLatest(assemblies, equipId, userId, includeDeleted);
							handledEquipIds.add(equipId);
						}
					}

					if (latest != null) {
						if (direction == DOWN) {
							AssemblyLineageItem ali = this.down(latest, mode, userId, includeDeleted, breadcrumb,
									legacyBreadcrumb);
							dli.getChildAssemblies().add(ali);
						} else {
							LineageItem li = this.up(latest, mode, userId, includeDeleted, breadcrumb,
									legacyBreadcrumb);
							li.getChildDataframes().add(dli);
						}
					}
				}
			}
			caTime = System.currentTimeMillis() - caTime;

			// HANDLE CHILD DATAFRAMES
			long cdfTime = System.currentTimeMillis();
			if (mode != DATA_LOAD_MODE) {
				List<Dataframe> childDataframes = this.getDataframeByParentDataframeId(dataframe.getId());
				handledEquipIds = new ArrayList<>();
				for (Dataframe df : childDataframes) {
					Dataframe latest = df;
					if (!latest.isDeleteFlag() || includeDeleted) {
						latest = null;
						String equipId = df.getEquipId();
						if (!handledEquipIds.contains(equipId)) {
							latest = this.getLatest(childDataframes, equipId, userId, includeDeleted);
							handledEquipIds.add(equipId);
						}
					}

					if (latest != null && !latest.getDataframeType().equalsIgnoreCase(Dataframe.PRIMARY_PARAMETERS_TYPE)
							&& !latest.getDataframeType().equalsIgnoreCase(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)
							&& !latest.getDataframeType().equalsIgnoreCase(Dataframe.KEL_FLAGS_TYPE)) {
						// We have to make sure this child dataframe isn't a member of a child assembly.
						// This is purely so that the dataframe isn't duplicated in the UI.
						boolean isMemberOfChildAssembly = false;
						for (AssemblyLineageItem ali : dli.getChildAssemblies()) {
							for (DataframeLineageItem mdf : ali.getMemberDataframes()) {
								if (mdf.getId().equalsIgnoreCase(latest.getId())) {
									isMemberOfChildAssembly = true;
									break;
								}
							}

							if (isMemberOfChildAssembly) {
								break;
							}
						}

						if (!isMemberOfChildAssembly) {
							DataframeLineageItem cli = this.down(latest, mode, userId, includeDeleted, breadcrumb,
									legacyBreadcrumb);
							dli.getChildDataframes().add(cli);
						}
					}
				}
			}
			cdfTime = System.currentTimeMillis() - cdfTime;

			totalTime = System.currentTimeMillis() - totalTime;
			LOGGER.info("Total time taken to fetch lineage for dataframe " + dataframe.getId() + " in "
					+ getModeName(mode) + " mode: " + totalTime + "ms; " + "Time taken to fetch member-of assemblies: "
					+ memTime + "ms (" + FormattingUtils.percentage(totalTime, memTime) + "%); "
					+ "Time taken to fetch lineage for child assemblies: " + caTime + "ms ("
					+ FormattingUtils.percentage(totalTime, caTime) + "%); "
					+ "Time takeb to fetch lineage for child dataframes: " + cdfTime + "ms ("
					+ FormattingUtils.percentage(totalTime, cdfTime) + "%);");

		}

		return dli;
	}

	// This map will contain objects that have been copied.
	// The reason for this is so that they can be updated with parent IDs of parents
	// that may not be on the same tree level.
	// Ex. id1
	// id2 id3
	// id4
	// id5
	// id6 (child of id2 & id5)
	private Map<String, EquipObject> copiedMap = new HashMap<>();

	private List<LineageItem> handledUp = new ArrayList<>();

	private LineageItem getHandledUpItem(String id) {
		for (LineageItem item : this.handledUp) {
			if (item.getId().equals(id)) {
				return item;
			}
		}

		return null;
	}

	private boolean isExcludedBreadcrumbScript(String id) {
		for (LibraryResponse lr : this.excludedBreadcrumbScripts) {
			if (lr.getArtifactId() != null && lr.getArtifactId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public LineageItem copyLineage(String startId) throws CopyException {
		return this.copyLineage(null, startId);
	}

	@Override
	public LineageItem copyLineage(String copier, String startId) throws CopyException {
		LineageItem copy = null;
		if (startId != null) {
			// Clear any objects that may be in the map from previous calls.
			this.copiedMap = new HashMap<>();
			this.copyLog = new ArrayList<>();

			ModeShapeDAO mdao = new ModeShapeDAO();
			EquipObject object = mdao.getEquipObject(startId);
			if (object != null) {
				if (object instanceof Assembly || object instanceof Dataframe) {
					// LineageItem lineage = this.down(startId);

					this.lineageBuilder.setAuthUserId(this.authUserId);
					LineageItem lineage = this.lineageBuilder.getLineageRoot(startId);
					if (lineage != null) {
						// copy = this.copyLineage(copier, lineage);
						copy = this.startCopy(copier, lineage);
					} else {
						throw new CopyException("Entity " + startId + " is not part of a lineage.");
					}
				} else {
					throw new CopyException("The starting entity is not an Assembly or Dataframe.");
				}
			} else {
				throw new CopyException("No starting entity with ID '" + startId + "' could be found.");
			}
		}

		return copy;
	}

	@Override
	public LineageItem reExecuteLineage(String startId, List<String> newDataframeIds) {
		return this.reExecuteLineage(null, startId, newDataframeIds);
	}

	@Override
	public LineageItem reExecuteLineage(String copier, String startId, List<String> newDataframeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	private DataframeLineageItem copyDataframe(String copier, DataframeLineageItem dli) throws CopyException {
		DataframeLineageItem copy = null;
		if (dli != null) {
			if(dli.userHasAccess()) {
				Dataframe dataframe = (Dataframe) this.copiedMap.get(dli.getId());
				if (dataframe != null) {
					// If the node exists in the copied map, then it has already been handled and has a sibling as a parent.
					// We simply update the parent IDs to handle any new ones.
					// Ex. DF1 has children DF2 and DF3. DF3 has DF2 as a parent as well. We copy DF1, but if we move to DF3 next,
					// it will still be pointing to DF2 instead of DF2's copy (since we haven't copied it yet).
					this.updateParentIds(dataframe);
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty("equip:dataframeIds", dataframe.getDataframeIds());
					pp.addProperty("equip:assemblyIds", dataframe.getAssemblyIds());
	
					ModeShapeDAO msDao = new ModeShapeDAO();
					msDao.updateNode(dataframe.getId(), pp);
					copy = DataframeLineageItem.fromDataframe(dataframe);
				} else if (dli.getPromotionStatus().equalsIgnoreCase("promoted")) {
					dataframe = this.dataframeDAO.getDataframe(dli.getId(), false);
					if (dataframe != null) {
						CopyValidation cv = this.canCopyLineage(dli);
						if (cv.canBeCopied()) {
							// Update the parent IDs with any new ones
							this.updateParentIds(dataframe);
	
							// Perform the copy
							dataframe = CopyUtils.copyDataframe(copier, dataframe);
							copy = DataframeLineageItem.fromDataframe(dataframe);
							this.copiedMap.put(dli.getId(), dataframe);
						} else {
							throw new CopyException(
									"Dataframe " + dli.getId() + " cannot be copied. " + cv.getCopyFailureReason());
						}
					} else {
						throw new CopyException("No dataframe with ID " + dli.getId() + " could be found.");
					}
				} else {
					throw new CopyException("Dataframe " + dli.getEquipId() + " (" + dli.getId() + ") is not promoted ("
							+ dli.getPromotionStatus() + ") and cannot be copied.");
				}
			}
			else {
				this.copyLog.add("User does not have access to dataframe " + dli.getEquipId() + "; did not copy.");
			}
		}

		return copy;
	}

	private AssemblyLineageItem copyAssembly(String copier, AssemblyLineageItem ali) throws CopyException {
		AssemblyLineageItem copy = null;
		if (ali != null) {
			if(ali.userHasAccess()) {
				Assembly assembly = (Assembly) this.copiedMap.get(ali.getId());
				if (assembly != null) {
					this.updateParentIds(assembly);
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty("equip:parentIds", assembly.getParentIds());
	
					ModeShapeDAO msDao = new ModeShapeDAO();
					msDao.updateNode(assembly.getId(), pp);
					copy = AssemblyLineageItem.fromAssembly(assembly);
				} else {
					assembly = this.assemblyDAO.getAssembly(ali.getId());
					if (assembly != null) {
						CopyValidation cv = this.canCopyLineage(ali);
						if (cv.canBeCopied()) {
							this.updateParentIds(assembly);
	
							if (assembly instanceof Analysis) {
								Analysis an = (Analysis) assembly;
	
								// Update the concentration ID
								if (!an.getDataframeIds().isEmpty()) {
									String concId = an.getDataframeIds().get(0);
									Dataframe conc = (Dataframe) this.copiedMap.get(concId);
									if (conc != null) {
										an.setDataframeIds(Arrays.asList(conc.getId()));
									}
								}
	
								// Copy the MCT, KEL, and PPM in this order as PPM may rely on MCT and KEL.
								if (an.getModelConfigurationDataframeId() != null) {
									DataframeLineageItem dli = this.copyDataframe(copier, ali.getMct());
									an.setModelConfigurationDataframeId(dli.getId());
								}
								if (an.getKelFlagsDataframeId() != null) {
									DataframeLineageItem dli = this.copyDataframe(copier, ali.getKel());
									an.setKelFlagsDataframeId(dli.getId());
								}
								if (an.getParametersDataframeId() != null) {
									DataframeLineageItem dli = this.copyDataframe(copier, ali.getPrimaryParameters());
									an.setParametersDataframeId(dli.getId());
								}
	
								assembly = an;
							}
							
							List<DataframeLineageItem> oldDfMembers = ali.getMemberDataframes();
							assembly = CopyUtils.copyAssembly(copier, assembly, false, null, false);
							for(DataframeLineageItem df : oldDfMembers) {
								String ofn = df.getOutputFileName();
								if(ofn != null) {
									for(Dataframe c : assembly.getMemberDataframes()) {
										if(ofn.equals(c.getOutputFileName())) {
											this.copiedMap.put(df.getId(), c);
											break;
										}
									}
								}
							}
							
							copy = AssemblyLineageItem.fromAssembly(assembly);
							this.copiedMap.put(ali.getId(), assembly);
						} else {
							throw new CopyException(
									"Assembly " + ali.getId() + " cannot be copied. " + cv.getCopyFailureReason());
						}
					}
					else {
						throw new CopyException("No Assembly with ID " + ali.getId() + " could be found.");
					}
				}
			}
			else {
				this.copyLog.add("User does not have access to one or more dataframes or assemblies within " + ali.getAssemblyType() + " " + ali.getEquipId() + "; did not copy.");
			}
		}

		return copy;
	}

	private LineageItem startCopy(String copier, LineageItem lineage) throws CopyException {
		LineageItem copy = null;
		if (lineage != null) {
			this.assemblyDAO = ModeShapeDAO.getAssemblyDAO();
			this.dataframeDAO = ModeShapeDAO.getDataframeDAO();

			if (lineage instanceof DataframeLineageItem) {
				copy = this.copyDataframe(copier, (DataframeLineageItem) lineage);
			} else if (lineage instanceof AssemblyLineageItem) {
				copy = this.copyAssembly(copier, (AssemblyLineageItem) lineage);
			}
			
			this.copyDown(copier, lineage);
			
			copy.setChildAssemblies(lineage.getChildAssemblies());
			copy.setChildDataframes(lineage.getChildDataframes());
		}

		return copy;
	}

	private List<LineageItem> copyDown(String copier, LineageItem parent) throws CopyException {
		List<LineageItem> childCopies = new ArrayList<>();
		if (parent != null) {
			List<LineageItem> actualChildren = new ArrayList<>();
			boolean isReportingEvent = false;
			if(parent instanceof AssemblyLineageItem) {
				AssemblyLineageItem ali = (AssemblyLineageItem) parent;
				isReportingEvent = ali.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE);
			}
			
			for (AssemblyLineageItem c : parent.getChildAssemblies()) {
				AssemblyLineageItem alic = this.copyAssembly(copier, c);
				childCopies.add(alic);
				actualChildren.add(c);
			}
			
			for (DataframeLineageItem c : parent.getChildDataframes()) {
				if(!(parent instanceof DataframeLineageItem) || c.getBatchId() == null) {
					// If we are copying the children of a reporting event, we don't want to copy any ATRs.
					boolean copyChild = true;
					if(isReportingEvent && c.getSubType() != null && c.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
						copyChild = false;
					}
					
					if(copyChild) {
						DataframeLineageItem dlic = this.copyDataframe(copier, c);
						childCopies.add(dlic);
						actualChildren.add(c);
					}
				}
			}

			parent.setChildDataframes(new ArrayList<>());
			parent.setChildAssemblies(new ArrayList<>());
			for (int i = 0; i < actualChildren.size(); i++) {
				LineageItem actualChild = actualChildren.get(i);
				List<LineageItem> subCopies = this.copyDown(copier, actualChild);

				LineageItem childCopy = childCopies.get(i);
				childCopy.addChildren(subCopies);

				parent.addChild(childCopy);
			}
		}

		return childCopies;
	}

	private void updateParentIds(EquipObject eo) {
		if (eo instanceof Dataframe) {
			Dataframe df = (Dataframe) eo;
			List<String> newDfParents = new ArrayList<>();
			for (String dfId : df.getDataframeIds()) {
				Dataframe p = (Dataframe) this.copiedMap.get(dfId);
				if (p != null) {
					newDfParents.add(p.getId());
				} else {
					newDfParents.add(dfId);
				}
			}

			List<String> newAsParents = new ArrayList<>();
			for (String asId : df.getAssemblyIds()) {
				Assembly p = (Assembly) this.copiedMap.get(asId);
				if (p != null) {
					newAsParents.add(p.getId());
				} else {
					newAsParents.add(asId);
				}
			}
			
			if(df.getBatchId() != null) {
				Assembly p = (Assembly) this.copiedMap.get(df.getBatchId());
				df.setBatchId(p.getId());
			}

			df.setDataframeIds(newDfParents);
			df.setAssemblyIds(newAsParents);
			eo = df;
		} else if (eo instanceof Assembly) {
			Assembly as = (Assembly) eo;
			List<String> newParents = new ArrayList<>();
			for (String pid : as.getParentIds()) {
				EquipObject p = this.copiedMap.get(pid);
				if (p != null) {
					newParents.add(p.getId());
				} else {
					newParents.add(pid);
				}
			}

			as.setParentIds(newParents);
			eo = as;
		}
	}

	private LineageItem copyLineage(String copier, LineageItem lineage) throws CopyException {
		LineageItem copy = null;
		if (lineage != null) {
			CopyValidation cv = this.canCopyLineage(lineage);
			if (cv.canBeCopied()) {
				String oldId = lineage.getId();
				AssemblyDAO adao = getAssemblyDAO();
				DataframeDAO ddao = getDataframeDAO();

				if (lineage instanceof AssemblyLineageItem) {
					Assembly assembly = adao.getAssembly(oldId);
					if (assembly != null
							&& !assembly.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
						String concId = null;

						String oldPPM = null;
						String oldMCT = null;
						String oldKEL = null;
						Analysis an = null;

						List<DataframeLineageItem> childDataframes = lineage.getChildDataframes();
						if (assembly.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
							an = (Analysis) assembly;
							String oldConcId = an.getDataframeIds().get(0);
							Dataframe conc = (Dataframe) this.copiedMap.get(oldConcId);
							concId = null;
							if (conc != null) {
								concId = conc.getId();
							}

							oldPPM = an.getParametersDataframeId();
							oldMCT = an.getModelConfigurationDataframeId();
							oldKEL = an.getKelFlagsDataframeId();

							AssemblyLineageItem ali = (AssemblyLineageItem) lineage;
							assembly = CopyUtils.copyAnalysis(copier, an, concId, false);
							if (ali.getPrimaryParameters() != null) {
								childDataframes.add(ali.getPrimaryParameters());
							}
							if (ali.getMct() != null) {
								childDataframes.add(ali.getMct());
							}
							if (ali.getKel() != null) {
								childDataframes.add(ali.getKel());
							}
						} else {
							assembly = CopyUtils.copyAssembly(copier, assembly);
						}

						copy = AssemblyLineageItem.fromAssembly(assembly);
						copiedMap.put(oldId, assembly);

						for (int i = 0; i < lineage.getChildAssemblies().size(); i++) {
							AssemblyLineageItem childAssembly = lineage.getChildAssemblies().get(i);
							String oldChildId = childAssembly.getId();
							AssemblyLineageItem childCopy = this.handleChildAssembly(copier, oldId, assembly.getId(),
									childAssembly);
							copy.getChildAssemblies().add(childCopy);

							// Need to make sure that the child updates its relationship to PPM, MCT, and/or
							// KEL, if there is one
							// The reason we cannot rely on concentration data doing the update is that the
							// analysis hasn't been copied yet,
							// so the new PPM, MCT, and KEL IDs are not the new ones yet
							if (an != null) {
								this.checkPPMMCTKEL(an, oldChildId, oldPPM, oldMCT, oldKEL);
							}
						}
						for (int i = 0; i < childDataframes.size(); i++) {
							DataframeLineageItem childDataframe = childDataframes.get(i);
							String oldChildId = childDataframe.getId();
							DataframeLineageItem childCopy = this.handleChildDataframe(copier, oldId, assembly.getId(),
									null, childDataframe);
							copy.getChildDataframes().add(childCopy);

							// Need to make sure that the child updates its relationship to PPM, MCT, and/or
							// KEL, if there is one
							if (an != null) {
								this.checkPPMMCTKEL(an, oldChildId, oldPPM, oldMCT, oldKEL);
							}
						}
					}
				} else if (lineage instanceof DataframeLineageItem) {
					Dataframe dataframe = ddao.getDataframe(oldId);
					if (dataframe != null) {
						if (dataframe.getPromotionStatus().equalsIgnoreCase("promoted")) {
							dataframe = CopyUtils.copyDataframe(copier, dataframe);
							copy = DataframeLineageItem.fromDataframe(dataframe);
							copiedMap.put(oldId, dataframe);
							for (int i = 0; i < lineage.getChildAssemblies().size(); i++) {
								AssemblyLineageItem childAssembly = lineage.getChildAssemblies().get(i);
								AssemblyLineageItem childCopy = this.handleChildAssembly(copier, oldId,
										dataframe.getId(), childAssembly);
								copy.getChildAssemblies().add(childCopy);
							}
							for (int i = 0; i < lineage.getChildDataframes().size(); i++) {
								DataframeLineageItem childDataframe = lineage.getChildDataframes().get(i);
								DataframeLineageItem childCopy = this.handleChildDataframe(copier, oldId, null,
										dataframe.getId(), childDataframe);
								copy.getChildDataframes().add(childCopy);
							}
						} else {
							throw new CopyException("Dataframe " + dataframe.getEquipId() + " (" + dataframe.getId()
									+ ") is not promoted (" + dataframe.getPromotionStatus()
									+ ") and cannot be copied.");
						}
					} else {
						throw new CopyException("No dataframe with ID " + oldId + " could be found.");
					}
				}
			} else {
				throw new CopyException("Cannot copy lineage. " + cv.getCopyFailureReason());
			}
		}

		return copy;
	}

	private void checkPPMMCTKEL(Analysis an, String oldChildId, String oldPPM, String oldMCT, String oldKEL) {
		if (an != null) {
			EquipObject eo = this.copiedMap.get(oldChildId);
			if (eo != null) {
				String prop = null;
				List<String> ids = null;
				if (eo instanceof Dataframe) {
					Dataframe df = (Dataframe) eo;
					prop = "equip:dataframeIds";
					ids = df.getDataframeIds();
				} else if (eo instanceof Assembly) {
					Assembly a = (Assembly) eo;
					prop = "equip:parentIds";
					ids = a.getParentIds();
				}

				if (ids != null) {
					if (oldPPM != null && ids.contains(oldPPM)) {
						ids.remove(oldPPM);
						ids.add(an.getParametersDataframeId());
					}
					if (oldMCT != null && ids.contains(oldMCT)) {
						ids.remove(oldMCT);
						ids.add(an.getModelConfigurationDataframeId());
					}
					if (oldKEL != null && ids.contains(oldKEL)) {
						ids.remove(oldKEL);
						ids.add(an.getKelFlagsDataframeId());
					}

					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty(prop, ids);
					ModeShapeDAO msDao = new ModeShapeDAO();
					msDao.updateNode(eo.getId(), pp);

					if (eo instanceof Assembly) {
						((Assembly) eo).setParentIds(ids);
					} else if (eo instanceof Dataframe) {
						((Dataframe) eo).setDataframeIds(ids);
					}

					this.copiedMap.put(oldChildId, eo);
				}
			}
		}
	}

	private AssemblyLineageItem handleChildAssembly(String copier, String oldParentId, String newParentId,
			AssemblyLineageItem ali) throws CopyException {
		AssemblyDAO adao = getAssemblyDAO();

		// Check to make sure this child hasn't already been copied.
		// If it has, update its parent IDs.
		Assembly ca = (Assembly) this.copiedMap.get(ali.getId());
		AssemblyLineageItem childCopy = null;
		if (ca == null) {
			childCopy = (AssemblyLineageItem) this.copyLineage(copier, ali);
			ca = adao.getAssembly(childCopy.getId());
		}

		ca.getParentIds().remove(oldParentId);
		ca.getParentIds().add(newParentId);

		PropertiesPayload pp = new PropertiesPayload();
		pp.addProperty("equip:parentIds", ca.getParentIds());
		ModeShapeDAO msDao = new ModeShapeDAO();
		msDao.updateNode(ca.getId(), pp);

		this.copiedMap.put(ali.getId(), ca);
		return AssemblyLineageItem.fromAssembly(ca);
	}

	private DataframeLineageItem handleChildDataframe(String copier, String oldParentId, String newAssemblyId,
			String newDataframeId, DataframeLineageItem dli) throws CopyException {
		DataframeDAO ddao = getDataframeDAO();

		// Check to make sure this child hasn't already been copied.
		// If it has, update its parent IDs.
		Dataframe cdf = (Dataframe) this.copiedMap.get(dli.getId());
		DataframeLineageItem childCopy = null;
		if (cdf == null) {
			childCopy = (DataframeLineageItem) this.copyLineage(copier, dli);
			cdf = ddao.getDataframe(childCopy.getId());
		}

		PropertiesPayload pp = new PropertiesPayload();
		if (newAssemblyId != null) {
			cdf.getAssemblyIds().remove(oldParentId);
			cdf.getAssemblyIds().add(newAssemblyId);
			pp.addProperty("equip:assemblyIds", cdf.getAssemblyIds());
		} else if (newDataframeId != null) {
			cdf.getDataframeIds().remove(oldParentId);
			cdf.getDataframeIds().add(newDataframeId);
			pp.addProperty("equip:dataframeIds", cdf.getDataframeIds());
		}

		ModeShapeDAO msDao = new ModeShapeDAO();
		msDao.updateNode(cdf.getId(), pp);

		this.copiedMap.put(dli.getId(), cdf);
		return DataframeLineageItem.fromDataframe(cdf);
	}

	private CopyValidation canCopyLineage(LineageItem li) {
		CopyValidation cv = new CopyValidation();
		if (li != null) {
			//if(li.userHasAccess()) {
				if (li instanceof AssemblyLineageItem) {
					AssemblyLineageItem ali = (AssemblyLineageItem) li;
					Assembly assembly = this.getAssembly(ali.getId());
					
					cv = CopyUtils.canBeCopied(assembly, null);
					// Do not check members; data sets cannot be copied
				} else if (li instanceof DataframeLineageItem) {
					DataframeLineageItem dli = (DataframeLineageItem) li;
					Dataframe dataframe = this.getDataframe(dli.getId());
					
					cv = CopyUtils.canBeCopied(dataframe, true, null);
				}
	
				if (cv.canBeCopied()) {
					for (AssemblyLineageItem child : li.getChildAssemblies()) {
						cv = this.canCopyLineage(child);
					}
					for (DataframeLineageItem child : li.getChildDataframes()) {
						cv = this.canCopyLineage(child);
					}
				}
			/*}
			else {
				String nodeType = "assembly";
				if(li instanceof DataframeLineageItem) {
					nodeType = "dataframe";
				}
				
				cv.setCopyFailureReason("User does not have access to " + nodeType + " " + li.getEquipId() + " v" + li.getEquipVersion() + ".");
			}*/
		}

		return cv;
	}

	private static final String getModeName(int mode) {
		String m = null;
		if (mode == DATA_LOAD_MODE) {
			m = "DATA LOAD";
		} else if (mode == ANALYSIS_PREP_MODE) {
			m = "ANALYSIS PREP";
		} else if (mode == PROMOTION_MODE) {
			m = "PROMOTION";
		} else if (mode == FULL_MODE) {
			m = "FULL";
		}

		return m;
	}

	@Override
	public List<AssemblyLineageItem> getRawAnalysisPrepLineage(String nodeId, String userId) {
		List<AssemblyLineageItem> lineage = new ArrayList<>();
		if (nodeId != null) {
			LineageBuilder builder = this.createLineageBuilder();
			builder.setUserId(userId);
			Map<String, List<AssemblyLineageItem>> map = builder.getRawLineage(nodeId, true);
			for (Entry<String, List<AssemblyLineageItem>> e : map.entrySet()) {
				lineage.addAll(e.getValue());
			}
		}

		return lineage;
	}

	private LineageBuilder createLineageBuilder() {
		return this.createLineageBuilder(false, null, LineageBuilder.ANALYSIS_PREP_MODE);
	}

	private LineageBuilder createLineageBuilder(boolean includeDeleted, String userId, int mode) {
		LineageBuilder builder = new LineageBuilder();
		builder.setIncludeDeleted(includeDeleted);
		builder.setUserId(userId);
		builder.setMode(mode);
		builder.setAuthUserId(this.authUserId);

		try {
			List<String> scriptNames = Props.getExcludedBreadcrumbScriptNames();
			LibraryServiceClient lsc = new LibraryServiceClient(Props.getLibraryServiceServer(),
					Props.getLibraryServicePort());
			lsc.setUser(Props.getServiceAccountName());
			this.excludedBreadcrumbScripts = lsc.getGlobalSystemScriptByName(scriptNames);
			builder.setExcludedBreadcrumbScripts(this.excludedBreadcrumbScripts);
		} catch (ServiceCallerException sce) {
			LOGGER.info(
					"Unable to retrieve excluded breadcrumb scripts. " + sce.getStatusCode() + ": " + sce.getMessage());
		}
		
		return builder;
	}

	@Override
	public String getAuthUserId() {
		return this.authUserId;
	}

	@Override
	public void setAuthUserId(String authUserId) {
		this.authUserId = authUserId;
	}
}
