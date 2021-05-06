package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;

import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;

import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;

public class VersioningDAO extends ModeShapeDAO {
	
	public enum ActionType { COMMIT_ACTION, DELETE_ACTION, NO_ACTION }
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VersioningDAO.class);

	private List<Dataframe> siblingList;

	private List<Dataframe> downstreamDataframes;
	private List<Assembly> downstreamAssemblies;
	String[] equipIds = new String[0];
	private DataframeDAO ddao;
	private AssemblyDAO adao;
	
	private String lastBlockingId;

	public VersioningDAO() {
		this(null, null);
	}

	public VersioningDAO(Dataframe dataframe) {
		this(null, dataframe.getEquipId());
	}
	
	public VersioningDAO(String equipId) {
		this(null, equipId);
	}

	public VersioningDAO(Assembly assembly) {
		this(null, assembly.getEquipId());
	}

	public VersioningDAO(EquipVersion object, String equipId) {
		super();
		
		ddao = getDataframeDAO();
		adao = getAssemblyDAO();
		
		if(equipId != null) {
			equipIds = new String[] { equipId };
		}
	}
	
	public String getLastBlockingId() {
		return this.lastBlockingId;
	}

	private List<Dataframe> getSiblingList() {
		// need to use edao to use equip:searchable to quickly search
		// then convert to dataframes
		EquipIDDAO edao = getEquipIDDAO();
		List<Dataframe> returnList = new ArrayList<>();
		List<EquipObject> equipObjects = edao.getItem(equipIds);
		for (EquipObject eo : equipObjects) {
			Dataframe df = ddao.getDataframe(eo.getId());
			returnList.add(df);
		}

		return returnList;
	}

	private List<String> getSiblingIds() {
		// need to use edao to use equip:searchable to quickly search
		EquipIDDAO edao = getEquipIDDAO();
		List<String> returnList = new ArrayList<String>();
		if (equipIdsExist(equipIds)) {
			List<EquipObject> equipObjects = edao.getItem(equipIds);
			for (EquipObject eo : equipObjects) {
				String id = eo.getId();
				returnList.add(id);
			}
		}

		return returnList;
	}

	private boolean equipIdsExist(String[] equipIds) {
		boolean retVal = false;

		for (String equipId : equipIds) {
			if (!equipId.isEmpty()) {
				retVal = true;
			}
		}

		return retVal;
	}
	
	public static final <T extends EquipObject> T getLatestVersion(List<T> items) {
		return VersioningDAO.getLatestVersion(items, null, false);
	}

	public static final <T extends EquipObject> T getLatestVersion(List<T> items, String userId) {
		return VersioningDAO.getLatestVersion(items, userId, false);
	}
	
	public static final <T extends EquipObject> T getLatestVersion(List<T> items, boolean includeDeleted) {
		return VersioningDAO.getLatestVersion(items, null, includeDeleted);
	}

	public static final <T extends EquipObject> T getLatestVersion(List<T> items, String userId, boolean includeDeleted) {
		T latestItem = null;
		List<T> subSorted = new ArrayList<>();
		for (T item : items) {
			boolean isDeleted = false;
			if (item instanceof EquipVersionable) {
				isDeleted = ((EquipVersionable) item).isDeleteFlag();
			}

			if (includeDeleted || !isDeleted) {
				String eid = ((EquipID) item).getEquipId();
				if(eid != null) {
					subSorted.add(item);
				}
			}
		}
		
		subSorted.sort(new Comparator<EquipObject>() {

			@Override
			public int compare(EquipObject a, EquipObject b) {
				Date ad = this.getDate(a);
				Date bd = this.getDate(b);
				
				// We want descending order
				if(ad.getTime() > bd.getTime()) {
					return -1;
				}
				else if(bd.getTime() > ad.getTime()) {
					return 1;
				}
				else {
					// If the created dates are the same, we use version as the tie breaker
					long av = this.getVersion(a);
					long bv = this.getVersion(b);
					
					if(av > bv) {
						return -1;
					}
					else if(bv > av) {
						return 1;
					}
				}
				
				return 0;
			}
			
			private Date getDate(EquipObject eo) {
				Date d = null;
				if(eo instanceof EquipCreatable) {
					d = ((EquipCreatable)eo).getCreated();
				}
				
				if(d == null) {
					d = new Date(0);
				}
				
				return d;
			}
			
			private long getVersion(EquipObject eo) {
				long v = 0;
				if(eo instanceof EquipVersionable) {
					v = ((EquipVersionable)eo).getVersionNumber();
				}
				
				return v;
			}
			
		});

		for (T i : subSorted) {
			boolean isCommitted = ((EquipVersionable) i).isCommitted();
			String createdBy = ((EquipCreatable) i).getCreatedBy();
			if (isCommitted || (userId != null && createdBy != null && createdBy.equals(userId))) {
				latestItem = i;
				break;
			}
		}
		
		return latestItem;
	}

	// if the highest node is committed, we will increment the version otherwise
	// return the highest node
	public Long getMaxVersion() {
		long maxVersion = 1;
		if (siblingList == null)
			siblingList = getSiblingList();
		for (Dataframe dataframe : siblingList) {
			if (dataframe.getVersionNumber() >= maxVersion) {
				maxVersion = dataframe.getVersionNumber();
				if (dataframe.isCommitted()) {
					maxVersion++;
				}
			}
		}
		return maxVersion;
	}

	public boolean checkEquipId() {

		if (siblingList == null)
			siblingList = getSiblingList();
		if (!siblingList.isEmpty() && checkSiblingNodes()) {
			return true;
		} else {
			return false; // there are no other nodes with this id

		}
	}

	private boolean checkSiblingNodes() {
		// determine whether the other nodes have been properly committed and superseded
		Boolean flag = true;
		if (siblingList == null)
			siblingList = getSiblingList();
		for (Dataframe dataframe : siblingList) {
			if (!dataframe.getVersionSuperSeded() && dataframe.isCommitted() && dataframe.isDeleteFlag()) {
				flag = false;
			}
		}
		// if we can get all the way through all dataframes without changing the flag,
		// then return true
		return flag;
	}

	public boolean deleteSiblings(long versionNumber, String myId) {
		List<String> siblingIds = getSiblingIds();
		boolean returnFlag = true;
		ModeShapeDAO bdao = new ModeShapeDAO();
		for (String id : siblingIds) {
			EquipObject eo = bdao.getEquipObject(id);
			EquipVersionable ev = (EquipVersionable) eo;
			if (ev.getVersionNumber() == versionNumber && !id.equals(myId)) {
				ev.setDeleteFlag(true);
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:deleteFlag", "true");
				
				bdao.updateNode(id, pp);
				String m = "\tVERSIONING: Deleted sibling " + ((EquipID) eo).getEquipId() + " v" + ev.getVersionNumber() + " (" + eo.getId() + ") // COMMITTED=" + ev.isCommitted();
				System.out.println(m);
				LOGGER.info(m);
			}
		}
		return returnFlag;
	}

	private List<Dataframe> getDownStreamDataframesByParentDataframe(String dataframeId) {
		List<String> ids = new ArrayList<>();
		ids.add(dataframeId);
		return this.getDownStreamDataframesByParentDataframe(ids);
	}

	private List<Dataframe> getDownStreamDataframesByParentDataframe(List<String> dataframeIds) {
		downstreamDataframes = ddao.getDataframeByParentDataframeId(dataframeIds);
		// downstreamDataframes = ddao.getDataframe(dataframeIds);
		return downstreamDataframes;
	}

	private List<Dataframe> getDownStreamDataframesByParentAssembly(String assemblyId) {
		List<String> ids = new ArrayList<>();
		ids.add(assemblyId);
		return this.getDownStreamDataframesByParentAssembly(ids);
	}

	private List<Dataframe> getDownStreamDataframesByParentAssembly(List<String> assemblyIds) {
		if( ddao == null) {
			ddao = getDataframeDAO();
		}
		downstreamDataframes = ddao.getDataframeByAssemblyId(assemblyIds);
		// downstreamDataframes = ddao.getDataframe(dataframeIds);
		return downstreamDataframes;
	}

	private List<Assembly> getDownStreamAssemblies(String parentId) {
		List<String> ids = new ArrayList<>();
		ids.add(parentId);
		return this.getDownStreamAssemblies(ids);
	}

	private List<Assembly> getDownStreamAssemblies(List<String> parentIds) {
		if( adao == null) {
			adao = getAssemblyDAO();
		}
		downstreamAssemblies = adao.getAssemblyByParentId(parentIds);
		// downstreamAssemblies = adao.getAssembly(assemblyIds);
		return downstreamAssemblies;
	}

	// recursive method to update all references and references of references etc
