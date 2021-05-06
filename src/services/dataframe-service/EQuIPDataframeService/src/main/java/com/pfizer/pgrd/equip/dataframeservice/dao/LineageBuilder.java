package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.search.SearchServiceClient;

import spark.HaltException;
import spark.Spark;

public class LineageBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(LineageBuilder.class);
	public static final int DATA_LOAD_MODE = 0, ANALYSIS_PREP_MODE = 1, PROMOTION_MODE = 2, FULL_MODE = 3;

	private List<LibraryResponse> excludedBreadcrumbScripts = new ArrayList<>();

	private Map<String, Assembly> ASSEMBLY_TABLE = new HashMap<>();
	private Map<String, Dataframe> DATAFRAME_TABLE = new HashMap<>();
	private Map<String, LineageItem> ALL_LINEAGE_ITEMS = new HashMap<>();
	private Map<String, Boolean> ACCESS_MAP = new HashMap<>();

	private String userId;
	private String authUserId;

	public String getAuthUserId() {
		return authUserId;
	}

	public void setAuthUserId(String authUserId) {
		this.authUserId = authUserId;
	}

	private boolean includeDeleted = false;
	private boolean latestOnly = true;
	private int mode = LineageBuilder.ANALYSIS_PREP_MODE;

	/**
	 * Returns a {@link Map} of {@link List} objects of {@link AssemblyLineageItem}
	 * objects who represent roots of the lineage maps created by the user ID in the
	 * builder settings. The {@code keys} to the map are the study IDs.
	 * 
	 * @return
	 */
	public Map<String, List<AssemblyLineageItem>> getLineage() {
		return this.getLineageBase(null);
	}

	/**
	 * Returns a {@link List} of {@link AssemblyLineageItem} objects who represent
	 * the roots of the lineage map related to the provided study ID.
	 * 
	 * @param studyId
	 * @return {@link List}<{@link AssemblyLineageItem}>
	 */
	public List<AssemblyLineageItem> getLineage(String studyId) {
		Map<String, List<AssemblyLineageItem>> lineages = this.getLineage(new String[] { studyId });
		List<AssemblyLineageItem> lineage = lineages.get(studyId);
		if (lineage == null) {
			lineage = new ArrayList<>();
		}

		return lineage;
	}

	/**
	 * Returns a {@link Map} of {@link List} objects of {@link AssemblyLineageItem}
	 * objects who represent roots of the lineage maps related to the provided study
	 * IDs. The {@code keys} to the map are the study IDs.
	 * 
	 * @param studyIds
	 * @return {@link Map}<{@link String},
	 *         {@link List}<{@link AssemblyLineageItem}>>
	 */
	public Map<String, List<AssemblyLineageItem>> getLineage(List<String> studyIds) {
		return this.getLineageBase(studyIds);
	}

	/**
	 * Returns a {@link Map} of {@link List} objects of {@link AssemblyLineageItem}
	 * objects who represent roots of the lineage maps related to the provided study
	 * IDs. The {@code keys} to the map are the study IDs.
	 * 
	 * @param studyIds
	 * @return {@link Map}<{@link String},
	 *         {@link List}<{@link AssemblyLineageItem}>>
	 */
	public Map<String, List<AssemblyLineageItem>> getLineage(String[] studyIds) {
		return this.getLineageBase(Arrays.asList(studyIds));
	}

	/**
	 * Returns a {@link Map} of {@link List} of {@link AssemblyLineageItem} objects
	 * representing the lineage containing the provided node ID. No version checking
	 * will be performed on the nodes; the direct links will be handled as they are.
	 * 
	 * @param nodeId
	 * @return {@link List}<{@link AssemblyLineageItem}>
	 */
	public Map<String, List<AssemblyLineageItem>> getRawLineage(String nodeId, boolean fetchData) {
		Map<String, List<AssemblyLineageItem>> lineage = new HashMap<>();
		if (nodeId != null) {
			EquipObject eo = null;
			if (fetchData) {
				this.ASSEMBLY_TABLE = new HashMap<>();
				this.DATAFRAME_TABLE = new HashMap<>();
				eo = this.bloomTables(nodeId);
			} else {
				Dataframe df = this.DATAFRAME_TABLE.get(nodeId);
				if (df != null) {
					eo = df;
				} else {
					Assembly a = this.ASSEMBLY_TABLE.get(nodeId);
					if (a != null) {
						eo = a;
					}
				}
			}

			if (eo != null) {
				if (fetchData) {
					this.rootTables(eo);
				}

				List<String> studyIds = new ArrayList<>();
				if (eo instanceof Assembly) {
					studyIds = ((Assembly) eo).getStudyIds();
				} else if (eo instanceof Dataframe) {
					studyIds = ((Dataframe) eo).getStudyIds();
				}

				this.populateAccessTable();

				// Since we populated the tables already, don't populate them again.
				lineage = this.getLineageBase(studyIds, false);
			}
		}

		return lineage;
	}

	private void populateAccessTable() {
		try {
			List<Dataframe> dataframes = new ArrayList<>();
			for (Entry<String, Dataframe> e : this.DATAFRAME_TABLE.entrySet()) {
				dataframes.add(e.getValue());
			}

			AuthorizationDAO authDao = new AuthorizationDAO();
			
			String uid = this.authUserId;
			if (uid == null) {
				uid = this.userId;
			}

			this.ACCESS_MAP = authDao.canViewDataframe(dataframes, uid);
		}
		catch(ServiceCallerException se) {
			LOGGER.error("Error when calling " + se.getServiceName() + ": " + se.getStatusCode() + " " + se.getMessage());
			Spark.halt(500, "Error when checking authorization for dataframes.");
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error when checking authorization for dataframes.", e);
			Spark.halt(500, "Error when checking authorization for dataframes.");
		}
	}

	/**
	 * Returns the {@link LineageItem} related to the provided ID as a root of a
	 * lineage.
	 * 
	 * @param nodeId
	 * @return {@link LineageItem}
	 */
	public LineageItem getLineageRoot(String nodeId) {
		return this.getLineageRoot(nodeId, false);
	}

	/**
	 * Returns the {@link LineageItem} related to the provided EQUIP ID as a root of
	 * a lineage.
	 * 
	 * @param equipId
	 * @return {@link LineageItem}
	 */
	public LineageItem getLineageRootByEquipId(String equipId) {
		return this.getLineageRoot(equipId, true);
	}

	private LineageItem getLineageRoot(String id, boolean byEquipId) {
		if (id != null) {
			List<EquipObject> objects = new ArrayList<>();
			if (byEquipId) {
				EquipIDDAO eDao = ModeShapeDAO.getEquipIDDAO();
				objects = eDao.getItem(id);
			} else {
				ModeShapeDAO msDao = new ModeShapeDAO();
				EquipObject eo = msDao.getEquipObject(id);
				if (eo != null) {
					objects.add(eo);
				}
			}

			if (!objects.isEmpty()) {
				EquipObject eo = objects.get(0);
				List<String> studyIds = new ArrayList<>();
				if (eo instanceof Assembly) {
					studyIds = ((Assembly) eo).getStudyIds();
				} else if (eo instanceof Dataframe) {
					studyIds = ((Dataframe) eo).getStudyIds();
				}

				if (!studyIds.isEmpty()) {
					this.populateTables(studyIds);
					Map<String, List<AssemblyLineageItem>> map = new HashMap<>();
					if (byEquipId) {
						map = this.getLineageBase(studyIds);
					} else {
						map = this.getRawLineage(id, false);
					}

					for (Entry<String, List<AssemblyLineageItem>> entry : map.entrySet()) {
						List<AssemblyLineageItem> lineage = entry.getValue();
						LineageItem item = null;
						if (byEquipId) {
							item = LineageSearcher.getItemAsRootByEquipId(lineage, id);
						} else {
							item = LineageSearcher.getItemAsRoot(lineage, id);
						}

						if (item != null) {
							return item;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns a {@link Map} of {@link List} objects of {@link AssemblyLineageItem}
	 * objects who represent roots of the lineage maps related to the provided study
	 * IDs. The {@code keys} to the map are the study IDs. <BR>
	 * <BR>
	 * If no study IDs are provided, the result will contain all lineages created by
	 * the user ID provided in the builder.
	 * 
	 * @param studyIds
	 * @return {@link Map}<{@link String},
	 *         {@link List}<{@link AssemblyLineageItem}>>
	 */
	private Map<String, List<AssemblyLineageItem>> getLineageBase(List<String> studyIds) {
		return this.getLineageBase(studyIds, true);
	}

	/**
	 * Returns a {@link Map} of {@link List} objects of {@link AssemblyLineageItem}
	 * objects who represent roots of the lineage maps related to the provided study
	 * IDs. The {@code keys} to the map are the study IDs. <BR>
	 * <BR>
	 * If no study IDs are provided, the result will contain all lineages created by
	 * the user ID provided in the builder.
	 * 
	 * @param studyIds
	 * @return {@link Map}<{@link String},
	 *         {@link List}<{@link AssemblyLineageItem}>>
	 */
	private Map<String, List<AssemblyLineageItem>> getLineageBase(List<String> studyIds, boolean populateTables) {
		Map<String, List<AssemblyLineageItem>> map = new HashMap<>();

		// If no study IDs were provided and a user ID is set, we gather all study IDs
		// that the user has contributed to.
		if (studyIds == null && this.userId != null) {
			studyIds = this.getStudyIds();
		}

		for (String studyId : studyIds) {
			if (populateTables) {
				this.populateTables(studyId);
			}

			this.ALL_LINEAGE_ITEMS = new HashMap<>();

			// We start with the Data Loads and move downward.
			// This is a breadth-first implementation. This was done so that creating the
			// full, flat breadcrumb becomes simpler
			// as it is guaranteed that all of a child's parents have already been handled.
			List<AssemblyLineageItem> roots = new ArrayList<>();
			List<Assembly> dataLoads = this.getDataLoadsByStudyId(studyId);
			dataLoads.sort(new EquipObjectComparator());
			List<String> handledEquipIds = new ArrayList<>();
			for (Assembly dataLoad : dataLoads) {
				String equipId = dataLoad.getEquipId();
				if (!handledEquipIds.contains(equipId)) {
					List<Assembly> filtered = this.filterItemsByEquipId(dataLoads, equipId);
					Assembly latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);

					if (latest != null) {
						if (this.mode != LineageBuilder.DATA_LOAD_MODE || (this.userId == null || latest.getCreatedBy().equalsIgnoreCase(this.userId))) {
							AssemblyLineageItem root = this.createLineageItem(dataLoad, null, null);
							this.ALL_LINEAGE_ITEMS.put(root.getId(), root);
							this.down(root, null, null);
							roots.add(root);
						}
					}

					handledEquipIds.add(equipId);
				}
			}

			map.put(studyId, roots);
		}

		return map;
	}

	/**
	 * Returns an {@link AssemblyLineageItem} object created from the provided
	 * {@link Assembly} object and breadcrumbs.
	 * 
	 * @param assembly
	 * @param breadcrumb
	 * @param legacyBreadcrumb
	 * @return {@link AssemblyLineageItem}
	 */
	private AssemblyLineageItem createLineageItem(Assembly assembly, String breadcrumb, String legacyBreadcrumb) {
		AssemblyLineageItem ali = AssemblyLineageItem.fromAssembly(assembly);

		// HANDLE BREADCRUMBS
		if (ali.getAssemblyType().equals(Assembly.DATA_LOAD_TYPE)) {
			breadcrumb = ali.getEquipId();
			ali.setBreadcrumb(breadcrumb);

			legacyBreadcrumb = ali.getName();
			ali.setLegacyBreadcrumb(legacyBreadcrumb);
		} else if (ali.getAssemblyType().equals(Assembly.ANALYSIS_TYPE)) {
			if (breadcrumb != null) {
				breadcrumb += Props.getLineageBreadcrumbSeparator() + ali.getEquipId();
				ali.setBreadcrumb(breadcrumb);
			}
			if (legacyBreadcrumb != null && ali.getName() != null) {
				legacyBreadcrumb += Props.getLineageBreadcrumbSeparator() + ali.getName();
				ali.setLegacyBreadcrumb(legacyBreadcrumb);
			}
		}
		else if(ali.getAssemblyType().equals(Assembly.BATCH_TYPE)) {
			ali.setBreadcrumb(breadcrumb);
			ali.setLegacyBreadcrumb(legacyBreadcrumb);
		}
		

		List<LineageItem> parents = this.getItems(assembly.getParentIds());
		this.populateFullBreadcrumbs(ali, parents);

		boolean hasBlockedDfs = false;

		// GET PRIMARY PARAMETERS
		if (assembly instanceof Analysis) {
			Analysis an = (Analysis) assembly;
			String ppmId = an.getParametersDataframeId();
			if (ppmId != null) {
				Dataframe ppm = this.DATAFRAME_TABLE.get(ppmId);
				if (ppm != null) {
					ali.setParametersEquipId(ppm.getEquipId());
					if (ppm.isPublished()) {
						ali.setPublishStatus("Published");
					}

					if (!this.ACCESS_MAP.get(ppm.getId())) {
						hasBlockedDfs = true;
					}
				}
			}
		}

		// HANDLE MEMBER DATAFRAMES
		String aType = assembly.getAssemblyType();
		for (String dfId : assembly.getDataframeIds()) {
			Dataframe memberDataframe = this.DATAFRAME_TABLE.get(dfId);
			if (memberDataframe != null && (memberDataframe.getDataframeType().equalsIgnoreCase(Dataframe.DATASET_TYPE)
					|| aType.equalsIgnoreCase(Assembly.ANALYSIS_TYPE) || aType.equalsIgnoreCase(Assembly.BATCH_TYPE))) {
				
				ali.setMemberDataframeType(memberDataframe.getDataframeType());
				
				DataframeLineageItem dli = this.createLineageItem(memberDataframe, breadcrumb, legacyBreadcrumb);
				//DataframeLineageItem dli = DataframeLineageItem.fromDataframe(memberDataframe, this.ACCESS_MAP.get(memberDataframe.getId()));
				this.gatherAttachments(dli);
				
				List<Assembly> memberOfs = this.getAssembliesByMemberDataframeId(dfId);
				for (Assembly owner : memberOfs) {
					dli.getMemberOfAssemblyIds().add(owner.getId());
				}

				ali.getMemberDataframes().add(dli);
				if (!dli.userHasAccess()) {
					hasBlockedDfs = true;
				}
				
				this.ALL_LINEAGE_ITEMS.put(dli.getId(), dli);
			}
		}

		// ADD HELPERS
		if (assembly instanceof Analysis || ali.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
			Dataframe df = this.DATAFRAME_TABLE.get(ali.getParametersDataframeId());
			if (df != null) {
				ali.setPrimaryParameters(DataframeLineageItem.fromDataframe(df, this.ACCESS_MAP.get(df.getId())));
				if (!ali.getPrimaryParameters().userHasAccess()) {
					hasBlockedDfs = true;
				}
			}

			df = this.DATAFRAME_TABLE.get(ali.getModelConfigurationDataframeId());
			if (df != null) {
				ali.setMct(DataframeLineageItem.fromDataframe(df, this.ACCESS_MAP.get(df.getId())));
				if (!ali.getMct().userHasAccess()) {
					hasBlockedDfs = true;
				}
			}

			df = this.DATAFRAME_TABLE.get(ali.getKelFlagsDataframeId());
			if (df != null) {
				ali.setKel(DataframeLineageItem.fromDataframe(df, this.ACCESS_MAP.get(df.getId())));
				if (!ali.getKel().userHasAccess()) {
					hasBlockedDfs = true;
				}
			}

			df = this.DATAFRAME_TABLE.get(ali.getEstimatedConcDataframeId());
			if (df != null) {
				ali.setEstimatedConcentraction(DataframeLineageItem.fromDataframe(df, this.ACCESS_MAP.get(df.getId())));
				if (!ali.getEstimatedConcentraction().userHasAccess()) {
					hasBlockedDfs = true;
				}
			}
		}

		if (hasBlockedDfs && (ali.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE) || ali.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE))) {
			ali.setUserHasAccess(false);
			ali.setComments(new ArrayList<>());
			ali.setMetadata(new ArrayList<>());
		} else {
			// HANDLE ATTACHMENTS
			this.gatherAttachments(ali);
		}
		
		List<String> cleanParents = new ArrayList<>();
		for(String id : ali.getParentIds()) {
			Dataframe d = this.DATAFRAME_TABLE.get(id);
			if(d != null) {
				cleanParents.add(d.getId());
				ali.getParentDataframeIds().add(id);
			}
			
			Assembly a = this.ASSEMBLY_TABLE.get(id);
			if(a != null) {
				ali.getParentAssemblyIds().add(id);
			}
		}
		
		if(ali.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE)) {
			ali.setParentIds(cleanParents);
		}
		
		return ali;
	}

	/**
	 * Returns an {@link AssemblyLineageItem} created from the provided
	 * {@link Assembly}. Recursively traverses <i>down</i> the lineage and populates
	 * all child and member nodes and breadcrumbs.
	 * 
	 * @param assembly
	 * @return {@link AssemblyLineageItem}
	 */
	private AssemblyLineageItem down(AssemblyLineageItem ali, String breadcrumb, String legacyBreadcrumb) {
		if (ali.getBreadcrumb() != null) {
			breadcrumb = ali.getBreadcrumb();
		}
		if (ali.getLegacyBreadcrumb() != null) {
			legacyBreadcrumb = ali.getLegacyBreadcrumb();
		}
		
		List<String> handledEquipIds = new ArrayList<>();
		
		// HANDLE CHILD ASSEMBLIES
		List<Assembly> childAssemblies = new ArrayList<>();
		if (mode == ANALYSIS_PREP_MODE) {
			if(ali.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE)) {
				//childDataframes = this.getDataframes(ali.getDataframeIds());
				for(String dfId : ali.getDataframeIds()) {
					List<Assembly> as = this.getChildAssemblies(dfId);
					childAssemblies.addAll(as);
				}
			}
			else {
				childAssemblies = this.getChildAssemblies(ali.getId());
			}
			
			handledEquipIds = new ArrayList<>();
			for (Assembly a : childAssemblies) {
				Assembly latest = null;
				String equipId = a.getEquipId();
				if (!handledEquipIds.contains(equipId)) {
					List<Assembly> filtered = this.filterItemsByEquipId(childAssemblies, equipId);
					latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
					handledEquipIds.add(equipId);
				}
				
				if (latest != null) {
					AssemblyLineageItem cli = this.createLineageItem(latest, breadcrumb, legacyBreadcrumb);
					this.ALL_LINEAGE_ITEMS.put(cli.getId(), cli);
					ali.getChildAssemblies().add(cli);
				}
			}
			
			for (AssemblyLineageItem child : ali.getChildAssemblies()) {
				this.down(child, breadcrumb, legacyBreadcrumb);
			}
		}
		
		// HANDLE CHILD DATAFRAMES
		List<Dataframe> childDataframes = new ArrayList<>();
		// A batch will never truly have any children (no dataframes will ever have a batch ID as a parent ID).
		// However, to make the lineage simpler and consistent, we will treat the members as children.
		if(ali.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE)) {
			//childDataframes = this.getDataframes(ali.getDataframeIds());
			for(String dfId : ali.getDataframeIds()) {
				List<Dataframe> dfs = this.getChildDataframes(dfId);
				for(Dataframe df : dfs) {
					boolean add = true;
					for(Assembly a : childAssemblies) {
						if(a.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE) && a.getDataframeIds().contains(df.getId())) {
							add = false;
							break;
						}
					}
					
					if(add) {
						childDataframes.add(df);
					}
				}
			}
		}
		else {
			childDataframes = this.getChildDataframes(ali.getId());
		}
		
		for (Dataframe childDataframe : childDataframes) {
			Dataframe latest = null;
			String equipId = childDataframe.getEquipId();
			if(childDataframe.getBatchId() == null) {
				if (!childDataframe.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)
						&& !handledEquipIds.contains(equipId)
						&& !childDataframe.getDataframeType().equalsIgnoreCase(Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE)
						&& (ali.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)
								|| childDataframe.getSubType() == null
								|| !childDataframe.getSubType().equalsIgnoreCase(Dataframe.ANALYSIS_QC_REPORT_SUB_TYPE))) {
					List<Dataframe> filtered = this.filterItemsByEquipId(childDataframes, equipId);
					latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
					handledEquipIds.add(equipId);
				}
	
				if (latest != null) {
					latest = this.DATAFRAME_TABLE.get(latest.getId());
					DataframeLineageItem dli = this.createLineageItem(latest, breadcrumb, legacyBreadcrumb);
					this.ALL_LINEAGE_ITEMS.put(dli.getId(), dli);
					ali.getChildDataframes().add(dli);
				}
			}
		}

		for (DataframeLineageItem child : ali.getChildDataframes()) {
			if (!child.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
				boolean stop = this.stopWithDataframe(child);
				if (!stop) {
					this.down(child, breadcrumb, legacyBreadcrumb);
				}
			}
		}
		
		return ali;
	}

	/**
	 * Returns a {@link DataframeLineageItem} object created from the provided
	 * {@link Dataframe} object and breadcrumbs.
	 * 
	 * @param dataframe
	 * @param breadcrumb
	 * @param legacyBreadcrumb
	 * @return {@link DataframeLineageItem}
	 */
	private DataframeLineageItem createLineageItem(Dataframe dataframe, String breadcrumb, String legacyBreadcrumb) {
		DataframeLineageItem dli = DataframeLineageItem.fromDataframe(dataframe);
		if (!this.ACCESS_MAP.get(dataframe.getId())) {
			dli.setUserHasAccess(false);
			dli.setComments(new ArrayList<>());
			dli.setMetadata(new ArrayList<>());
		} else {
			// HANDLE ATTACHMENTS
			this.gatherAttachments(dli);
		}

		// HANDLE BREADCRUMBS
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

		List<String> parentIds = new ArrayList<>();
		for (String aid : dataframe.getAssemblyIds()) {
			parentIds.add(aid);
		}
		for (String did : dataframe.getDataframeIds()) {
			parentIds.add(did);
		}

		List<LineageItem> parents = this.getItems(parentIds);
		this.populateFullBreadcrumbs(dli, parents);

		return dli;
	}

	/**
	 * Returns an {@link DataframeLineageItem} created from the provided
	 * {@link Dataframe}. Recursively traverses <i>down</i> the lineage and
	 * populates all child nodes and breadcrumbs.
	 * 
	 * @param dataframe
	 * @return {@link DataframeLineageItem}
	 */
	private DataframeLineageItem down(DataframeLineageItem dli, String breadcrumb, String legacyBreadcrumb) {
		if (dli.getBreadcrumb() != null) {
			breadcrumb = dli.getBreadcrumb();
		}
		if (dli.getLegacyBreadcrumb() != null) {
			legacyBreadcrumb = dli.getLegacyBreadcrumb();
		}

		List<String> handledEquipIds = new ArrayList<>();

		// GET MEMBER ASSEMBLIES
		// We need to retrieve all Assemblies that this dataframe is a member of.
		List<Assembly> memberOf = this.getAssembliesByMemberDataframeId(dli.getId());
		for (Assembly a : memberOf) {
			Assembly latest = null;
			String equipId = a.getEquipId();
			if (!handledEquipIds.contains(equipId)) {
				List<Assembly> filtered = this.filterItemsByEquipId(memberOf, equipId);
				latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
				handledEquipIds.add(equipId);
			}

			if (latest != null) {
				dli.getMemberOfAssemblyIds().add(latest.getId());
			}
		}

		// HANDLE CHILD ASSEMBLIES
		// If we are not in DATA_LOAD_MODE, we get all Assemblies that are children of
		// this dataframe.
		List<String> batchMembers = new ArrayList<>();
		if (mode != DATA_LOAD_MODE && mode != PROMOTION_MODE) {
			handledEquipIds = new ArrayList<>();

			List<Assembly> assemblies = this.getChildAssemblies(dli.getId());
			for (Assembly a : assemblies) {
				Assembly latest = null;
				String equipId = a.getEquipId();
				if (!handledEquipIds.contains(equipId)) {
					List<Assembly> filtered = this.filterItemsByEquipId(assemblies, equipId);
					latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
					handledEquipIds.add(equipId);
				}

				if (latest != null) {
					boolean isChildOfAnalysis = false;
					for(String aid : latest.getParentIds()) {
						Assembly pa = this.getAssemblyTable().get(aid);
						if(pa != null && pa.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
							isChildOfAnalysis = true;
							break;
						}
					}
					
					if(!isChildOfAnalysis) {
						AssemblyLineageItem ali = this.createLineageItem(latest, breadcrumb, legacyBreadcrumb);
						if(ali.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE)) {
							batchMembers.addAll(ali.getDataframeIds());
						}
						
						this.ALL_LINEAGE_ITEMS.put(ali.getId(), ali);
						dli.getChildAssemblies().add(ali);
					}
				}
			}

			for (AssemblyLineageItem child : dli.getChildAssemblies()) {
				this.down(child, breadcrumb, legacyBreadcrumb);
			}
		}

		// HANDLE CHILD DATAFRAMES
		if (mode != DATA_LOAD_MODE) {
			List<Dataframe> childDataframes = this.getChildDataframes(dli.getId());
			handledEquipIds = new ArrayList<>();
			for (Dataframe df : childDataframes) {
				Dataframe latest = null;
				if(!batchMembers.contains(df.getId()) && df.getBatchId() == null) {
					String equipId = df.getEquipId();
					if (!df.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)
							&& !df.getDataframeType().equalsIgnoreCase(Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE)
							&& !handledEquipIds.contains(equipId) && (df.getSubType() == null
									|| !df.getSubType().equalsIgnoreCase(Dataframe.ANALYSIS_QC_REPORT_SUB_TYPE))) {
						List<Dataframe> filtered = this.filterItemsByEquipId(childDataframes, equipId);
						latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
						handledEquipIds.add(equipId);
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
						
						boolean isChildOfAnalysis = false;
						for(String aid : latest.getAssemblyIds()) {
							Assembly a = this.getAssemblyTable().get(aid);
							if(a != null && a.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
								isChildOfAnalysis = true;
								break;
							}
						}
	
						if (!isMemberOfChildAssembly && !isChildOfAnalysis) {
							DataframeLineageItem cli = this.createLineageItem(latest, breadcrumb, legacyBreadcrumb);
							this.ALL_LINEAGE_ITEMS.put(cli.getId(), cli);
							dli.getChildDataframes().add(cli);
						}
					}
				}
			}

			for (DataframeLineageItem child : dli.getChildDataframes()) {
				if (!child.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
					this.down(child, breadcrumb, legacyBreadcrumb);
				}
			}
		}

		return dli;
	}

	/**
	 * Returns a {@link List} of unique study IDs that the set user ID has
	 * contributed to.
	 * 
	 * @return {@link List}<{@link String}>
	 */
	private List<String> getStudyIds() {
		List<String> studyIds = new ArrayList<>();
		if (this.userId != null) {
			AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
			List<Assembly> assemblies = aDao.getAssemblyByUserId(this.userId);

			for (Assembly a : assemblies) {
				for (String studyId : a.getStudyIds()) {
					if (!studyIds.contains(studyId)) {
						studyIds.add(studyId);
					}
				}
			}
		}

		return studyIds;
	}

	/**
	 * Populates the {@code attachments} field of the provided {@link LineageItem}
	 * object.
	 * 
	 * @param item
	 */
	private void gatherAttachments(LineageItem item) {
		if (item != null && item.userHasAccess()) {
			List<Dataframe> children = this.getChildDataframes(item.getId());
			List<String> handledIds = new ArrayList<>();
			for (Dataframe df : children) {
				if (df.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE) && df.getEquipId() != null
						&& !handledIds.contains(df.getEquipId())) {
					String equipId = df.getEquipId();
					List<Dataframe> filtered = this.filterItemsByEquipId(children, equipId);
					Dataframe latest = VersioningDAO.getLatestVersion(filtered, this.userId, this.includeDeleted);
					if (latest != null) {
						DataframeLineageItem dli = DataframeLineageItem.fromDataframe(df);
						item.getAttachments().add(dli);
					}

					handledIds.add(df.getEquipId());
				}
			}
		}
	}

	private void populateTables(List<String> studyIds) {
		this.ASSEMBLY_TABLE = new HashMap<>();
		this.DATAFRAME_TABLE = new HashMap<>();

		for (String studyId : studyIds) {
			this.populateTables(studyId, false);
		}
	}

	/**
	 * Populates the {@code ASSEMBLY_TABLE} and {@code DATAFRAME_TABLE} with all
	 * Assemblies and Dataframes relating to the provided study ID from the
	 * repository. Will filter-out all deleted nodes if {@code includedDeleted} is
	 * {@code false}.
	 * 
	 * @param studyIds
	 */
	private void populateTables(String studyId) {
		this.populateTables(studyId, true);
	}

	/**
	 * Populates the {@code ASSEMBLY_TABLE} and {@code DATAFRAME_TABLE} with all
	 * Assemblies and Dataframes relating to the provided study ID from the
	 * repository. Will filter-out all deleted nodes if {@code includedDeleted} is
	 * {@code false}.
	 * 
	 * @param studyIds
	 */
	private void populateTables(String studyId, boolean restart) {
		try {
			if (restart) {
				this.ASSEMBLY_TABLE = new HashMap<>();
				this.DATAFRAME_TABLE = new HashMap<>();
			}

			String json = null;
			List<Dataframe> dataframes = new ArrayList<>();
			if (studyId != null) {
				String lineageDataSource = Props.getLineageDataSource();
				if (lineageDataSource.equalsIgnoreCase(Props.LINEAGE_DATA_SOURCE_ELASTIC)) {
					SearchServiceClient ssClient = ModeShapeDAO.getSearchServiceClient();

					ServiceResponse response = ssClient.searchByStudyId(studyId);
					if (response != null && response.getCode() == HTTPStatusCodes.OK) {
						json = response.getResponseAsString();
					} else {
						LOGGER.error("An error ocurred when calling the Search Service for study ID '" + studyId + "'. "
								+ response.getCode() + ": " + response.getResponseAsString());
					}
				} else if (lineageDataSource.equalsIgnoreCase(Props.LINEAGE_DATA_SOURCE_MODESHAPE)) {
					AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
					DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();

					List<Assembly> atemp = aDao.getAssemblyByStudyId(studyId);
					for (Assembly a : atemp) {
						if (this.includeAssembly(a)) {
							this.ASSEMBLY_TABLE.put(a.getId(), aDao.getAssembly(a.getId()));
						}
					}

					List<Dataframe> dtemp = dDao.getDataframeByStudyId(studyId);
					for (Dataframe df : dtemp) {
						if (this.includeDataframe(df)) {
							this.DATAFRAME_TABLE.put(df.getId(), dDao.getDataframe(df.getId(), false));
							dataframes.add(df);
						}
					}
					
					json = "[]";
				}
			}

			if (json != null) {
				GsonBuilder gb = new GsonBuilder();
				gb.registerTypeHierarchyAdapter(EquipObject.class,
						new SearchServiceClient.SearchServiceResultAdapter());
				Gson gson = gb.create();

				EquipObject[] equipObjects = gson.fromJson(json, EquipObject[].class);
				List<String> dataframeIds = new ArrayList<>();
				for (EquipObject eo : equipObjects) {
					if (eo instanceof Analysis) {
						Analysis an = (Analysis) eo;
						if (this.includeAssembly(an)) {
							this.ASSEMBLY_TABLE.put(an.getId(), an);
						}
					} else if (eo instanceof Batch) {
						Batch b = (Batch) eo;
						if(b.getEquipId() != null) {
							if (this.includeAssembly(b)) {
								this.ASSEMBLY_TABLE.put(b.getId(), b);
							}
						}
					} else if (eo instanceof Assembly) {
						Assembly a = (Assembly) eo;
						if (this.includeAssembly(a)) {
							this.ASSEMBLY_TABLE.put(a.getId(), a);
						}
					} else if (eo instanceof Dataframe) {
						Dataframe df = (Dataframe) eo;
						if (this.includeDataframe(df)) {
							this.DATAFRAME_TABLE.put(df.getId(), df);
							dataframeIds.add(df.getId());
							dataframes.add(df);
						}
					}
				}

				// We fetch Reporting Event info for each dataframe.
				// This information is placed in a virtual metadata item, which is why it is not
				// returned in the search service call.
				ReportingAndPublishingDAO rdao = new ReportingAndPublishingDAO();
				Map<String, List<ReportingEventItem>> map = rdao.getReportingEventItemsByDataframeId(dataframeIds);
				String mkey = "Reporting Events";
				for (Entry<String, Dataframe> entry : this.DATAFRAME_TABLE.entrySet()) {
					Dataframe df = entry.getValue();
					List<ReportingEventItem> items = map.get(df.getId());
					if (items != null) {
						List<String> handledIds = new ArrayList<>();
						for (ReportingEventItem item : items) {
							if (!item.isDeleteFlag()) {
								String aId = item.getReportingEventId();
								if (!handledIds.contains(aId)) {
									Assembly re = this.ASSEMBLY_TABLE.get(aId);
									if (re != null && !re.isDeleteFlag()) {
										Metadatum meta = df.getMetadatum(mkey);
										if (meta == null) {
											meta = new Metadatum();
											meta.setKey(mkey);
											df.getMetadata().add(meta);
										}

										String val = re.getEquipId();
										if (re.getName() != null) {
											val += " " + re.getName();
										}

										meta.getValue().add(val);
									}

									handledIds.add(aId);
								}
							}
						}
					}
				}
			}

			// Check access to each dataframe
			this.populateAccessTable();
		}
		catch(HaltException he) {
			throw he;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("An error ocurred when fetching assemblies and dataframes for study ID '" + studyId + "'.", e);
		}
	}

	private boolean includeAssembly(Assembly assembly) {
		if (assembly != null && (this.includeDeleted || !assembly.isDeleteFlag())) {
			return true;
		}

		return false;
	}

	private boolean includeDataframe(Dataframe df) {
		if (df != null && (this.includeDeleted || !df.isDeleteFlag())
				&& (df.getSubType() == null || !df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE))) {
			return true;
		}

		return false;
	}

	/**
	 * Populates the {@code DATAFRAME_TABLE} and {@code ASSEMBLY_TABLE} "backwards",
	 * starting with the provided node and finding all parents.
	 * 
	 * @param nodeId
	 * @return {@link EquipObject} the object related to the provided node ID
	 */
	private EquipObject bloomTables(String nodeId) {
		EquipObject eo = null;
		if (nodeId != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			eo = msDao.getEquipObject(nodeId);

			List<String> parentIds = new ArrayList<>();
			if (eo instanceof Analysis) {
				Analysis a = (Analysis) eo;
				Assembly existing = this.ASSEMBLY_TABLE.get(a.getId());
				if (existing == null) {
					this.ASSEMBLY_TABLE.put(a.getId(), a);
				}

				parentIds = a.getParentIds();
			} else if (eo instanceof Assembly) {
				Assembly a = (Assembly) eo;
				Assembly existing = this.ASSEMBLY_TABLE.get(a.getId());
				if (existing == null) {
					this.ASSEMBLY_TABLE.put(a.getId(), a);
				}

				parentIds = a.getParentIds();
			} else if (eo instanceof Dataframe) {
				Dataframe df = (Dataframe) eo;
				Dataframe existing = this.DATAFRAME_TABLE.get(df.getId());
				if (existing == null) {
					this.DATAFRAME_TABLE.put(df.getId(), df);
				}

				parentIds.addAll(df.getAssemblyIds());
				parentIds.addAll(df.getDataframeIds());
			}

			for (String parentId : parentIds) {
				this.bloomTables(parentId);
			}
		}

		return eo;
	}

	private EquipObject rootTables(String nodeId) {
		EquipObject eo = null;
		if (nodeId != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			eo = msDao.getEquipObject(nodeId);

			this.rootTables(eo);
		}

		return eo;
	}

	private void rootTables(EquipObject eo) {
		if (eo != null) {
			AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
			List<Assembly> childAssemblies = aDao.getAssemblyByParentId(eo.getId());
			for (Assembly a : childAssemblies) {
				this.rootTables(a);
			}

			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			List<Dataframe> childDataframes = new ArrayList<>();
			if (eo instanceof Dataframe) {
				this.DATAFRAME_TABLE.put(eo.getId(), (Dataframe) eo);
				childDataframes = dDao.getDataframeByParentDataframeId(eo.getId());
			} else if (eo instanceof Assembly) {
				this.ASSEMBLY_TABLE.put(eo.getId(), (Assembly) eo);
				childDataframes = dDao.getDataframeByAssemblyId(eo.getId());
			}

			for (Dataframe d : childDataframes) {
				this.rootTables(d);
			}
		}
	}

	private void populateFullBreadcrumbs(LineageItem item, List<LineageItem> parents) {
		String fullBreadcrumb = null;
		String fullLegacyBreadcrumb = null;
		if (!parents.isEmpty()) {
			fullBreadcrumb = "";
			boolean includeTrim = parents.size() > 1;

			for (LineageItem parent : parents) {
				if (!fullBreadcrumb.isEmpty()) {
					fullBreadcrumb += ",";
				}

				String pfb = parent.getFullBreadcrumb();
				String pflb = parent.getFullLegacyBreadcrumb();
				if (includeTrim) {
					String pre = "(";
					String suf = ")";
					if (pfb.contains("(")) {
						pre = "[";
						suf = "]";
					}

					pfb = pre + pfb + suf;
					if (pflb != null) {
						pflb = pre + pflb + suf;
					}
				}

				fullBreadcrumb += pfb;
				if (pflb != null) {
					if (fullLegacyBreadcrumb == null) {
						fullLegacyBreadcrumb = "";
					}

					fullLegacyBreadcrumb += pflb;
				}
			}
		}

		if (item.getEquipId() != null) {
			if (fullBreadcrumb == null) {
				fullBreadcrumb = "";
			}

			if (!fullBreadcrumb.isEmpty()) {
				fullBreadcrumb += Props.getLineageBreadcrumbSeparator();
			}

			fullBreadcrumb += item.getEquipId();

		}
		if (item.getName() != null && !item.getName().trim().isEmpty()) {
			if (fullLegacyBreadcrumb == null) {
				fullLegacyBreadcrumb = "";
			}

			if (!fullLegacyBreadcrumb.isEmpty()) {
				fullLegacyBreadcrumb += Props.getLineageBreadcrumbSeparator();
			}

			fullLegacyBreadcrumb += item.getName().trim();
		}

		item.setFullBreadcrumb(fullBreadcrumb);
		item.setFullLegacyBreadcrumb(fullLegacyBreadcrumb);
	}

	/**
	 * Returns {@code true} if the lineage should continue with the provided
	 * {@link Dataframe} object, {@code false} otherwise.
	 * 
	 * @param dataframe
	 * @return {@code boolean}
	 */
	private boolean stopWithDataframe(DataframeLineageItem dataframe) {
		boolean stop = true;
		if (this.mode == LineageBuilder.PROMOTION_MODE) {
			// If we're in PROMOTION_MODE, we want to stop once we hit a dataframe that has
			// a promotion status of Promoted, Revoked, or Fail.
			String promotionStatus = dataframe.getPromotionStatus();
			stop = promotionStatus != null && (promotionStatus.equalsIgnoreCase("Promoted")
					|| promotionStatus.equalsIgnoreCase("Revoked") || promotionStatus.equalsIgnoreCase("Fail"));
		} else if (this.mode == LineageBuilder.ANALYSIS_PREP_MODE) {
			stop = false;
		}

		return stop;
	}

	/**
	 * Returns a {@link List} of {@link Assembly} objects that have the provided
	 * dataframe ID as a member dataframe.
	 * 
	 * @param dataframeId
	 * @return {@link List}<{@link Assembly}>
	 */
	private List<Assembly> getAssembliesByMemberDataframeId(String dataframeId) {
		List<Assembly> list = new ArrayList<>();
		for (Entry<String, Assembly> set : this.ASSEMBLY_TABLE.entrySet()) {
			Assembly a = set.getValue();
			if (a.getDataframeIds().contains(dataframeId)) {
				list.add(a);
			}
		}

		return list;
	}

	/**
	 * Returns {@code true} if the provided script ID relates to an excluded
	 * breadcrumb script, {@code false} otherwise.
	 * 
	 * @param id
	 * @return {@code boolean}
	 */
	private boolean isExcludedBreadcrumbScript(String id) {
		for (LibraryResponse lr : this.excludedBreadcrumbScripts) {
			if (lr.getArtifactId() != null && lr.getArtifactId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a subset of the provided {@code T} objects whose EQUIP ID matches the
	 * one provided.
	 * 
	 * @param items
	 * @param equipId
	 * @return {@link List}<{@code T}>
	 */
	private <T extends EquipID> List<T> filterItemsByEquipId(List<T> items, String equipId) {
		List<T> list = new ArrayList<>();
		for (T item : items) {
			if (item == null || item.getEquipId() == null) {
				String s = "";
			}
			
			if (item.getEquipId().equals(equipId)) {
				list.add(item);
			}
		}

		return list;
	}

	/**
	 * Returns all {@link Assembly} objects from the {@code ASSEMBLY_TABLE} whose
	 * type matches the one provided.
	 * 
	 * @param type
	 * @return {@link List}<{@link Assembly}>
	 */
	private List<Assembly> getDataLoadsByStudyId(String studyId) {
		List<Assembly> list = new ArrayList<>();
		for (Entry<String, Assembly> entry : this.ASSEMBLY_TABLE.entrySet()) {
			Assembly a = entry.getValue();
			if (a.getStudyIds().contains(studyId) && a.getAssemblyType().equals(Assembly.DATA_LOAD_TYPE)) {
				list.add(a);
			}
		}

		return list;
	}

	/**
	 * Returns all {@link Assembly} objects from the {@code ASSEMBLY_TABLE} whose
	 * parent IDs contains the one provided.
	 * 
	 * @param parentId
	 * @return {@link List}<{@link Assembly}>
	 */
	private List<Assembly> getChildAssemblies(String parentId) {
		List<Assembly> list = new ArrayList<>();
		for (Entry<String, Assembly> entry : this.ASSEMBLY_TABLE.entrySet()) {
			Assembly a = entry.getValue();
			if (a.getParentIds().contains(parentId)) {
				list.add(a);
			}
		}

		return list;
	}

	/**
	 * Returns all {@link Dataframe} objects from the {@code DATAFRAME_TABLE} whose
	 * assembly IDs or dataframe IDs contain the one provided.
	 * 
	 * @param parentId
	 * @return {@link List}<{@link Dataframe}>
	 */
	private List<Dataframe> getChildDataframes(String parentId) {
		List<Dataframe> list = new ArrayList<>();
		for (Entry<String, Dataframe> entry : this.DATAFRAME_TABLE.entrySet()) {
			Dataframe df = entry.getValue();
			if (df.getAssemblyIds().contains(parentId) || df.getDataframeIds().contains(parentId)) {
				list.add(df);
			}
		}

		return list;
	}
	
	private List<Dataframe> getDataframes(List<String> ids) {
		List<Dataframe> list = new ArrayList<>();
		for (Entry<String, Dataframe> entry : this.DATAFRAME_TABLE.entrySet()) {
			Dataframe df = entry.getValue();
			
			String foundId = null;
			for(String id : ids) {
				if(df.getId().equals(id)) {
					list.add(df);
					foundId = id;
					break;
				}
			}
			
			if(foundId != null) {
				ids.remove(foundId);
			}
		}

		return list;
	}

	private List<LineageItem> getItems(List<String> ids) {
		List<LineageItem> items = new ArrayList<>();
		for (String id : ids) {
			LineageItem item = this.ALL_LINEAGE_ITEMS.get(id);
			if (item != null) {
				items.add(item);
			}
		}

		return items;
	}

	public Map<String, Assembly> getAssemblyTable() {
		return this.ASSEMBLY_TABLE;
	}

	public Map<String, Dataframe> getDataframeTable() {
		return this.DATAFRAME_TABLE;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isIncludeDeleted() {
		return includeDeleted;
	}

	public void setIncludeDeleted(boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
	}

	public boolean isLatestOnly() {
		return latestOnly;
	}

	public void setLatestOnly(boolean latestOnly) {
		this.latestOnly = latestOnly;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public List<LibraryResponse> getExcludedBreadcrumbScripts() {
		return excludedBreadcrumbScripts;
	}

	public void setExcludedBreadcrumbScripts(List<LibraryResponse> excludedBreadcrumbScripts) {
		this.excludedBreadcrumbScripts = excludedBreadcrumbScripts;
	}
}

/**
 * Compares {@link EquipObject} objects' {@code created} date property.
 * 
 * @author QUINTJ16
 *
 */
class EquipObjectComparator implements Comparator<EquipObject> {

	@Override
	public int compare(EquipObject a, EquipObject b) {
		Date ad = new Date(0);
		Date bd = ad;
		
		if (a instanceof EquipCreatable) {
			ad = ((EquipCreatable) a).getCreated();
		}
		if (b instanceof EquipCreatable) {
			bd = ((EquipCreatable) b).getCreated();
		}

		if (ad.getTime() > bd.getTime()) {
			return 1;
		} else if (bd.getTime() > ad.getTime()) {
			return -1;
		}

		return 0;
	}

}