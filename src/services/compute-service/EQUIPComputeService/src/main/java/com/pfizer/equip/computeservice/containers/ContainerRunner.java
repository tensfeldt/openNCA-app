package com.pfizer.equip.computeservice.containers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.pfizer.equip.computeservice.AppPropertyNames;
import com.pfizer.equip.computeservice.Application;
import com.pfizer.equip.computeservice.dto.ComputeLaunchResponse;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.ContainerInput;
import com.pfizer.equip.computeservice.dto.CreatedDatasetInfo;
import com.pfizer.equip.computeservice.dto.CreatedDatasets;
import com.pfizer.equip.computeservice.dto.DataframeDataset;
import com.pfizer.equip.computeservice.dto.Executor;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.equip.utils.TypedValue;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.services.client.MultipartUtility;
import com.wha.docker.client.DockerClient;
import com.wha.docker.client.DockerClientImpl;
import com.wha.docker.engine.EngineResponse;

public class ContainerRunner {
	private static final String RSTUDIO_PREAMBLE_END = "## ^^^^^^^^^^^^^^^^^^ Do not delete or alter the lines above ^^^^^^^^^^^^^^^^^^\n";
	private static Logger log = LoggerFactory.getLogger(ContainerRunner.class);	
	private static final String DOCKER_INPUT_FOLDER = "/home/docker/input";
	private static final String DOCKER_OUTPUT_FOLDER = "/home/docker/output";
	private static final String DOCKER_SCRIPT_FOLDER = "/home/docker/script";
	private static final String SUCCESS_STATUS = "Success";
	private static final int SHINY_RETRY_SLEEP = 250;
	private static final int SHINY_RETRIES = 50;
	private static final int MAX_PARAMETER_SIZE = 256;
	private static ContainerCloser closer;
	private static String UUID_REGEX = "^[0-9,a-f]{8}-[0-9,a-f]{4}-[0-9,a-f]{4}-[0-9,a-f]{4}-[0-9,a-f]{12}";
	private static ExecutorService launchService = Executors.newCachedThreadPool();
	private static Map<String, ComputeLaunchResponse> launchMap = new ConcurrentHashMap<>();
	private static LinkedBlockingQueue<Integer> portFifo = new LinkedBlockingQueue<>();
	private static int MIN_PORT = 9000;
	private static int MAX_PORT = 9500;
	

	static {
		try {
			closer = new ContainerCloser();
			closer.start();
			for (int i = MIN_PORT, n = MAX_PORT; i < n; i++) {
				portFifo.add(i);
			}
		} catch (MalformedURLException e) {
			log.error("", e);
		}
	}
	
	public ComputeResponse run(
			String userId,
			Executor executor, 
			Map<String, DataframeDataset> datasets,
			String scriptName,
			String scriptId,
			byte[] script,
			List<ScriptItem> subScriptItems,
			Map<String, TypedValue> parameters,
			boolean prepareInContainer) throws IOException, GeneralSecurityException {
		DockerClient dockerClient = new DockerClientImpl();
		Boolean privileged = true;
		String id = createContainer(dockerClient, executor.getRepositoryUrl(), null, null, privileged);
		String command = executor.getCommand();
		// dockerClient.startContainer(id);
		return executeContainer(userId, dockerClient, id, datasets, scriptName, scriptId, script, subScriptItems, command, parameters, prepareInContainer);
	}
	
	public ComputeLaunchResponse launch(
			String userId,
			Executor executor, 
			List<Dataframe> parentDataframes,
			Map<String, DataframeDataset> datasets,
			String scriptName,
			String scriptId,
			byte[] script, 
			List<ScriptItem> subScriptItems,
			Map<String, TypedValue> parameters,
			boolean prepareInContainer,
			boolean useBatch) throws IOException, GeneralSecurityException, InterruptedException, ComputeException {
		DockerClient dockerClient = new DockerClientImpl();
		String command = executor.getCommand();
		String internalPort = "8888/tcp";
		Boolean privileged = true;
		String image = executor.getRepositoryUrl();
		if (image.contains("equip-rstudio")) {
			internalPort = "8787/tcp";	// TODO take a look at this
		}
		int port = portFifo.take();
		String id = createContainer(dockerClient, image, internalPort, port, privileged);
		// dockerClient.startContainer(id);
		ComputeLaunchResponse returnValue = null;
		try {
			returnValue = launchContainer(
					dockerClient,
					userId,
					id,
					parentDataframes,
					datasets,
					scriptName,
					scriptId,
					script,
					subScriptItems,
					command, 
					parameters,
					port,
					prepareInContainer,
					useBatch);
			returnValue.setRstudio(command == null);
		} catch(IOException | GeneralSecurityException ex) {
			portFifo.add(port);
			throw ex;
		}
		String launchUri = getLaunchUri(returnValue.getPort());
		String msg = String.format("launchUri: %s", launchUri);
		log.info(msg);
		returnValue.setUrl(launchUri);
		launchMap.put(id, returnValue);
		
		testLaunchUri(launchUri);
		
		return returnValue;
	}
	
