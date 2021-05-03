package com.pfizer.pgrd.equip.dataframe.dto.lineage;

import java.util.ArrayList;
import java.util.List;

public class LineageReExecutionParameters {
	/**
	 * The data to use in place of the data at the start ID.
	 */
	private List<String> newDataIds = new ArrayList<>();
	
	/**
	 * Where to start the re-execution from. Re-execution will use the scripts at the start ID on the 
	 * data in the {@code newDataIds} field.
	 */
	private String startId;
	
	/**
	 * The environment to perform the re-execution in.
	 */
	private String environment;
	
	private boolean skipInitialItem;
	
	public String getStartId() {
		return startId;
	}
	public void setStartId(String startId) {
		this.startId = startId;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public List<String> getNewDataIds() {
		return newDataIds;
	}
	public void setNewDataIds(List<String> newDataIds) {
		this.newDataIds = newDataIds;
	}
	public boolean skipInitialItem() {
		return skipInitialItem;
	}
	public void setSkipInitialItem(boolean skipInitialItem) {
		this.skipInitialItem = skipInitialItem;
	}
}