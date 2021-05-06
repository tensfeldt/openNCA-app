package com.pfizer.equip.computeservice.dto;

import java.util.HashMap;
import java.util.Map;

public class ContainerOutput {
	private Map<String, String> dataframes = new HashMap<>();

	public Map<String, String> getDataframes() {
		return dataframes;
	}

	public void setDataframes(Map<String, String> dataframes) {
		this.dataframes = dataframes;
	}
}
