package com.pfizer.pgrd.equip.dataframeservice.copyutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistSummaryItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.CopyException;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeRootResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;

public class CopyUtils {
	private CopyUtils() { }
	
	/**
	 * Returns an ordered {@link List} of {@link LineageItem} objects that are the children of the provided parent {@link LineageItem} objects. The 
	 * children will be ordered so that all children that do not have any of the other children as parents will be at the front of the list. The rest 
	 * will be at the end of the list.
	 * @param parents
	 * @return
	 */
	public static List<LineageItem> prioritizeChildren(List<LineageItem> parents) {
		List<LineageItem> ordered = new ArrayList<>();
		if(parents != null) {
			Map<String, LineageItem> level = new HashMap<>();
			Map<String, Integer> counts = new HashMap<>();
			List<LineageItem> allChildren = new ArrayList<>();
			for(LineageItem parent : parents) {
				for(AssemblyLineageItem ali : parent.getChildAssemblies()) {
					if(level.get(ali.getId()) == null) {
						level.put(ali.getId(), ali);
						counts.put(ali.getId(), 0);
						allChildren.add(ali);
					}
				}
				for(DataframeLineageItem dli : parent.getChildDataframes()) {
					if(level.get(dli.getId()) == null && (dli.getBatchId() == null || dli.getBatchId().isEmpty())) {
						level.put(dli.getId(), dli);
						counts.put(dli.getId(), 0);
						allChildren.add(dli);
					}
				}
			}
			
			for(LineageItem child : allChildren) {
				for(String pid : child.getParentIds()) {
					if(level.get(pid) != null) {
						int count = counts.get(child.getId()) + 1;
						counts.put(child.getId(), count);
					}
				}
				
				ordered.add(child);
			}
			
			// Sort in ascending order
			ordered.sort(new Comparator<LineageItem>() {

				@Override
				public int compare(LineageItem a, LineageItem b) {
					int acount = counts.get(a.getId());
					int bcount = counts.get(b.getId());
					
					return acount - bcount;
				}
				
			});
		}
		
		return ordered;
	}
	
	/**
	 * Returns an ordered {@link List} of {@link LineageItem} objects that are the children of the provided parent {@link LineageItem} object. The 
	 * children will be ordered so that all children that do not have any of the other children as parents will be at the front of the list. The rest 
	 * will be at the end of the list.
	 * @param parent
	 * @return {@link List}<{@link LineageItem}>
	 */
	public static List<LineageItem> prioritizeChildren(LineageItem parent) {
		List<LineageItem> ordered = new ArrayList<>();
		if(parent != null) {
			ordered = CopyUtils.prioritizeChildren(Arrays.asList(parent));
		}
		
		return ordered;
	}
	
	/**
	 * Returns the provided {@link Dataframe} and {@link Assembly} objects in an ordered {@link List} so that objects that do not have any of the other provided objects as parents 
	 * are at the front of the list. The rest are at the end of the list.
	 * @param dfChildren
	 * @param assemblyChildren
	 * @return {@link List}<{@link EquipObject}>
	 */
	public static List<EquipObject> prioritizeChildren(List<Dataframe> dfChildren, List<Assembly> assemblyChildren) {
		List<EquipObject> all = new ArrayList<>();
		if(dfChildren != null) {
			all.addAll(dfChildren);
		}
		if(assemblyChildren != null) {
			all.addAll(assemblyChildren);
		}
		
		return CopyUtils.prioritzeChildren(all);
	}
	
	/**
	 * Returns the provided {@link EquipObject} objects in an ordered {@link List} so that objects that do not have any of the other provided objects as parents 
	 * are at the front of the list. The rest are at the end of the list.
	 * @param children
	 * @return {@link List}<{@link EquipObject}>
	 */
	public static List<EquipObject> prioritzeChildren(List<EquipObject> children) {
		List<EquipObject> ordered = new ArrayList<>();
		if(children != null) {
			Map<String, EquipObject> level = new HashMap<>();
			Map<String, Integer> counts = new HashMap<>();
			for(EquipObject eo : children) {
				level.put(eo.getId(), eo);
				counts.put(eo.getId(), 0);
			}
			
			for(EquipObject eo : children) {
				if(eo instanceof Dataframe) {
					Dataframe df = (Dataframe) eo;
					for(String pid : df.getParentIds()) {
						if(level.get(pid) != null) {
							int count = counts.get(df.getId()) + 1;
							counts.put(df.getId(), count);
						}
					}
				}
				else if(eo instanceof Assembly) {
					Assembly a = (Assembly) eo;
					for(String pId : a.getParentIds()) {
						if(level.get(pId) != null) {
							int count = counts.get(a.getId()) + 1;
							counts.put(a.getId(), count);
						}
					}
				}
				
				ordered.add(eo);
			}
			
			ordered.sort(new Comparator<EquipObject>() {

				@Override
				public int compare(EquipObject a, EquipObject b) {
					int acount = counts.get(a.getId());
					int bcount = counts.get(b.getId());
					
					return acount - bcount;
				}
				
			});
		}
		
		return ordered;
	}

