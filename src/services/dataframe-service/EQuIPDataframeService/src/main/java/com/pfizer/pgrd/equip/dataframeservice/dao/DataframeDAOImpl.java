package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.dto.DataframeDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PromotionDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.extractor.DataframeExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ResultSetExtractor;

public class DataframeDAOImpl extends ModeShapeDAO implements DataframeDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(DataframeDAOImpl.class);
	private static final String ALIAS = "dataframe";
	private static final String NODE = "equip:dataframe";

	@Override
	public List<Dataframe> getDataframe() {
		return new ArrayList<>();
	}

	@Override
	public Dataframe getDataframe(String dataframeId) {
		return this.getDataframe(dataframeId, true);
	}

	public List<Dataframe> getDataframeAttachments(String nodeId, String userId) {
		String sql = "SELECT dataframe.*\r\n" + "FROM [equip:dataframe] AS dataframe\r\n"
				+ "WHERE dataframe.[equip:dataframeIds] = '" + nodeId + "'\r\n"
				+ "  AND dataframe.[equip:dataframeType] = 'Attachment'\r\n"
				+ "  AND dataframe.[equip:deleteFlag] = 'false'";
		List<Dataframe> dataframes = getResultSetExtractor().extract(runSelect(nodeId, sql));
		return this.fullyPopulate(dataframes);
	}

	public List<Dataframe> getAssociatedAssemblyAttachments(String nodeId, String userId) {
		String sql = "SELECT dataframe.*\r\n" + "FROM [equip:dataframe] AS dataframe\r\n"
				+ "WHERE dataframe.[equip:assemblyIds] = '" + nodeId + "'\r\n"
				+ "  AND dataframe.[equip:dataframeType] = 'Attachment'\r\n"
				+ "  AND dataframe.[equip:deleteFlag] = 'false'";
		List<Dataframe> dataframes = getResultSetExtractor().extract(runSelect(nodeId, sql));
		return this.fullyPopulate(dataframes);
	}

	private List<Dataframe> fullyPopulate(List<Dataframe> dataframes) {
		List<Dataframe> returnList = new ArrayList<>();
		for (Dataframe df : dataframes) {
			returnList.add(this.getDataframe(df.getId()));
		}

		return returnList;
	}

	@Override
	public Dataframe getDataframe(String dataframeId, boolean includeReportingMetadata) {
		Dataframe df = null;
		if (dataframeId != null && !dataframeId.isEmpty()) {
			ModeShapeClient client = this.getModeShapeClient();
			DataframeDTO dto = client.getNode(DataframeDTO.class, dataframeId);
			if (dto != null) {
				df = dto.toDataframe();
				df.setAssemblyIds(this.fetchId(df.getAssemblyIds()));
				df.setDataframeIds(this.fetchId(df.getDataframeIds()));

				df.setProgramIds(this.fetchId(df.getProgramIds()));
				df.setProjectIds(this.fetchId(df.getProjectIds()));
				df.setProtocolIds(this.fetchId(df.getProtocolIds()));
				
				Script script = df.getScript();
				if (script != null) {
					LibraryReference libRef = script.getScriptBody();
					// libRef.setLibraryRef(libRef.getLibraryRef());
					libRef.setLibraryRef(this.fetchId(libRef.getLibraryRef()));
				}

				if (includeReportingMetadata) {
					// Get reporting events
					ReportingAndPublishingDAO rdao = new ReportingAndPublishingDAO();
					List<Assembly> reportingEvents = rdao.getReportingEventsByDataframeId(dataframeId);
					Metadatum eventMeta = null;
					for (Assembly re : reportingEvents) {
						if(!re.isDeleteFlag()) {
							if (eventMeta == null) {
								eventMeta = new Metadatum();
								eventMeta.setKey("Reporting Events");
								df.getMetadata().add(eventMeta);
							}
							
							eventMeta.getValue().add(re.getEquipId() + " " + re.getName());
						}
					}
				}
			}
		}
		
		return df;
	}

	@Override
	public List<Dataframe> getDataframe(List<String> dataframeIds) {
		return this.getDataframe(dataframeIds, true);
	}
	
	@Override
	public List<Dataframe> getDataframe(List<String> dataframeIds, boolean includeReportingMetadata) {
		List<Dataframe> list = new ArrayList<>();
		if (dataframeIds != null) {
			String[] ida = new String[dataframeIds.size()];
			dataframeIds.toArray(ida);

			list = getDataframe(ida, includeReportingMetadata);
		}

		return list;
	}
	
	@Override
	public List<Dataframe> getDataframe(String[] dataframeIds) {
		return this.getDataframe(dataframeIds, true);
	}

	@Override
	public List<Dataframe> getDataframe(String[] dataframeIds, boolean includeReportingMetadata) {
		List<Dataframe> list = new ArrayList<>();
		if (dataframeIds != null) {
			for (String id : dataframeIds) {
				Dataframe df = getDataframe(id, includeReportingMetadata);
				if (df != null) {
					list.add(df);
				}
			}
		}

		return list;
	}

	@Override
	public Dataframe insertDataframe(Dataframe dataframe) {
		Dataframe df = null;
		if (dataframe != null && dataframe.getStudyIds() != null && !dataframe.getStudyIds().isEmpty()) {
			String studyId = dataframe.getStudyIds().get(0);

			DataframeDTO dto = new DataframeDTO(dataframe);
			dto.setNodeName(dto.generateNodeName());

			String folderName = "DataLoads";
			if (dataframe.getDataframeType().contains("Report")) {
				folderName = "Reports";
			}
			if (dataframe.getDataframeType().contains("Attachment")) {
				folderName = "DataframeAttachments";
			}
			String path = this.constructPath(studyId, folderName);
			path += dto.getNodeName();

			ModeShapeClient client = this.getModeShapeClient();

			try {
				dto = client.postNode(dto, path, true);
				if (dto != null) {
					df = dto.toDataframe();
				}
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("Error inserting dataframe: " + maie.getMessage(), maie);
				throw new RuntimeException("Persistence layer exception upon dataframe insertion");
			}
		}

		return df;
	}
	
	@Override
	public List<Dataframe> insertDataframe(List<Dataframe> dataframes) {
		Map<String, ModeShapeNode> map = new HashMap<>();
		String basePath = null;
		for(Dataframe dataframe : dataframes) {
			String studyId = dataframe.getStudyIds().get(0);

			DataframeDTO dto = new DataframeDTO(dataframe);
			dto.setNodeName(dto.generateNodeName());

			String folderName = "DataLoads";
			if (dataframe.getDataframeType().contains("Report")) {
				folderName = "Reports";
			}
			if (dataframe.getDataframeType().contains("Attachment")) {
				folderName = "DataframeAttachments";
			}
			
			String path = this.constructPath(studyId, folderName);
			path += dto.getNodeName();
			
			String[] paths = path.split("/items/");
			String childPath = paths[1];
			if(basePath == null) {
				basePath = paths[0];
			}
			
			map.put(childPath, dto);
		}
		
		ModeShapeClient client = this.getModeShapeClient();
		try {
			client.postNode(map, basePath);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon multiple dataframe insertion");
		}
		
		return new ArrayList<>();
	}

	@Override
	public boolean copyGroupAccess(Dataframe dataframe, String userId) {
		boolean access = true;

		// copy this df's access to its parents
		try {
			AuthorizationDAO auth = new AuthorizationDAO();
			List<String> parentIds = dataframe.getDataframeIds();
			String equipId = dataframe.getEquipId();
			if (equipId != null) {
				for (String parentId : parentIds) {
					Dataframe parentDf = getDataframe(parentId);
					if (!auth.copyGroupAccess(dataframe, parentDf.getEquipId(), userId)) {
						access = false;
					}
				}
			}
		} catch (Exception e) {
			lOGGER.error("", e);
			throw new RuntimeException("Persistence layer exception upon copy group access");
		}

		return access;
	}

	@Override
	public Dataframe updateDataframe(Dataframe dataframe, String id) {
		Dataframe df = null;
		if (dataframe != null) {
			ModeShapeClient client = this.getModeShapeClient();
			DataframeDTO dto = client.getNode(DataframeDTO.class, id);
			if (dto != null) {
				dto = new DataframeDTO(dataframe);

				try {
					client.updateNode(dto, id);
					df = dto.toDataframe();
				} catch (ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon dataframe update");
				}
			}
		}

		return df;
	}

	@Override
	public List<Dataframe> getDataframeByType(String dataframeType) {
		List<String> list = new ArrayList<>();
		list.add(dataframeType);

		return this.getDataframeByType(list);
	}

	@Override
	public List<Dataframe> getDataframeByType(List<String> dataframeTypes) {
		List<Dataframe> list = new ArrayList<>();
		if (dataframeTypes != null) {
			list = this.getDataframeByType(dataframeTypes.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByType(String[] dataframeTypes) {
		return this.getByStringProperty("equip:dataframeType", dataframeTypes, NODE, ALIAS, getResultSetExtractor());
	}

	@Override
	public List<Dataframe> getDataframeByStudyId(String studyId) {
		return this.getDataframeByStudyId(new String[] { studyId });
	}

	@Override
	public List<Dataframe> getDataframeByStudyId(List<String> studyIds) {
		List<Dataframe> list = new ArrayList<>();
		if (studyIds != null) {
			list = this.getDataframeByStudyId(studyIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByStudyId(String[] studyIds) {
		return this.getByStringProperty("equip:studyId", studyIds, NODE, ALIAS, getResultSetExtractor(),
				ALIAS + ".[jcr:created]");
	}

	@Override
	public List<Dataframe> getDataframeByAssemblyId(String assemblyId) {
		return this.getDataframeByAssemblyId(new String[] { assemblyId });
	}

	@Override
	public List<Dataframe> getDataframeByAssemblyId(List<String> assemblyIds) {
		List<Dataframe> list = new ArrayList<>();
		if (assemblyIds != null) {
			list = this.getDataframeByAssemblyId(assemblyIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByAssemblyId(String[] assemblyIds) {
		return this.getByStringProperty("equip:assemblyIds", assemblyIds, NODE, ALIAS, getResultSetExtractor());
	}

	@Override
	public List<Dataframe> getDataframeByParentDataframeId(String dataframeId) {
		return this.getDataframeByParentDataframeId(new String[] { dataframeId });
	}

	@Override
	public List<Dataframe> getDataframeByParentDataframeId(List<String> dataframeIds) {
		List<Dataframe> list = new ArrayList<>();
		if (dataframeIds != null) {
			list = this.getDataframeByParentDataframeId(dataframeIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByParentDataframeId(String[] dataframeIds) {
		return this.getByStringProperty("equip:dataframeIds", dataframeIds, NODE, ALIAS, getResultSetExtractor());
	}

	@Override
	public List<Dataframe> getDataframeByUserId(String userId) {
		return this.getDataframeByUserId(new String[] { userId });
	}

	@Override
	public List<Dataframe> getDataframeByUserId(List<String> userIds) {
		List<Dataframe> list = new ArrayList<>();
		if (userIds != null) {
			list = this.getDataframeByUserId(userIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByUserId(String[] userIds) {
		return this.getByStringProperty("equip:createdBy", userIds, NODE, ALIAS, getResultSetExtractor());
	}

	@Override
	public Promotion addPromotion(Promotion promotion, String dataframeId) {
		Promotion p = null;
		PropertiesPayload pp = new PropertiesPayload();
		if (promotion != null && dataframeId != null) {
			Dataframe df = this.getDataframe(dataframeId);
			// We will update the dataframe with values from the promotion
			// so long as they are not null and not empty.

			String dataStatus = promotion.getDataStatus();
			if (dataStatus != null) {
				pp.addProperty("equip:dataStatus", dataStatus);
			}

			String promotionStatus = promotion.getPromotionStatus();
			if (promotionStatus != null) {
				promotionStatus = promotionStatus.trim();
				if (!promotionStatus.isEmpty()) {
					pp.addProperty("equip:promotionStatus", promotionStatus);
					if (promotionStatus.equalsIgnoreCase("revoke/fail")) {
						// revoke the parent dataframes too
						List<String> parents = getPromotedParents(df);

						for (String parent : parents) {
							addPromotion(promotion, parent);
						}
					}
				}
			}

			String restrictionStatus = promotion.getRestrictionStatus();
			if (restrictionStatus != null) {
				restrictionStatus = restrictionStatus.trim();
				pp.addProperty("equip:restrictionStatus", restrictionStatus);
			}

			String dataBlindingStatus = promotion.getDataBlindingStatus();
			if (dataBlindingStatus != null) {
				dataBlindingStatus = dataBlindingStatus.trim();
				pp.addProperty("equip:dataBlindingStatus", dataBlindingStatus);
			}
			ModeShapeDAO bdao = new ModeShapeDAO();
			bdao.updateNode(dataframeId, pp);

			// Add the promotion to the dataframe
			ModeShapeClient client = getModeShapeClient();
			ModeShapeNode node = client.getNode(dataframeId);
			String path = node.getSelf() + "/equip:promotion";
			PromotionDTO dto = new PromotionDTO(promotion);

			// update the promotion equip id
			promotion.setEquipId(EquipIdCalculator.calculate("promotion"));

			try {
				dto = client.postNode(dto, path, true);
				p = dto.toPromotion();
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon add promotion");
			}
		}
		return p;
	}

	@Override
	public boolean isPromotionRevokable(Dataframe df) {
		List<String> parents = getPromotedParents(df);
		if (parents.isEmpty()) {
			return isChildFromScript(df); // if this is the first promoted df,
											// and it has a child, the child must be
											// from the script
		} else if (parents.size() >= 1) {
			ModeShapeClient client = getModeShapeClient();
			ModeShapeNode node = client.getNode(df.getId());
			Dataframe fullDf = (Dataframe) node.toEquipObject();// make sure the df will have the script node

			return isFromLibraryScript(fullDf, "blq-adjustments.r"); // if this has one promoted parent,
			// then this must be from the script itself
		} else
			return false;

	}

	private List<String> getPromotedParents(Dataframe df) {
		List<String> parentIds = new ArrayList();
		List<String> parentDfIds = df.getDataframeIds();
		for (String parentId : parentDfIds) {
			Dataframe parentDf = this.getDataframe(parentId);
			if (parentDf.getPromotionStatus().equalsIgnoreCase("promoted") && !parentDf.isDeleteFlag()) {
				parentIds.add(parentId);
			}
			//add parent's parent if it is from de-identify script
			for (String dfId : parentDf.getDataframeIds()) {
				Dataframe gpDf = this.getDataframe(dfId);
				ModeShapeClient client = getModeShapeClient();
				ModeShapeNode node = client.getNode(gpDf.getId());
				Dataframe fullDf = (Dataframe) node.toEquipObject();
				if (isFromLibraryScript(fullDf, "de-identify-and-calc-sdeid.R") && !gpDf.isDeleteFlag()) {
					parentIds.add(dfId);
				}
			}
		}
		return parentIds;
	}

	private boolean isChildFromScript(Dataframe df) {
		// the children are dataframes
		List<Dataframe> children = this.getDataframeByParentDataframeId(df.getId());
		List<Dataframe> cleanChildren = new ArrayList();
		for (Dataframe child : children) {
			if (!child.isDeleteFlag()) // maybe need to check other things here?
				cleanChildren.add(child);
		}

		if (cleanChildren.isEmpty()) {
			return true;

		} else if (cleanChildren.size() > 1) {

			return false;
		} else {
			Dataframe childDf = cleanChildren.get(0);
			// check for grandchildren - if it has any then do not revoke
			List<Dataframe> grandChildren = this.getDataframeByParentDataframeId(childDf.getId());
			if (!grandChildren.isEmpty())
				return false;
			// Dataframe fullDf = this.getDataframe(childDf.getId()); // need to do this
			// otherwise script node not included with df

			// get df from modeshape node
			ModeShapeClient client = getModeShapeClient();
			ModeShapeNode node = client.getNode(childDf.getId());
			Dataframe fullDf = (Dataframe) node.toEquipObject();

			// ModeShapeNode node = client.getNode(pointer);

			return isFromLibraryScript(fullDf, "blq-adjustments.r");

		}
	}

	private boolean isFromLibraryScript(Dataframe df, String scriptName) {
		// equip:script/equip:scriptBody/equip:libraryRef pointing to
		// <>/library/global/system-scripts/blq-adjustments.r
		Script script = df.getScript();
		if (script != null) {
			LibraryReference lr = script.getScriptBody();
			if (lr != null) {
				String pointer = lr.getLibraryRef();
				if (pointer != null) {
					return pointer.contains(scriptName);
				} else
					return false;
			} else
				return false;
		}
		return false;
	}

	@Override
	public List<Dataframe> getDataframeByEquipId(String equipId) {
		return this.getDataframeByEquipId(new String[] { equipId });
	}

	@Override
	public List<Dataframe> getDataframeByEquipId(List<String> equipIds) {
		List<Dataframe> list = new ArrayList<>();
		if (equipIds != null) {
			list = this.getDataframeByEquipId(equipIds.toArray(new String[0]));
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByEquipId(String[] equipIds) {
		return this.getByStringProperty("equip:equipId", equipIds, NODE, ALIAS, getResultSetExtractor());
	}

	private List<Dataframe> query(String sql) {
		List<Dataframe> list = new ArrayList<>();
		if (sql != null) {
			ModeShapeClient client = this.getModeShapeClient();

			try {
				JCRQueryResultSet rs = client.query(sql);
				if (rs != null) {
				}
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon query");
			}
		}

		return list;
	}

	@Override
	public List<Dataframe> getDataframeByProgramProtocol(String programId, String protocolId) {
		return this.getDataframeByProgramProtocol(programId, new String[] { protocolId });
	}

	@Override
	public List<Dataframe> getDataframeByProgramProtocol(String programId, List<String> protocolIds) {
		List<Dataframe> dataframes = new ArrayList<>();
		if (protocolIds != null) {
			dataframes = this.getDataframeByProgramProtocol(programId, protocolIds.toArray(new String[0]));
		}

		return dataframes;
	}

	@Override
	public List<Dataframe> getDataframeByProgramProtocol(String programId, String[] protocolIds) {
		List<Dataframe> dataframes = new ArrayList<>();
		if (programId != null && protocolIds != null && protocolIds.length > 0) {
			String basePath = "/Programs/" + programId;
			List<String> paths = new ArrayList<>();
			for (String pid : protocolIds) {
				String path = basePath + "/Protocols/" + pid + "/DataLoads";
				paths.add(path);
			}

			JCRQueryResultSet rs = this.getByPath(NODE, ALIAS, paths);
			dataframes.addAll(this.getResultSetExtractor().extract(rs));
		}

		return dataframes;
	}

	private ResultSetExtractor<Dataframe> getResultSetExtractor() {
		DataframeExtractor de = new DataframeExtractor();
		de.setAlias(ALIAS);
		return de;
	}

	@Override
	public Dataframe getLatestDataframeByEquipId(String equipId) {
		return this.getLatestDataframeByEquipId(equipId, null, false);
	}

	@Override
	public Dataframe getLatestDataframeByEquipId(String equipId, String userId) {
		return this.getLatestDataframeByEquipId(equipId, userId, false);
	}

	@Override
	public Dataframe getLatestDataframeByEquipId(String equipId, boolean includeDeleted) {
		return this.getLatestDataframeByEquipId(equipId, null, includeDeleted);
	}

	@Override
	public Dataframe getLatestDataframeByEquipId(String equipId, String userId, boolean includeDeleted) {
		Dataframe latest = null;
		if (equipId != null) {
			List<Dataframe> dataframes = this.getDataframeByEquipId(equipId);
			latest = VersioningDAO.getLatestVersion(dataframes, userId, includeDeleted);
		}
		
		return latest;
	}

	@Override
	public List<Dataframe> searchReports(ReportSearch searchCriteria) {
		List<Dataframe> dataframes = new ArrayList<>();
		String jcrSql = "SELECT n.* FROM [equip:dataframe] AS n WHERE n.[equip:dataframeType] = \"Report\"";
		if(searchCriteria != null) {
			if(!searchCriteria.includeSuperseded()) {
				jcrSql += " AND n.[equip:versionSuperSeded] = false";
			}
			if(!searchCriteria.includeUncommitted()) {
				jcrSql += " AND n.[equip:versionCommitted] = true";
			}
			if(searchCriteria.getParentAssemblyId() != null) {
				jcrSql += " AND n.[equip:assemblyIds] = \"" + searchCriteria.getParentAssemblyId() + "\"";
			}
			if(!searchCriteria.includeDeleted()) {
				jcrSql += " AND n.[equip:deleteFlag] = false";
			}
			if(searchCriteria.getSubType() != null) {
				jcrSql += " AND n.[equip:subType] = \"" + searchCriteria.getSubType() + "\"";
			}
		}
		
		ModeShapeClient client = this.getModeShapeClient();
		try {
			JCRQueryResultSet resultSet = client.query(jcrSql);
			if(resultSet != null) {
				DataframeExtractor extractor = new DataframeExtractor();
				dataframes = extractor.extract(resultSet, "n");
			}
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("Searching for reports.", maie);
		}
		
		return dataframes;
	}

	@Override
	public boolean deleteDataframe(String id) {
		boolean success = false;
		ModeShapeClient client = this.getModeShapeClient();
		try {
			ModeShapeNode node = client.getNode(id);
			if(node != null) {
				client.delete(node.getSelf());
			}
			
			success = true;
		}
		catch(Exception e) { }
		
		return success;
	}
}
