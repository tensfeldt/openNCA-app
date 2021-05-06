package com.pfizer.equip.computeservice.dao;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.computeservice.containers.ContainerRunner;
import com.pfizer.equip.computeservice.dto.ComputeLaunchResponse;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.DataframeDataset;
import com.pfizer.equip.computeservice.dto.Executor;
import com.pfizer.equip.computeservice.dto.RequestBody;
import com.pfizer.equip.computeservice.exception.ComputeDataAccessException;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.equip.utils.TypedValue;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;

import spark.Spark;

public class ComputeLaunchDao extends ComputeDAOImpl {
	private static Logger log = LoggerFactory.getLogger(ComputeLaunchDao.class);	
	private ExecutorService auditExecutor = Executors.newSingleThreadExecutor();

	public ComputeLaunchResponse doLaunch(
			String serverUri, 
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			RequestBody rb) throws ComputeException, InterruptedException, ServiceCallerException {
		boolean isVirtual = false;
		log.info("isVirtual: " + isVirtual);
		String container = rb.getComputeContainer();		
//		boolean prepareInContainer = !isVirtual
//		&& (container.equalsIgnoreCase("equip-r-base")
//				|| container.equalsIgnoreCase("equip-opennca")
//				|| container.equalsIgnoreCase("equip-rmarkdown"));
		boolean prepareInContainer = !isVirtual;
		boolean useBatch = prepareInContainer && !rb.isDontBatch();

		try {
			Map<String, TypedValue> parameters = rb.getParameters() != null ? rb.getParameters() : new HashMap<>();

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
			
			ScriptItem scriptItem = getScript(serverUri, system, rb.getUser(), rb.getScriptId());
			Script scriptNode = createScriptNode(rb, scriptItem);

			// Get the scripts for the Script parameters
			List<ScriptItem> subScriptItems = getSubScripts(serverUri, system, rb.getUser(), subScriptParams, prepareInContainer);
			
			List<Dataframe> parentDataframes = getDataFrames(system, user, requestHeaders, rb.getDataframes());
			if (!parentDataframes.isEmpty()) {
				String dataframeType = ((rb.getDataframeType() != null) && !rb.getDataframeType().isEmpty()) ? rb.getDataframeType().get(0) : parentDataframes.get(0).getDataframeType();
				if (dataframeType == null) {
					dataframeType = "Data Transformation";
					List<String> dataframeTypes = new ArrayList<>();
					dataframeTypes.add(dataframeType);
					rb.setDataframeType(dataframeTypes);
				}
				boolean userHasPrivileges = privilegeCheck(system, requestHeaders, dataframeType, user, scriptItem);
				if(!userHasPrivileges) {
					Spark.halt(403, "User doesn't have required privilege to perform this operation.");
				}
			}
			
			// Create container and execute computation
			ComputeLaunchResponse returnValue = doLaunch(
					system,
					user,
					requestHeaders,
					rb.getComputeContainer(),
					scriptItem.getName(),
					scriptItem.getId(),
					scriptItem.getScript(), 
					subScriptItems,
					parameters, 
					parentDataframes,
					prepareInContainer,
					useBatch);
			returnValue.setRequestHeaders(requestHeaders);
			returnValue.setParameters(parameters);
			returnValue.setAssemblies(rb.getAssemblies());
			returnValue.setDataframes(rb.getDataframes());
			returnValue.setParentDataframes(parentDataframes);
			returnValue.setDataframeType(rb.getDataframeType());
			returnValue.setSubType(rb.getSubType());
			returnValue.setEquipId(rb.getEquipId());
			returnValue.setScriptId(scriptItem.getId());
			returnValue.setScriptName(scriptItem.getName());
			returnValue.setScript(scriptItem.getScript());
			returnValue.setScriptNode(scriptNode);
			returnValue.setUseBatch(useBatch);
			returnValue.setBatch(rb.isBatch());
			makeAuditEntry(rb, requestHeaders, returnValue, user, isVirtual, true);
			return returnValue;
		} catch(ServiceCallerException ex) {
			throw new ComputeException(ex.getMessage(), ex);
		} catch(IOException | JAXBException | GeneralSecurityException | SQLException | ComputeDataAccessException | NamingException ex) {
			makeAuditEntry(rb, requestHeaders, null, user, isVirtual, false);
			throw new ComputeException(ex);
		}	
	}
	