	public static EquipObject copyNode(String nodeId, String userId) throws CopyException, ServiceCallerException {
		return CopyUtils.copyNode(null, nodeId, userId);
	}

	public static EquipObject copyNode(String copier, String nodeId, String userId) throws CopyException, ServiceCallerException {
		EquipObject copy = null;
		if (nodeId != null) {
			ModeShapeDAO dao = new ModeShapeDAO();
			EquipObject node = dao.getEquipObject(nodeId);
			copy = CopyUtils.copyNode(copier, node, userId);
		}

		return copy;
	}

	public static EquipObject copyNode(EquipObject eo, String userId) throws CopyException, ServiceCallerException {
		return CopyUtils.copyNode(null, eo, userId);
	}

	public static EquipObject copyNode(String copier, EquipObject eo, String userId) throws CopyException, ServiceCallerException {
		AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
		EquipObject copy = null;
		if (eo != null) {
			if (eo instanceof Analysis) {
				copy = CopyUtils.copyAnalysis(copier, (Analysis) eo);
				if( Props.isAudit()) {
					if(userId != null) {
						if( eo instanceof EquipID && eo instanceof EquipVersionable){
							EquipID equipIdObject = (EquipID)copy;
							EquipVersionable equipVersionableObject = (EquipVersionable)eo;
							/*asc.logAuditEntry(	"Copy of node with id = " + eo.getId(),
												equipIdObject.getEquipId(),
												"",
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												equipVersionableObject.getVersionNumber() );
							*/
							AuditDetails details = asc.new AuditDetails("Copy of node with id = ", (EquipObject)copy, userId);
							details.setContextEntity(eo);
							asc.logAuditEntryAsync(details);
						
						}
					}
				}
			} else if (eo instanceof Assembly) {
				copy = CopyUtils.copyAssembly(copier, (Assembly) eo);
				if( Props.isAudit()){
					if(userId != null){
						if( eo instanceof EquipID && eo instanceof EquipVersionable){
							EquipID equipIdObject = (EquipID)copy;
							EquipVersionable equipVersionableObject = (EquipVersionable)eo;
							/*asc.logAuditEntry(	"Copy of node with id = " + eo.getId(),
												equipIdObject.getEquipId(),
												"",
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												equipVersionableObject.getVersionNumber() );
							*/
							AuditDetails details = asc.new AuditDetails("Copy of node with id = ", (EquipObject)copy, userId);
							details.setContextEntity(eo);
							asc.logAuditEntryAsync(details);
						}
					}
				}
			} else if (eo instanceof Dataframe) {
				copy = CopyUtils.copyDataframe(copier, (Dataframe) eo);
				if( Props.isAudit()){
					if(userId != null){
						if( eo instanceof EquipID && eo instanceof EquipVersionable){
							EquipID equipIdObject = (EquipID)copy;
							EquipVersionable equipVersionableObject = (EquipVersionable)eo;
							/*asc.logAuditEntry(	"Copy of node with id = " + eo.getId(),
												equipIdObject.getEquipId(),
												"",
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												equipVersionableObject.getVersionNumber() );*/
							AuditDetails details = asc.new AuditDetails("Copy of node with id = ", (EquipObject)copy, userId);
							details.setContextEntity(eo);
							asc.logAuditEntryAsync(details);
						}
					}
				}
			} else {
				throw new CopyException("Only analyses, non-data load assemblies, and dataframes may be copied.");
			}
		}

		return copy;
	}

	public static Analysis copyAnalysis(Analysis analysis) throws CopyException {
		return CopyUtils.copyAnalysis(null, analysis, null, true);
	}
	
	public static Analysis copyAnalysis(Analysis analysis, String newConcId) throws CopyException {
		return CopyUtils.copyAnalysis(null, analysis, newConcId, true);
	}

	public static Analysis copyAnalysis(String copier, Analysis analysis) throws CopyException {
		return (Analysis) CopyUtils.copyAssembly(copier, analysis);
	}
	
	public static Analysis copyAnalysis(String copier, Analysis analysis, String newConcId, boolean copyAnalysisParts) throws CopyException {
		return (Analysis) CopyUtils.copyAssembly(copier, analysis, false, newConcId, copyAnalysisParts);
	}
	
	public static void copyMCT(String copier, Analysis analysis, Analysis fromAnalysis) throws CopyException, ServiceCallerException {
		CopyUtils.copyMCT(copier, analysis, fromAnalysis, false);
	}

	public static void copyMCT(String copier, Analysis analysis, Analysis fromAnalysis, boolean asNewVersion) throws CopyException, ServiceCallerException {
		CopyUtils.copyMCTKEL(copier, analysis, fromAnalysis, true, false, asNewVersion);
	}
	
