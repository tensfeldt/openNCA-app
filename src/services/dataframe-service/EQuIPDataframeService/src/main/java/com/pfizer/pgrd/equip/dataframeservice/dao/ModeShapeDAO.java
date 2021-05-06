package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.search.SearchServiceClient;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ResultSetExtractor;

public class ModeShapeDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(ModeShapeDAO.class);

	public static DataframeDAO getDataframeDAO() {
		return new DataframeDAOImpl();
	}

	public static MetadataDAO getMetadataDAO() {
		return new MetadataDAOImpl();
	}

	public static CommentDAO getCommentDAO() {
		return new CommentDAOImpl();
	}

	public static QCRequestDAO getQCRequestDAO() {
		return new QCRequestDAOImpl();
	}

	public static AssemblyDAO getAssemblyDAO() {
		return new AssemblyDAOImpl();
	}

	public static ScriptDAO getScriptDAO() {
		return new ScriptDAOImpl();
	}

	public static EquipIDDAO getEquipIDDAO() {
		return new EquipIDDAOImpl();
	}

	public static LineageDAO getLineageDAO() {
		return new LineageDAOImpl();
	}

	public static AnalysisDAO getAnalysisDAO() {
		return new AnalysisDAOImpl();
	}

	public static DatasetDAO getDatasetDAO() {
		return new DatasetDAOImpl();
	}

	public static ReportingAndPublishingDAO getReportingAndPublishingDAO() {
		return new ReportingAndPublishingDAO();
	}
	
	public static SearchServiceClient getSearchServiceClient() throws ServiceCallerException {
		SearchServiceClient ssClient = new SearchServiceClient();
		ssClient.setUser(Props.getServiceAccountName());
		ssClient.setHost(Props.getExternalServicesHost());
		ssClient.setPort(Props.getComputeServicePort());
		
		return ssClient;
	}

	protected String constructDataloadPath(String programStudy) {
		return this.constructPath(programStudy, "DataLoads");
	}

	protected String constructReportingEventPath(String programStudy) {
		return this.constructPath(programStudy, "ReportingEvents");
	}

	protected String constructPublishingPath(String programStudy) {
		return this.constructPath(programStudy, "PublishingEvents");
	}
	
	protected String constructBatchPath(String programStudy) {
		return this.constructPath(programStudy, "Batches");
	}

	protected String constructPath(String programStudy, String folderName) {
		String path = null;
		if (programStudy != null && folderName != null) {
			String[] parts = programStudy.split(":");
			String programCode = parts[0].trim();
			if (parts.length < 2) {
				throw new IllegalStateException("study id value was not composed of program and protocol");
			}
			String studyId = parts[1].trim();

			ModeShapeClient client = this.getModeShapeClient();
			String walkingPath = client.getBaseUri() + "/items/Programs/" + programCode;
			ModeShapeNode programFolder = client.getNodeByPath(walkingPath, false);
			if (programFolder != null) {
				walkingPath += "/Protocols/" + studyId;
				ModeShapeNode protocolFolder = client.getNodeByPath(walkingPath, false);
				if (protocolFolder != null) {
					String dlName = folderName;
					walkingPath += "/" + dlName + "/";
					ModeShapeNode dataloadsFolder = null;
					try {
						dataloadsFolder = client.getNodeByPath(walkingPath, false);
						if (dataloadsFolder == null) {
							dataloadsFolder = new ModeShapeNode();
							dataloadsFolder.setNodeName(dlName);
							client.postNode(dataloadsFolder, walkingPath, true);
						}
					} catch (Exception ex) {
						// if cannont get an existing dataloads folder
						dataloadsFolder = new ModeShapeNode();
						dataloadsFolder.setNodeName(dlName);
						try {
							client.postNode(dataloadsFolder, walkingPath, true);
						} catch (ModeShapeAPIException maie) {
							lOGGER.error("", maie);
							throw new RuntimeException("Persistence layer exception upon construct path");
						}
					}

					path = walkingPath;
				}
			}
		}

		return path;
	}

	public <T extends ModeShapeNode> T getNode(String nodeId, Class<T> classOfNode) {
		T node = null;
		if (nodeId != null && classOfNode != null) {
			ModeShapeClient client = this.getModeShapeClient();
			node = client.getNode(classOfNode, nodeId);
		}

		return node;
	}

	public List<ModeShapeNode> getNode(List<String> nodeIds) {
		List<ModeShapeNode> list = new ArrayList<>();
		if (nodeIds != null) {
			list = this.getNode(nodeIds.toArray(new String[0]));
		}

		return list;
	}

	public List<ModeShapeNode> getNode(String[] nodeIds) {
		List<ModeShapeNode> list = new ArrayList<>();
		if (nodeIds != null) {
			for (String id : nodeIds) {
				ModeShapeNode node = this.getNode(id);
				if (node != null) {
					list.add(node);
				}
			}
		}

		return list;
	}
	
	public ModeShapeNode getNode(String nodeId) {
		ModeShapeClient client = this.getModeShapeClient();
		return client.getNode(nodeId);
	}

	public ModeShapeNode getNode(String nodeId, boolean includeChildren) {
		ModeShapeClient client = this.getModeShapeClient();
		return client.getNode(nodeId, includeChildren);
	}

	public EquipObject getEquipObject(String id) {
		return this.getEquipObject(id, true);
	}
	
	public ModeShapeNode getNodeByPath(String path) {
		return this.getNodeByPath(path, true);
	}
	
	public ModeShapeNode getNodeByPath(String path, boolean includeChildren) {
		ModeShapeClient client = this.getModeShapeClient();
		return client.getNodeByPath(path, includeChildren);
	}

	public EquipObject getEquipObject(String id, boolean includeChildren) {
		EquipObject obj = null;
		if (id != null) {
			ModeShapeNode node = this.getNode(id, includeChildren);
			if (node != null) {
				obj = node.toEquipObject();
				this.swapIDs(obj);
			}
		}

		return obj;
	}

	public void swapIDs(EquipObject eo) {
		if (eo != null) {
			if (eo instanceof Assembly) {
				Assembly a = (Assembly) eo;
				a.setLibraryReferences(this.fetchId(a.getLibraryReferences()));
				a.setAssemblyIds(this.fetchId(a.getAssemblyIds()));
				a.setDataframeIds(this.fetchId(a.getDataframeIds()));
				a.setReportingItemIds(this.fetchId(a.getReportingItemIds()));
				a.setProgramIds(this.fetchId(a.getProgramIds()));
				a.setParentIds(this.fetchId(a.getParentIds()));
				a.setProjectIds(this.fetchId(a.getProjectIds()));
				a.setProtocolIds(this.fetchId(a.getProtocolIds()));
				
				for(Script script : a.getScripts()) {
					LibraryReference libRef = script.getScriptBody();
					libRef.setLibraryRef(this.fetchId(libRef.getLibraryRef()));
				}

				if (eo instanceof Analysis) {
					Analysis an = (Analysis) eo;
					an.setKelFlagsDataframeId(this.fetchId(an.getKelFlagsDataframeId()));
					an.setConfigurationTemplateId(this.fetchId(an.getConfigurationTemplateId()));
					an.setModelConfigurationDataframeId(this.fetchId(an.getModelConfigurationDataframeId()));
					an.setParametersDataframeId(this.fetchId(an.getParametersDataframeId()));
					an.setSubsetDataframeIds(this.fetchId(an.getSubsetDataframeIds()));
					an.setEstimatedConcDataframeId(this.fetchId(an.getEstimatedConcDataframeId()));
				}
			} else if (eo instanceof Dataframe) {
				Dataframe df = (Dataframe) eo;
				df.setAssemblyIds(this.fetchId(df.getAssemblyIds()));
				df.setDataframeIds(this.fetchId(df.getDataframeIds()));

				df.setProgramIds(this.fetchId(df.getProgramIds()));
				df.setProjectIds(this.fetchId(df.getProjectIds()));
				df.setProtocolIds(this.fetchId(df.getProtocolIds()));

				Script script = df.getScript();
				if (script != null) {
					LibraryReference libRef = script.getScriptBody();
					libRef.setLibraryRef(this.fetchId(libRef.getLibraryRef()));
				}
			}
		}
	}

	public List<EquipObject> getEquipObject(List<String> ids) {
		List<EquipObject> objects = new ArrayList<>();
		if (ids != null) {
			objects = this.getEquipObject(ids.toArray(new String[0]));
		}

		return objects;
	}

	public List<EquipObject> getEquipObject(String[] ids) {
		List<EquipObject> objects = new ArrayList<>();
		if (ids != null) {
			List<ModeShapeNode> nodes = this.getNode(ids);
			for (ModeShapeNode node : nodes) {
				objects.add(node.toEquipObject());
			}
		}

		return objects;
	}

	public ModeShapeNode updateNode(ModeShapeNode node) {
		ModeShapeNode n = null;
		if (node != null && node.getJcrId() != null) {
			n = this.updateNode(node, node.getJcrId());
		}

		return n;
	}

	public ModeShapeNode updateNode(ModeShapeNode node, String nodeId) {
		ModeShapeNode n = null;
		if (node != null) {
			ModeShapeClient client = this.getModeShapeClient();

			try {
				String json = client.updateNode(node, nodeId);
				n = ModeShapeNode.unmarshal(json, ModeShapeNode.class);
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon node update");
			}
		}

		return n;
	}

	public <T extends ModeShapeNode> T postNode(T node) {
		return this.postNode(node, null, false);
	}

	public <T extends ModeShapeNode> T postNode(T node, String path) {
		return this.postNode(node, path, false);
	}

	public <T extends ModeShapeNode> T postNode(T node, String path, boolean asChild) {
		T n = null;
		if (node != null) {
			ModeShapeClient client = this.getModeShapeClient();

			try {
				if (path != null) {
					n = client.postNode(node, path, asChild);
				} else {
					n = client.postNode(node, asChild);
				}
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon node post");
			}
		}

		return n;
	}

	public String fetchId(String path) {
		String id = null;
		if (path != null) {
			List<String> paths = new ArrayList<>();
			paths.add(path);

			List<String> ids = this.fetchId(paths);
			if (ids.size() == 1) {
				id = ids.get(0);
			}
		}

		return id;
	}

	public List<String> fetchId(List<String> paths) {
		List<String> ids = new ArrayList<>();
		if (paths != null) {
			ModeShapeClient client = this.getModeShapeClient();
			for (String path : paths) {
				ModeShapeNode node = client.getNodeByPath(path, false);
				if (node != null) {
					ids.add(node.getJcrId());
				}
			}
		}

		return ids;
	}
	
	public void deleteNode(String nodeId) throws ModeShapeAPIException {
		this.deleteNode(Arrays.asList(nodeId));
	}
	
	public void deleteNode(List<String> nodeIds) throws ModeShapeAPIException {
		if(nodeIds != null) {
			ModeShapeClient client = this.getModeShapeClient();
			for(String nodeId : nodeIds) {
				ModeShapeNode node = this.getNode(nodeId);
				if(node != null) {
					String path = node.getSelf();
					if(path != null) {
						client.delete(path);
					}
				}
			}
		}
	}

	/**
	 * Returns a pre-configured {@link ModeShapeClient}.
	 * 
	 * @return {@link ModeShapeClient}
	 */
	public ModeShapeClient getModeShapeClient() {
		ModeShapeClient client = new ModeShapeClient();
		client.setUsername(Props.getModeShapeUser());
		client.setPassword(Props.getModeShapePassword());
		client.setHost(Props.getModeShapeServer());
		client.setPort(Props.getModeShapePort());
		client.setContextName(Props.getModeShapeContext());

		client.setRepositoryName(Props.getModeShapeRespository());
		client.setWorkspaceName(Props.getModeShapeWorkspace());
		return client;
	}
	
	/**
	 * Updates the node whose ID matches the {@code nodeId}, setting its {@code equip:modifiedBy} property to the {@code userId} provided and its 
	 * {@code equip:modified} property to the current date/time.
	 * @param nodeId
	 * @param userId
	 * @return {@link String}
	 */
	public String updateModified(String nodeId, String userId) {
		return this.updateModified(nodeId, new Date(), userId);
	}
	
	/**
	 * Updates the node whose ID matches the {@code nodeId}, setting its {@code equip:modifiedBy} property to the {@code userId} provided and its 
	 * {@code equip:modified} property to the provided date/time.
	 * @param nodeId
	 * @param modifiedDate
	 * @param userId
	 * @return {@link String}
	 */
	public String updateModified(String nodeId, Date modifiedDate, String userId) {
		PropertiesPayload pp = new PropertiesPayload();
		pp.addProperty("equip:modified", modifiedDate);
		pp.addProperty("equip:modifiedBy", userId);
		return this.updateNode(nodeId, pp);
	}
	
	/**
	 * Updates the node whose ID matches the one provided with the provided property values.
	 * @param nodeId
	 * @param propertiesPayload
	 * @return 
	 */
	public String updateNode(String nodeId, PropertiesPayload propertiesPayload) {
		return this.updateNode(nodeId, propertiesPayload, true);
	}

	/**
	 * Updates the node whose ID matches the one provided with the provided property values.
	 * @param nodeId
	 * @param propertiesPayload
	 * @return 
	 */
	public String updateNode(String nodeId, PropertiesPayload propertiesPayload, boolean updateModifiedProps) {
		ModeShapeClient client = this.getModeShapeClient();
		String r = null;
		if(nodeId != null && propertiesPayload != null) {
			try {
				r = client.updateNode(nodeId, propertiesPayload);
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon node update");
			}
		}

		return r;
	}

	public <T> List<T> getByStringProperty(String propertyName, String[] vals, String node, String alias,
			ResultSetExtractor<T> extractor) {
		return this.getByStringProperty(propertyName, vals, node, alias, extractor, null);
	}

	public <T> List<T> getByStringProperty(String propertyName, String[] vals, String node, String alias,
			ResultSetExtractor<T> extractor, String orderBy) {
		List<T> list = new ArrayList<>();
		if (extractor != null) {
			long time = System.currentTimeMillis();
			JCRQueryResultSet resultSet = this.getByStringPropertyOrderBy(propertyName, vals, node, alias, orderBy);
			time = System.currentTimeMillis() - time;
			System.out.println("\t\tTook " + time + "ms to query for nodes by study ID.");
			
			time = System.currentTimeMillis();
			list = extractor.extract(resultSet);
			time = System.currentTimeMillis() - time;
			System.out.println("\t\tTook " + time + "ms to convert JCR results to objects.");
		}

		return list;
	}

	public <T> List<T> getParentOfSnsChild(String childId, String parentType, String childType, String alias,
			ResultSetExtractor<T> extractor) {
		List<T> list = new ArrayList<>();
		if (extractor != null) {
			JCRQueryResultSet resultSet = this.getParentOfSnsChild(childId, parentType, childType, alias);
			list = extractor.extract(resultSet);
		}

		return list;
	}

	public JCRQueryResultSet runSelect(String nodeId, String selectStatement) {
		JCRQueryResultSet resultSet = null;

		ModeShapeClient client = this.getModeShapeClient();

		try {
			resultSet = client.query(selectStatement);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon get parent of chlid");
		}

		return resultSet;
	}

	public JCRQueryResultSet getParentOfSnsChild(String childId, String parentType, String childType, String alias) {
		JCRQueryResultSet resultSet = null;
		String sql = "SELECT parent.* AS " + alias + "\r\n" + "FROM [" + parentType + "] AS parent\r\n" + "INNER JOIN ["
				+ childType + "] AS child\r\n" + "ON ISCHILDNODE(child,parent)\r\n" + "WHERE child.[mode:id] = '"
				+ childId + "'";

		ModeShapeClient client = this.getModeShapeClient();

		try {
			resultSet = client.query(sql);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon get parent of chlid");
		}

		return resultSet;
	}

	public <T> List<T> getAnalysisFromFlag(String flag, String dataframeId, String alias,
			ResultSetExtractor<T> extractor) {
		List<T> list = new ArrayList<>();
		if (extractor != null) {
			JCRQueryResultSet resultSet = this.getAnalysisFromFlag(flag, dataframeId, alias);
			list = extractor.extract(resultSet);
		}

		return list;
	}

	public JCRQueryResultSet getAnalysisFromFlag(String flag, String dataframeId, String alias) {
		JCRQueryResultSet resultSet = null;
		String sql = "SELECT " + alias + ".*\r\n" + "FROM [equip:analysis] AS " + alias + "\r\n" + "WHERE ";

		if (flag.equals(Dataframe.KEL_FLAGS_TYPE)) {
			sql += alias + ".[equip:kelFlagsDataframeId] = '" + dataframeId + "'";
		} else if (flag.equals(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
			sql += alias + ".[equip:modelConfigurationDataframeId] = '" + dataframeId + "'";
		} else if (flag.equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
			sql += alias + ".[equip:parametersDataframeId] = '" + dataframeId + "'";
		}

		ModeShapeClient client = this.getModeShapeClient();

		try {
			resultSet = client.query(sql);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon get assembly from flag");
		}

		return resultSet;
	}

	public JCRQueryResultSet getAnalysisContainingProperties(String childId, String parentType, String childType,
			String alias) {
		JCRQueryResultSet resultSet = null;
		String sql = "SELECT [equip:equipId], [equip:versionNumber]\r\n" + "FROM [equip:analysis]\r\n"
				+ "WHERE [equip:kelFlagsDataframeId] = '" + childId + "'";

		ModeShapeClient client = this.getModeShapeClient();

		try {
			resultSet = client.query(sql);
		} catch (ModeShapeAPIException maie) {
			lOGGER.error("", maie);
			throw new RuntimeException("Persistence layer exception upon get parent of chlid");
		}

		return resultSet;
	}

	public JCRQueryResultSet getByStringProperty(String propertyName, String[] vals, String node, String alias) {
		return this.getByStringPropertyOrderBy(propertyName, vals, node, alias, null);
	}

	public JCRQueryResultSet getByStringPropertyOrderBy(String propertyName, String[] vals, String node, String alias,
			String orderBy) {
		JCRQueryResultSet resultSet = null;
		if (propertyName != null && vals != null && vals.length > 0) {
			propertyName = propertyName.trim();
			StringBuilder sqlBuilder = new StringBuilder();
			for (int i = 0; i < vals.length; i++) {
				String val = vals[i];
				if (val == null) {
					continue;
				}
				if (sqlBuilder.length() > 0) {
					sqlBuilder.append(" UNION ");
				}
				sqlBuilder.append("SELECT " + alias + ".* FROM [" + node + "] AS " + alias + " WHERE ");
				if (propertyName.equalsIgnoreCase("equip:studyId")) {
					String[] parts = val.trim().split(":");
					sqlBuilder.append(
							String.format("(ISDESCENDANTNODE('/Programs/%s/Protocols/%s') and ", parts[0], parts[1]));
				}
				sqlBuilder.append(alias + ".[" + propertyName + "] = \"" + val.trim() + "\"");
				if (propertyName.equalsIgnoreCase("equip:studyId")) {
					sqlBuilder.append(")");
				}
			}
			
			if (orderBy != null) {
				sqlBuilder.append(" ORDER BY " + orderBy);
			}

			String sql = sqlBuilder.toString();
			if (!sql.isEmpty()) {
				ModeShapeClient client = this.getModeShapeClient();

				try {
					resultSet = client.query(sql);
				} catch (ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon get by string property");
				}
			}
		}

		return resultSet;
	}

	public JCRQueryResultSet getByPath(String nodeName, String alias, List<String> paths) {
		JCRQueryResultSet result = null;
		if (nodeName != null && alias != null && paths != null && !paths.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT " + alias + ".* FROM [" + nodeName + "] AS " + alias + " WHERE ");
			int s = paths.size();
			for (int i = 0; i < s; i++) {
				String path = paths.get(i);
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append("ISDESCENDANTNODE(" + alias + ", '" + path + "')");
			}

			String sql = sb.toString();
			ModeShapeClient client = this.getModeShapeClient();

			try {
				result = client.query(sql);
			} catch (ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon get by path");
			}
		}

		return result;
	}
}