	public ComputeResponse stopLaunched(String id, String command, boolean save) throws IOException, GeneralSecurityException, InterruptedException, ExecutionException {
		log.info(String.format("stopLaunched(%s, %s, %s)", id, command, save));
		ComputeLaunchResponse clr = getComputeLaunchResponseForId(id);
		Future<ContainerResponse> fcr = clr.getContainerResponseFuture();
		
		DockerClient dockerClient = new DockerClientImpl();
		// EngineResponse er = dockerClient.pause(id);
		if ((command != null) && !command.isEmpty() && command.contains("Rscript")) {
			log.info("killall");
			List<String> cmds = new ArrayList<>();
			cmds.add("killall");
			cmds.add("R");
			try {
				dockerClient.exec(id, cmds);
			} catch(IOException | GeneralSecurityException ex) {
				log.error("Error killall", ex);
			}
		}
		ContainerResponse r = fcr.get();
		log.info("after fcr.get()");
		ComputeResponse returnValue = new ComputeResponse();
		returnValue.setContainerId(id);
		returnValue.setStarted(clr.getStarted());
		if (r != null) {
			if ((r.getStatus() != null) && r.getStatus().isSuccess()) {
				returnValue.setStatus(SUCCESS_STATUS);
			}
			returnValue.setStdin(r.getStdin());
			returnValue.setStdout(getStdout(r));
		} else {
			// Rstudio doesn't actually complete.
			returnValue.setStatus(SUCCESS_STATUS);
			returnValue.setStdin("");
			returnValue.setStdout("");
		}
		
		if (save && !clr.isPrepareInContainer()) {
			if (clr.isRstudio()) {
				// Get script from container and see if we need to update the library
				ScriptItem scriptItem = extractScript(dockerClient, id, clr.getScriptName());
				byte[] scriptFromContainer = exciseRstudioRunHeader(scriptItem.getScript());
				byte[] originalScript = clr.getScript();
				if (this.scriptsDiffer(originalScript, scriptFromContainer)) {
					createNewHiddenArtifact(
							clr.getUserId(),
							scriptItem,
							clr.getScriptNode(),
							scriptFromContainer);
					clr.setScript(scriptFromContainer);
					clr.setScriptId(scriptItem.getId());
					clr.setScriptName(scriptItem.getName());
				}
			}
			// Get created datasets
			List<CreatedDatasetInfo> childDatasets = extractCreatedDatasets(dockerClient, id);
			returnValue.setChildDatasets(childDatasets);
			List<byte[]> returnedDatasets = new ArrayList<>();
			for (CreatedDatasetInfo cdi : childDatasets) {
				returnedDatasets.add(cdi.getData());
			}
			returnValue.setDatasetData(returnedDatasets);
		}
		returnValue.setCompleted(new Date());
		return returnValue;
	}

	public void shutdownContainer(String id, String userId, int port) {
		log.info(String.format("shutdownContainer(%s, %s, %d)", id, userId, port));
		launchMap.remove(id);
		closer.add(id, userId);
		portFifo.add(port);
	}
	