	public static void copyKEL(String copier, Analysis analysis, Analysis fromAnalysis) throws CopyException, ServiceCallerException {
		CopyUtils.copyKEL(copier, analysis, fromAnalysis, false);
	}

	public static void copyKEL(String copier, Analysis analysis, Analysis fromAnalysis, boolean asNewVersion) throws CopyException, ServiceCallerException {
		CopyUtils.copyMCTKEL(copier, analysis, fromAnalysis, false, true, asNewVersion);
	}
	
	public static void copyMCTKEL(String copier, Analysis analysis, Analysis fromAnalysis) throws CopyException, ServiceCallerException {
		CopyUtils.copyMCTKEL(copier, analysis, fromAnalysis, false);
	}

	public static void copyMCTKEL(String copier, Analysis analysis, Analysis fromAnalysis, boolean asNewVersion) throws CopyException, ServiceCallerException {
		CopyUtils.copyMCTKEL(copier, analysis, fromAnalysis, true, true, asNewVersion);
	}

	private static void copyMCTKEL(String copier, Analysis analysis, Analysis fromAnalysis, boolean copyMct,
			boolean copyKel, boolean asNewVersion) throws CopyException, ServiceCallerException {
		if (copier != null && analysis != null && fromAnalysis != null) {
			DataframeDAO dfdao = ModeShapeDAO.getDataframeDAO();

			// Copy MCT
			if (copyMct && analysis.getModelConfigurationDataframeId() == null
					&& fromAnalysis.getModelConfigurationDataframeId() != null) {
				Dataframe mct = dfdao.getDataframe(fromAnalysis.getModelConfigurationDataframeId());
				if (mct != null) {
					mct = CopyUtils.copyDataframe(mct, asNewVersion);
					analysis.setModelConfigurationDataframeId(null);
					if (mct != null) {
						analysis.setModelConfigurationDataframeId(mct.getId());
					} else {
						Comment mctFail = new Comment();
						mctFail.setCreated(new Date());
						mctFail.setCreatedBy(copier);
						mctFail.setCommentType("Copy Comment");
						mctFail.setBody("Failed to copy MCT.");
						
						analysis.getComments().add(mctFail);
					}
				}
			}

			// Copy KEL
			if (copyKel && !analysis.getDataframeIds().isEmpty() && !fromAnalysis.getDataframeIds().isEmpty() && fromAnalysis.getKelFlagsDataframeId() != null) {
				String nId = fromAnalysis.getDataframeIds().get(0);
				String fId = analysis.getDataframeIds().get(0);
				
				List<String> dfIds = new ArrayList<>();
				dfIds.add(nId);
				dfIds.add(fId);
				dfIds.add(fromAnalysis.getKelFlagsDataframeId());

				LibraryServiceClient lsclient = new LibraryServiceClient();
				lsclient.setHost(Props.getLibraryServiceServer());
				lsclient.setPort(Props.getLibraryServicePort());
				lsclient.setUser(copier);
				LibraryResponse lr = lsclient.getGlobalSystemScriptByName("conc-data-ismatch.R");
				if (lr != null && lr.getArtifactId() != null) {
					ComputeServiceClient csclient = new ComputeServiceClient();
					csclient.setHost(Props.getComputeServiceServer());
					csclient.setPort(Props.getComputeServicePort());
					csclient.setUser(copier);
					
					ComputeParameters params = new ComputeParameters();
					params.setDataframeIds(dfIds);
					params.setScriptId(lr.getArtifactId());
					params.setComputeContainer("equip-r-base");
					params.setEnvironment("Server");
					params.setDataframeType(Dataframe.KEL_FLAGS_TYPE);
					params.setUser(copier);
					if(asNewVersion) {
						Dataframe kel = dfdao.getDataframe(fromAnalysis.getKelFlagsDataframeId());
						if(kel != null) {
							params.setEquipId(kel.getEquipId());
						}
					}

					ComputeResult cr = csclient.compute(params);
					analysis.setKelFlagsDataframeId(null);
					if(cr != null) {
						if (cr.getDataframeIds().size() == 1) {
							analysis.setKelFlagsDataframeId(cr.getDataframeIds().get(0));
						}
						else if(cr.getStdout() != null) {
							Comment kelFail = new Comment();
							kelFail.setCreated(new Date());
							kelFail.setCreatedBy(copier);
							kelFail.setCommentType("Copy Comment");
							kelFail.setBody("Failed to copy KEL flags from analysis " + fromAnalysis.getId() + ". " + cr.getStdout());
							
							analysis.getComments().add(kelFail);
						}
					}
					else {
						Comment kelFail = new Comment();
						kelFail.setCreated(new Date());
						kelFail.setCreatedBy(copier);
						kelFail.setCommentType("Copy Comment");
						kelFail.setBody("Failed to copy KEL flags from analysis " + fromAnalysis.getId() + ". No result was returned from the compute service.");
						
						analysis.getComments().add(kelFail);
					}
				}
			}
		}
	}

