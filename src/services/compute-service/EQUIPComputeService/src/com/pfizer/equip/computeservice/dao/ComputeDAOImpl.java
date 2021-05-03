package com.pfizer.equip.computeservice.dao;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.pfizer.equip.computeservice.AppPropertyNames;
import com.pfizer.equip.computeservice.Application;
import com.pfizer.equip.computeservice.containers.ContainerRunner;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.ContainerRunResponse;
import com.pfizer.equip.computeservice.dto.CreatedDatasetInfo;
import com.pfizer.equip.computeservice.dto.DataframeDataset;
import com.pfizer.equip.computeservice.dto.Executor;
import com.pfizer.equip.computeservice.dto.ParentData;
import com.pfizer.equip.computeservice.dto.RequestBody;
import com.pfizer.equip.computeservice.exception.ComputeDataAccessException;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.equip.dataframe.client.DataframeServiceClient;
import com.pfizer.equip.dataframe.client.DataframeUtils;
import com.pfizer.equip.utils.TypedValue;
import com.pfizer.equip.utils.UuidType;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.services.audit.AuditEntryRequestBody;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.wha.docker.client.DockerClient;
import com.wha.docker.client.DockerClientImpl;
import com.wha.docker.engine.EngineResponse;

import spark.Spark;

/**
 * @author HeinemanWP
 *
 */
public class ComputeDAOImpl implements ComputeDAO {
	private static Logger log = LoggerFactory.getLogger(ComputeDAOImpl.class);	
	private static final String VIEW_VISUALIZATION = "VIEW_VISUALIZATION";
	private static final String VISUALIZATION = "Visualization";
	private static final String AUTHORIZATION_SERVICE = "Authorization Service";
	private static final String RUN_ANALYSIS = "RUN_ANALYSIS";
	private static final String ALTER_DATA_BLINDING = "ALTER_DATA_BLINDING";
	private static final String RUN_DATA_TRANSFORM = "RUN_DATA_TRANSFORM";
	private static final String SHINY_CONTAINER = "equip-r-shiny";
	private static final String VIRTUAL_COMPUTATION = "Virtual Computation";
	private static final String DATA_TRANSFORMATION = "data transformation";
	private static final String PRIMARY_PARAMETERS = "primary parameters";
	private static final String DATA_BLINDING_RSCRIPT = "data-blinding.r";
	private static final String EQUIP_SERVICES_LIBRARIAN_PREFIX = "/%s/equip-services/librarian";
	private static final String EQUIP_DATAFRAME_SERVICE = "/EQuIPDataframeService";
	private static final String EQUIP_ACTUALUSERID = "EQUIP_ACTUALUSERID";
	private ExecutorService auditExecutor = Executors.newSingleThreadExecutor();
	private Map<String, String> filenameToTypeMap = new HashMap<>();
	
	public ComputeDAOImpl() {
		if (filenameToTypeMap.isEmpty()) {
			filenameToTypeMap.put("OUTPUT1", "Primary Parameters");
			filenameToTypeMap.put("cest.json", "Estimated Concentration Data");
			filenameToTypeMap.put("flags.json", "KEL Flags");
			filenameToTypeMap.put("mct.json", "Model Configuration Template");
			filenameToTypeMap.put("Derived Parameters",  "Derived Parameters");
		}
	}
	