	private void createNewHiddenArtifact(String user, ScriptItem scriptItem, Script scriptNode, byte[] script) throws IOException {
		String libraryServiceHost = Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_HOST, AppPropertyNames.LIBRARY_SERVICE_HOST_DEFAULT);
		int libraryServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
				AppPropertyNames.LIBRARY_SERVICE_PORT, AppPropertyNames.LIBRARY_SERVICE_PORT_DEFAULT));
		String url = String.format("http://%s:%d/equip-services/nca/librarian/artifact/hidden", libraryServiceHost, libraryServicePort);  // TODO
		Map<String, String> headers = new LinkedHashMap<>();
    	headers.put("IAMPFIZERUSERCN", user);
    	headers.put("Accept", "application/json");
    	MultipartUtility mpu = new MultipartUtility(url, headers, "UTF-8");
		mpu.addFormField("primaryType", "equipLibrary:script");
		mpu.addFormField("equipName", scriptItem.getName());
		mpu.addFormField("originalEquipName", scriptItem.getName());
		// mpu.addFormField("description", "");  // TODO
		try (ByteArrayInputStream bais = new ByteArrayInputStream(script)) {
			mpu.addFilePart("fileContent", scriptItem.getName(), bais);
			List<String> response = mpu.finish();
			String json = String.join("\n", response);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(json);
			String scriptId = element.getAsJsonObject().get("artifactId").getAsString();
			String scriptRef = element.getAsJsonObject().get("artifactPath").getAsString();
			scriptItem.setId(scriptId);
			scriptItem.setReference(scriptRef);
			scriptNode.getScriptBody().setLibraryRef(scriptId);
		}
	}

	public ComputeLaunchResponse getComputeLaunchResponseForId(String id) {
		return launchMap.get(id);
	}
	
	private String getLaunchUri(int port) throws UnknownHostException {
		String launchUri = "http://" + InetAddress.getLocalHost().getHostName();
		return launchUri + ":" + port;
	}

	private String createContainer(DockerClient dockerClient, String image) throws IOException, GeneralSecurityException {
		return createContainer(dockerClient, image, null, null, null);
	}
	
	private String createContainer(DockerClient dockerClient, String image, String internalPort, Integer port, Boolean privileged) throws IOException, GeneralSecurityException {
		LinkedHashMap<String, Object> containerConfig = new LinkedHashMap<>();
		containerConfig.put("AttachStdin", true);
		containerConfig.put("Tty", true);
		containerConfig.put("Image", image);
		LinkedHashMap<String, Object> exposedPorts = new LinkedHashMap<>();
		if ((port != null) || (privileged != null)) {
			Map<String, Object> hostConfig = new LinkedHashMap<>();
			if (port != null) {
				exposedPorts.put(internalPort, new LinkedHashMap<>());
				containerConfig.put("ExposedPorts", exposedPorts);
				Map<String, Object> portBindings = new LinkedHashMap<>();
				List<Object> hostPorts = new ArrayList<>();
				Map<String, Object> hostPort = new LinkedHashMap<>();
				hostPort.put("HostIp", "");
				hostPort.put("HostPort", Integer.toString(port));
				hostPorts.add(hostPort);
				portBindings.put(internalPort, hostPorts);
				hostConfig.put("PortBindings", portBindings);
			}
			if (privileged != null) {
				hostConfig.put("Privileged", privileged);
			}
			containerConfig.put("HostConfig", hostConfig);
		}
		EngineResponse r = dockerClient.createContainer(containerConfig);
		Map<String, String> content = (Map<String, String>) r.getContent();
		return content.get("Id");
	}

	private ComputeResponse executeContainer(
			String username,
			DockerClient dockerClient, 
			String id, 
			Map<String, DataframeDataset> datasets,
			String scriptName,
			String scriptId,
			byte[] script,
			List<ScriptItem> subScriptItems,
			String command, 
			Map<String, TypedValue> parameters,
			boolean prepareInContainer) throws IOException, GeneralSecurityException {
		ComputeResponse returnValue = new ComputeResponse();
		returnValue.setContainerId(id);
		returnValue.setStatus("Failed");
		returnValue.setStarted(new Date());
		
		// Add INPUTx for each dataset
		for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
			String datasetName = entry.getKey();
			String filename = datasetName;
			if (parameters.containsKey(datasetName)) {
				// The filename for this dataset has been passed as a parameter.
				filename = parameters.get(datasetName).getValue();
			}
			parameters.put(datasetName, new TypedValue(DOCKER_INPUT_FOLDER + "/" + filename));
			if ((entry.getValue().profileConfig != null) && !entry.getValue().profileConfig.isEmpty()) {
				parameters.put(datasetName + "_PROFILE_CONFIG", 
						new TypedValue(DOCKER_INPUT_FOLDER + "/" + datasetName + "_PROFILE_CONFIG.json"));
			}
		}
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if (parameter.getKey().matches(UUID_REGEX)) {
				toRemove.add(parameter.getKey());
			}
		}
		for (String key : toRemove) {
			parameters.remove(key);
		}
		
		
		// Determine which parameters would be best treated as files because of their size
		// or content.
		List<String> parametersToBeTreatedAsFiles = new ArrayList<>();
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if ((parameter.getValue().getValue() == null)
					|| parameter.getValue().getValue().isEmpty()) {
				parameter.getValue().setValue("NULL");
			}
			if (parameter.getValue().toString().length() > MAX_PARAMETER_SIZE) {
				parametersToBeTreatedAsFiles.add(parameter.getKey());
			}
		}
		
		{
			ContainerResponse cr = new ContainerResponse(dockerClient.startContainer(id));
			// Change the conditional in the following if clause to true to enable
			// the interface to the library in all the containers that are run
			// including equip-r-base, equip-opennca, equip-rmarkdown.
			// The launched containers equip-rstudio and equip-r-shiny have the
			// interface enabled by default.
			if (false) {
				String dataframeServiceHost = Application.getAppProperties().getProperty(
						AppPropertyNames.DATAFRAME_SERVICE_HOST, AppPropertyNames.DATAFRAME_SERVICE_HOST_DEFAULT);
				int dataframeServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
						AppPropertyNames.DATAFRAME_SERVICE_PORT, AppPropertyNames.DATAFRAME_SERVICE_PORT_DEFAULT));
				List<String> cmds = new ArrayList<>();
				cmds.add("/home/docker/bin/equip_lib_fuse");
				cmds.add(dataframeServiceHost + ":" + dataframeServicePort);
				cmds.add(username);
				cmds.add("/home/docker/library");
				try {
					dockerClient.exec(id, cmds);
				} catch(IOException | GeneralSecurityException ex) {
					log.error("Error launching equip_lib_fuse", ex);
				}
			}
		}

		if (prepareInContainer) {
			prepareInputInContainer(
					username, 
					dockerClient, 
					id, 
					datasets, 
					scriptId, 
					subScriptItems, 
					parameters, 
					parametersToBeTreatedAsFiles,
					command == null);
		} else {
			// Create a TarOutputStream containing the datasets and the script
			prepareInput(
					dockerClient, 
					id, 
					datasets, 
					scriptName, 
					script, 
					subScriptItems, 
					parameters, 
					parametersToBeTreatedAsFiles);
		}
		
		// Execute the script in the image
		ContainerResponse r = execute(dockerClient, id, command, scriptName, parameters, parametersToBeTreatedAsFiles);
		if (r.getStatus().isSuccess()) {
			returnValue.setStatus(SUCCESS_STATUS);
		}
		returnValue.setStdin(r.getStdin());
		returnValue.setStdout(getStdout(r));
		
		log.info("stdout: " + returnValue.getStdout());	// DELETE ME when no longer needed
	
		// Get created datasets
		List<CreatedDatasetInfo> childDatasets = extractCreatedDatasets(dockerClient, id);
		returnValue.setChildDatasets(childDatasets);
		List<byte[]> returnedDatasets = new ArrayList<>();
		for (CreatedDatasetInfo cdi : childDatasets) {
			returnedDatasets.add(cdi.getData());
		}
		returnValue.setDatasetData(returnedDatasets);
		returnValue.setCompleted(new Date());
		return returnValue;
	}

	private String constructRstudioPreamble(
			Map<String, DataframeDataset> datasets,
			Map<String, TypedValue> parameters
			) {
		Map<String, String> commandLineArgs = getCommandLineArgs(datasets, parameters);
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (Map.Entry<String, String> entry : commandLineArgs.entrySet()) {
			if (count > 0) {
				sb.append(',');
			}
			sb.append(String.format("\n\"%s=%s\"", entry.getKey(), entry.getValue()));
			count += 1;
		}
		String arguments = sb.toString();
		StringBuilder returnValue = new StringBuilder();
		returnValue.append("## vvvvvvvvvvvvvvvvvv Do not delete or alter the lines below vvvvvvvvvvvvvvvvvv\n");
		returnValue.append("commandArgs <- function(trailingOnly = FALSE) {\n");
		returnValue.append(String.format("   return(c(%s\n))\n}\n", arguments));
		returnValue.append("setwd(\"/home/rstudio/input\")\n");
		returnValue.append(RSTUDIO_PREAMBLE_END);
		return returnValue.toString();
	}
	
	private Map<String, String> getCommandLineArgs(
			Map<String, DataframeDataset> datasets,
			Map<String, TypedValue> parameters
			) {
		Map<String, String> returnValue = new LinkedHashMap<>();
		
		// Add INPUTx for each dataset
		for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
			String datasetName = entry.getKey();
			String filename = datasetName;
			if (parameters.containsKey(datasetName)) {
				// The filename for this dataset has been passed as a parameter.
				filename = parameters.get(datasetName).getValue();
			}
			parameters.put(datasetName, new TypedValue(filename));
		}
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if (parameter.getKey().matches(UUID_REGEX)) {
				toRemove.add(parameter.getKey());
			}
		}
		for (String key : toRemove) {
			parameters.remove(key);
		}
		
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if ((parameter.getValue().getValue() == null)
					|| parameter.getValue().getValue().isEmpty()) {
					parameter.getValue().setValue("NULL");
			}
			// Determine which parameters would be best treated as files because of their size.
			if (parameter.getValue().toString().length() > MAX_PARAMETER_SIZE) {
				returnValue.put(parameter.getKey(), DOCKER_INPUT_FOLDER + "/" + parameter.getKey());
			} else {
				returnValue.put(parameter.getKey(), parameter.getValue().getValue());
			}
		}
		returnValue.put("OUTPUT", DOCKER_OUTPUT_FOLDER);
		return returnValue;
	}
	
	private ComputeLaunchResponse launchContainer(
			DockerClient dockerClient,
			String username,
			String id, 
			List<Dataframe> parentDataframes,
			Map<String, DataframeDataset> datasets,
			String scriptName,
			String scriptId,
			byte[] script,
			List<ScriptItem> subScriptItems,
			String command, 
			Map<String, TypedValue> parameters,
			int port,
			boolean prepareInContainer,
			boolean useBatch) throws IOException, GeneralSecurityException {
		ComputeLaunchResponse returnValue = new ComputeLaunchResponse();
		returnValue.setUserId(username);
		returnValue.setId(id);
		returnValue.setParentDataframes(parentDataframes);
		returnValue.setParameters(parameters);
		returnValue.setSubScriptItems(subScriptItems);
		returnValue.setCommand(command);
		returnValue.setStarted(new Date());
		returnValue.setPrepareInContainer(prepareInContainer);
		returnValue.setUseBatch(useBatch);
		
		// Add INPUTx for each dataset
		for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
			String datasetName = entry.getKey();
			String filename = datasetName;
			if (parameters.containsKey(datasetName)) {
				// The filename for this dataset has been passed as a parameter.
				filename = parameters.get(datasetName).getValue();
			}
			parameters.put(datasetName, new TypedValue(DOCKER_INPUT_FOLDER + "/" + filename));
			if ((entry.getValue().profileConfig != null) && !entry.getValue().profileConfig.isEmpty()) {
				parameters.put(datasetName + "_PROFILE_CONFIG", 
						new TypedValue(DOCKER_INPUT_FOLDER + "/" + datasetName + "_PROFILE_CONFIG.json"));
			}
		}
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if (parameter.getKey().matches(UUID_REGEX)) {
				toRemove.add(parameter.getKey());
			}
		}
		for (String key : toRemove) {
			parameters.remove(key);
		}
		
		
		// Determine which parameters would be best treated as files because of their size.
		List<String> parametersToBeTreatedAsFiles = new ArrayList<>();
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if ((parameter.getValue().getValue() == null)
					|| parameter.getValue().getValue().isEmpty()) {
				parameter.getValue().setValue("NULL");
			}
			if (parameter.getValue().toString().length() > MAX_PARAMETER_SIZE) {
				parametersToBeTreatedAsFiles.add(parameter.getKey());
			}
		}
		
		{
			ContainerResponse cr = new ContainerResponse(dockerClient.startContainer(id));
			String dataframeServiceHost = Application.getAppProperties().getProperty(
					AppPropertyNames.DATAFRAME_SERVICE_HOST, AppPropertyNames.DATAFRAME_SERVICE_HOST_DEFAULT);
			int dataframeServicePort = Integer.parseInt(Application.getAppProperties().getProperty(
					AppPropertyNames.DATAFRAME_SERVICE_PORT, AppPropertyNames.DATAFRAME_SERVICE_PORT_DEFAULT));
			List<String> cmds = new ArrayList<>();
			cmds.add("/home/docker/bin/equip_lib_fuse");
			cmds.add(dataframeServiceHost + ":" + dataframeServicePort);
			cmds.add(username);
			cmds.add("/home/docker/library");
			try {
				dockerClient.exec(id, cmds);
			} catch(IOException | GeneralSecurityException ex) {
				log.error("Error launching equip_lib_fuse", ex);
			}
		}
		// Create a TarOutputStream containing the datasets and the script
		if (prepareInContainer) {
			prepareInputInContainer(
					username, 
					dockerClient, 
					id, 
					datasets, 
					scriptId, 
					subScriptItems, 
					parameters, 
					parametersToBeTreatedAsFiles, 
					command == null);
		} else {
			if (command == null) {
				if (script != null) {
					// Rstudio
					script = prependScript(constructRstudioPreamble(datasets, parameters), script);
				}
			}
			prepareInput(
					dockerClient, 
					id, 
					datasets, 
					scriptName, 
					script, 
					subScriptItems, 
					parameters, 
					parametersToBeTreatedAsFiles);
		}
		
		// Execute the script in the image
		returnValue.setPort(port);
		Future<ContainerResponse> r = launch(
				dockerClient, 
				id, 
				command, 
				scriptName, 
				parameters, 
				parametersToBeTreatedAsFiles);
		returnValue.setContainerResponseFuture(r);
		return returnValue;
	}

	private byte[] prependScript(String rstudioPreamble, byte[] script) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(rstudioPreamble.getBytes());
			baos.write(script);
			return baos.toByteArray();
		}
	}

	private ScriptItem extractScript(DockerClient dockerClient, String id, String scriptName) throws IOException, GeneralSecurityException {
		ScriptItem returnValue = new ScriptItem();
		returnValue.setName(scriptName);
		returnValue.setScript(new byte[] {});
		EngineResponse er;
		er = dockerClient.getArchive(id, DOCKER_SCRIPT_FOLDER);
		try (TarInputStream tis = new TarInputStream(new BufferedInputStream(er.getStream()))) {
			TarEntry entry;
			while ((entry = tis.getNextEntry()) != null) {
				log.info("Script tar entry name:" + entry.getName());
				if (entry.isDirectory()) {
					continue;
				}
				if (!entry.getName().equals("script/" + scriptName)) {
					continue;
				}
				String filename = entry.getName();
				returnValue.setName(filename);
				Date modTime = entry.getModTime();
				long size = entry.getSize();
				int count;
				byte[] data = new byte[8192];
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					try (BufferedOutputStream destBos = new BufferedOutputStream(baos)) {
						while ((count = tis.read(data)) != -1) {
							destBos.write(data, 0, count);
						}
						destBos.flush();
						returnValue.setScript(baos.toByteArray());
					}
				}
			}
		}
		return returnValue;
	}
	
	private byte[] exciseRstudioRunHeader(byte[] scriptBytes) {
		String script = new String(scriptBytes);
		script = script.substring(script.indexOf(RSTUDIO_PREAMBLE_END) + RSTUDIO_PREAMBLE_END.length());
		return script.getBytes();
	}
	
	private boolean scriptsDiffer(byte[] script1, byte[] script2) {
		return !Arrays.equals(script1, script2);
	}
	
	private List<CreatedDatasetInfo> extractCreatedDatasets(DockerClient dockerClient, String id)
			throws IOException, GeneralSecurityException {
		List<CreatedDatasetInfo> returnValue = new ArrayList<>();
		EngineResponse er;
		er = dockerClient.getArchive(id, DOCKER_OUTPUT_FOLDER);
		try (TarInputStream tis = new TarInputStream(new BufferedInputStream(er.getStream()))) {
			TarEntry entry;
			while ((entry = tis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				String filename = entry.getName();
				Date modTime = entry.getModTime();
				long size = entry.getSize();
				log.info(String.format("Extracted %s %s %d %d", filename, modTime, modTime.getTime(), size));
				CreatedDatasetInfo cdi = new CreatedDatasetInfo(filename, modTime, size);
				int count;
				byte[] data = new byte[8192];
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					try (BufferedOutputStream destBos = new BufferedOutputStream(baos)) {
						while ((count = tis.read(data)) != -1) {
							destBos.write(data, 0, count);
						}
						destBos.flush();
						cdi.setData(baos.toByteArray());
						returnValue.add(cdi);
					}
				}
			}
		}
		
		
		Collections.sort(returnValue, (c1, c2) -> {
			long c1Timestamp = c1.getLastModified().getTime();
			long c2Timestamp = c2.getLastModified().getTime();
			if (c1Timestamp < c2Timestamp) {
				return -1;
			} else if (c1Timestamp > c2Timestamp) {
				return 1;
			}
			return 0;
		});
		
		StringBuilder sb = new StringBuilder();
		for(CreatedDatasetInfo cdi : returnValue) {
			sb.append(String.format("%s %s %d %d", 
					cdi.getFilename(), 
					cdi.getLastModified(),
					cdi.getLastModified().getTime(),
					cdi.getSize()));
			sb.append("\n");
		}
		log.info("CreatedDatasetInfo returned: " + sb.toString());
		return returnValue;
	}

	
    private CreatedDatasets getCreatedDatasets(DockerClient dockerClient, String id)
			throws IOException, GeneralSecurityException {
		CreatedDatasets returnValue = new CreatedDatasets();
		EngineResponse er;
		er = dockerClient.getArchive(id, DOCKER_OUTPUT_FOLDER);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try(BufferedInputStream bis = new BufferedInputStream(er.getStream())) {
				byte[] data = new byte[8192];
				int count = 0;
				while((count = bis.read(data)) != -1) {
					baos.write(data, 0, count);
				}
			}
			baos.flush();
			returnValue.setTarData(baos.toByteArray());
		}
		try (ByteArrayInputStream bais = new ByteArrayInputStream(returnValue.getTarData())) {
			try (TarInputStream tis = new TarInputStream(new BufferedInputStream(bais))) {
				TarEntry entry;
				while ((entry = tis.getNextEntry()) != null) {
					if (entry.isDirectory()) {
						continue;
					}
					File f = new File(entry.getName());
					String filename = f.getName();
					Date modTime = entry.getModTime();
					long size = entry.getSize();
					CreatedDatasetInfo cdi = new CreatedDatasetInfo(filename, modTime, size);
					returnValue.getDatasetInfo().add(cdi);
				}
			}
		}
		return returnValue;
	}
	
	private void prepareInput(
			DockerClient dockerClient, 
			String id, 
			Map<String, DataframeDataset> datasets,
			String scriptName,
			byte[] script,
			List<ScriptItem> subScriptItems,
			Map<String, TypedValue> parameters,
			List<String> parametersToBeTreatedAsFiles) throws IOException, GeneralSecurityException {
		try (ByteArrayOutputStream dest = new ByteArrayOutputStream()) {
			try (TarOutputStream out = new TarOutputStream(dest)) {
				long modTime = Instant.now().getEpochSecond();
				boolean isDir = false;
				int permissions = 0777;
				for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
					String datasetName = entry.getKey();
					if (parameters.containsKey(datasetName)) {
						datasetName = parameters.get(datasetName).getValue();
						String[] parts = datasetName.split("/");
						if (parts.length > 1) {
							datasetName = parts[parts.length - 1];
						}
					}
					byte[] dataset = (entry.getValue()).datasetData;
					TarHeader tarHeader = TarHeader.createHeader(
							"input/" + datasetName, 
							dataset.length, 
							modTime, 
							isDir,
							permissions);
					out.putNextEntry(new TarEntry(tarHeader));
					out.write(dataset);
					if ((entry.getValue().profileConfig != null) && !entry.getValue().profileConfig.isEmpty()) {
						String profileConfig = stringListToJson(entry.getValue().profileConfig);
						byte[] profileConfigData = profileConfig.getBytes();
						tarHeader = TarHeader.createHeader(
								datasetName + "_PROFILE_CONFIG.json", 
								profileConfigData.length, 
								modTime, 
								isDir,
								permissions);
						out.putNextEntry(new TarEntry(tarHeader));
						out.write(profileConfigData);
					}
					out.flush();
				}
				for (String name : parametersToBeTreatedAsFiles) {
					byte[] parameterData = parameters.get(name).getValue().getBytes();
					TarHeader tarHeader = TarHeader.createHeader(
							"input/" + name, 
							parameterData.length, 
							modTime, 
							isDir,
							permissions);
					out.putNextEntry(new TarEntry(tarHeader));
					out.write(parameterData);
					out.flush();
				}
				TarHeader tarHeader;
				if (script != null) {
				// Add script
					tarHeader = TarHeader.createHeader(
							"script/" + scriptName, 
							script.length, 
							modTime, 
							isDir, 
							permissions);
					out.putNextEntry(new TarEntry(tarHeader));
					out.write(script);
					out.flush();
				}
				// Add any subscripts
				for (ScriptItem subScriptItem : subScriptItems) {
					File f = new File(subScriptItem.getPath());
					tarHeader = TarHeader.createHeader(
							"script/" + f.getName(), 
							subScriptItem.getScript().length, 
							modTime, 
							isDir,
							permissions);
					out.putNextEntry(new TarEntry(tarHeader));
					out.write(subScriptItem.getScript());
					out.flush();
				}
			}
			try (ByteArrayInputStream input = new ByteArrayInputStream(dest.toByteArray())) {
				EngineResponse er = dockerClient.putArchive(id, "/home/docker", input);
			}
		}
	}

	private void prepareInputInContainer(
			String username,
			DockerClient dockerClient, 
			String id, 
			Map<String, DataframeDataset> datasets,
			String scriptId,
			List<ScriptItem> subScriptItems,
			Map<String, TypedValue> parameters,
			List<String> parametersToBeTreatedAsFiles,
			boolean isRstudio) throws IOException, GeneralSecurityException {
		ContainerInput ci = new ContainerInput();
		String modeshapeServer = Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_SERVICE_HOST, 
				AppPropertyNames.MODESHAPE_SERVICE_HOST_DEFAULT).trim();
		modeshapeServer = modeshapeServer.substring(modeshapeServer.lastIndexOf('/') + 1);
		ci.setModeshapeServer(modeshapeServer);
		ci.setModeshapeUser(Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_USERNAME, 
				AppPropertyNames.MODESHAPE_USERNAME_DEFAULT).trim());
		ci.setModeshapePassword(Application.getAppProperties().getProperty(
				AppPropertyNames.MODESHAPE_AUTH, 
				AppPropertyNames.MODESHAPE_AUTH_DEFAULT).trim());
		ci.setUsername(username);
		ci.setScriptsDestination(DOCKER_SCRIPT_FOLDER);
		ci.setDataframesDestination(DOCKER_INPUT_FOLDER);
		ci.setParameters(parameters);
		
		// if (isRstudio) {
			ci.setCommandLineArgs(getCommandLineArgs(datasets, parameters));
		// }

		
		for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
			String datasetName = entry.getKey();
			String dataframeId = entry.getValue().dataframeId;