	public static Assembly copyAssembly(Assembly assembly) throws CopyException {
		return CopyUtils.copyAssembly(null, assembly);
	}
	
	public static Assembly copyAssembly(Assembly assembly, boolean asNewVersion) throws CopyException {
		return CopyUtils.copyAssembly(null, assembly, asNewVersion);
	}

	public static Assembly copyAssembly(String copier, Assembly assembly) throws CopyException {
		return CopyUtils.copyAssembly(copier, assembly, false);
	}
	
	public static Assembly copyAssembly(String copier, Assembly assembly, String newConcId) throws CopyException {
		return CopyUtils.copyAssembly(copier, assembly, false, newConcId, true);
	}
	
	public static Assembly copyAssembly(String copier, Assembly assembly, boolean asNewVersion) throws CopyException {
		return CopyUtils.copyAssembly(copier, assembly, asNewVersion, null, true);
	}
	
	public static Assembly copyAssembly(String copier, Assembly assembly, boolean asNewVersion, String newConcId, boolean copyAnalysisParts) throws CopyException {
		Assembly copy = null;
		if (assembly != null) {
			CopyValidation cv = canBeCopied(assembly, copier);
			if (cv.canBeCopied()) {
				String oldId = assembly.getId();
				String oldEquipId = assembly.getEquipId();
				long oldVersion = assembly.getVersionNumber();
				
				assembly.setId(null);
				List<String> oldEventItemIds = assembly.getReportingItemIds();
				assembly.setReportingItemIds(new ArrayList<>());
				
				assembly.setLocked(false);
				assembly.setLockedByUser(null);
				assembly.setQcStatus(null);
				
				// assembly.setEquipId("TEST-" + System.currentTimeMillis());
				if(!asNewVersion) {
					CopyUtils.resetEquipId(assembly, assembly.getAssemblyType());
				}
				else {
					AssemblyBaseResource.applyVersionIncrementingLogic(assembly);
				}
				
				CopyUtils.resetCreated(copier, assembly);
				assembly.setComments(CopyUtils.resetComments(copier, assembly));
				CopyUtils.resetMetadata(copier, assembly);
				assembly.getComments()
						.add(CopyUtils.createCopyComment(assembly.getCreatedBy(), oldEquipId, oldVersion));
				assembly.setReportingEventStatusChangeWorkflows(new ArrayList<>());
				
				if(assembly.getScripts() != null) {
					for(Script script : assembly.getScripts()) {
						script.setId(null);
					}
				}
				
				assembly.setPublished(false);
				if(assembly.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
					assembly.setReleaseStatus(Const.UNRELEASED_STATUS);
					assembly.setReleased(false);
				}
				
				for(Script script : assembly.getScripts()) {
					script.setId(null);
					CopyUtils.resetCreated(copier, script);
				}
				
				// If this is a batch, we want to insert it first so that we can set the batch ID of the members.
				// We'll need to update the batch node with the copied dataframe IDs.
				AssemblyDAO adao = ModeShapeDAO.getAssemblyDAO();
				if(assembly instanceof Batch) {
					copy = adao.insertAssembly(assembly);
					copy = adao.getAssembly(copy.getId());
				}
				
				// If this is not an analysis, copy its members.
				List<Dataframe> newMembers = new ArrayList<>();
				if(!(assembly instanceof Analysis)) {
					// We need to copy all members as well.
					// We do this first.
					DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
					List<Dataframe> members = ddao.getDataframe(assembly.getDataframeIds());
					assembly.setDataframeIds(new ArrayList<>());
					assembly.setMemberDataframes(new ArrayList<>());
					
					for (Dataframe df : members) {
						if(assembly instanceof Batch) {
							df.setBatchId(copy.getId());
						}
						Dataframe ndf = CopyUtils.copyDataframe(copier, df);
						if (ndf != null) {
							assembly.getDataframeIds().add(ndf.getId());
							newMembers.add(ndf);
						}
					}
				}
				
				/*for(ReportingEventStatusChangeWorkflow scw : assembly.getReportingEventStatusChangeWorkflows()) {
					scw.setId(null);
					CopyUtils.resetCreated(copier, scw);
					scw.setComments(CopyUtils.resetComments(copier, scw));
					CopyUtils.resetMetadata(scw);
					
					scw.getComments().add(CopyUtils.createCopyComment(scw.getCreatedBy(), oldEquipId, oldVersion));
				}*/
				
				
				if (assembly instanceof Analysis) {
					Analysis an = (Analysis) assembly;
					if(newConcId != null) {
						an.getDataframeIds().set(0, newConcId);
					}
					
					if(copyAnalysisParts) {
						if (an.getParametersDataframeId() != null) {
							Dataframe df = CopyUtils.copyDataframe(copier, an.getParametersDataframeId());
							if (df != null) {
								an.setParametersDataframeId(df.getId());
							}
						}
						if (an.getModelConfigurationDataframeId() != null) {
							Dataframe df = CopyUtils.copyDataframe(copier, an.getModelConfigurationDataframeId());
							if (df != null) {
								an.setModelConfigurationDataframeId(df.getId());
							}
						}
						if (an.getKelFlagsDataframeId() != null) {
							Dataframe df = CopyUtils.copyDataframe(copier, an.getKelFlagsDataframeId());
							if (df != null) {
								an.setKelFlagsDataframeId(df.getId());
							}
						}
						if(an.getEstimatedConcDataframeId() != null) {
							Dataframe df = CopyUtils.copyDataframe(copier, an.getEstimatedConcDataframeId());
							if (df != null) {
								an.setEstimatedConcDataframeId(df.getId());
							}
						}
					}
					
					AnalysisDAO andao = ModeShapeDAO.getAnalysisDAO();
					copy = andao.insertAnalysis(an);
					copy = andao.getAnalysis(copy.getId());
					
					// Copy any Analysis QC reports
					DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
					List<Dataframe> children = ddao.getDataframeByAssemblyId(an.getId());
					for(Dataframe df : children) {
						if(df.getSubType() != null && df.getSubType().equalsIgnoreCase(Dataframe.ANALYSIS_QC_REPORT_SUB_TYPE)) {
							int i = df.getAssemblyIds().indexOf(oldId);
							df.getAssemblyIds().set(i, copy.getId());
							CopyUtils.copyDataframe(df, true);
						}
					}
				} 
				else if(assembly instanceof Batch) {
					copy.setDataframeIds(assembly.getDataframeIds());
					
					ModeShapeDAO msDao = new ModeShapeDAO();
					PropertiesPayload pp = new PropertiesPayload();
					pp.addProperty("equip:dataframeIds", copy.getDataframeIds());
					msDao.updateNode(copy.getId(), pp);
				}
				else {
					copy = adao.insertAssembly(assembly);
					copy = adao.getAssembly(copy.getId());
				}
				
				ReportingAndPublishingDAO rpdao = new ReportingAndPublishingDAO();
				List<ReportingEventItem> items = rpdao.getReportingItem(oldEventItemIds);
				DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
				for (ReportingEventItem item : items) {
					boolean makeCopy = true;
					if(item.getDataFrameId() != null && copy.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
						Dataframe df = ddao.getDataframe(item.getDataFrameId());
						if(df != null && df.getSubType() != null && df.getSubType().equalsIgnoreCase(Dataframe.ATR_SUB_TYPE)) {
							makeCopy = false;
						}
					}
					
					if(makeCopy) {
						item.setReportingEventId(copy.getId());
						ReportingEventItem nrei = CopyUtils.copyReportingEventItem(copier, item);
						if (nrei != null) {
							copy.getReportingItemIds().add(nrei.getId());
						}
					}
				}
				
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:reportingEventItemIds", copy.getReportingItemIds());
				ModeShapeDAO mdao = new ModeShapeDAO();
				mdao.updateNode(copy.getId(), pp);
				
				if(!(assembly instanceof Analysis)) {
					copy.setMemberDataframes(newMembers);
				}

				/*
				 * QCRequestDAO qcdao = ModeShapeDAO.getQCRequestDAO(); List<QCRequest>
				 * qcRequests = qcdao.getQCRequestByAssemblyId(oldId); for(QCRequest request :
				 * qcRequests) { request.setAssemblyId(copy.getId());
				 * CopyUtils.copyQCRequest(request); }
				 */
			} else {
				throw new CopyException(cv.getCopyFailureReason());
			}
		}

		return copy;
	}
	
