package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PublishItemDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PublishStatusChangeDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ReportingEventItemDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ReportingEventStatusChangeDTO;
import com.pfizer.pgrd.equip.services.search.SearchServiceClient;
import com.pfizer.pgrd.equip.utils.ConstAPI;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.extractor.AssemblyExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.PublishItemExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ReportingEventItemExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ResultSetExtractor;

public class ReportingAndPublishingDAO extends ModeShapeDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(ReportingAndPublishingDAO.class);

	private static final String REPORTING_EVENT_ITEM_NODE = "equip:reportingEventItem";
	private static final String REPORTING_EVENT_ITEM_ALIAS = "rei";

	private static final String PUBLISH_ITEM_NODE = "equip:publishedItem";
	private static final String PUBLISH_ITEM_ALIAS = "pi";

	public ReportingEventItem getReportingItem(String id) {
		ReportingEventItem item = null;
		if (id != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ReportingEventItemDTO dto = client.getNode(ReportingEventItemDTO.class, id);
			if (dto != null) {
				item = dto.toReportingItem();
			}
		}

		return item;
	}

	public List<ReportingEventItem> getReportingItem(List<String> ids) {
		List<ReportingEventItem> list = new ArrayList<>();
		if (ids != null) {
			list = this.getReportingItem(ids.toArray(new String[0]));
		}

		return list;
	}

	public List<ReportingEventItem> getReportingItem(String[] ids) {
		List<ReportingEventItem> list = new ArrayList<>();
		if (ids != null) {
			for (String id : ids) {
				ReportingEventItem rei = this.getReportingItem(id);
				if (rei != null) {
					list.add(rei);
				}
			}
		}

		return list;
	}

	public PublishItem getPublishItem(String id) {
		PublishItem item = null;
		if (id != null) {
			ModeShapeClient client = this.getModeShapeClient();
			PublishItemDTO dto = client.getNode(PublishItemDTO.class, id);
			if (dto != null) {
				item = dto.toPublishedItem();
				item.setPublishedViewTemplateId(this.fetchId(item.getPublishedViewTemplateId()));
			}
		}

		return item;
	}

	public ReportingEventItem insertReportingItem(ReportingEventItem item) {
		ReportingEventItem i = null;
		if (item != null) {
			AssemblyDAO adao = getAssemblyDAO();
			Assembly re = adao.getAssembly(item.getReportingEventId());
			if (re != null && re.getStudyIds() != null && !re.getStudyIds().isEmpty()) {
				ModeShapeClient client = this.getModeShapeClient();
				ReportingEventItemDTO dto = new ReportingEventItemDTO(item);
				dto.setNodeName(dto.generateNodeName());

				ModeShapeDAO bdao = new ModeShapeDAO();
				String path = bdao.constructReportingEventPath(re.getStudyIds().get(0));
				path += dto.getNodeName();

				try {
					dto = client.postNode(dto, path, true);

					if (dto != null) {
						i = dto.toReportingItem();
					}
				} catch (ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon reporting item insert");
				}
			}
		}

		return i;
	}

	public PublishItem insertPublishItem(PublishItem item) {
		PublishItem i = null;
		if (item != null) {
			ModeShapeClient client = this.getModeShapeClient();
			PublishItemDTO dto = new PublishItemDTO(item);

			try {
				dto = client.createFolderAndPostNode(dto, "Publishing");

				if (dto != null) {
					i = dto.toPublishedItem();
				}
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon publish item insert");
			}
		}

		return i;
	}

	public ReportingEventStatusChangeWorkflow insertReportingEventStatusChangeWorkflow(
			ReportingEventStatusChangeWorkflow scw) {
		ReportingEventStatusChangeWorkflow i = null;
		if (scw != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ReportingEventStatusChangeDTO dto = new ReportingEventStatusChangeDTO(scw);

			try {
				dto = client.postNode(dto);

				if (dto != null) {
					i = dto.toReportingEventStatusChangeWorkflow();
				}
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException(
						"Persistence layer exception upon reporting event status change work flow insert");
			}
		}

		return i;
	}

	public PublishItemPublishStatusChangeWorkflow insertPublishItemPublishStatusChangeWorkflow(String id,
			PublishItemPublishStatusChangeWorkflow scw) {
		ModeShapeDAO baseDao = new ModeShapeDAO();
		ModeShapeNode msn = baseDao.getNode(id);

		ModeShapeClient client = this.getModeShapeClient();

		PublishStatusChangeDTO dto = new PublishStatusChangeDTO(scw);
		String fullPath = msn.getSelf() + "/" + dto.getPrimaryType();

		try {
			dto = client.postNode(dto, fullPath, true);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException(
					"Persistence layer exception upon publish item publish status change work flow insert");
		}

		return dto.toPublishItemPublishStatusChangeWorkflow();
	}

	public PublishItem addPublishItemToReportingEventItem(String reportingEventItemId, PublishItem publishItem) {
		ModeShapeDAO baseDAO = new ModeShapeDAO();
		ModeShapeNode msn = baseDAO.getNode(reportingEventItemId);
		ModeShapeClient client = getModeShapeClient();
		PublishItemDTO dto = new PublishItemDTO(publishItem);

		String path = msn.getSelf() + "/" + dto.getPrimaryType();

		try {
			dto = client.postNode(dto, path, true);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon adding publish item to reporting event item");
		}

		return dto.toPublishedItem();
	}

	public PublishItemPublishStatusChangeWorkflow addStatusChangeWorkflow(String publishItemId,
			PublishItemPublishStatusChangeWorkflow scw) {
		ModeShapeDAO baseDAO = new ModeShapeDAO();
		ModeShapeNode msn = baseDAO.getNode(publishItemId);
		ModeShapeClient client = getModeShapeClient();
		PublishStatusChangeDTO dto = new PublishStatusChangeDTO(scw);

		String path = msn.getSelf() + "/" + dto.getPrimaryType();
		PublishItemPublishStatusChangeWorkflow psc = null;
		try {
			dto = client.postNode(dto, path, true);
			psc = dto.toPublishItemPublishStatusChangeWorkflow();
			psc.setPublishStatus(this.fetchId(psc.getPublishStatus()));
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon adding status change workflow");
		}

		return psc;
	}

	public PublishItem updatePublishItem(String publishItemId, PublishItem publishItem) {
		if (publishItemId != null && publishItem != null) {
			PublishItemDTO publishItemDTO = new PublishItemDTO(publishItem);
			ModeShapeClient client = this.getModeShapeClient();

			try {
				client.updateNode(publishItemDTO, publishItemId);
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon update publish item");
			}
		}

		return publishItem;
	}

	public List<ReportingEventItem> getReportingEventItemByDataframeId(String dataframeId) {
		return this.getReportingEventItemByDataframeId(new String[] { dataframeId });
	}

	public List<ReportingEventItem> getReportingEventItemByDataframeId(List<String> dataframeIds) {
		List<ReportingEventItem> list = new ArrayList<>();
		if (dataframeIds != null) {
			list = this.getReportingEventItemByDataframeId(dataframeIds.toArray(new String[0]));
		}

		return list;
	}
	
	public Map<String, List<ReportingEventItem>> getReportingEventItemsByDataframeId(List<String> dataframeIds) {
		Map<String, List<ReportingEventItem>> map = new HashMap<>();
		if(!dataframeIds.isEmpty()) {
			String where = "";
			for(String dfId : dataframeIds) {
				if(!where.isEmpty()) {
					where += "\r\n	 OR ";
				}
				
				where += "n.[equip:dataframeId] = \"" + dfId + "\"";
			}
			
			String sql = "SELECT n.*\r\n" + 
						 "FROM [equip:reportingEventItem] AS n\r\n" +
						 "WHERE " + where;
			
			try {
				ModeShapeClient client = this.getModeShapeClient();
				JCRQueryResultSet resultSet = client.query(sql);
				
				ReportingEventItemExtractor reiEx = new ReportingEventItemExtractor();
				List<ReportingEventItem> items = reiEx.extract(resultSet, "n");
				for(ReportingEventItem item : items) {
					if(!map.containsKey(item.getDataFrameId())) {
						map.put(item.getDataFrameId(), new ArrayList<>());
					}
					
					map.get(item.getDataFrameId()).add(item);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				lOGGER.error("Error when querying for reporting event items by dataframe ID.", e);
			}
		}
		
		return map;
	}

	public List<ReportingEventItem> getReportingEventItemByDataframeId(String[] dataframeIds) {
		List<ReportingEventItem> items = this.getByStringProperty("equip:dataframeId", dataframeIds, REPORTING_EVENT_ITEM_NODE,
				REPORTING_EVENT_ITEM_ALIAS, getReportingEventItemResultSetExtractor());
		List<ReportingEventItem> fullItems = new ArrayList<>();
		for(ReportingEventItem item : items) {
			ReportingEventItem fi = (ReportingEventItem) this.getEquipObject(item.getId());
			fullItems.add(fi);
		}
		
		return fullItems;
	}

	public List<ReportingEventItem> getReportingEventItemByAssemblyId(String assemblyId) {
		return this.getReportingEventItemByAssemblyId(new String[] { assemblyId });
	}

	public List<ReportingEventItem> getReportingEventItemByAssemblyId(List<String> assemblyIds) {
		List<ReportingEventItem> list = new ArrayList<>();
		if (assemblyIds != null) {
			list = this.getReportingEventItemByAssemblyId(assemblyIds.toArray(new String[0]));
		}

		return list;
	}

	public List<ReportingEventItem> getReportingEventItemByAssemblyId(String[] assemblyIds) {
		return this.getByStringProperty("equip:assemblyId", assemblyIds, REPORTING_EVENT_ITEM_NODE,
				REPORTING_EVENT_ITEM_ALIAS, getReportingEventItemResultSetExtractor());
	}

	public List<ReportingEventItem> getReportingEventItemsByEquipId(String equipId) {
		return this.getByStringProperty("equip:equipId", new String[] { equipId }, REPORTING_EVENT_ITEM_NODE,
				REPORTING_EVENT_ITEM_ALIAS, getReportingEventItemResultSetExtractor());
	}

	private ResultSetExtractor<ReportingEventItem> getReportingEventItemResultSetExtractor() {
		ReportingEventItemExtractor e = new ReportingEventItemExtractor();
		e.setAlias(REPORTING_EVENT_ITEM_ALIAS);
		return e;
	}

	public List<PublishItem> getPublishItemsByEquipId(String equipId) {
		return this.getByStringProperty("equip:equipId", new String[] { equipId }, PUBLISH_ITEM_NODE,
				PUBLISH_ITEM_ALIAS, getPublishItemResultSetExtractor());
	}

	private ResultSetExtractor<PublishItem> getPublishItemResultSetExtractor() {
		PublishItemExtractor pie = new PublishItemExtractor();
		pie.setAlias(PUBLISH_ITEM_ALIAS);
		return pie;
	}

	public ReportingEventStatusChangeWorkflow addReportingEventStatusChangeWorkflow(Assembly assembly,
			ReportingEventStatusChangeWorkflow scw) {
		ModeShapeDAO baseDao = new ModeShapeDAO();
		ModeShapeNode msn = baseDao.getNode(assembly.getId());

		ModeShapeClient client = this.getModeShapeClient();

		ReportingEventStatusChangeDTO dto = new ReportingEventStatusChangeDTO(scw);
		String fullPath = msn.getSelf() + "/" + dto.getPrimaryType();

		try {
			dto = client.postNode(dto, fullPath, true);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException(
					"Persistence layer exception upon adding reporting event status change workflow");
		}

		return dto.toReportingEventStatusChangeWorkflow();
	}

	public List<Assembly> getReportingEventAssembliesByStudyIds(List<String> studyIds) {
		AssemblyDAO dao = new AssemblyDAOImpl();
		
		long time = System.currentTimeMillis();
		List<Assembly> assemblies = dao.getAssembliesByStudyIds(studyIds);
		time = System.currentTimeMillis() - time;
		System.out.println("\tTook " + time + "ms to fetch assemblies by study ID.");
		
		List<Assembly> reportingEventAssemblies = new ArrayList<Assembly>();

		for (Assembly a : assemblies) {
			if (a.getAssemblyType().equals(ConstAPI.REPORTING_EVENT_ASSEMBLY_TYPE) && !a.isDeleteFlag()) {
				reportingEventAssemblies.add(a);
			}
		}

		return reportingEventAssemblies;
	}

	public ReportingEventItem getReportingEventItemFromPublishItem(PublishItem pi) {
		// get the parent reporting event item for this published itme
		ModeShapeClient client = getModeShapeClient();
		ModeShapeNode piNode = client.getNode(pi.getId());
		ReportingEventItemDTO reiNode = client.getNodeByPath(ReportingEventItemDTO.class, piNode.getUp(), false);
		return reiNode.toReportingItem();
	}
	
	/**
	 * Returns all reporting event {@link Assembly} objects whose reporting event items are related to the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link List}<{@link Assembly}>
	 */
	public List<Assembly> getReportingEventsByDataframeId(String dataframeId) {
		Map<String, List<Assembly>> map = this.getReportingEventsByDataframeId(Arrays.asList(dataframeId));
		List<Assembly> reportingEvents = map.get("");
		if(reportingEvents == null) {
			reportingEvents = new ArrayList<>();
		}
		
		return reportingEvents;
	}
	
	/**
	 * Returns a {@link Map} object of {@link List}<{@link Assembly}> objects whose reporting event items relate to at least one of the provided dataframe IDs.
	 * @param dataframeIds
	 * @return {@link Map}<{@link String}, {@link List}<{@link Assembly}>
	 */
	public Map<String, List<Assembly>> getReportingEventsByDataframeId(List<String> dataframeIds) {
		Map<String, List<Assembly>> reportingEvents = new HashMap<>();
		if(!dataframeIds.isEmpty()) {
			String where = "";
			for(String dfId : dataframeIds) {
				if(!where.isEmpty()) {
					where += "\r\n						   OR ";
				}
				
				where += "n.[equip:dataframeId] = \"" + dfId + "\"";
			}
			
			String sql = "SELECT DISTINCT a.*\r\n" + 
						 "FROM [equip:assembly] AS a\r\n" + 
						 "WHERE a.[equip:assemblyType] = \"" + Assembly.REPORTING_EVENT_TYPE + "\" AND a.[jcr:uuid] IN (SELECT n.[equip:parentReportingEventId]\r\n" + 
						 "						 FROM [equip:reportingEventItem] AS n\r\n" + 
						 "					    WHERE (" + where + ") AND n.[equip:deleteFlag] = false)";
			
			try {
				ModeShapeClient client = this.getModeShapeClient();
				JCRQueryResultSet resultSet = client.query(sql);
				
				AssemblyExtractor aEx = new AssemblyExtractor();
				List<Assembly> list = aEx.extract(resultSet, "a");
				reportingEvents.put("", list);
			}
			catch(Exception e) {
				e.printStackTrace();
				lOGGER.error("Error when querying for reporting events by dataframe ID.", e);
			}
		}
		
		return reportingEvents;
	}
	
	public List<Assembly> getReportingEventsByDataframeIdSearchService(String dfId, String study) {
		List<Assembly> list = new ArrayList<>();
		SearchServiceClient ssClient = null;
		
		return list;
	}

	// No longer necessary but here as an example of a modeshape query
//	private static final String ALIAS = "publishitem";
//	
//	public PublishItem getPublishItemViaReportingEventItemId(String reportingEventItemId) {
//		String sql = "select * from [equip:publishedItem] as " + ALIAS + " where " + ALIAS + ".[equip:reportingEventItemIds] = " + reportingEventItemId;
//		
//		return this.query(sql);
//	}
//	
//	private PublishItem query(String sql) {
//		ModeShapeClient client = this.getModeShapeClient();
//		JCRQueryResultSet rs = client.query(sql);
//		PublishItem pi = new PublishItem();
//
//		for(Row row : rs.getRows()) {
//			//pi.setComments(comments);
//			//pi.setMetadata(metadata);
//			//pi.setPublishedTags(row.getStringList(ALIAS + ".equip:publishedTags")); --in CND change to a list
//			//pi.setWorkflowItems(workflowItems);
//			pi.setCommitted(row.getBoolean(ALIAS + ".equip:versionCommitted"));
//			pi.setCreated(row.getDate(ALIAS + ".equip:created"));
//			pi.setCreatedBy(row.getString(ALIAS + ".equip:createdBy"));
//			pi.setDeleteFlag(row.getBoolean(ALIAS + ".equip:deleteFlag"));
//			pi.setEquipId(row.getString(ALIAS + ".equip:equipId"));
//			pi.setExpirationDate(row.getDate(ALIAS + ".equip:expirationDate"));
//			pi.setId(row.getString(ALIAS + ".mode:id"));
//			pi.setModifiedBy(row.getString(ALIAS + ".equip:modifiedBy"));
//			pi.setModifiedDate(row.getDate(ALIAS + ".equip:modified"));
//			pi.setObsoleteFlag(row.getBoolean(ALIAS + ".equip:obsoleteFlag"));
//			pi.setPublishDate(row.getDate(ALIAS + ".equip:publishedDate"));
//			pi.setPublishedViewSubFilter(row.getString(ALIAS + ".equip:publishedViewSubfilter"));
//			pi.setPublishedViewTemplateId(row.getString(ALIAS + ".equip:publishedViewTemplateId"));
//			//pi.setPublishEventId(row.getString(ALIAS + ".equip:")); -- this is missing from the CND
//			pi.setReportingEventItemIds(row.getStringList(ALIAS + ".equip:reportingEventItemIds"));
//			pi.setVersionNumber(row.getLong(ALIAS + ".equip:versionNumber"));
//			pi.setVersionSuperSeded(row.getBoolean(ALIAS + ".equip:versionSuperSeded"));
//			pi.setViewFilterCriteria(row.getString(ALIAS + ".equip:publishedViewFilterCriteria"));
//			
//			break; //grab first row only
//		}
//		
//		return pi;
//	}	
}
