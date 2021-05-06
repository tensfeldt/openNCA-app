package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.List;

public class PimsDataLoadInput {
	List<PimsPkTermDosepMapping> dosepMapping;
	String assemblyId;
	String pkDefDataframeId;
	String createdBy;
	String dosepJson;
	
	public String getDosepJson() {
		return dosepJson;
	}
	public void setDosepJson(String dosepJson) {
		this.dosepJson = dosepJson;
	}
	public String getAssemblyId() {
		return assemblyId;
	}
	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}
	public String getPkDefDataframeId() {
		return pkDefDataframeId;
	}
	public void setPkDefDataframeId(String pkDefDataframeId) {
		this.pkDefDataframeId = pkDefDataframeId;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public List<PimsPkTermDosepMapping> getDosepMapping() {
		return dosepMapping;
	}
	public void setDosepMapping(List<PimsPkTermDosepMapping> dosepMapping) {
		this.dosepMapping = dosepMapping;
	}
}