	public static QCRequest copyQCRequest(QCRequest request) {
		return CopyUtils.copyQCRequest(null, request);
	}

	public static QCRequest copyQCRequest(String copier, QCRequest request) {
		QCRequest copy = null;
		if (request != null) {
			QCRequestDAO qcdao = ModeShapeDAO.getQCRequestDAO();
			String oldEquipId = request.getEquipId();

			for (QCChecklistItem clitem : request.getQcChecklistItems()) {
				String oeid = clitem.getEquipId();

				clitem.setComments(CopyUtils.resetComments(copier, clitem));
				CopyUtils.resetMetadata(copier, clitem);
				CopyUtils.resetEquipId(clitem, "QC Checklist Item");
				CopyUtils.resetCreated(copier, clitem);

				clitem.getComments().add(CopyUtils.createCopyComment(clitem.getCreatedBy(), oeid));
				clitem.setId(null);
			}

			for (QCChecklistSummaryItem sitem : request.getQcChecklistSummaryItems()) {
				String oeid = sitem.getEquipId();

				sitem.setComments(CopyUtils.resetComments(copier, sitem));
				CopyUtils.resetMetadata(copier, sitem);
				CopyUtils.resetEquipId(sitem, "QC Checklist Summary Item");
				CopyUtils.resetCreated(copier, sitem);

				sitem.getComments().add(CopyUtils.createCopyComment(sitem.getCreatedBy(), oeid));
				sitem.setId(null);
			}

			for (QCWorkflowItem witem : request.getQcWorkflowItems()) {
				String oeid = witem.getEquipId();

				witem.setComments(CopyUtils.resetComments(copier, witem));
				CopyUtils.resetMetadata(copier, witem);
				CopyUtils.resetEquipId(witem, "QC Workflow Item");
				CopyUtils.resetCreated(copier, witem);

				witem.getComments().add(CopyUtils.createCopyComment(witem.getCreatedBy(), oeid));
				witem.setId(null);
			}

			request.setComments(CopyUtils.resetComments(copier, request));
			CopyUtils.resetMetadata(copier, request);
			CopyUtils.resetEquipId(request, "QC Request");
			CopyUtils.resetCreated(copier, request);

			request.getComments().add(CopyUtils.createCopyComment(request.getCreatedBy(), oldEquipId));
			request.setId(null);
			qcdao.insertQCRequest(request);
		}

		return copy;
	}

