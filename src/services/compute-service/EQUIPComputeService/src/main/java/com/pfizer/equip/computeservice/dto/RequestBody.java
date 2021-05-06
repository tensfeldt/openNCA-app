package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.pfizer.equip.utils.ParametersAdapter;
import com.pfizer.equip.utils.TypedValue;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;

@XmlRootElement
@XmlType(propOrder = {"user", "computeContainer", "environment", "scriptId", "equipId", "parameters", "assemblies", "dataframes", "dataframeType", "subType", "batch", "requestJson"})
public class RequestBody {
	private String user;
	private String computeContainer;
	private String environment;
	private List<String> assemblies = new ArrayList<>();
	private List<String> dataframes = new ArrayList<>();
	private String scriptId;
	private List<String> equipId = new ArrayList<>();
	@XmlJavaTypeAdapter(ParametersAdapter.class)
	@XmlPath(".")
	private Map<String, TypedValue> parameters = new HashMap<> ();
	private List<String> dataframeType = new ArrayList<>();
	private String subType;
	private boolean batch;
	private boolean dontBatch;
	private String requestJson;
	private List<Dataframe> dataframeEntities = null;
			
	public List<Dataframe> getDataframeEntities() {
		return dataframeEntities;
	}

	public void setDataframeEntities(List<Dataframe> dataframeEntities) {
		this.dataframeEntities = dataframeEntities;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getComputeContainer() {
		return computeContainer;
	}

	public void setComputeContainer(String computeContainer) {
		this.computeContainer = computeContainer;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getScriptId() {
		return scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public Map<String, TypedValue> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, TypedValue> parameters) {
		this.parameters = parameters;
	}

	public List<String> getEquipId() {
		return equipId;
	}

	public void setEquipId(List<String> equipId) {
		this.equipId = equipId;
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

	public boolean isBatch() {
		return batch;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}

	public boolean isDontBatch() {
		return dontBatch;
	}

	public void setDontBatch(boolean dontBatch) {
		this.dontBatch = dontBatch;
	}

	public String getRequestJson() {
		return requestJson;
	}

	public void setRequestJson(String requestJson) {
		this.requestJson = requestJson;
	}

	@Override
	public String toString() {
		return "RequestBody [user=" + user + ", computeContainer=" + computeContainer + ", environment=" + environment
				+ ", assemblies=" + assemblies + ", dataframes=" + dataframes + ", scriptId=" + scriptId + ", equipId="
				+ equipId + ", parameters=" + parameters + ", dataframeType=" + dataframeType + ", subType=" + subType
				+ ", batch=" + batch
				+ ", requestJson=" + requestJson + "]";
	}

}

