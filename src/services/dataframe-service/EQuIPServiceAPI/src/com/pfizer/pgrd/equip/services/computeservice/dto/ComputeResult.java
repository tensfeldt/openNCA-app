package com.pfizer.pgrd.equip.services.computeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComputeResult {
	private String id;
	private String status;
	private String environment;
	private Date started;
	private Date completed;
	private String stdin;
	private String stdout;
	private String stderr;
	private String uri;
	private String batch;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	private List<String> dataframes = new ArrayList<>();
	private List<String> datasetData = new ArrayList<>();
	
	public String getId() {
		return this.id;
	}
	public String getStatus() {
		return this.status;
	}
	public String getEnvironment() {
		return this.environment;
	}
	public Date getStarted() {
		return this.started;
	}
	public Date getCompleted() {
		return this.completed;
	}
	public String getStdin() {
		return this.stdin;
	}
	public String getStdout() {
		return this.stdout;
	}
	public String getStderr() {
		return this.stderr;
	}
	public List<String> getDataframeIds() {
		return this.dataframes;
	}
	public List<String> getDatasetData() {
		return this.datasetData;
	}
	public String getBatchId() {
		return this.batch;
	}
}