	public static Dataframe copyDataframe(String dataframeId) throws CopyException {
		return CopyUtils.copyDataframe(dataframeId, false);
	}

	public static Dataframe copyDataframe(String dataframeId, boolean asNewVersion) throws CopyException {
		return CopyUtils.copyDataframe(null, dataframeId, asNewVersion);
	}

	public static Dataframe copyDataframe(String copier, String dataframeId) throws CopyException {
		return CopyUtils.copyDataframe(copier, dataframeId, false);
	}

	public static Dataframe copyDataframe(String copier, String dataframeId, boolean asNewVersion)
			throws CopyException {
		DataframeDAO dao = ModeShapeDAO.getDataframeDAO();
		Dataframe df = dao.getDataframe(dataframeId);
		return CopyUtils.copyDataframe(copier, df, asNewVersion);
	}

	public static Dataframe copyDataframe(Dataframe dataframe) throws CopyException {
		return CopyUtils.copyDataframe(dataframe, false);
	}

	public static Dataframe copyDataframe(Dataframe dataframe, boolean asNewVersion) throws CopyException {
		return CopyUtils.copyDataframe(null, dataframe, asNewVersion);
	}

	public static Dataframe copyDataframe(String copier, Dataframe dataframe) throws CopyException {
		return CopyUtils.copyDataframe(copier, dataframe, false);
	}

	public static Dataframe copyDataframe(String copier, Dataframe dataframe, boolean asNewVersion)
			throws CopyException {
		Dataframe copy = null;
		if (dataframe != null) {
			CopyValidation cv = canBeCopied(dataframe, copier);
			if (cv.canBeCopied()) {
				String oldId = dataframe.getId();
				String oldEquipId = dataframe.getEquipId();
				long oldVersion = dataframe.getVersionNumber();

				dataframe.setId(null);
				//dataframe.setEquipId("TEST-" + System.currentTimeMillis());
				
				dataframe.setLocked(false);
				dataframe.setLockedByUser(null);
				dataframe.setQcStatus(null);

				if (!asNewVersion) {
					CopyUtils.resetEquipId(dataframe, dataframe.getDataframeType());
				} else {
					DataframeRootResource.applyVersionIncrementingLogic(dataframe, ModeShapeDAO.getDataframeDAO());
					dataframe.setVersionSuperSeded(false);
					dataframe.setCommitted(false);
				}
				
				CopyUtils.resetCreated(copier, dataframe);
				dataframe.setComments(CopyUtils.resetComments(copier, dataframe));
				CopyUtils.resetMetadata(copier, dataframe);
				if (dataframe.getScript() != null) {
					Script script = dataframe.getScript();
					script.setId(null);
					CopyUtils.resetCreated(copier, script);
				}

				String oldDatasetId = null;
				String oldCDId = null;
				if (dataframe.getDataset() != null) {
					Dataset ds = dataframe.getDataset();
					oldDatasetId = ds.getId();
					oldCDId = ds.getComplexDataId();
					ds.setId(null);
					ds.setComplexDataId(null);
					CopyUtils.resetMetadata(copier, ds);
				}
				
				dataframe.setReleased(false);
				dataframe.setPublished(false);

				dataframe.getComments()
						.add(CopyUtils.createCopyComment(dataframe.getCreatedBy(), oldEquipId, oldVersion));

				DataframeDAO ddao = ModeShapeDAO.getDataframeDAO();
				copy = ddao.insertDataframe(dataframe);
				copy = ddao.getDataframe(copy.getId());

				if (oldDatasetId != null && copy != null && copy.getDataset() != null) {
					DatasetDAO datasetDao = ModeShapeDAO.getDatasetDAO();
					ComplexData cd = datasetDao.getData(oldCDId);
					if (cd != null && cd.getBytes() != null) {
						datasetDao.insertData(copy.getDataset().getId(), cd.getBytes());
					}
				}

				/*
				 * QCRequestDAO qcdao = ModeShapeDAO.getQCRequestDAO(); List<QCRequest>
				 * qcRequests = qcdao.getQCRequestByDataframeId(oldId); for(QCRequest request :
				 * qcRequests) { request.setDataframeId(copy.getId()); request =
				 * CopyUtils.copyQCRequest(request); }
				 */
			} else {
				throw new CopyException(cv.getCopyFailureReason());
			}
		}

		return copy;
	}

