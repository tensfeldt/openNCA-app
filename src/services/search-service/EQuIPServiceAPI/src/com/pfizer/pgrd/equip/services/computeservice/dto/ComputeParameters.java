package com.pfizer.pgrd.equip.services.computeservice.dto;

import java.util.ArrayList;
import java.util.List;

public class ComputeParameters {
	private String user;
	private String computeContainer;
	private String environment;
	private String scriptId;
	private List<String> dataframeType = new ArrayList<>();
	private List<String> dataframes = new ArrayList<>();
	private List<Parameter> parameters = new ArrayList<>();
	private List<String> equipId = new ArrayList<>();
	private List<String> assemblies = new ArrayList<>();
	private boolean dontBatch = false;
	private boolean batch = false;
	
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
	public List<String> getDataframeIds() {
		return dataframes;
	}
	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframes = dataframeIds;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	public String getDataframeType() {
		if(this.dataframeType.size() == 1) {
			return this.dataframeType.get(0);
		}
		
		return null;
	}
	public void addDataframeType(String dataframeType) {
		if(dataframeType != null) {
			if(this.dataframeType == null) {
				this.dataframeType = new ArrayList<>();
			}
			
			this.dataframeType.add(dataframeType);
		}
	}
	public String getEquipId() {
		if(this.equipId.size() == 1) {
			return this.equipId.get(0);
		}
		
		return null;
	}
	public void addEquipId(String equipId) {
		if(equipId != null) {
			if(this.equipId == null) {
				this.equipId = new ArrayList<>();
			}
			
			this.equipId.add(equipId);
		}
	}
	public List<String> getAssemblyIds() {
		return this.assemblies;
	}
	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblies = assemblyIds;
	}
	public void setDataframeTypes(List<String> dataframeTypes) {
		this.dataframeType = dataframeTypes;
	}
	public void setEquipIds(List<String> equipIds) {
		this.equipId = equipIds;
	}
	public void setDataframeType(String dataframeType) {
		this.dataframeType = new ArrayList<>();
		this.dataframeType.add(dataframeType);
	}
	public void setEquipId(String equipId) {
		this.equipId = new ArrayList<>();
		this.equipId.add(equipId);
	}
	public List<String> getDataframeTypes() {
		return this.dataframeType;
	}
	public List<String> getEquipIds() {
		return this.equipId;
	}
	public boolean dontBatch() {
		return dontBatch;
	}
	public void setDontBatch(boolean dontBatch) {
		this.dontBatch = dontBatch;
	}
	public boolean isBatch() {
		return batch;
	}
	public void setBatch(boolean batch) {
		this.batch = batch;
	}
}