//			List<String> profileConfig = new ArrayList<>();
//			if (!entry.getValue().profileConfig.isEmpty()) {
//				profileConfig = entry.getValue().profileConfig;
//			}
			if (parameters.containsKey(datasetName)) {
				datasetName = parameters.get(datasetName).getValue();
				String[] parts = datasetName.split("/");
				if (parts.length > 1) {
					datasetName = parts[parts.length - 1];
				}
			}
			ci.getDataframes().put(datasetName, dataframeId);
//			if (!profileConfig.isEmpty()) {
//				ci.getProfileConfigs().put(datasetName, profileConfig);
//			}
		}
		ci.setScriptId(scriptId);
		for (ScriptItem subScriptItem : subScriptItems) {
			ci.getSubScriptItems().add(subScriptItem);
		}
		String ciJson = new Gson().toJson(ci);
		log.info("ciJson: " + ciJson);
		byte[] ciJsonBytes = ciJson.getBytes();
		
		DockerClient dc = new DockerClientImpl();

		try (ByteArrayOutputStream dest = new ByteArrayOutputStream()) {
			try (TarOutputStream out = new TarOutputStream(dest)) {
				long modTime = Instant.now().getEpochSecond();
				boolean isDir = false;
				int permissions = 0777;
				TarHeader tarHeader = TarHeader.createHeader(
						"setup.json", 
						ciJsonBytes.length, 
						modTime, 
						isDir,
						permissions);
				out.putNextEntry(new TarEntry(tarHeader));
				out.write(ciJsonBytes);
				out.flush();
			}
			try (ByteArrayInputStream input = new ByteArrayInputStream(dest.toByteArray())) {
				EngineResponse er = dc.putArchive(id, "/home/docker/extra", input);
			}
		}
		
		try (ByteArrayOutputStream dest = new ByteArrayOutputStream()) {
			try (TarOutputStream out = new TarOutputStream(dest)) {
				long modTime = Instant.now().getEpochSecond();
				boolean isDir = false;
				int permissions = 0777;
				for (String name : parametersToBeTreatedAsFiles) {
					byte[] parameterData = parameters.get(name).getValue().getBytes();
					TarHeader tarHeader = TarHeader.createHeader(name, parameterData.length, modTime, isDir,
							permissions);
					out.putNextEntry(new TarEntry(tarHeader));
					out.write(parameterData);
					out.flush();
				}
				for (Map.Entry<String, DataframeDataset> entry : datasets.entrySet()) {
					List<String> profileConfig = entry.getValue().profileConfig;
					log.info(String.format("profileConfig: %s", profileConfig));
					if ((profileConfig != null) && !profileConfig.isEmpty()) {
						byte[] data = stringListToJson(profileConfig).getBytes();
						String profileConfigName = entry.getKey() + "_PROFILE_CONFIG.json";
						TarHeader tarHeader = TarHeader.createHeader(profileConfigName, data.length, modTime, isDir,
								permissions);
						out.putNextEntry(new TarEntry(tarHeader));
						out.write(data);
						out.flush();
					}
				}
			}
			byte[] destBytes = dest.toByteArray();
			if (destBytes.length > 0) {
				try (ByteArrayInputStream input = new ByteArrayInputStream(destBytes)) {
					EngineResponse er = dc.putArchive(id, "/home/docker/input", input);
				}
			}
		}

		// Run the executable that will fetch and store the input data.
		List<String> cmd = new ArrayList<>();
		cmd.add("/home/docker/bin/container_setup");
		cmd.add("/home/docker/extra/setup.json");
		log.info("id: " + id);	// DELETE ME when no longer neccessary
		EngineResponse er = dc.exec(id, cmd);
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
		log.info("container_setup stdout: " + sb.toString());	// DELETE ME when no longer needed
	}
	
	private String stringListToJson(List<String> arry) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (String item : arry) {
			if (sb.length() > 1) {
				sb.append(',');
			}
			sb.append('"');
			sb.append(item);
			sb.append('"');
		}
		sb.append(']');
		return sb.toString();
	}

	private ContainerResponse execute(
			DockerClient dockerClient, 
			String id, 
			String command,
			String scriptName,
			Map<String, TypedValue> parameters,
			List<String> parametersToBeTreatedAsFiles) throws IOException, GeneralSecurityException {
		List<String> cmd = new ArrayList<>();
		cmd.add(command);
		cmd.add(DOCKER_SCRIPT_FOLDER + "/" + scriptName);
		for (Map.Entry<String, TypedValue> parameter : parameters.entrySet()) {
			if ((parameter.getValue().getValue() == null)
					|| parameter.getValue().getValue().isEmpty()) {
				parameter.getValue().setValue("NULL");
			}
			String value = parameter.getValue().getValue();
			if (parametersToBeTreatedAsFiles.contains(parameter.getKey())) {
				cmd.add(parameter.getKey() + "=" + DOCKER_INPUT_FOLDER + "/" + parameter.getKey());
			} else {
				cmd.add(parameter.getKey() + "=" + value);
			}
		}
		cmd.add("OUTPUT=/home/docker/output");
		
		StringBuilder sb = new StringBuilder();
		for(String e : cmd) {
			sb.append(e);
			sb.append(" ");
		}
		String cmdLine = sb.toString();
		log.info(String.format("cmdLine: %s", cmdLine));	// DELETE ME when no longer needed
		ContainerResponse r = null;
		if ((command != null) && command.length() > 0) {
			r = new ContainerResponse(dockerClient.exec(id, cmd));
			r.setStdin(cmdLine);
		}
		return r;
	}

	private Future<ContainerResponse> launch(
			DockerClient dockerClient, 
			String id, 
			String command, 
			String scriptName,
			Map<String, TypedValue> parameters,
			List<String> parametersToBeTreatedAsFiles
			) {
		return launchService.submit(new Callable<ContainerResponse>() {
			@Override
			public ContainerResponse call() throws Exception {
				return execute(dockerClient, id, command, scriptName, parameters, parametersToBeTreatedAsFiles);
			}
		});
	}

	private String getStdout(ContainerResponse r) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedInputStream bis = new BufferedInputStream(r.getStream())) {
			byte[] data = new byte[8192];
		    int numBytesRead = 0;
		    do {
		    	numBytesRead = bis.read(data);
		    	if (numBytesRead > 0) {
		    		sb.append(new String(data, 0, numBytesRead));
		    	}
		    } while (numBytesRead > -1);
		}
		return sb.toString();
	}

	/**
	 * Tests that the launchUri is active before returning. If it times out, throws an exception.
	 * 
	 * @param launchUri
	 * @throws MalformedURLException
	 * @throws ComputeException
	 */
	private void testLaunchUri(String launchUri) throws MalformedURLException, ComputeException {
		for (int i = 0, n = SHINY_RETRIES; i < n; i++) {
			if (canConnect(launchUri)) {
				return;
			}
			try {
				Thread.sleep(SHINY_RETRY_SLEEP);
			} catch (InterruptedException e) {
				// deliberately ignored.
			}
		}
		throw new ComputeException(String.format("Not able to connect to url: %s", launchUri));
	}

	/**
	 * Tests if the uri can be connected to.
	 * 
	 * @param uri
	 * @return
	 * @throws MalformedURLException
	 */
	private boolean canConnect(String uri) throws MalformedURLException {
		boolean returnValue = false;
		URL url = new URL(uri);
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			returnValue = true;
		} catch (IOException ex) {
			// Ignoring exception
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}		
		return returnValue;
	}
	
	public static void killAllPreExistingRunningContainers() throws IOException, GeneralSecurityException {
        Map<String, Object> query = new HashMap<>();
        query.put("state", "running");
		DockerClient dockerClient = new DockerClientImpl();
		EngineResponse er = dockerClient.ps(query);
		for (Object imageObj : ((List<?>) er.getContent())) {
			String imageId = (String) ((Map<String, ?>) imageObj).get("Id");
			dockerClient.kill(imageId);
		}
	}

	public static ContainerCloser getCloser() {
		return closer;
	}
	
}