	public static ReportingEventItem copyReportingEventItem(ReportingEventItem item) {
		return CopyUtils.copyReportingEventItem(null, item);
	}

	public static ReportingEventItem copyReportingEventItem(String copier, ReportingEventItem item) {
		ReportingEventItem nrei = null;
		if (item != null) {
			item.setId(null);
			String oldEquipId = item.getEquipId();
			long oldVersion = item.getVersionNumber();

			CopyUtils.resetEquipId(item, "Reporting Event Item");
			CopyUtils.resetCreated(copier, item);
			item.setComments(CopyUtils.resetComments(copier, item));
			CopyUtils.resetMetadata(copier, item);
			
			item.getComments().add(CopyUtils.createCopyComment(item.getCreatedBy(), oldEquipId, oldVersion));

			if (item.getPublishItem() != null) {
				PublishItem publishItem = item.getPublishItem();
				publishItem.setId(null);
				String oeid = publishItem.getEquipId();
				long ov = publishItem.getVersionNumber();
				
				publishItem.setPublishStatus(Const.UNPUBLISHED_STATUS);
				publishItem.setExpirationDate(null);
				
				CopyUtils.resetCreated(copier, publishItem);
				CopyUtils.resetEquipId(publishItem, "Publishing Event Item");
				publishItem.setComments(CopyUtils.resetComments(copier, publishItem));
				CopyUtils.resetMetadata(copier, publishItem);
				
				if (oeid != null ) {
					publishItem.getComments().add(CopyUtils.createCopyComment(publishItem.getCreatedBy(), oeid, ov));
				}
				
				/*for (PublishItemPublishStatusChangeWorkflow scw : item.getPublishItem().getWorkflowItems()) {
					scw.setId(null);
					CopyUtils.resetCreated(copier, scw);
					scw.setComments(CopyUtils.resetComments(copier, scw));
					CopyUtils.resetMetadata(scw);
					if (oeid != null ) {
						scw.getComments().add(CopyUtils.createCopyComment(scw.getCreatedBy(), oeid, ov));
					}
				}*/
				item.getPublishItem().setWorkflowItems(new ArrayList<>());
			}

			ReportingAndPublishingDAO rpdao = new ReportingAndPublishingDAO();
			nrei = rpdao.insertReportingItem(item);
		}

		return nrei;
	}

	public static CopyValidation canBeCopied(Dataframe dataframe, String userId) {
		return CopyUtils.canBeCopied(dataframe, false, userId);
	}

	/**
	 * Returns a {@link CopyValidation} object indicating whether the provided
	 * dataframe can be copied or not. Will ignore the promotion status of the
	 * dataframe if {@code ignorePromotionStatus} is {@code true}.
	 * 
	 * @param dataframe
	 * @param ignorePromotionStatus
	 * @return {@link CopyValidation}
	 */
	public static CopyValidation canBeCopied(Dataframe dataframe, boolean ignorePromotionStatus, String userId) {
		CopyValidation cv = new CopyValidation();
		if (dataframe != null) {
			try {
				boolean hasAccess = true;
				if(userId != null) {
					AuthorizationDAO aDao = new AuthorizationDAO();
					hasAccess = aDao.canViewDataframe(dataframe, userId);
				}
				
				if(hasAccess) {
					if (!dataframe.getDataframeType().equalsIgnoreCase("dataset")) {
						if (ignorePromotionStatus || dataframe.getPromotionStatus().equalsIgnoreCase("promoted")) {
							cv.setCanBeCopied(true);
						} else {
							cv.setCopyFailureReason("Can only copy a dataframe that is in PROMOTED status. Dataframe "
									+ dataframe.getId() + " is in " + dataframe.getPromotionStatus() + " status.");
						}
					} else {
						cv.setCopyFailureReason(
								"Cannot copy a dataset dataframe. Dataframe " + dataframe.getId() + " is a dataset.");
					}
				}
				else {
					cv.setCopyFailureReason("User does not have access to dataframe " + dataframe.getEquipId() + " v" + dataframe.getVersionNumber() + ".");
				}
			}
			catch(ServiceCallerException sce) {
				cv.setCopyFailureReason("Error when checking access for dataframe " + dataframe.getId() + ". " + sce.getMessage());
				sce.printStackTrace();
			}
		} else {
			cv.setCopyFailureReason("Dataframe is null.");
		}

		return cv;
	}

