package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.List;

public class ContainerRunResponse {
	private List<String> scriptIds = new ArrayList<>();
	private String batchId;
	private List<String> dataframeIds = new ArrayList<>();
	private List<String> errors = new ArrayList<>();
	
	public List<String> getScriptIds() {
		return scriptIds;
	}
	public void setScriptIds(List<String> scriptIds) {
		this.scriptIds = scriptIds;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public List<String> getDataframeIds() {
		return dataframeIds;
	}
	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframeIds = dataframeIds;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
}