	public ComputeResponse stopLaunched(String serverUri, String system, String user, String id, boolean save) throws IOException, GeneralSecurityException, InterruptedException, ExecutionException, ServiceCallerException, JAXBException {
		log.info(String.format("stopLaunched(%s, %s, %s, %s, %s)", serverUri, system, user, id, save));
		boolean isVirtual = false;
		ContainerRunner runner = new ContainerRunner();
		ComputeLaunchResponse clr = runner.getComputeLaunchResponseForId(id);
		ComputeResponse returnValue = runner.stopLaunched(id, clr.getCommand(), save);
		if (!isVirtual) {
			
			if (save) {
				if (clr.isPrepareInContainer()) {
					returnValue = createChildDataFramesInContainer(
							clr.isUseBatch(),
							clr.isBatch(),
							clr.isRstudio(),
							system,
							user,
							clr.getRequestHeaders(),
							clr.getDataframeType(),
							clr.getSubType(),
							clr.getEquipId(), 
							clr.getAssemblies(), 
							clr.getParentDataframes(),
							clr.getScriptId(),
							clr.getScriptNode(), 
							clr.getParameters(),
							clr.getSubScriptItems(),
							returnValue);
				} else {
					returnValue = createChildDataFramesParallel(
							system,
							user,
							clr.getRequestHeaders(),
							clr.getDataframeType(),
							clr.getSubType(),
							clr.getEquipId(), 
							clr.getAssemblies(), 
							clr.getParentDataframes(), 
							clr.getScriptNode(), 
							clr.getParameters(),
							clr.getSubScriptItems(),
							returnValue);
				}
			}
			// No need to include the child datasets
			// if this is not a virtual call.
			returnValue.setDatasetData(null);
			returnValue.setChildDatasets(null);	
		}
		log.info("shutting down container id: " + id);
		runner.shutdownContainer(id, user, clr.getPort());
		return returnValue;
	}
	
	private ComputeLaunchResponse doLaunch(
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
			boolean prepareInContainer,
			boolean useBatch) throws IOException, GeneralSecurityException, JAXBException, ServiceCallerException, ComputeException, SQLException, ComputeDataAccessException, InterruptedException, NamingException {
		ComputeDatabaseDAO csdao = new ComputeDatabaseDAO();
		List<Executor> executors = csdao.getExecutionFromName(image);
		if (executors.isEmpty()) {
			throw new ComputeException(String.format("No engine found for %s", image));
		}
		Map<String, DataframeDataset> parentDatasets = new HashMap<>();
		if (prepareInContainer) {
			parentDatasets = getDataframeDatasetsForInContainer(system, user, parameters, parentDataframes);
		} else {
			parentDatasets = getDataframeDatasetsParallel(system, user, requestHeaders, parameters, parentDataframes);
		}
		ContainerRunner runner = new ContainerRunner();
		Executor executor = executors.get(0);
		return runner.launch(
				user, 
				executor, 
				parentDataframes, 
				parentDatasets,
				scriptName,
				scriptId, 
				script, 
				subScriptItems, 
				parameters, 
				prepareInContainer,
				useBatch);
	}

	private void makeAuditEntry(RequestBody rb, Map<String, String> requestHeaders, ComputeLaunchResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful) {
		Runnable task = () -> { 
			try {
				makeAuditEntryTask(rb, requestHeaders, computeResponse, user, isVirtual, isSuccessful);
			} catch (UnknownHostException | ServiceCallerException ex) {
				log.error("Error making audit entry", ex);
			}
		};
		auditExecutor.execute(task);
	}
	
	private void makeAuditEntryTask(RequestBody rb, Map<String, String> requestHeaders, ComputeLaunchResponse computeResponse, String user, boolean isVirtual, boolean isSuccessful)
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
		String entityType = "Visualization";
		String userId = requestHeaders.containsKey("EQUIP_ACTUALUSERID") ? requestHeaders.get("EQUIP_ACTUALUSERID") : user;
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

}
