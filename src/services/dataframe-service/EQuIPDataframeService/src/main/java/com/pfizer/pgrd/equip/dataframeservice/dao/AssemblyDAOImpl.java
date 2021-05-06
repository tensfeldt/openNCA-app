package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.dto.AnalysisDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.AssemblyDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.BatchDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.resource.event.PublishEventResource;
import com.pfizer.pgrd.equip.utils.ConstAPI;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.extractor.AssemblyExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ResultSetExtractor;

public class AssemblyDAOImpl extends ModeShapeDAO implements AssemblyDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyDAOImpl.class);

	private static final String ALIAS = "assembly";
	private static final String NODE = "equip:assembly";

	@Override
	public Assembly getAssembly(String assemblyId) {
		Assembly c = null;
		if (assemblyId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			AssemblyDTO dto = client.getNode(AssemblyDTO.class, assemblyId);
			if (dto != null) {
				c = dto.toAssembly();
				this.swapURIs(c);
			}
		}

		return c;
	}

	@Override
	public List<Assembly> getAssembly(List<String> assemblyIds) {
		List<Assembly> assemblies = new ArrayList<>();
		if (assemblyIds != null) {
			String[] ida = new String[assemblyIds.size()];
			assemblyIds.toArray(ida);
			assemblies = getAssembly(ida);
		}

		return assemblies;
	}

	@Override
	public List<Assembly> getAssembly(String[] assemblyIds) {
		List<Assembly> assemblies = new ArrayList<>();
		if (assemblyIds != null) {
			for (String id : assemblyIds) {
				Assembly c = getAssembly(id);
				if (c != null) {
					assemblies.add(c);
				}
			}
		}

		return assemblies;
	}

	@Override
	public List<Assembly> getAssemblyByMemberDataframeId(String dataframeId) {
		return this.getAssemblyByMemberDataframeId(new String[] { dataframeId });
	}

	@Override
	public List<Assembly> getAssemblyByMemberDataframeId(List<String> dataframeIds) {
		List<Assembly> list = new ArrayList<>();
		if (dataframeIds != null) {
			list = this.getAssemblyByMemberDataframeId(dataframeIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByMemberDataframeId(String[] dataframeIds) {
		List<Assembly> list = convertFromGenericListType(this.getByStringProperty("equip:dataframeIds", dataframeIds, NODE, ALIAS,
				this.getResultSetExtractor()));
		this.swapURIs(list);
		return list;
	}
	
	@Override
	public List<Assembly> getAssemblyByReportingEventItemId(String reiId) {
		return this.getAssemblyByReportingEventItemId(new String[] { reiId });
	}
	
	
	
	@Override
	public List<Assembly> getAssemblyByReportingEventItemId(String[] reiIds) {
		List<Assembly> list = convertFromGenericListType(this.getByStringProperty("equip:reportingEventItemIds",reiIds, NODE, ALIAS,
				this.getResultSetExtractor()));
		this.swapURIs(list);
		return list;
	}

	@Override
	public List<Assembly> getAssemblyByParentId(String parentId) {
		return this.getAssemblyByParentId(new String[] { parentId });
	}

	@Override
	public List<Assembly> getAssemblyByParentId(List<String> parentIds) {
		List<Assembly> list = new ArrayList<>();
		if (parentIds != null) {
			list = this.getAssemblyByParentId(parentIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByParentId(String[] parentIds) {
		List<Assembly> list = convertFromGenericListType(
				this.getByStringProperty("equip:parentIds", parentIds, NODE, ALIAS, this.getResultSetExtractor()));
		this.swapURIs(list);
		return list;
	}

	@Override
	public List<Assembly> getAssemblyByStudyId(String studyId) {
		return this.getAssemblyByStudyId(new String[] { studyId });
	}

	@Override
	public List<Assembly> getAssembliesByStudyIds(List<String> studyIds) {
		List<Assembly> list = new ArrayList<>();
		if (studyIds != null) {
			list = this.getAssemblyByStudyId(studyIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByStudyId(String[] studyIds) {
		List<Assembly> list = this.getByStringProperty("equip:studyId", studyIds, NODE, ALIAS, this.getResultSetExtractor(), ALIAS + ".[jcr:created]");
		
		long t = System.currentTimeMillis();
		this.swapURIs(list);
		t = System.currentTimeMillis() - t;
		System.out.println("\t\tTook " + t + "ms to swap URIs for UUIDS.");
		
		return list;
	}

	@Override
	public List<Assembly> getAssemblyByUserId(String userId) {
		return this.getAssemblyByUserId(new String[] { userId });
	}

	@Override
	public List<Assembly> getAssemblyByUserId(List<String> userIds) {
		List<Assembly> list = new ArrayList<>();
		if (userIds != null) {
			list = this.getAssemblyByUserId(userIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByUserId(String[] userIds) {
		List<Assembly> list = convertFromGenericListType(
				this.getByStringProperty("equip:createdBy", userIds, NODE, ALIAS, this.getResultSetExtractor()));
		this.swapURIs(list);
		return list;
	}

	@Override
	public List<Assembly> getAssemblyByMemberAssemblyId(String assemblyId) {
		return this.getAssemblyByMemberAssemblyId(new String[] { assemblyId });
	}

	@Override
	public List<Assembly> getAssemblyByMemberAssemblyId(List<String> assemblyIds) {
		List<Assembly> list = new ArrayList<>();
		if (assemblyIds != null) {
			list = this.getAssemblyByMemberAssemblyId(assemblyIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByMemberAssemblyId(String[] assemblyIds) {
		List<Assembly> list = convertFromGenericListType(
				this.getByStringProperty("equip:assemblyIds", assemblyIds, NODE, ALIAS, this.getResultSetExtractor()));
		this.swapURIs(list);
		return list;
	}

	@Override
	public Assembly insertAssembly(Assembly assembly) {
		if (assembly == null) {
			throw new IllegalStateException("Tried to insert null assembly");
		}

		String studyId = null;
		String assemblyType = assembly.getAssemblyType();
		if (assembly.getStudyIds() != null && !assembly.getStudyIds().isEmpty()) {
			studyId = assembly.getStudyIds().get(0);
		}
		
		AssemblyDTO dto = new AssemblyDTO(assembly);
		if(assembly instanceof Analysis) {
			dto = new AnalysisDTO((Analysis) assembly);
		}
		else if(assembly instanceof Batch) {
			dto = new BatchDTO((Batch) assembly);
		}
		
		dto.setNodeName(dto.generateNodeName());
		ModeShapeClient client = this.getModeShapeClient();
		if (studyId != null && assemblyType != null) {
			ModeShapeDAO bdao = new ModeShapeDAO();
			String path = bdao.constructDataloadPath(studyId);
			if (assemblyType.equalsIgnoreCase(ConstAPI.REPORTING_EVENT_ASSEMBLY_TYPE)) {
				path = bdao.constructReportingEventPath(studyId);
			}
			else if(assemblyType.equalsIgnoreCase(PublishEventResource.PUBLISHING_EVENT_ASSEMBLY_TYPE)) {
				path = bdao.constructPublishingPath(studyId);
			}
			else if(assemblyType.equalsIgnoreCase(Assembly.BATCH_TYPE)) {
				path = bdao.constructBatchPath(studyId);
			}
			
			path += dto.getNodeName();
			
			try {
				dto = client.postNode(dto, path, true);
				assembly = this.getAssembly(dto.getJcrId());
			}
			catch(ModeShapeAPIException maie) {
				LOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon assembly inertion");			
			}
		}
		else {
			throw new IllegalStateException("An assembly type and at least one study ID must be provided.");
		}

		return assembly;
	}

	@Override
	public List<Assembly> getAssemblysByEquipId(String equipId) {
		return this.getAssemblyByEquipId(new String[] { equipId });
	}

	@Override
	public List<Assembly> getAssemblyByEquipId(List<String> equipIds) {
		List<Assembly> list = new ArrayList<>();
		if (equipIds != null) {
			list = this.getAssemblyByEquipId(equipIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Assembly> getAssemblyByEquipId(String[] equipIds) {
		List<Assembly> list = this.getByStringProperty("equip:equipId", equipIds, NODE, ALIAS, this.getResultSetExtractor());
		this.swapURIs(list);
		return convertFromGenericListType(list);
				
	}

	private List<Assembly> convertFromGenericListType(List list) {
		List<Assembly> listAssemblies = new ArrayList<Assembly>();

		for (Object obj : list) {
			listAssemblies.add((Assembly) obj);
		}
		
		return listAssemblies;
	}
	
	@Override
	public Assembly updateAssembly(String assemblyId, Assembly assembly, boolean includeChildren) {
		if (assemblyId != null && assembly != null) {
			ModeShapeNode dto = null;
			if(assembly instanceof Analysis) {
				dto = new AnalysisDTO((Analysis)assembly);
			}
			else {
				dto = new AssemblyDTO(assembly);
			}
			
			ModeShapeClient client = this.getModeShapeClient();
			try {
				client.updateNode(dto, assemblyId, includeChildren);
			}
			catch(ModeShapeAPIException maie) {
				LOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon assembly update");				
			}
		}

		return assembly;
	}

	public Assembly updateAssembly(String assemblyId, Assembly assembly) {
		return updateAssembly(assemblyId, assembly, false);
	}

	@Override
	public List<Assembly> getRootDataLoads(String id) {
		List<Assembly> roots = new ArrayList<>();
		if (id != null) {
			EquipObject object = this.getEquipObject(id);
			if (object != null) {
				List<EquipObject> list = new ArrayList<>();
				list.add(object);

				long time = System.currentTimeMillis();
				roots = this.getRootDataLoads(list, roots);
				time = System.currentTimeMillis() - time;
				LOGGER.info("Time taken to get root data loads based on ID " + object.getId() + ": " + time + "ms.");
			}
		}

		return roots;
	}

	private List<Assembly> getRootDataLoads(List<EquipObject> objects, List<Assembly> currentRoots) {
		if (objects != null) {
			// First, gather all unique parent IDs
			List<String> parentIds = new ArrayList<>();
			ModeShapeDAO bDao = new ModeShapeDAO();

			long totalTime = System.currentTimeMillis();
			for (EquipObject object : objects) {
				List<String> pids = new ArrayList<>();
				if (object instanceof Assembly) {
					Assembly a = (Assembly) object;
					//pids = bDao.fetchId(a.getParentIds());
				} else if (object instanceof Dataframe) {
					Dataframe d = (Dataframe) object;
					pids.addAll(bDao.fetchId(d.getAssemblyIds()));
					pids.addAll(bDao.fetchId(d.getDataframeIds()));

					long memberTime = System.currentTimeMillis();
					List<Assembly> memberOfs = this.getAssemblyByMemberDataframeId(d.getId());
					for (Assembly owner : memberOfs) {
						pids.add(owner.getId());
					}
					memberTime = System.currentTimeMillis() - memberTime;
					LOGGER.info("Time taken to fetch member-of assemblies for dataframe ID " + d.getId() + ": "
							+ memberTime + "ms.");
				}

				for (String id : pids) {
					if (!parentIds.contains(id)) {
						parentIds.add(id);
					}
				}
			}

			// Next, we grab the unique parents
			long parentTime = System.currentTimeMillis();
			List<ModeShapeNode> parents = bDao.getNode(parentIds);
			parentTime = System.currentTimeMillis() - parentTime;
			LOGGER.info("Time to fetch " + parentIds.size() + " parent node(s): " + parentTime + "ms.");

			List<EquipObject> more = new ArrayList<>();
			for (ModeShapeNode p : parents) {
				EquipObject parent = p.toEquipObject();
				if (parent instanceof Assembly) {
					Assembly a = (Assembly) parent;

					if (!a.isDeleteFlag()) {
						// If this is an Assembly with no parent IDs and is a data load, add it to the
						// roots
						if (a.getParentIds().isEmpty() && a.getAssemblyType().equalsIgnoreCase(Assembly.DATA_LOAD_TYPE)) {
							currentRoots.add(a);
						} else if (!a.getParentIds().isEmpty()) {
							more.add(parent);
						}
					}
				} else if (parent instanceof Dataframe) {
					Dataframe d = (Dataframe) parent;
					if (!d.isDeleteFlag()) {
						if (!d.getAssemblyIds().isEmpty() || !d.getDataframeIds().isEmpty()) {
							more.add(parent);
						}

						long memberTime = System.currentTimeMillis();
						List<Assembly> memberOfs = this.getAssemblyByMemberDataframeId(d.getId());
						memberTime = System.currentTimeMillis() - memberTime;
						LOGGER.info("Time taken to fetch member-of assemblies for dataframe ID " + d.getId() + ": "
								+ memberTime + "ms.");

						for (Assembly a : memberOfs) {
							boolean add = true;
							for (EquipObject eo : more) {
								if (eo.getId() == a.getId()) {
									add = false;
									break;
								}
							}

							if (add) {
								more.add(a);
							}
						}
					}
				}
			}

			// If there is still more searching to do
			if (!more.isEmpty()) {
				currentRoots = this.getRootDataLoads(more, currentRoots);
			}

			totalTime = System.currentTimeMillis() - totalTime;
			LOGGER.info(
					"Time taken to fetch root dataloads for " + objects.size() + " sobject(s): " + totalTime + "ms.");
		}

		return currentRoots;
	}

	@Override
	public List<Assembly> getAssemblyByProgramProtocol(String programId, String protocolId) {
		return this.getAssemblyByProgramProtocol(programId, new String[] { protocolId });
	}

	@Override
	public List<Assembly> getAssemblyByProgramProtocol(String programId, List<String> protocolIds) {
		List<Assembly> assemblies = new ArrayList<>();
		if (protocolIds != null) {
			assemblies = this.getAssemblyByProgramProtocol(programId, protocolIds.toArray(new String[0]));
		}

		return assemblies;
	}
	
	private void swapURIs(List<Assembly> list) {
		if(list != null) {
			for(Assembly a : list) {
				this.swapURIs(a);
			}
		}
	}
	
	private void swapURIs(Assembly a) {
		if(a != null) {
			a.setAssemblyIds(this.fetchId(a.getAssemblyIds()));
			a.setDataframeIds(this.fetchId(a.getDataframeIds()));
			a.setReportingItemIds(this.fetchId(a.getReportingItemIds()));
			a.setPublishItemIds(this.fetchId(a.getPublishItemIds()));
	
			a.setProgramIds(this.fetchId(a.getProgramIds()));
			a.setParentIds(this.fetchId(a.getParentIds()));
			a.setProjectIds(this.fetchId(a.getProjectIds()));
			a.setProtocolIds(this.fetchId(a.getProtocolIds()));
			
			for(Script s : a.getScripts()) {
				LibraryReference lr = s.getScriptBody();
				if(lr != null && lr.getLibraryRef() != null) {
					lr.setLibraryRef(this.fetchId(lr.getLibraryRef()));
				}
			}
			
			if(a instanceof Analysis) {
				Analysis an = (Analysis) a;
				an.setKelFlagsDataframeId(this.fetchId(an.getKelFlagsDataframeId()));
				an.setConfigurationTemplateId(this.fetchId(an.getConfigurationTemplateId()));
				an.setModelConfigurationDataframeId(this.fetchId(an.getModelConfigurationDataframeId()));
				an.setParametersDataframeId(this.fetchId(an.getParametersDataframeId()));
				an.setSubsetDataframeIds(this.fetchId(an.getSubsetDataframeIds()));
				an.setEstimatedConcDataframeId(this.fetchId(an.getEstimatedConcDataframeId()));
			}
		}
	}

	@Override
	public List<Assembly> getAssemblyByProgramProtocol(String programId, String[] protocolIds) {
		List<Assembly> assemblies = new ArrayList<>();
		if (programId != null && protocolIds != null && protocolIds.length > 0) {
			String basePath = "/Programs/" + programId;
			List<String> paths = new ArrayList<>();
			for (String pid : protocolIds) {
				String path = basePath + "/Protocols/" + pid + "/DataLoads";
				paths.add(path);
			}
			
			JCRQueryResultSet rs = this.getByPath(NODE, ALIAS, paths);
			assemblies.addAll(this.getResultSetExtractor().extract(rs));
		}

		return assemblies;
	}
	
	private ResultSetExtractor<Assembly> getResultSetExtractor() {
		AssemblyExtractor ae = new AssemblyExtractor();
		ae.setAlias(ALIAS);
		return ae;
	}

	@Override
	public Assembly getLatestAssemblyByEquipId(String equipId) {
		return this.getLatestAssemblyByEquipId(equipId, null, false);
	}

	@Override
	public Assembly getLatestAssemblyByEquipId(String equipId, String userId) {
		return this.getLatestAssemblyByEquipId(equipId, userId, false);
	}

	@Override
	public Assembly getLatestAssemblyByEquipId(String equipId, boolean includeDeleted) {
		return this.getLatestAssemblyByEquipId(equipId, null, includeDeleted);
	}

	@Override
	public Assembly getLatestAssemblyByEquipId(String equipId, String userId, boolean includeDeleted) {
		Assembly latest = null;
		if(equipId != null) {
			List<Assembly> assemblies = this.getAssemblysByEquipId(equipId);
			latest = VersioningDAO.getLatestVersion(assemblies, userId, includeDeleted);
		}
		
		return latest;
	}
}