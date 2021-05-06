package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "id", "started", "completed", "status", "environment", "stdin", "stdout", 
		"stderr", "scriptId", "dataframes",
		"datasetData", "childDatasets", "batch" })
public class ComputeResponse {
	@XmlElement
	private String id;
	@XmlElement
	private String status;
	@XmlElement
	private String environment;
	@XmlElement
	private Date started;
	@XmlElement
	private Date completed;
	@XmlElement
	private String stdin;
	@XmlElement
	private String stdout;
	@XmlElement
	private String stderr;
	@XmlElement
	private String scriptId;
	@XmlElement
	private List<String> dataframes = new ArrayList<String>();
	@XmlElement
	private List<byte[]> datasetData = new ArrayList<byte[]>();
	@XmlElement
	private List<CreatedDatasetInfo> childDatasets;
	@XmlElement
	private String batch;
	private byte[] script;

	private CreatedDatasets unextractedChildDatasets;
	
	private String containerId;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Date getCompleted() {
		return completed;
	}

	public void setCompleted(Date completed) {
		this.completed = completed;
	}

	public String getStdin() {
		return stdin;
	}

	public void setStdin(String stdin) {
		this.stdin = stdin;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	public String getScriptId() {
		return scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public List<String> getDataframes() {
		return dataframes;
	}

	public void setDataframes(List<String> dataframes) {
		this.dataframes = dataframes;
	}

	public List<byte[]> getDatasetData() {
		return datasetData;
	}

	public void setDatasetData(List<byte[]> datasetData) {
		this.datasetData = datasetData;
	}

	public List<CreatedDatasetInfo> getChildDatasets() {
		return childDatasets;
	}

	public void setChildDatasets(List<CreatedDatasetInfo> childDatasets) {
		this.childDatasets = childDatasets;
	}

	public CreatedDatasets getUnextractedChildDatasets() {
		return unextractedChildDatasets;
	}

	public void setUnextractedChildDatasets(CreatedDatasets unextractedChildDatasets) {
		this.unextractedChildDatasets = unextractedChildDatasets;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public byte[] getScript() {
		return script;
	}

	public void setScript(byte[] script) {
		this.script = script;
	}
	@Override
	public String toString() {
		return "ComputeResponse [id=" + id + ", status=" + status + ", environment=" + environment + ", started="
				+ started + ", completed=" + completed + ", stdin=" + stdin + ", stdout=" + stdout + ", stderr="
				+ stderr + ", scriptId=" + scriptId + ", dataframes=" + dataframes + ", datasetData=" + datasetData + ", childDatasets="
				+ childDatasets 
				+ ", batch=" + batch
				+ "]";
	}

}
