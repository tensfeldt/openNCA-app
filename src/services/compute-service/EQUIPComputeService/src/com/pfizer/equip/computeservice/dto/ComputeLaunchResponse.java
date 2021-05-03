package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.pfizer.equip.computeservice.containers.ContainerResponse;
import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.equip.utils.TypedValue;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"id", "url", "environment", "started", "status"})
public class ComputeLaunchResponse {
	private String userId;
	@XmlElement
	private String id;
	@XmlElement
	private String url;
	@XmlElement
	private String environment;
	@XmlElement
	private Date started;
	@XmlElement
	private String status;
	private Map<String, String> requestHeaders = new HashMap<>();
	private String scriptId;
	private String scriptName;
	private byte[] script;
	private Script scriptNode;
	private String command;
	private List<String> equipId = new ArrayList<>();
	private List<String> dataframeType = new ArrayList<>();
	private String subType;
	private List<String> assemblies = new ArrayList<>();
	private List<String> dataframes = new ArrayList<>();
	private List<Dataframe> parentDataframes = new ArrayList<>();
	private Map<String, TypedValue> parameters = new HashMap<>();
	private List<ScriptItem> subScriptItems = new ArrayList<>();
	private int port;
	private boolean prepareInContainer;
	private boolean useBatch;
	private boolean batch;
	private boolean rstudio;
	private Future<ContainerResponse> containerResponseFuture;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public Date getStarted() {
		return started;
	}
	public void setStarted(Date started) {
		this.started = started;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}
	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}
	public String getScriptId() {
		return scriptId;
	}
	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}
	public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public byte[] getScript() {
		return script;
	}

	public void setScript(byte[] script) {
		this.script = script;
	}
	public Script getScriptNode() {
		return scriptNode;
	}
	public void setScriptNode(Script scriptNode) {
		this.scriptNode = scriptNode;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public List<String> getEquipId() {
		return equipId;
	}
	public void setEquipId(List<String> equipId) {
		this.equipId = equipId;
	}
	public List<String> getDataframeType() {
		return dataframeType;
	}
	public void setDataframeType(List<String> dataframeType) {
		this.dataframeType = dataframeType;
	}	
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public List<String> getAssemblies() {
		return assemblies;
	}
	public void setAssemblies(List<String> assemblies) {
		this.assemblies = assemblies;
	}
	public List<String> getDataframes() {
		return dataframes;
	}
	public void setDataframes(List<String> dataframes) {
		this.dataframes = dataframes;
	}
	public List<Dataframe> getParentDataframes() {
		return parentDataframes;
	}
	public void setParentDataframes(List<Dataframe> parentDataframes) {
		this.parentDataframes = parentDataframes;
	}
	public Map<String, TypedValue> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, TypedValue> parameters) {
		this.parameters = parameters;
	}
	public List<ScriptItem> getSubScriptItems() {
		return subScriptItems;
	}
	public void setSubScriptItems(List<ScriptItem> subScriptItems) {
		this.subScriptItems = subScriptItems;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isPrepareInContainer() {
		return prepareInContainer;
	}
	public void setPrepareInContainer(boolean prepareInContainer) {
		this.prepareInContainer = prepareInContainer;
	}
	public boolean isUseBatch() {
		return useBatch;
	}
	public void setUseBatch(boolean useBatch) {
		this.useBatch = useBatch;
	}
	public boolean isBatch() {
		return batch;
	}
	public void setBatch(boolean batch) {
		this.batch = batch;
	}
	public boolean isRstudio() {
		return rstudio;
	}
	public void setRstudio(boolean rstudio) {
		this.rstudio = rstudio;
	}
	public Future<ContainerResponse> getContainerResponseFuture() {
		return containerResponseFuture;
	}
	public void setContainerResponseFuture(Future<ContainerResponse> containerResponseFuture) {
		this.containerResponseFuture = containerResponseFuture;
	}
}