	/**
	 * doCompute - executes computation synchronously
	 * 
	 * @param serverUri - the server that the compute service is running on.
	 * @param system - System that's calling. Always "NCA" currently.
	 * @param user - NTID of user calling resource.
	 * @param requestHeaders
	 * @param rb - RequestBody 
	 * @param isVirtual - boolean indicating whether or not to return created child 
	 *                    datasets as base64 encoded entities in the response.
	 * @return a ComputeResponse instance.
	 * @throws ComputeException
	 * @throws ServiceCallerException
	 * @throws UnknownHostException
	 * 
	 */
	@Override
	public ComputeResponse doCompute(
			String serverUri, 
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			RequestBody rb, 
			boolean isVirtual) 
			throws ComputeException, ServiceCallerException, UnknownHostException {
		log.info("isVirtual: " + isVirtual);
		String container = rb.getComputeContainer();
		boolean prepareInContainer = true;
		// boolean prepareInContainer = !isVirtual;
		log.info(String.format("rb.isDontBatch(): ", rb.isDontBatch()));
		boolean useBatch = !isVirtual && !rb.isDontBatch();

		try {
			Map<String, TypedValue> parameters = rb.getParameters() != null ? rb.getParameters() : new HashMap<>();

			// Get all the parameters that have "script" as a value, store them in a map
			// and remove them from the parameters map.
			List<Map.Entry<String, TypedValue>> subScriptParams = new ArrayList<>();
			for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
				if ((parameter.getValue().getValue() == null)
					|| parameter.getValue().getValue().isEmpty()) {
					parameter.getValue().setValue("NULL");
				}
				if (parameter.getValue().getValue().equalsIgnoreCase("script")) {
					subScriptParams.add(parameter);
				}
			}
			for (Map.Entry<String, TypedValue> parameter : subScriptParams) {
				parameters.remove(parameter.getKey(), parameter.getValue());
			}
			
			// Fetch script
			ScriptItem scriptItem = getScript(serverUri, system, rb.getUser(), rb.getScriptId());
			if (!scriptItem.isEmpty()) {
				// Check that shiny scripts will be handled by shiny container image
				if (!SHINY_CONTAINER.equalsIgnoreCase(rb.getComputeContainer()) && scriptItem.scriptContains("library(shiny)")) {
					Spark.halt(400, String.format("Can't run shiny script with container image %s", rb.getComputeContainer()));
				} else if (SHINY_CONTAINER.equalsIgnoreCase(rb.getComputeContainer()) && !scriptItem.scriptContains("library(shiny)")) {
					Spark.halt(400, String.format("Can't run non-shiny script with shiny container image %s", rb.getComputeContainer()));				
				}
			}
			Script scriptNode = createScriptNode(rb, scriptItem);
			
			// Get the scripts for the Script parameters
			List<ScriptItem> subScriptItems = getSubScripts(serverUri, system, rb.getUser(), subScriptParams, prepareInContainer);
			
			List<Dataframe> parentDataframes = getDataFrames(system, user, requestHeaders, rb.getDataframes());
			if (!parentDataframes.isEmpty()) {
				//need to pass the data frame objects for audit logging
				rb.setDataframeEntities(parentDataframes);
				String dataframeType = ((rb.getDataframeType() != null) && !rb.getDataframeType().isEmpty()) ? rb.getDataframeType().get(0) : parentDataframes.get(0).getDataframeType();
				// This should be fixed to support checking more than one dataframe
				boolean userHasPrivileges = privilegeCheck(system, requestHeaders, dataframeType, user, scriptItem);
				if (!userHasPrivileges) {
					Spark.halt(403, "User doesn't have required privilege to perform this operation.");
				}
				if (prepareInContainer) {
					makeAuditEntryForDatasetAccess(rb, requestHeaders, null, user, isVirtual, true);
				}
			}

			String computeContainer = rb.getComputeContainer();
			log.info("computeContainer: " + computeContainer);
			log.info("Creating and calling container...");
			// Create container and execute computation
			ComputeResponse returnValue = null;
			try {
				returnValue = doCompute(
						system,
						user,
						requestHeaders,
						rb.getComputeContainer(),
						scriptItem.getName(),
						rb.getScriptId(),
						scriptItem.getScript(),
						subScriptItems,
						parameters, 
						parentDataframes,
						prepareInContainer);
				log.info("Finished creating and calling container");
				if (prepareInContainer) {
					if (!isVirtual) {
						returnValue = createChildDataFramesInContainer(
								useBatch,
								rb.isBatch(),
								rb.getComputeContainer().equalsIgnoreCase("equip-rstudio"),
								system,
								user,
								requestHeaders, 
								rb.getDataframeType(),
								rb.getSubType(),
								rb.getEquipId(), 
								rb.getAssemblies(), 
								parentDataframes, 
								rb.getScriptId(),
								scriptNode, 
								parameters,
								subScriptItems,
								returnValue);
					}					
				} else {
					if (!isVirtual) {
						returnValue = createChildDataFramesParallel(
								system,
								user,
								requestHeaders, 
								rb.getDataframeType(),
								rb.getSubType(),
								rb.getEquipId(), 
								rb.getAssemblies(), 
								parentDataframes, 
								scriptNode, 
								parameters,
								subScriptItems,
								returnValue);
					}
				}
				log.info("Finished creating and populating child dataframes");
				if (!isVirtual) {
					// No need to include the child datasets
					// if this is not a virtual call. Datasets are only
					// returned if this is a virtual call.
					returnValue.setDatasetData(null);
					returnValue.setChildDatasets(null);	
				}
				makeAuditEntry(rb, requestHeaders, returnValue, user, isVirtual, true);
			} finally {
				// WPH
				if (returnValue != null) {
					ContainerRunner.getCloser().add(returnValue.getContainerId(), user);
				}
			}
			return returnValue;
		} catch(ServiceCallerException ex) {
			throw new ComputeException(ex.getMessage(), ex);
		} catch(IOException | JAXBException | GeneralSecurityException | SQLException | ComputeDataAccessException | NamingException ex) {
			makeAuditEntry(rb, requestHeaders, null, user, isVirtual, false);
			throw new ComputeException(ex);
		}
	}

	public boolean privilegeCheck(String system, Map<String, String> requestHeaders, String dataframeType, String username, ScriptItem scriptItem) throws ServiceCallerException, IOException, JAXBException {
		boolean userHasPrivileges = false;
		
		String authServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.AUTH_SERVICE_HOST, AppPropertyNames.AUTH_SERVICE_HOST_DEFAULT);
		int authServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.AUTH_SERVICE_PORT, AppPropertyNames.AUTH_SERVICE_PORT_DEFAULT));
		List<String> privileges = new ArrayList<>();
		
		if (dataframeType.equalsIgnoreCase(DATA_TRANSFORMATION)) {
			privileges.add(RUN_DATA_TRANSFORM);
			
			if((scriptItem.getPath() != null) && scriptItem.getPath().toLowerCase().endsWith(DATA_BLINDING_RSCRIPT)) {
				privileges.add(ALTER_DATA_BLINDING);
			}
			
		} else if(dataframeType.equalsIgnoreCase(PRIMARY_PARAMETERS)) {
			privileges.add(RUN_ANALYSIS);
		} else if (dataframeType.equalsIgnoreCase(VISUALIZATION)) {
			privileges.add(VIEW_VISUALIZATION);
		}
		
		if(!privileges.isEmpty()) {
			AuthorizationServiceClient asc = new AuthorizationServiceClient(authServiceHost, authServicePort, system);
			try {
				// String authUser = requestHeaders.containsKey(EQUIP_ACTUALUSERID) ? requestHeaders.get(EQUIP_ACTUALUSERID) : username;
				String authUser = username;
				userHasPrivileges = asc.checkAuthorization(privileges, authUser);
			} catch(ServiceCallerException ex) {
				throw new ServiceCallerException(AUTHORIZATION_SERVICE, ex.getStatusCode(), ex.getCause());
			}
		} else {
			userHasPrivileges = true;
		}
		
		return userHasPrivileges;
	}

	protected Script createScriptNode(RequestBody rb, ScriptItem scriptItem) {
		Script scriptNode = new Script();
		scriptNode.setCreated(new Date());
		scriptNode.setCreatedBy(rb.getUser());
		scriptNode.setComputeContainer(rb.getComputeContainer());
		scriptNode.setEnvironment(rb.getEnvironment());
		if (!scriptItem.isEmpty()) {
			LibraryReference libRef = new LibraryReference();
			libRef.setLibraryRef(scriptItem.getId());
			scriptNode.setScriptBody(libRef);
		}
		scriptNode.setScriptCriteria(rb.getRequestJson());
		return scriptNode;
	}

	/**
	 * @param serverUri
	 * @param user
	 * @param image
	 * @param script
	 * @param parameters
	 * @param parentDatasets
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws ServiceCallerException 
	 * @throws JAXBException 
	 * @throws ComputeDataAccessException 
	 * @throws SQLException 
	 * @throws ComputeException 
	 * @throws ClassNotFoundException 
	 * @throws NamingException 
	 */
	private ComputeResponse doCompute(
			String system,
			String user, 
			Map<String, String> requestHeaders,
			String image,
			String scriptName,
			String scriptId,
			byte[] script,
			List<ScriptItem> subScriptItems,
			Map<String, TypedValue> parameters, 
			List<Dataframe> parentDataframes,
			boolean prepareInContainer) throws IOException, GeneralSecurityException, JAXBException, ServiceCallerException, ComputeException, SQLException, ComputeDataAccessException, NamingException {
		ComputeDatabaseDAO csdao = new ComputeDatabaseDAO();
		List<Executor> executors = csdao.getExecutionFromName(image);
		if (executors.isEmpty()) {
			throw new ComputeException(String.format("No engine found for %s", image));
		}
		// Map<String, byte[]> parentDatasets = getDataframeDatasets(system, user, parameters, parentDataframes);
		Map<String, DataframeDataset> parentDatasets = new HashMap<>();
		if (prepareInContainer) {
			parentDatasets = getDataframeDatasetsForInContainer(system, user, parameters, parentDataframes);
		} else {
			parentDatasets = getDataframeDatasetsParallel(system, user, requestHeaders, parameters, parentDataframes);
		}
		ContainerRunner runner = new ContainerRunner();
		Executor executor = executors.get(0);
		return runner.run(user, executor, parentDatasets, scriptName, scriptId, script, subScriptItems, parameters, prepareInContainer);
	}

	protected ComputeResponse createChildDataFramesParallel(
			String system,
			String user, 
			Map<String, String> requestHeaders, 
			List<String> dataframeTypes,
			String subType,
			List<String> equipIds, 
			List<String> assemblies, 
			List<Dataframe> parentDataframes,
			Script scriptNode,
			Map<String, TypedValue> parameters,
			List<ScriptItem> subScriptItems,
			ComputeResponse computeResponse) throws ServiceCallerException, IOException, JAXBException {
		List<Assembly> assemblyParents = !assemblies.isEmpty() ? getAssemblies(system, user, requestHeaders, assemblies) : new ArrayList<>();
		
		List<String> studyIds = getStudyIds(assemblyParents, parentDataframes);
		List<String> protocolIds = getProtocolIds(assemblyParents, parentDataframes);
		List<String> projectIds = getProjectIds(assemblyParents, parentDataframes);
		List<String> programIds = getProgramIds(assemblyParents, parentDataframes);
		String promotionStatus = getPromotionStatus(parentDataframes);
		String dataBlindingStatus = getDataBlindingStatus(parentDataframes);
		String restrictionStatus = getRestrictionStatus(parentDataframes);
		String dataStatus = getDataStatus(parentDataframes);
		List<String> profileConfig = new ArrayList<>();
		TypedValue profileConfigValue = parameters.get("profileConfig");
		if (profileConfigValue != null) {
			JsonParser jp = new JsonParser();
			JsonArray jarray = jp.parse(profileConfigValue.getValue()).getAsJsonArray();
			for (int i = 0, n = jarray.size(); i < n; i++) {
				profileConfig.add(jarray.get(i).getAsString());
			}
		}
		TypedValue dataBlindingStatusValue = parameters.get("dataBlindingStatus");
		if (dataBlindingStatusValue != null) {
			dataBlindingStatus = dataBlindingStatusValue.getValue();
		}

		int ndx = 0;
		List<CreatedDatasetInfo> childDatasets = computeResponse.getChildDatasets();
		if (childDatasets != null) {
			log.info("number of child datasets returned: " + childDatasets.size());
			if (!childDatasets.isEmpty()) {
				ExecutorService executor = Executors.newFixedThreadPool(Math.min(childDatasets.size(), 10));
				try {
				    createChildDatasets(
				    		system, 
				    		user, 
							requestHeaders, 
				    		dataframeTypes, 
				    		subType, 
				    		equipIds, 
				    		parentDataframes, 
				    		scriptNode,
							parameters, 
							subScriptItems, 
							computeResponse, 
							assemblyParents, 
							studyIds, 
							protocolIds, 
							projectIds,
							programIds, 
							promotionStatus, 
							dataBlindingStatus, 
							restrictionStatus, 
							dataStatus, 
							profileConfig,
							profileConfigValue, 
							ndx, 
							childDatasets, 
							executor);
				} finally {
					executor.shutdown();
				}
			}
		}
		List<String> childDataframeIds = computeResponse.getDataframes();
		if ((childDataframeIds != null) && !childDataframeIds.isEmpty()) {
			log.info("child dataframe ids:");
			for (String id : childDataframeIds) {
				log.info(id);
			}
		}
		return computeResponse;
	}

	protected ComputeResponse createChildDataFramesInContainer(
			boolean useBatches,
			boolean isBatch,
			boolean isRstudio,
			String system,
			String user,
			Map<String, String> requestHeaders, 
			List<String> dataframeTypes,
			String subType,
			List<String> equipIds, 
			List<String> assemblies, 
			List<Dataframe> parentDataframes,
			String scriptId,
			Script scriptNode,
			Map<String, TypedValue> parameters,
			List<ScriptItem> subScriptItems,
			ComputeResponse computeResponse) throws ServiceCallerException, IOException, JAXBException {

		List<Assembly> assemblyParents = !assemblies.isEmpty() ? getAssemblies(system, user, requestHeaders, assemblies) : new ArrayList<>();
		List<String> studyIds = getStudyIds(assemblyParents, parentDataframes);
		List<String> protocolIds = getProtocolIds(assemblyParents, parentDataframes);
		List<String> projectIds = getProjectIds(assemblyParents, parentDataframes);
		List<String> programIds = getProgramIds(assemblyParents, parentDataframes);
		String promotionStatus = getPromotionStatus(parentDataframes);
		String dataBlindingStatus = getDataBlindingStatus(parentDataframes);
		String qcStatus = getQcStatus(parentDataframes);
		String restrictionStatus = getRestrictionStatus(parentDataframes);
		String dataStatus = getDataStatus(parentDataframes);
		TypedValue dataBlindingStatusValue = parameters.get("dataBlindingStatus");
		if (dataBlindingStatusValue != null) {
			dataBlindingStatus = dataBlindingStatusValue.getValue();
		}
		
		boolean isSystemReport = false;
		if ((subType != null) && !subType.isEmpty()) {
			isSystemReport = subType.equalsIgnoreCase("Analysis QC") || subType.equalsIgnoreCase("ATR");
		}
		boolean isATR = false;
		if ((subType != null) && !subType.isEmpty()) {
			isATR = subType.equalsIgnoreCase("ATR");
		}
		
		// Put the subscript items pack into the parameters
		if ((parameters != null) && (subScriptItems != null) && !subScriptItems.isEmpty()) {
			for (ScriptItem subScript : subScriptItems) {
				parameters.put(subScript.getPath(), new TypedValue("string", "script"));
			}
		}
		Properties appProperties = Application.getAppProperties();
		ParentData pd = new ParentData();
		pd.setHeaders(requestHeaders);
		pd.setUseBatches(useBatches);
		pd.setBatch(isBatch);
		pd.setRstudio(isRstudio);
		String modeshapeServer = Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_SERVICE_HOST, 
				AppPropertyNames.MODESHAPE_SERVICE_HOST_DEFAULT).trim();
		modeshapeServer = modeshapeServer.substring(modeshapeServer.lastIndexOf('/') + 1);
		pd.setModeshapeServer(modeshapeServer);
		pd.setModeshapeUsername(Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_USERNAME, 
				AppPropertyNames.MODESHAPE_USERNAME_DEFAULT).trim());
		pd.setModeshapePassword(Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_AUTH, 
				AppPropertyNames.MODESHAPE_AUTH_DEFAULT).trim());
		String servicesServer = Application.getAppProperties().getProperty(
				AppPropertyNames.DATAFRAME_SERVICE_HOST, 
				AppPropertyNames.DATAFRAME_SERVICE_HOST_DEFAULT).trim();
		String servicesServerPort = Application.getAppProperties().getProperty(
				AppPropertyNames.DATAFRAME_SERVICE_PORT, 
				AppPropertyNames.DATAFRAME_SERVICE_PORT_DEFAULT).trim();
		pd.setServicesServer(String.format("%s:%s", servicesServer, servicesServerPort));
		pd.setUsername(user);
		pd.setDataframeTypes(dataframeTypes);
		pd.setSubType(subType);
		pd.setEquipIds(equipIds);
		pd.setAssemblyIds(assemblies);
		List<String> dataframeIds = parentDataframes.stream()
				.map(Dataframe::getId)
				.collect(java.util.stream.Collectors.toList());
		pd.setDataframeIds(dataframeIds);
		pd.setScriptId(scriptId);
		pd.setScript(scriptNode);
		pd.setParameters(parameters);
		pd.setStudyIds(studyIds);
		pd.setProtocolIds(protocolIds);
		pd.setProjectIds(projectIds);
		pd.setProgramIds(programIds);
		pd.setPromotionStatus(isATR ? "Promoted" : promotionStatus);
		pd.setDataBlindingStatus(dataBlindingStatus);
		pd.setQcStatus(qcStatus);
		pd.setRestrictionStatus(restrictionStatus);
		pd.setDataStatus(dataStatus);
		if (!isSystemReport) {
			pd.setComments(DataframeUtils.getCommentsForChild(
					assemblyParents, 
					parentDataframes));
		}
		List<Metadatum> metadata = DataframeUtils.getMetadataForChild(assemblyParents, parentDataframes, false);
		if (!dataframeTypes.isEmpty() && !dataframeTypes.get(0).equalsIgnoreCase(DATA_TRANSFORMATION)) {
			metadata.removeIf(metadatum -> metadatum.getKey().equalsIgnoreCase("Data Type"));
		}
		pd.setMetadata(metadata);
		pd.setStdIn(computeResponse.getStdin());
		pd.setStdOut(computeResponse.getStdout());
		pd.setStdErr(computeResponse.getStdout());
		
		if (pd.getProfileConfig().isEmpty()) {
			for (Metadatum metadatum : pd.getMetadata()) {
				if (metadatum.getKey().equalsIgnoreCase("Profile Configuration")
						&& !metadatum.getValue().isEmpty())
				{
					pd.getProfileConfig().add(metadatum.getValue().get(0));
					break;
				}
			}
		}
		String pdJson = new Gson().toJson(pd);
		log.info("pdJson:" + pdJson);
		
		byte[] pdJsonBytes = pdJson.getBytes();
		
		DockerClient dockerClient = new DockerClientImpl();
		
		// Copy the parentData to the Container.
		try (ByteArrayOutputStream dest = new ByteArrayOutputStream()) {
			try (TarOutputStream out = new TarOutputStream(dest)) {
				long modTime = Instant.now().getEpochSecond();
				boolean isDir = false;
				int permissions = 0777;
				TarHeader tarHeader = TarHeader.createHeader(
						"parent_data.json", 
						pdJsonBytes.length, 
						modTime, 
						isDir,
						permissions);
				out.putNextEntry(new TarEntry(tarHeader));
				out.write(pdJsonBytes);
				out.flush();
			}
			
			try (ByteArrayInputStream input = new ByteArrayInputStream(dest.toByteArray())) {
				try {
					log.info("computeResponse.getContainerId(): " + computeResponse.getContainerId());
					dockerClient.putArchive(computeResponse.getContainerId(), "/home/docker/teardown", input);
				} catch (IOException e) {
					throw e;
				} catch (GeneralSecurityException e) {
					log.error("", e);
				}
			}
		}

		// Run the executable that will create and store the children.
		List<String> cmd = new ArrayList<>();
		cmd.add("/home/docker/bin/container_teardown");
		cmd.add("/home/docker/output");
		cmd.add("/home/docker/teardown/parent_data.json");
		try {
			EngineResponse er = dockerClient.exec(computeResponse.getContainerId(), cmd);
			er.getStream();
			StringBuilder sb = new StringBuilder();
			try (BufferedInputStream bis = new BufferedInputStream(er.getStream())) {
				byte[] data = new byte[8192];
			    int numBytesRead = 0;
			    do {
			    	numBytesRead = bis.read(data);
			    	if (numBytesRead > 0) {
			    		sb.append(new String(data, 0, numBytesRead));
			    	}
			    } while (numBytesRead > -1);
			}
			String output = sb.toString();
			log.info("output: " + output);
//			String[] childDfUuids = output.split("\\s+");
//			if (childDfUuids[0].startsWith("Batch")) {
//				computeResponse.setBatch(childDfUuids[1]);
//			} else if (childDfUuids[0].startsWith("Dataframes")) {
//				for (int i = 1, n = childDfUuids.length; i < n; i++) {
//					computeResponse.getDataframes().add(childDfUuids[i]);
//				}
//			}
		    Gson gson = new Gson();
			ContainerRunResponse containerRunResponse = gson.fromJson(output, ContainerRunResponse.class);
			if (containerRunResponse != null) {
				if ((containerRunResponse.getScriptIds() != null) && !containerRunResponse.getScriptIds().isEmpty()) {
					if (!containerRunResponse.getScriptIds().get(0).isEmpty()) {
						computeResponse.setScriptId(containerRunResponse.getScriptIds().get(0));
					}
				}
				if ((containerRunResponse.getDataframeIds() != null) 
						&& !containerRunResponse.getDataframeIds().isEmpty()) {
					for (String dataframeId : containerRunResponse.getDataframeIds()) {
						computeResponse.getDataframes().add(dataframeId);
					}
				}
				if ((containerRunResponse.getBatchId() != null) && !containerRunResponse.getBatchId().isEmpty()) {
					computeResponse.setBatch(containerRunResponse.getBatchId());
				}
				if ((containerRunResponse.getErrors() != null) 
						&& !containerRunResponse.getErrors().isEmpty()) {
					for (String error : containerRunResponse.getErrors()) {
						log.error(error);
					}
				}
			}

		} catch (IOException e) {
			throw e;
		} catch (GeneralSecurityException e) {
			log.error("", e);
		}
		
		return computeResponse;
	}
	
	
	private void createChildDatasets(
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			List<String> dataframeTypes, 
			String subType,
			List<String> equipIds, 
			List<Dataframe> parentDataframes, 
			Script scriptNode,
			Map<String, TypedValue> parameters, 
			List<ScriptItem> subScriptItems, 
			ComputeResponse computeResponse,
			List<Assembly> assemblyParents, 
			List<String> studyIds, 
			List<String> protocolIds, 
			List<String> projectIds,
			List<String> programIds, 
			String promotionStatus, 
			String dataBlindingStatus, 
			String restrictionStatus,
			String dataStatus, 
			List<String> profileConfig, 
			TypedValue profileConfigValue, 
			int ndx,
			List<CreatedDatasetInfo> childDatasets, 
			ExecutorService executor) throws ServiceCallerException {
		CompletionService<String> ecs = new ExecutorCompletionService<>(executor);
		List<Future<String>> futures = new ArrayList<>(childDatasets.size());

		boolean isSystemReport = false;
		if ((subType != null) && !subType.isEmpty()) {
			isSystemReport = subType.equalsIgnoreCase("Analysis QC") || subType.equalsIgnoreCase("ATR");
		}
				
		for (CreatedDatasetInfo childDatasetInfo : childDatasets) {
			childDatasetInfo.setSystemReport(isSystemReport);
			String childFilename = (new File(childDatasetInfo.getFilename())).getName();
			log.info("childFilename: " + childFilename);
			byte[] childDatasetData = childDatasetInfo.getData();
			boolean isAnalysisRequest = isAnalysisRequest(dataframeTypes);
			// boolean isAnalysisRequest = false;	// To remove the order checking comment out
													// the above line and uncomment this line.
			String dataframeType = null;
			if (dataframeTypes != null && !dataframeTypes.isEmpty()) {
				if (isAnalysisRequest) {
					dataframeType = getDataframeTypeForAnalysisRequestChild(childFilename, dataframeTypes);
				} else {
					if (ndx < dataframeTypes.size()) {
						dataframeType = dataframeTypes.get(ndx);
					} else {
						dataframeType = dataframeTypes.get(dataframeTypes.size() - 1);
					}
				}
			}
			if (dataframeType == null) {
				dataframeType = "Data Transformation";
			}
			Dataframe child = DataframeUtils.createChildFrom(
					dataframeType, 
					assemblyParents, 
					parentDataframes,
					childDatasetInfo.isSystemReport());
			child.setDataframeType(dataframeType);
			log.info("child.getDataframeType(): " + child.getDataframeType());
			if( subType != null && !subType.isEmpty() ) {
				child.setSubType(subType);
			}
			log.info("child.getSubType(): " + child.getSubType());
			if (equipIds != null && !equipIds.isEmpty()) {
				if (isAnalysisRequest) {
					child.setEquipId(getEquipIdForAnalysisRequestChild(childFilename, dataframeTypes, equipIds));						
				} else {
					if (ndx < equipIds.size()) {
						child.setEquipId(equipIds.get(ndx));
					} else {
						child.setEquipId(equipIds.get(equipIds.size() -1));
					}
				}
				if (!isSystemReport && !isAnalysisDataframe(child.getDataframeType())) {
					child.setComments(DataframeUtils.getCommentsForChild(
							assemblyParents, 
							parentDataframes));
				}
			}
			log.info("child.getEquipId(): " + child.getEquipId());
			
			child.setStudyIds(studyIds);
			child.setProtocolIds(protocolIds);
			child.setProjectIds(projectIds);
			child.setProgramIds(programIds);
			child.setScript(scriptNode);
			child.setPromotionStatus(promotionStatus);

			child.setDataBlindingStatus(dataBlindingStatus);
			child.setRestrictionStatus(restrictionStatus);
			child.setDataStatus(dataStatus);
			if (profileConfigValue != null) {
				child.setProfileConfig(profileConfig); 
			}
			Metadatum filenameMetadata = new Metadatum();
			filenameMetadata.setKey("output filename");
			List<String> filenameValues = new ArrayList<>();
			filenameValues.add(childFilename);
			filenameMetadata.setValue(filenameValues);
			child.getMetadata().add(filenameMetadata);

			futures.add(ecs.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return createChild(system, user, requestHeaders, parameters, subScriptItems, computeResponse,
							childDatasetData, child);
				}
				}));
			
			ndx += 1;	    	    	
		}
		
		for (Future<String> future : futures) {
			try {
				String childId;
				boolean interrupted = false;
				do {
		    		try {
		    			interrupted = false;
		    			childId = future.get();
						computeResponse.getDataframes().add(childId);
		    		} catch (InterruptedException ex) {
		    			interrupted = true;
		    		}
				} while (interrupted);
			} catch (ExecutionException ex) {
				throw new ServiceCallerException(ex);
			}
		}
	}

	private boolean isAnalysisDataframe(String dataframeType) {
		return filenameToTypeMap.containsValue(dataframeType);
	}
	
	private boolean isAnalysisRequest(List<String> dataframeTypes) {
		// ","dataframeType":["Primary Parameters","Estimated Concentration Data","KEL Flags"]
		if (dataframeTypes != null && !dataframeTypes.isEmpty()) {
			return dataframeTypes.contains("Primary Parameters")
					&& dataframeTypes.contains("Estimated Concentration Data")
					&& dataframeTypes.contains("KEL Flags");
		}
		return false;
	}

	private String getDataframeTypeForAnalysisRequestChild(String filename, List<String> dataframeTypes) {
		return filenameToTypeMap.get(filename);
	}
	
	private String getEquipIdForAnalysisRequestChild(String filename, List<String> dataframeTypes, List<String> equipIds) {
		String returnValue = null;
		String dataframeTypeForFile = filenameToTypeMap.get(filename);
		for (int i = 0, n = dataframeTypes.size(); i < n; i++) {
			if (dataframeTypes.get(i).equalsIgnoreCase(dataframeTypeForFile)) {
				returnValue = equipIds.get(i);
				break;
			}
		}
		log.info(String.format("getEquipIdForAnalysisRequestChild(%s, .., ..) : dataframeTypeForFile %s returns %s", filename, dataframeTypeForFile, returnValue));
		return returnValue;
	}
	
	private String createChild(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			Map<String, TypedValue> parameters,
			List<ScriptItem> subScriptItems, 
			ComputeResponse computeResponse,
			byte[] childDatasetData, 
			Dataframe child)
			throws JAXBException, ServiceCallerException, IOException {
		DataframeServiceClient dfClient = getDataframeServiceClient();
		child = dfClient.addDataframe(system, user, requestHeaders, child);
		Dataset childDataset = new Dataset();
		childDataset.setData("");
		childDataset.setDataSize(childDatasetData.length);
		Metadatum paramMetadata = new Metadatum();
		paramMetadata.setKey("parameters");
		List<String> paramValues = new ArrayList<>();
		for (Map.Entry<String, TypedValue> entry : parameters.entrySet()) {
			TypedValue tv = entry.getValue();
			String paramValue = tv.getValue().replace("\"", "\\\"");
			paramValues.add(
					String.format(
							"{ \"key\" : \"%s\", \"type\" : \"%s\", \"value\" : \"%s\" }", 
							entry.getKey(), 
							tv.getType(), 
							paramValue));
		}
		// Add back in the subscript items.
		for (ScriptItem subScriptItem : subScriptItems) {
			paramValues.add(
					String.format(
							"{ \"key\" : \"%s\", \"type\" : \"%s\", \"value\" : \"%s\" }", 
							subScriptItem.getPath(), 
							"string", 
							"script"));
		}
		paramMetadata.setValue(paramValues);
		childDataset.getMetadata().add(paramMetadata);
		childDataset.setStdIn(computeResponse.getStdin());
		childDataset.setStdOut(computeResponse.getStdout());
		childDataset.setStdErr(computeResponse.getStdout());
		boolean isCsv = isCsv(childDatasetData);
		if (isCsv) {
			childDataset.setMimeType("text/csv");
		}
		// Add dataset to new child
		String datasetId = dfClient.addDataframeDataset(system, user, requestHeaders, child.getId(), childDataset);
		dfClient.addDataToDataframeDataset(system, user, requestHeaders, datasetId, "", childDatasetData);
		if (isCsv) {
			DataframeUtils.setDatasetFileMimeType(getModeshapeServiceClient(), datasetId, "text/csv");
		}
		return child.getId();
	}

	private static boolean isCsv(byte[] fileData) {
		if (isJson(fileData)) {
			return false;
		}
        try (Reader in = new InputStreamReader(new ByteArrayInputStream(fileData))) {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withIgnoreEmptyLines().parse(in);
            for (CSVRecord record : records) {
                if (record.size() < 2) {
                    return false;
                }
                if (!record.isConsistent()) {
                    return false;
                }
                if (record.get(0).startsWith("Missing Report File")) {
                	return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

	private static boolean isJson(byte[] fileData) {
        try (Reader in = new InputStreamReader(new ByteArrayInputStream(fileData))) {
        	int firstChar = in.read();
        	return ((firstChar == '{') || (firstChar == '['));
        } catch (Exception e) {
            return false;
        }
		
	}
	/**
	 * Retrieves the script for the passed scriptId value. If the script is in the script cache
	 * returns it from the cache, otherwise fetches the script from the library service and
	 * caches it.
	 * 
	 * @param serverUri
	 * @param user
	 * @param scriptId
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws JAXBException 
	 * @throws ComputeException 
	 */
	protected ScriptItem getScript(String serverUri, String system, String user, String scriptId) throws ServiceCallerException, IOException, ComputeException {
		if (scriptId == null) {
			return new ScriptItem();
		}
		String librarianUri = String.format(EQUIP_SERVICES_LIBRARIAN_PREFIX, system);
		String libraryServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_HOST, AppPropertyNames.LIBRARY_SERVICE_HOST_DEFAULT);
		int libraryServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_PORT, AppPropertyNames.LIBRARY_SERVICE_PORT_DEFAULT));
		LibraryServiceClient lsClient = new LibraryServiceClient(libraryServiceHost, libraryServicePort);
		lsClient.setUser(user);
		LibraryResponse la = lsClient.getScriptById(scriptId);
		if (la == null) {
			throw new ComputeException(String.format("Library service returned null for script id: %s", scriptId));
		}
		String reference =  String.format("%s%s/%s", serverUri, librarianUri, la.getArtifactPath());
		byte[] script = lsClient.getItemContent(scriptId);
		String path = la.getArtifactPath();
		String name = la.getProperties().getEquipName();
		return new ScriptItem(scriptId, name, reference, script, path);
	}

	protected List<ScriptItem> getSubScripts(
			String serverUri, 
			String system, 
			String user, 
			String[] scriptPaths, 
			boolean prepareInContainer) throws ServiceCallerException, IOException, ComputeException {
		String librarianUri = String.format(EQUIP_SERVICES_LIBRARIAN_PREFIX, system);
		String libraryServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_HOST, AppPropertyNames.LIBRARY_SERVICE_HOST_DEFAULT);
		int libraryServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_PORT, AppPropertyNames.LIBRARY_SERVICE_PORT_DEFAULT));
		LibraryServiceClient lsClient = new LibraryServiceClient(libraryServiceHost, libraryServicePort);
		lsClient.setUser(user);
		List<ScriptItem> returnValue = new ArrayList<>();
		LibraryResponse la = null;
		for (String scriptPath : scriptPaths) {
			// Sub scripts could be identified by either UUID or path
			if (UuidType.isUUID(scriptPath)) {
				la = lsClient.getScriptById(scriptPath);
			} else {
				File f = new File(scriptPath);
				String directory = f.getParent().replace("\\", "/");
				if (directory.startsWith("library/")) {
					directory = directory.substring(8);
				}
				String scriptName = f.getName();
				la = lsClient.getScriptByName(directory, scriptName);
			}
			if ((la == null) || (la.getArtifactId() == null))  {
				throw new ComputeException(String.format("Library service returned null for script: %s", scriptPath));
			}
			String reference =  String.format("%s%s/%s", serverUri, librarianUri, la.getArtifactPath());
			String scriptId = la.getArtifactId();
			String name = la.getProperties().getEquipName();
			byte[] script = null;
			if (!prepareInContainer) {
				script = lsClient.getItemContent(scriptId);
			}
			String path = la.getArtifactPath();
			if (path.startsWith("library/")) {
				path = path.substring(8);
			}
			returnValue.add(new ScriptItem(scriptId, name, reference, script, path));
		}
		return returnValue;
	}
	

	protected List<ScriptItem> getSubScripts(
			String serverUri, 
			String system, 
			String user,
			List<Entry<String, TypedValue>> subScriptParams, 
			boolean prepareInContainer) throws ServiceCallerException, IOException, ComputeException {
		List<String> subScriptPaths = new ArrayList<>();
		for (Entry<String, TypedValue> subScriptPath : subScriptParams) {
			subScriptPaths.add(subScriptPath.getKey());
		}
		
		return getSubScripts(
				serverUri, 
				system, 
				user, 
				subScriptPaths.toArray(new String[subScriptPaths.size()]), 
				prepareInContainer);
	}

	/**
	 * Retrieves a list of Assembly from the Dataframe Service from a list of Assembly IDs
	 * 
	 * @param serverUri
	 * @param user
	 * @param assemblyIds
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private List<Assembly> getAssemblies(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			List<String> assemblyIds) throws ServiceCallerException, IOException, JAXBException {
		String assemblyListJson = new Gson().toJson(assemblyIds);
		DataframeServiceClient dfClient = getDataframeServiceClient();
		return dfClient.getAssemblies(system, user, requestHeaders, assemblyListJson);
	}
	
	/**
	 * Retrieves a list of Dataframes from the Dataframe Service from a list of Dataframe IDs
	 * 
	 * @param serverUri
	 * @param user
	 * @param dataframeIds
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws JAXBException
	 */
	protected List<Dataframe> getDataFrames(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			List<String> dataframeIds) throws JAXBException, ServiceCallerException, IOException {
		List<Dataframe> returnValue = new ArrayList<>();
		if ((dataframeIds != null) && !dataframeIds.isEmpty()) {
			String dataframesListJson = new Gson().toJson(dataframeIds);
			DataframeServiceClient dfClient = getDataframeServiceClient();
			returnValue = dfClient.getDataframes(system, user, requestHeaders, dataframesListJson);
		}
		return returnValue;
	}
	
	/**
	 * Retrieves Dataframes in List<Byte[]> format from the passed in list of Dataframe IDs
	 * 
	 * 
	 * @param serverUri
	 * @param user
	 * @param dataframeIds
	 * @return
	 * @throws JAXBException
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws ComputeException 
	 */
	protected Map<String, byte[]> getDataframeDatasets(
			String system,
			String user, 
			Map<String, String> requestHeaders,
			Map<String, TypedValue> parameters, 
			List<Dataframe> dataframes) throws JAXBException, ServiceCallerException, IOException, ComputeException {
		Map<String, byte[]> returnValue = new LinkedHashMap<>();
		if ((dataframes != null) && !dataframes.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("getDataframeDatasets() - Request Headers:\n");
			for (Map.Entry<String,String> entry : requestHeaders.entrySet()) {
				sb.append(String.format("%s : %s%n", entry.getKey(), entry.getValue()));
			}
			log.info(sb.toString());
			DataframeServiceClient dfClient = getDataframeServiceClient();
			int count = 1;
			for (Dataframe dataframe : dataframes) {
				Dataset ds = dfClient.getDataframeDataset(system, user, requestHeaders, dataframe.getId());
				if (ds.getComplexDataId() == null) {
					throw new ComputeException(String.format("Dataframe: %s has a null value for the complex data id.", dataframe.getId()));
				}
				String datasetName;
				datasetName = "INPUT" + count;
				if (parameters.containsKey(dataframe.getId())) {
					// The filename for this dataset has been defined as a parameter
					// entry in the parameters map where the key is the dataframe uuid
					// and the value is the filename to use.
					parameters.put(datasetName, parameters.get(dataframe.getId()));
				}
				count += 1;
				returnValue.put(datasetName, dfClient.getDataFrameData(system, user, requestHeaders, ds.getComplexDataId()));
			}
		}
		return returnValue;
	}

	protected Map<String, DataframeDataset> getDataframeDatasetsParallel(
			String system,
			String user, 
			Map<String, String> requestHeaders,
			Map<String, TypedValue> parameters, 
			List<Dataframe> dataframes) throws ComputeException {
		Map<String, DataframeDataset> returnValue = new LinkedHashMap<>();

	    if ((dataframes != null) && !dataframes.isEmpty()) {
			ExecutorService executor = Executors.newFixedThreadPool(Math.min(dataframes.size(), 10));
			try {
			    CompletionService<DataframeDataset> ecs = new ExecutorCompletionService<>(executor);
			    List<Future<DataframeDataset>> futures = new ArrayList<>(dataframes.size());
				
				int count = 1;
				
				for (Dataframe dataframe : dataframes) {
					String dfId = dataframe.getId();
					String datasetName = "INPUT" + count;
					
					futures.add(ecs.submit(new Callable<DataframeDataset>() {
	
						@Override
						public DataframeDataset call() throws Exception {
							return getDataframeDatasetData(
									system, 
									user,
									requestHeaders,
									parameters, 
									datasetName, 
									dataframe);
						}
	
					}));
					count += 1;
				}
	
				for (Future<DataframeDataset> future : futures) {
			    	try {
			    		DataframeDataset dfDataset;
			    		boolean interrupted = false;
			    		do {
							try {
								interrupted = false;
								dfDataset = future.get();
					    		returnValue.put(dfDataset.name, dfDataset);
							} catch (InterruptedException e) {
								interrupted = true;
							}
			    		} while(interrupted);
			    	} catch (ExecutionException ex) {
						throw new ComputeException(ex);
					}
			    }
			} finally {
				executor.shutdown();
			}
		}
		return returnValue;
	}

	protected Map<String, DataframeDataset> getDataframeDatasetsForInContainer(String system, String user,
			Map<String, TypedValue> parameters, List<Dataframe> dataframes) {
		Map<String, DataframeDataset> returnValue = new LinkedHashMap<>();
		int count = 1;
		for (Dataframe dataframe : dataframes) {
			List<String> profileConfig = dataframe.getProfileConfig();
			if (profileConfig == null || profileConfig.isEmpty()) {
				for (Metadatum metadata : dataframe.getMetadata()) {
					if (metadata.getKey().equalsIgnoreCase("Profile Configuration")
							&& !metadata.getValue().isEmpty())
					{
						String[] parts = metadata.getValue().get(0).split(",");
						profileConfig = Arrays.asList(parts);
						// profileConfig = metadata.getValue();
						break;
					}
				}
			}
			if ((profileConfig != null) && !profileConfig.isEmpty()) {
				log.info(String.format("%s - profileConfig: %s", dataframe.getId(), profileConfig));
			}
			String dataframeId = dataframe.getId();
			String datasetName = "INPUT" + count;
			DataframeDataset dfds = new DataframeDataset();
			dfds.dataframeId = dataframeId;
			dfds.name = datasetName;
			if (parameters.containsKey(dataframeId)) {
				// The filename for this dataset has been defined as a parameter
				// entry in the parameters map where the key is the dataframe uuid
				// and the value is the filename to use.
				parameters.put(datasetName, parameters.get(dataframeId));
			}
			if ((profileConfig != null) && !profileConfig.isEmpty()) {
				dfds.profileConfig = profileConfig;
			}
			returnValue.put(datasetName, dfds);
			count += 1;
		}
		return returnValue;
	}

	
	private DataframeDataset getDataframeDatasetData(
			String system, 
			String user,
			Map<String, String> requestHeaders,
			Map<String, TypedValue> parameters, 
			String datasetName, 
			Dataframe dataframe)
			throws JAXBException, ServiceCallerException, IOException, ComputeException {
		StringBuilder sb = new StringBuilder();
		sb.append("getDataframeDatasetData() - Request Headers:\n");
		for (Map.Entry<String,String> entry : requestHeaders.entrySet()) {
			sb.append(String.format("%s : %s%n", entry.getKey(), entry.getValue()));
		}
		log.info(sb.toString());
		DataframeServiceClient dfClient = getDataframeServiceClient();
		List<String> profileConfig = dataframe.getProfileConfig();
		if (profileConfig == null || profileConfig.isEmpty()) {
			for (Metadatum metadata : dataframe.getMetadata()) {
				if (metadata.getKey().equalsIgnoreCase("Profile Configuration")
						&& !metadata.getValue().isEmpty())
				{
					String[] parts = metadata.getValue().get(0).split(",");
					profileConfig = Arrays.asList(parts);
					// profileConfig = metadata.getValue();
					break;
				}
			}
		}
		String dataframeId = dataframe.getId();
		Dataset ds = dfClient.getDataframeDataset(system, user, requestHeaders, dataframeId);
		if (ds.getComplexDataId() == null) {
			throw new ComputeException(String.format("Dataframe: %s has a null value for the complex data id.", dataframeId));
		}
		if (parameters.containsKey(dataframeId)) {
			// The filename for this dataset has been defined as a parameter
			// entry in the parameters map where the key is the dataframe uuid
			// and the value is the filename to use.
			parameters.put(datasetName, parameters.get(dataframeId));
		}

		DataframeDataset returnValue = new DataframeDataset();
		
		returnValue.name = datasetName;
		returnValue.datasetData = dfClient.getDataFrameData(system, user, requestHeaders, ds.getComplexDataId());
		if (profileConfig != null) {
			returnValue.profileConfig = profileConfig;
		}
		return returnValue;
	}
	
	private DataframeServiceClient getDataframeServiceClient() {
		String dataframeServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.DATAFRAME_SERVICE_HOST, AppPropertyNames.DATAFRAME_SERVICE_HOST_DEFAULT);
		int dataframeServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.DATAFRAME_SERVICE_PORT, AppPropertyNames.DATAFRAME_SERVICE_PORT_DEFAULT));
		String dataframeUri = String.format("http://%s:%s", dataframeServiceHost, dataframeServicePort);
		return new DataframeServiceClient(dataframeUri + EQUIP_DATAFRAME_SERVICE);
	}

	private ModeshapeClient getModeshapeServiceClient() throws ServiceCallerException {
		String serviceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_SERVICE_HOST, AppPropertyNames.MODESHAPE_SERVICE_HOST_DEFAULT).trim();
		String serviceUsername = Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_USERNAME, AppPropertyNames.MODESHAPE_USERNAME_DEFAULT).trim();
		String serviceAuth = Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_AUTH, AppPropertyNames.MODESHAPE_AUTH_DEFAULT).trim();
		return new ModeshapeClient(serviceHost, serviceUsername, serviceAuth);
	}

	private List<String> getStudyIds(List<Assembly> parentAssemblies, List<Dataframe> parentDataframes) {
		Set<String> unique = new HashSet<>();
		for (Dataframe parent : parentDataframes) {
			if (parent.getStudyIds() != null) {
				unique.addAll(parent.getStudyIds());
			}
		}
		for (Assembly assembly : parentAssemblies) {
			if (assembly.getStudyIds() != null) {
				unique.addAll(assembly.getStudyIds());
			}
		}
		List<String> returnValue = new ArrayList<>(unique);
		Collections.sort(returnValue);
		return returnValue;
	}

	private List<String> getProtocolIds(List<Assembly> parentAssemblies, List<Dataframe> parentDataframes) {
		Set<String> unique = new HashSet<>();
		for (Dataframe parent : parentDataframes) {
			if (parent.getProtocolIds() != null) {
				unique.addAll(parent.getProtocolIds());
			}
		}
		for (Assembly assembly : parentAssemblies) {
			if (assembly.getProtocolIds() != null) {
				unique.addAll(assembly.getProtocolIds());
			}
		}
		List<String> returnValue = new ArrayList<>(unique);
		Collections.sort(returnValue);
		return returnValue;
	}
	
	private List<String> getProjectIds(List<Assembly> parentAssemblies, List<Dataframe> parentDataframes) {
		Set<String> unique = new HashSet<>();
		for (Dataframe parent : parentDataframes) {
			if (parent.getProjectIds() != null) {
				unique.addAll(parent.getProjectIds());
			}
		}
		for (Assembly assembly : parentAssemblies) {
			if (assembly.getProjectIds() != null) {
				unique.addAll(assembly.getProjectIds());
			}
		}
		List<String> returnValue = new ArrayList<>(unique);
		Collections.sort(returnValue);
		return returnValue;
	}
	
	private List<String> getProgramIds(List<Assembly> parentAssemblies, List<Dataframe> parentDataframes) {
		Set<String> unique = new HashSet<>();
		for (Dataframe parent : parentDataframes) {
			if (parent.getProgramIds() != null) {
				unique.addAll(parent.getProgramIds());
			}
		}
		for (Assembly assembly : parentAssemblies) {
			if (assembly.getProgramIds() != null) {
				unique.addAll(assembly.getProjectIds());
			}
		}
		List<String> returnValue = new ArrayList<>(unique);
		Collections.sort(returnValue);
		return returnValue;
	}
	
	private String getPromotionStatus(List<Dataframe> parentDataframes) {
		List<Dataframe> pdfs = new ArrayList<>(parentDataframes);
		Predicate<Dataframe> pred = p-> p.getPromotionStatus() == null || p.getPromotionStatus().isEmpty();
		pdfs.removeIf(pred);
		String returnValue = null;
		for (Dataframe parent : pdfs) {
			if (returnValue == null) {
				returnValue = parent.getPromotionStatus();
			} else {
				if (!returnValue.equals(parent.getPromotionStatus())) {
					returnValue = "Pending Review";
					break;
				}
			}
		}
		if (returnValue == null) {
			returnValue = "Pending Review";
		}
		return returnValue;
	}
	
	private String getDataBlindingStatus(List<Dataframe> parentDataframes) {
		List<Dataframe> pdfs = new ArrayList<>(parentDataframes);
		Predicate<Dataframe> pred = p-> p.getDataBlindingStatus() == null || p.getDataBlindingStatus().isEmpty();
		pdfs.removeIf(pred);
		String returnValue = "Blinded";
		for (Dataframe parent : pdfs) {
			if (!returnValue.equals(parent.getDataBlindingStatus())) {
				returnValue = "Unblinded";
				break;
			}
		}		
		return returnValue;
	}
	
	private String getQcStatus(List<Dataframe> parentDataframes) {
		List<Dataframe> pdfs = new ArrayList<>(parentDataframes);
		Predicate<Dataframe> pred = p-> p.getQcStatus() == null || p.getQcStatus().isEmpty();
		pdfs.removeIf(pred);
		String returnValue = pdfs.isEmpty() ? "Not QC'd" : "QC'd";
		for (Dataframe parent : pdfs) {
			if (!returnValue.equals(parent.getQcStatus())) {
				returnValue = "Not QC'd";
				break;
			}
		}		
		return returnValue;
	}
	
	private String getRestrictionStatus(List<Dataframe> parentDataframes) {
		List<Dataframe> pdfs = new ArrayList<>(parentDataframes);
		Predicate<Dataframe> pred = p-> p.getRestrictionStatus() == null || p.getRestrictionStatus().isEmpty();
		pdfs.removeIf(pred);
		String returnValue = "Not Restricted";
		for (Dataframe parent : pdfs) {
			if (!returnValue.equals(parent.getRestrictionStatus())) {
				returnValue = "Restricted";
				break;
			}
		}		
		return returnValue;
	}
	
	private String getDataStatus(List<Dataframe> parentDataframes) {
		List<Dataframe> pdfs = new ArrayList<>(parentDataframes);
		Predicate<Dataframe> pred = p-> p.getDataStatus() == null || p.getDataStatus().isEmpty();
		pdfs.removeIf(pred);
		String returnValue = null;
		for (Dataframe parent : pdfs) {
			if (returnValue == null) {
				returnValue = parent.getDataStatus();
			} else {
				if (!returnValue.equals(parent.getDataStatus())) {
					returnValue = "Draft";
					break;
				}
			}
		}
		if (returnValue == null) {
			returnValue = "Draft";
		}
		return returnValue;
	}
	
	private void makeAuditEntry(RequestBody rb, Map<String, String> requestHeaders, ComputeResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful) {
		Runnable task = () -> { 
			try {
				makeAuditEntryTask(rb, requestHeaders, computeResponse, user, isVirtual, isSuccessful);
			} catch (UnknownHostException | ServiceCallerException ex) {
				log.error("Error making audit entry", ex);
			}
		};
		auditExecutor.execute(task);
	}
	
	private void makeAuditEntryTask(RequestBody rb, Map<String, String> requestHeaders, ComputeResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful)
			throws UnknownHostException, ServiceCallerException {
		String action = "";
		List<String> entityIds = new ArrayList<>();
		if(isVirtual) {
			action = isSuccessful ? "Script Executed (virtual)" : "Script Not Executed (virtual)";
			if(!rb.getDataframes().isEmpty()) {
				entityIds = rb.getDataframes();
			}
		} else {
			action = isSuccessful ? "Script Executed, systematic edit" : "Script Not Executed"; 
			if (computeResponse != null) {
				entityIds = computeResponse.getDataframes();
			}
		}
		String entityType = !isVirtual ? rb.getDataframeType().get(0) : null;
		if (((entityType == null) || entityType.isEmpty()) && isVirtual) {
			entityType = VIRTUAL_COMPUTATION;
		}
		String userId = requestHeaders.containsKey(EQUIP_ACTUALUSERID) ? requestHeaders.get(EQUIP_ACTUALUSERID) : user;
		String actionStatus = isSuccessful ? "SUCCESS" : "FAILURE";
		String scriptId = rb.getScriptId();
		String contextEntityId = "";
		String hostName = InetAddress.getLocalHost().getHostName();
		String operatingSystem = String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version"));
		String executionEngineName = rb.getComputeContainer();
		String executionEngineVersion = "1";
		String runtimeEnvironment = "NA";
		String runtimePath = "NA";
		
		makeAuditEntries(entityIds, user, action, entityType, userId, actionStatus, scriptId, 
				contextEntityId, hostName, operatingSystem, executionEngineName, executionEngineVersion,
				runtimeEnvironment, runtimePath);
		
	}

	private void makeAuditEntryForDatasetAccess(RequestBody rb, Map<String, String> requestHeaders, ComputeResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful) {
		Runnable task = () -> { 
			try {
				makeAuditEntryForDatasetAccessTask(rb, requestHeaders, computeResponse, user, isVirtual, isSuccessful);
			} catch (UnknownHostException | ServiceCallerException ex) {
				log.error("Error making audit entry", ex);
			}
		};
		auditExecutor.execute(task);
	}
	
	private void makeAuditEntryForDatasetAccessTask(RequestBody rb, Map<String, String> requestHeaders, ComputeResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful)
			throws UnknownHostException, ServiceCallerException {
		
		String action = "Dataframe was accessed via complexDataId";
		List<String> entityIds = rb.getDataframes();
		String entityType = (rb.getDataframeType() != null) && !rb.getDataframeType().isEmpty() ? rb.getDataframeType().get(0) : "Data Transformation";
		String userId = requestHeaders.containsKey(EQUIP_ACTUALUSERID) ? requestHeaders.get(EQUIP_ACTUALUSERID) : user;
		String actionStatus = isSuccessful ? "SUCCESS" : "FAILURE";
		String scriptId = null;
		String contextEntityId = "";
		String hostName = InetAddress.getLocalHost().getHostName();
		String operatingSystem = String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version"));
		String executionEngineName = null;
		String executionEngineVersion = null;
		String runtimeEnvironment = null;
		String runtimePath = null;
		if(rb.getDataframeEntities() != null && !rb.getDataframeEntities().isEmpty()) {
			logAuditEntries(rb.getDataframeEntities(), user, action, entityType, userId, actionStatus, scriptId, contextEntityId, hostName, operatingSystem, executionEngineName, executionEngineVersion, runtimeEnvironment, runtimePath);
		}else {
			makeAuditEntries(entityIds, user, action, entityType, userId, actionStatus, scriptId, 
					contextEntityId, hostName, operatingSystem, executionEngineName, executionEngineVersion,
					runtimeEnvironment, runtimePath);																		
		}
	}
	protected void logAuditEntries(List<Dataframe> dataframes, String user, String action, String entityType, String userId, String actionStatus,
			String scriptId, String contextEntityId, String hostName, String operatingSystem,
			String executionEngineName, String executionEngineVersion, String runtimeEnvironment, String runtimePath) throws ServiceCallerException {
		String auditServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.AUDIT_SERVICE_HOST, AppPropertyNames.AUDIT_SERVICE_HOST_DEFAULT);
		int auditServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.AUDIT_SERVICE_PORT, AppPropertyNames.AUDIT_SERVICE_PORT_DEFAULT));
		AuditServiceClient asc = new AuditServiceClient(auditServiceHost, auditServicePort);
		asc.setUser(userId);
		for(Dataframe df : dataframes) {
			AuditDetails details = asc.new AuditDetails(action, df, user);
			
			asc.logAuditEntryAsync(details);
		}
	}
	protected void makeAuditEntries(List<String> entityIds, String user, String action, String entityType, String userId, String actionStatus,
			String scriptId, String contextEntityId, String hostName, String operatingSystem,
			String executionEngineName, String executionEngineVersion, String runtimeEnvironment, String runtimePath) throws ServiceCallerException {
	
		String auditServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.AUDIT_SERVICE_HOST, AppPropertyNames.AUDIT_SERVICE_HOST_DEFAULT);
		int auditServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.AUDIT_SERVICE_PORT, AppPropertyNames.AUDIT_SERVICE_PORT_DEFAULT));
		AuditServiceClient asc = new AuditServiceClient(auditServiceHost, auditServicePort);

		log.info("user: " + user + " userId: " + userId);
		
		List<String> equipIds = new ArrayList<>();
		if (!entityIds.isEmpty()) {
			equipIds = DataframeUtils.getEquipIdsForNodes(this.getModeshapeServiceClient(), entityIds);
		}
		if (equipIds.isEmpty()) {
			equipIds.add("NA");
		}
		
		for (String equipId : equipIds) {
			AuditEntryRequestBody aerb = new AuditEntryRequestBody();
			aerb.setAction(action);
			aerb.setEntityId(equipId);
			aerb.setEntityType(entityType);
			aerb.setUserId(userId);
			aerb.setActionStatus(actionStatus);
			aerb.setScriptId(scriptId);
			aerb.setContextEntityId(contextEntityId);
			aerb.setHostname(hostName);
			aerb.setOperatingSystem(operatingSystem);
			aerb.setExecutionEngineName(executionEngineName);
			aerb.setExecutionEngineVersion(executionEngineVersion);
			aerb.setRuntimeEnvironment(runtimeEnvironment);
			aerb.setRuntimePath(runtimePath);
			
			try {
				log.info("user: " + user);		// Remove when no longer needed
				log.info("aerb.getUserId(): " + aerb.getUserId());	// Remove when no longer needed
				asc.addAuditEntry(user, aerb);
			} catch(ServiceCallerException ex) {
				throw new ServiceCallerException("Audit Service", ex.getStatusCode(), ex.getCause());
			}
		}
	}

}