////Modified by Varun FF1 Performance improvements release
	public boolean supersedeDownstream(Dataframe dataframe, String username) {
		boolean returnFlag = true;
		if(dataframe != null) {
			final boolean[] ret = new boolean[1];
			ret[0] = true;
			Thread t3 = new Thread(new Runnable() {
				public void run() {
					if(!dataframe.getVersionSuperSeded()) {
						long ts = System.currentTimeMillis();
						dataframe.setVersionSuperSeded(true);
				
						PropertiesPayload payload = new PropertiesPayload();
						payload.addProperty("equip:versionSuperSeded", true);
						payload.addProperty("equip:lockedByUser", username);
				
						ModeShapeDAO bdao = new ModeShapeDAO();
						String r = bdao.updateNode(dataframe.getId(), payload);
						if (r == null || r.isEmpty()) {
							ret[0] = false;
						}
						LOGGER.info("updatenode "+(System.currentTimeMillis()-ts));
					}
				}
			});
			
			final List<Dataframe> downstreamDf = new ArrayList<Dataframe>();
			final List<Assembly> downstreamAssembly = new ArrayList<Assembly>();
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					final DataframeDAO dfDao = new DataframeDAOImpl(); 
					List<Dataframe> d = dfDao.getDataframeByParentDataframeId(dataframe.getId());
					downstreamDf.addAll(d);
				}
			});
			
			Thread t2 = new Thread(new Runnable() {
				public void run() {
					final AssemblyDAO asDao = new AssemblyDAOImpl(); 
					List<Assembly> d  = asDao.getAssemblyByMemberDataframeId(dataframe.getId());
					downstreamAssembly.addAll(d);
				}
			});
			t3.start();
			t1.start();
			t2.start();
			try {
				t1.join();
				t2.join();
				t3.join();
			}catch(InterruptedException ex) {
				LOGGER.error(ex.getMessage());
				returnFlag = false;
				//LOGGER.;
			}
			returnFlag = returnFlag && ret[0];
			//downstreamDataframes = getDownStreamDataframesByParentDataframe(dataframe.getId());
			for (Dataframe childDataframe : downstreamDf) {
				returnFlag = returnFlag && supersedeDownstream(childDataframe, username);
			}
			
			//downstreamAssemblies = getDownStreamAssemblies(dataframe.getId());
			for (Assembly childAssembly : downstreamAssembly) {
				returnFlag = returnFlag && supersedeDownstream(childAssembly, username);
			}
		}
		
		return returnFlag;
	}
	
	//Modified by Varun FF1 Performance improvements release
	public boolean supersedeDownstream(Assembly assembly, String username) {
		boolean returnFlag = true;
		if(assembly != null) {
			
			final boolean[] ret = new boolean[1];
			ret[0] =true;
			Thread t3 = new Thread(new Runnable() {
				public void run() {
					if(!assembly.getVersionSuperSeded()) {
						assembly.setVersionSuperSeded(true);
						long ts = System.currentTimeMillis();
						PropertiesPayload payload = new PropertiesPayload();
						payload.addProperty("equip:versionSuperSeded", true);
						payload.addProperty("equip:lockedByUser", username);
				
						ModeShapeDAO bdao = new ModeShapeDAO();
						String r = bdao.updateNode(assembly.getId(), payload);
						if (r == null || r.isEmpty()) {
							ret[0] = false;
						}
						LOGGER.info("updatenode "+(System.currentTimeMillis()-ts));
					}
				}
			});
			LOGGER.info(assembly.getId()+" assembly.getId() "+assembly.getAssemblyType()+" assembly equip id "+assembly.getEquipId());
			boolean isAnalysisType = assembly.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE);
			if(assembly instanceof Analysis || isAnalysisType) {
				Analysis an = null;
				if(isAnalysisType) {
					AnalysisDAO anDao = ModeShapeDAO.getAnalysisDAO();
					an = anDao.getAnalysis(assembly.getId());
				}
				else {
					an = (Analysis) assembly;
				}
				
				DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
				List<String> dfIds = new ArrayList<String>();
				if(an.getKelFlagsDataframeId() != null && !an.getKelFlagsDataframeId().isEmpty())
					dfIds.add(an.getKelFlagsDataframeId());
				if(an.getParametersDataframeId() != null && !an.getParametersDataframeId().isEmpty())
					dfIds.add(an.getParametersDataframeId());
				if(an.getModelConfigurationDataframeId() != null && !an.getModelConfigurationDataframeId().isEmpty())
					dfIds.add(an.getModelConfigurationDataframeId());
				if(an.getEstimatedConcDataframeId() != null && !an.getEstimatedConcDataframeId().isEmpty())
					dfIds.add(an.getEstimatedConcDataframeId());
				
				if(dfIds.size() > 0) {
					List<Dataframe> dataframes = ddao.getDataframe(dfIds);
					for(Dataframe df : dataframes) {
						if(df != null)
							this.supersedeDownstream(df, username);
					}
					
				}
				/*
				Dataframe kel = ddao.getDataframe(an.getKelFlagsDataframeId());
				if(kel != null) {
					this.supersedeDownstream(kel, username);
				}
				
				Dataframe params = ddao.getDataframe(an.getParametersDataframeId());
				if(params != null) {
					this.supersedeDownstream(params, username);
				}
				
				Dataframe mct = ddao.getDataframe(an.getModelConfigurationDataframeId());
				if(mct != null) {
					this.supersedeDownstream(mct, username);
				}
				
				Dataframe econ = ddao.getDataframe(an.getEstimatedConcDataframeId());
				if(econ != null) {
					this.supersedeDownstream(econ, username);
				}*/
			}
			final List<Dataframe> downstreamDf = new ArrayList<Dataframe>();
			final List<Assembly> downstreamAssembly = new ArrayList<Assembly>();
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					final DataframeDAO dfDao = new DataframeDAOImpl(); 
					List<Dataframe> d = dfDao.getDataframeByAssemblyId(assembly.getId());
					downstreamDf.addAll(d);
				}
			});
			
			Thread t2 = new Thread(new Runnable() {
				public void run() {
					final AssemblyDAO asDao = new AssemblyDAOImpl(); 
					List<Assembly> d  = asDao.getAssemblyByParentId(assembly.getId());
					downstreamAssembly.addAll(d);
				}
			});
			t3.start();
			t1.start();
			t2.start();
			
			try {
				t1.join();
				t2.join();
				t3.join();
			}catch(InterruptedException ex) {
				LOGGER.error(ex.getMessage());
				LOGGER.error("Exception in executing threads in Supersede VersioningDAO");
				returnFlag = false;
				//LOGGER.;
			}
			returnFlag = returnFlag && ret[0];
			//downstreamAssemblies = getDownStreamAssemblies(assembly.getId());
			//downstreamDataframes = getDownStreamDataframesByParentAssembly();
			for (Dataframe childDataframe : downstreamDf) {
				returnFlag = returnFlag && supersedeDownstream(childDataframe, username);
			}
			
			
			for (Assembly childAssembly : downstreamAssembly) {
				returnFlag = returnFlag && supersedeDownstream(childAssembly, username);
			}
		}
		
		return returnFlag;
	}

	// mark all downstream assemblies for deletion or commit
	
	public boolean updateDownstream(Assembly assembly, String username, ActionType actionType) {

		boolean returnFlag = true;
		
		PropertiesPayload payload = new PropertiesPayload();
		if( actionType == ActionType.DELETE_ACTION ) {
			assembly.setDeleteFlag(true);
			payload.addProperty("equip:deleteFlag", true);
		}
		else if(actionType == ActionType.COMMIT_ACTION) {
			assembly.setCommitted(true);
			payload.addProperty("equip:versionCommitted", true);
		}
		else {
			return false;
		}
		
		ModeShapeDAO bdao = new ModeShapeDAO();
		String r = bdao.updateNode(assembly.getId(), payload);
		if (r == null || r.isEmpty()) {
			returnFlag = false;
		}
		
		downstreamDataframes = getDownStreamDataframesByParentAssembly(assembly.getId());
		for (Dataframe childDataframe : downstreamDataframes) {
			returnFlag = updateDownstream(childDataframe, username, actionType);
		}
		
		downstreamAssemblies = getDownStreamAssemblies(assembly.getId());
		for (Assembly childAssembly : downstreamAssemblies) {
			returnFlag = updateDownstream(childAssembly, username, actionType);
		}

		return returnFlag;
	}

	// mark all downstream dataframes for deletion or committed
	
	public boolean updateDownstream(Dataframe dataframe, String username, ActionType actionType) {
		boolean returnFlag = true;

		PropertiesPayload payload = new PropertiesPayload();
		if( actionType == ActionType.DELETE_ACTION ) {
			dataframe.setDeleteFlag(true);
			payload.addProperty("equip:deleteFlag", true);
		}
		else if(actionType == ActionType.COMMIT_ACTION) {
			dataframe.setCommitted(true);
			payload.addProperty("equip:versionCommitted", true);
		}
		else {
			return false;
		}
		
		ModeShapeDAO bdao = new ModeShapeDAO();
		String r = bdao.updateNode(dataframe.getId(), payload);
		if (r == null || r.isEmpty()) {
			returnFlag = false;
		}
		downstreamDataframes = getDownStreamDataframesByParentAssembly(dataframe.getId());
		for (Dataframe childDataframe : downstreamDataframes) {
			returnFlag = updateDownstream(childDataframe, username, actionType);
		}
		downstreamAssemblies = getDownStreamAssemblies(dataframe.getId());
		for (Assembly childAssembly : downstreamAssemblies) {
			returnFlag = updateDownstream(childAssembly, username, actionType);
		}
		return returnFlag;
	}
	
	// method to check whether this is referenced by any other non-deleted node
	public boolean checkDownstream(List<String> dataframeIds, List<String> assemblyIds) {
		boolean returnFlag = true;
		downstreamDataframes = getDownStreamDataframesByParentDataframe(dataframeIds);
		for (Dataframe childDataframe : downstreamDataframes) {
			if (!childDataframe.isDeleteFlag()) {
				returnFlag = false;
			}
		}
		downstreamAssemblies = getDownStreamAssemblies(assemblyIds);
		for (Assembly childAssembly : downstreamAssemblies) {
			if (!childAssembly.isDeleteFlag()) {
				returnFlag = false;
			}
		}
		return returnFlag;
	}

	public boolean canDeleteAssembly(String assemblyId) {
		boolean canDelete = true;

		long bigTime = System.currentTimeMillis();
		long assemblyTime = 0;
		long dataframeTime = 0;
		long qcTime = 0;
		long eventTime = 0;

		AssemblyDAO _adao = ModeShapeDAO.getAssemblyDAO();
		assemblyTime = System.currentTimeMillis();
		List<Assembly> assemblies = _adao.getAssemblyByParentId(assemblyId);
		canDelete &= this.allDeleted(assemblies);
		assemblyTime = System.currentTimeMillis() - assemblyTime;
		
		Assembly a = _adao.getAssembly(assemblyId);
		DataframeDAO _ddao = ModeShapeDAO.getDataframeDAO();
		if (canDelete) {
			List<String> ignore = new ArrayList<>();
			dataframeTime = System.currentTimeMillis();
			List<Dataframe> dataframes = _ddao.getDataframeByAssemblyId(assemblyId);
			
			// If this is an analysis, ignore PPRM, MCT, KEL, and CONCE (they will be deleted later).
			if(a.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
				AnalysisDAO anDao = ModeShapeDAO.getAnalysisDAO();
				Analysis an = anDao.getAnalysis(assemblyId);
				ignore.add(an.getParametersDataframeId());
				ignore.add(an.getModelConfigurationDataframeId());
				ignore.add(an.getKelFlagsDataframeId());
				ignore.add(an.getEstimatedConcDataframeId());
			}
			else {
				// If this is a reporting event, ignore all dependent ATRs (they will be deleted later).
				if(a != null && a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
					for(Dataframe df : dataframes) {
						if(df.getSubType() != null && df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
							ignore.add(df.getId());
						}
					}
				}
			}
			
			canDelete &= this.allDeleted(dataframes, ignore);
			dataframeTime = System.currentTimeMillis() - dataframeTime;
		}

		// IGNORE QC FOR NOW
		/*
		 * if (canDelete) { QCRequestDAO qcdao = this.getQCRequestDAO(); qcTime =
		 * System.currentTimeMillis(); List<QCRequest> qcRequests =
		 * qcdao.getQCRequestByAssemblyId(assemblyId); canDelete &=
		 * qcRequests.isEmpty(); qcTime = System.currentTimeMillis() - qcTime; }
		 */

		if (canDelete) {
			List<String> ignore = new ArrayList<>();
			ReportingAndPublishingDAO rpdao = VersioningDAO.getReportingAndPublishingDAO();
			eventTime = System.currentTimeMillis();
			List<ReportingEventItem> items = rpdao.getReportingEventItemByAssemblyId(assemblyId);
			for(ReportingEventItem item : items) {
				if(!item.isDeleteFlag() && item.getDataFrameId() != null) {
					Dataframe df = _ddao.getDataframe(item.getDataFrameId());
					if(df != null && df.getSubType() != null &&  df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
						ignore.add(item.getId());
					}
				}
			}
			
			canDelete &= this.allDeleted(items, ignore);
			eventTime = System.currentTimeMillis() - eventTime;
		}

		bigTime = System.currentTimeMillis() - bigTime;
		LOGGER.info("Performed checks to delete assembly " + assemblyId + ". Total time: " + bigTime
				+ "ms; Check child assmblies: " + assemblyTime + "ms (" + this.percentage(bigTime, assemblyTime)
				+ "%); " + "Check child dataframes: " + dataframeTime + "ms (" + this.percentage(bigTime, dataframeTime)
				+ "%); Check QC requests: " + qcTime + "ms (" + this.percentage(bigTime, qcTime) + "%); "
				+ "Check reporting events: " + eventTime + "ms (" + this.percentage(bigTime, eventTime) + "%);");

		return canDelete;
	}
	
	public boolean canDeleteDataframe(String dataframeId) {
		DataframeDAO _ddao = VersioningDAO.getDataframeDAO();
		Dataframe df = _ddao.getDataframe(dataframeId);
		return this.canDeleteDataframe(df);
	}

	public boolean canDeleteDataframe(Dataframe df) {
		boolean canDelete = true;

		long bigTime = System.currentTimeMillis();
		long caTime = 0;
		long paTime = 0;
		long dataframeTime = 0;
		long qcTime = 0;
		long eventTime = 0;

		DataframeDAO _ddao = VersioningDAO.getDataframeDAO();
		dataframeTime = System.currentTimeMillis();
		List<Dataframe> dataframes = _ddao.getDataframeByParentDataframeId(df.getId());
		canDelete &= this.allDeleted(dataframes);
		dataframeTime = System.currentTimeMillis() - dataframeTime;

		if (canDelete) {
			AssemblyDAO _adao = VersioningDAO.getAssemblyDAO();
			caTime = System.currentTimeMillis();
			List<Assembly> childAssemblies = _adao.getAssemblyByParentId(df.getId());
			canDelete &= this.allDeleted(childAssemblies);
			caTime = System.currentTimeMillis() - caTime;

			if (canDelete) {
				paTime = System.currentTimeMillis();
				List<Assembly> parentAssemblies = _adao.getAssemblyByMemberDataframeId(df.getId());
				canDelete &= this.allDeleted(parentAssemblies);
				paTime = System.currentTimeMillis() - paTime;
			}
		}

		// IGNORE QC STUFF FOR NOW
		/*
		 * if (canDelete) { QCRequestDAO qcdao = this.getQCRequestDAO(); qcTime =
		 * System.currentTimeMillis(); List<QCRequest> qcRequests =
		 * qcdao.getQCRequestByDataframeId(dataframeId); canDelete &=
		 * qcRequests.isEmpty(); qcTime = System.currentTimeMillis() - qcTime; }
		 */

		if (canDelete) {
			ReportingAndPublishingDAO rpdao = VersioningDAO.getReportingAndPublishingDAO();
			eventTime = System.currentTimeMillis();
			
			if(df.getSubType() == null || !df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
				List<ReportingEventItem> items = rpdao.getReportingEventItemByDataframeId(df.getId());
				canDelete &= this.allDeleted(items);
			}
			eventTime = System.currentTimeMillis() - eventTime;
		}

		bigTime = System.currentTimeMillis() - bigTime;
		LOGGER.info("Performed checks to delete dataframe " + df.getId() + ". Total time: " + bigTime
				+ "ms; Check child assmblies: " + caTime + "ms (" + this.percentage(bigTime, caTime) + "%); "
				+ "Check member-of assemblies: " + paTime + "ms (" + this.percentage(bigTime, paTime) + "%) "
				+ "Check child dataframes: " + dataframeTime + "ms (" + this.percentage(bigTime, dataframeTime)
				+ "%); Check QC requests: " + qcTime + "ms (" + this.percentage(bigTime, qcTime) + "%); "
				+ "Check reporting events: " + eventTime + "ms (" + this.percentage(bigTime, eventTime) + "%);");

		return canDelete;
	}

	private double percentage(double total, double piece) {
		double p = (piece / total) * 100;
		return Math.round(p * 10.0) / 10.0;
	}
	
	private <T extends EquipVersionable> boolean allDeleted(List<T> items) {
		return this.allDeleted(items, null);
	}

	private <T extends EquipVersionable> boolean allDeleted(List<T> items, List<String> ignoreIds) {
		boolean allDeleted = true;
		for (T item : items) {
			boolean ignore = false;
			if(item instanceof EquipObject) {
				EquipObject eo = (EquipObject) item;
				if(ignoreIds != null) {
					ignore = ignoreIds.contains(eo.getId());
				}
			}
			
			if(!ignore) {
				if (!item.isDeleteFlag()) {
					allDeleted = false;
					
					if(item instanceof EquipID) {
						this.lastBlockingId = ((EquipID)item).getEquipId();
					}
					else {
						this.lastBlockingId = ((EquipObject)item).getId();
					}
					
					break;
				}
			}
		}

		return allDeleted;
	}
	
	
	public void applyVersionIncrementingLogic(EquipVersionable versionableObject, String type,
			EquipVersionableListGetter siblingGetter) {
		EquipID equipIDObject = (EquipID) versionableObject;
		
		if (equipIDObject.getEquipId() == null || equipIDObject.getEquipId().trim().equals("")) {
			equipIDObject.setEquipId(EquipIdCalculator.calculate(type));
			versionableObject.setVersionNumber(1);
		} else {
			List<EquipVersionable> siblings = siblingGetter.get(equipIDObject.getEquipId());
			EquipVersionable siblingWithMaxVersionNumber = this.getMaxVersion(siblings);
			
			// Requirement 1
			// If there is no node with this EQUIP ID, the request is invalid.
			if (siblingWithMaxVersionNumber == null) {
				throw new ErrorCodeException(400, "Invalid EQUIP ID provided");
			}
			
			String m = "\tVERSIONING: Fetched sibling with max version number: "  + ((EquipID) siblingWithMaxVersionNumber).getEquipId() + " v" + siblingWithMaxVersionNumber.getVersionNumber() + " (" + ((EquipObject)siblingWithMaxVersionNumber).getId() + ") // COMMITTED=" + siblingWithMaxVersionNumber.isCommitted();
			System.out.println(m);
			LOGGER.info(m);

			// Requirement 2
			// if a node exists with same equip id
			if (this.isCommittedButNotSupersededOrDeleted(siblingWithMaxVersionNumber)) {
				throw new ErrorCodeException(409,
						"For a valid committed node you must supersede it before creating a new version. Sibling " + ((EquipID)siblingWithMaxVersionNumber).getEquipId()
								+ " v" + siblingWithMaxVersionNumber.getVersionNumber()
								+ " is comitted but not superseded.");
			}
			
			// Requirement 3
			// If at least one node exists with same EQUIP ID
			if (this.noNodesAreCommittedButNotSupersededOrDeleted(siblings)) {
				if (siblingWithMaxVersionNumber.isCommitted()) {
					versionableObject.setVersionNumber(siblingWithMaxVersionNumber.getVersionNumber() + 1);
				} else {
					versionableObject.setVersionNumber(siblingWithMaxVersionNumber.getVersionNumber());
				}
				
				// If this is an analysis, also inherit the locked status of the sibling
				if(versionableObject instanceof Analysis) {
					Analysis e = (Analysis) siblingWithMaxVersionNumber;
					Analysis an = (Analysis) versionableObject;
					an.setLocked(e.isLocked());
					an.setLockedByUser(e.getLockedByUser());
					versionableObject = an;
				}
			}
		}
	}

	/**
	 * Returns the {@link EquipVersionable} object from the provided objects with the greatest version number.
	 * @param versionableObjects
	 * @return {@link EquipVersionable}
	 */
	public EquipVersionable getMaxVersion(List<EquipVersionable> versionableObjects) {
		EquipVersionable retVal = null;
		for (EquipVersionable v : versionableObjects) {
			if (retVal == null) {
				retVal = v;
			}
			else if(v.getVersionNumber() == retVal.getVersionNumber() && v.isCommitted() && !v.isDeleteFlag()) {
				// If this item is the same version number, but is committed, it takes precedence
				retVal = v;
			}
			else if (v.getVersionNumber() > retVal.getVersionNumber() && !v.isDeleteFlag()) {
				retVal = v;
			}
			else if(retVal.isDeleteFlag() && !v.isDeleteFlag()) {
				retVal = v;
			}
		}
		
		return retVal;
	}

	/**
	 * Returns {@code true} if <i>all</i> provided {@link EquipVersionable} objects are not deleted, not superseded, and committed; {@code false} otherwise.
	 * @param siblings
	 * @return {@link boolean}
	 */
	private boolean noNodesAreCommittedButNotSupersededOrDeleted(List<EquipVersionable> siblings) {
		boolean retVal = true;

		for (EquipVersionable sibling : siblings) {
			if (this.isCommittedButNotSupersededOrDeleted(sibling)) {
				retVal = false;
				break;
			}
		}

		return retVal;
	}
	
	/**
	 * Returns {@code true} if the provided {@link EquipVersionable} object is not deleted, not superseded, and committed; {@code false} otherwise.
	 * @param sibling
	 * @return {@code boolean}
	 */
	private boolean isCommittedButNotSupersededOrDeleted(EquipVersionable sibling) {
		return sibling.getVersionSuperSeded() == false && sibling.isDeleteFlag() == false
				&& sibling.isCommitted() == true;
	}

	public boolean isREReleased(String dataframeId) {
		// return true if there are released reporting events assoc with this df
		ReportingAndPublishingDAO rdao = new ReportingAndPublishingDAO();
		AssemblyDAO adao = getAssemblyDAO();
		String[] dfIds = new String[] { dataframeId };
		List<ReportingEventItem> list = rdao.getReportingEventItemByDataframeId(dfIds);

		for (ReportingEventItem rei : list) {
			if (!rei.isDeleteFlag() && rei.isIncluded()) {
				String reID = rei.getReportingEventId();
				Assembly a = adao.getAssembly(reID);
				if ((a != null)) {

					if (a.isReleased() && !a.isDeleteFlag()) {
						return true;
					}
				}
			}
		} // end for

		return false;
	}

	// check all REIs assoc with a df - true if any are published
	public boolean isREIPublished(String dataframeId) {
		ReportingAndPublishingDAO rdao = new ReportingAndPublishingDAO();
		String[] dfIds = new String[] { dataframeId };
		List<ReportingEventItem> list = rdao.getReportingEventItemByDataframeId(dfIds);
		for (ReportingEventItem rei : list) {
			// get the full rei with publish items via another call
			rei = rdao.getReportingItem(rei.id);
			if (!rei.isDeleteFlag() && (rei.getPublishItem() != null)) {
				if (rei.getPublishItem().getPublishStatus().equals("Published")
						&& !rei.getPublishItem().isDeleteFlag()) {
					return true;
				}
			}
		}
		return false;
	}

	// check all REIs assoc with an RE (assembly) - true if any are published
	public boolean isREPublished(String assemblyId) {
		AssemblyDAO adao = getAssemblyDAO();
		Assembly a = adao.getAssembly(assemblyId);
		ReportingAndPublishingDAO rdao = getReportingAndPublishingDAO();
		Boolean isPublished = false;
		for (String reiId : a.getReportingItemIds()) {
			ReportingEventItem rei2 = rdao.getReportingItem(reiId);
			if (rei2.isIncluded() && !rei2.isDeleteFlag()
					&& rei2.getPublishItem().getPublishStatus().equals("Published")) {
				return true;
			}
		}

		return isPublished;
	}

	public void updateDFReleaseStatus(Assembly a) {
		ReportingAndPublishingDAO rdao = new ReportingAndPublishingDAO();
		DataframeDAO ddao = getDataframeDAO();
		// we are deleting or undeleting the released re
		// get all dfs from all children to see if there are other released res related
		for (String reId : a.getReportingItemIds()) {
			ReportingEventItem rei = rdao.getReportingItem(reId);

			if (!rei.isDeleteFlag() && rei.isIncluded()) {
				Dataframe df = ddao.getDataframe(rei.getDataFrameId());
				Boolean released = isREReleased(rei.getDataFrameId());
				if (released != df.isReleased()) {
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty("equip:released", released);
					ModeShapeDAO bdao = new ModeShapeDAO();
					bdao.updateNode(rei.getDataFrameId(), pp);
				}
			}
		}
	}

	public boolean checkDownstreamReporting(String nodeId) {
		// This will determine if there are any reporting event items or publish items
		// associated with the node
		ModeShapeNode node = this.getNode(nodeId);
		ReportingAndPublishingDAO rdao = getReportingAndPublishingDAO();
		List<ReportingEventItem> reiList = new ArrayList<>();
		if (node.getPrimaryType().equalsIgnoreCase("equip:dataframe")) {
			// Check if there is at least one reporting event that has a reporting event
			// item which references this id in its equip:dataframeId property and reporting
			// event item's equip:included is equal to true
			reiList = rdao.getReportingEventItemByDataframeId(nodeId);
		} else if (node.getPrimaryType().equalsIgnoreCase("equip:assembly")) {
			// Check if there is at least one reporting event that has a reporting event
			// item which references this id in its equip:assemblyId property and reporting
			// event item's equip:included is equal to true
			reiList = rdao.getReportingEventItemByAssemblyId(nodeId);
		}

		for (ReportingEventItem rei : reiList) {
			if (rei.isIncluded())
				return false;
		}
		return true;
	}

	public void deleteAssociatedDataframes(Assembly a) {
		ModeShapeDAO bdao = new ModeShapeDAO();
		// deletes dataframes when teh Data Load assembly is deleted

		List<String> dfURLs = a.getDataframeIds();

		for (String dfURL : dfURLs) {
			PropertiesPayload pp = new PropertiesPayload();
			pp.addProperty("equip:deleteFlag", true);
			bdao.updateNode(bdao.fetchId(dfURL), pp);
		}

	}
	
	public void unSupersedePrevious(Dataframe df) {
		// df must be the only node in its version
		List<Dataframe> siblingList = this.getSiblingList();
			Long prevVersion = df.getVersionNumber() -1;  
		for (Dataframe sibDf : siblingList) {
			if (sibDf.getVersionNumber() == prevVersion) { //change on 7/3 to account for multiple committed versions of reports
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:versionSuperSeded", false);
				ModeShapeDAO bdao = new ModeShapeDAO();
				bdao.updateNode(sibDf.getId(), pp);
			}
		}
	}


}