	public static CopyValidation canBeCopied(Assembly assembly, String userId) {
		CopyValidation cv = new CopyValidation();
		if (assembly != null) {
			try {
				boolean hasAccess = true;
				if(userId != null) {
					AuthorizationDAO aDao = new AuthorizationDAO();
					hasAccess = aDao.canViewAssembly(assembly, userId);
				}
				
				if(hasAccess) {
					if (!assembly.getAssemblyType().equalsIgnoreCase("data load")) {
						cv.setCanBeCopied(true);
					} else {
						cv.setCopyFailureReason(
								"Data Loads cannot be copied. Assembly " + assembly.getId() + " is a data load.");
					}
				}
				else {
					cv.setCopyFailureReason("User does not have access to assembly " + assembly.getEquipId() + " v" + assembly.getVersionNumber()  + ".");
				}
			}
			catch(ServiceCallerException sce) {
				cv.setCopyFailureReason("Error when checking access for assembly " + assembly.getId() + ". " + sce.getMessage());
				sce.printStackTrace();
			}
		} else {
			cv.setCopyFailureReason("Assembly is null.");
		}

		return cv;
	}

	public static CopyValidation canBeCopied(ReportingEventItem item) {
		CopyValidation cv = new CopyValidation();
		if (item != null) {
			cv.setCanBeCopied(true);
		}

		return cv;
	}

	public static void resetEquipId(EquipID ei, String type) {
		if (ei != null) {
			ei.setEquipId(EquipIdCalculator.calculate(type, true));
			//ei.setEquipId("EI-" + System.currentTimeMillis());
			if (ei instanceof EquipVersionable) {
				((EquipVersionable) ei).setVersionNumber(1);
				((EquipVersionable) ei).setVersionSuperSeded(false);
			}
		}
	}

	public static void resetCreated(EquipCreatable ec) {
		CopyUtils.resetCreated(null, ec);
	}

	public static void resetCreated(String copier, EquipCreatable ec) {
		if (ec != null) {
			ec.setCreated(new Date());
			if (copier != null) {
				ec.setCreatedBy(copier);
			}

			if (ec instanceof EquipModifiable) {
				((EquipModifiable) ec).setModifiedBy(null);
				((EquipModifiable) ec).setModifiedDate(null);
			}
		}
	}

	public static List<Comment> resetComments(EquipCommentable ec) {
		return CopyUtils.resetComments(null, ec);
	}

	public static List<Comment> resetComments(String copier, EquipCommentable ec) {
		List<Comment> comments = new ArrayList<>();
		if (ec != null) {
			Date newCreated = new Date();
			for (Comment c : ec.getComments()) {
				if(!c.getCommentType().equalsIgnoreCase(Comment.ANALYSIS_SAVE_ERROR_TYPE)) {
					c.setId(null);
					c.setCreated(newCreated);
					if (copier != null) {
						c.setCreatedBy(copier);
					}
					
					comments.add(c);
				}
			}
		}
		
		return comments;
	}

	public static void resetMetadata(String copier, EquipMetadatable em) {
		if (em != null) {
			Date newCreated = new Date();
			for (Metadatum md : em.getMetadata()) {
				md.setId(null);
				md.setCreated(newCreated);
				if(copier != null) {
					md.setCreatedBy(copier);
				}
			}
		}
	}

	public static Comment createReExecuteComment(String userId, String equipId) {
		return CopyUtils.createReExecuteComment(userId, equipId, null);
	}

	public static Comment createReExecuteComment(String userId, String equipId, Long version) {
		return CopyUtils.createComment(userId, equipId, version, true);
	}

	public static Comment createCopyComment(String userId, String equipId) {
		return CopyUtils.createCopyComment(userId, equipId, null);
	}

	public static Comment createCopyComment(String userId, String equipId, Long version) {
		return CopyUtils.createComment(userId, equipId, version, false);
	}

	private static Comment createComment(String userId, String equipId, Long version, boolean isReExecute) {
		Comment c = null;
		if (userId != null && equipId != null) {
			c = new Comment();
			c.setCreatedBy(userId);
			c.setCreated(new Date());
			c.setCommentType("Copy Comment");

			String verb = "Copied";
			if (isReExecute) {
				verb = "Re-executed";
			}

			String body = verb + " from " + equipId;
			if (version != null) {
				body += ", v" + version;
			}
			body += ".";

			c.setBody(body);
		}

		return c;
	}
}