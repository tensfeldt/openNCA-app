package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

public class AnalysisData {
	private String analysisEquipId;
	private long analysisVersion;
	private String parametersEquipId;
	private String parametersId;
	private long parametersVersion;
	private List<String> firstPromotedDataframeIds = new ArrayList<>();
	private String concentrationDataId;
	private long concentrationVersion;
	private String kelFlagsId;
	private long kelFlagsVersion;
	
	public String getParametersId() {
		return parametersId;
	}
	public void setParametersId(String parametersId) {
		this.parametersId = parametersId;
	}
	public String getConcentrationDataId() {
		return concentrationDataId;
	}
	public void setConcentrationDataId(String concentrationDataId) {
		this.concentrationDataId = concentrationDataId;
	}
	public String getKelFlagsId() {
		return kelFlagsId;
	}
	public void setKelFlagsId(String kelFlagsId) {
		this.kelFlagsId = kelFlagsId;
	}
	public String getAnalysisEquipId() {
		return analysisEquipId;
	}
	public void setAnalysisEquipId(String analysisEquipId) {
		this.analysisEquipId = analysisEquipId;
	}
	public String getParametersEquipId() {
		return parametersEquipId;
	}
	public void setParametersEquipId(String parametersEquipId) {
		this.parametersEquipId = parametersEquipId;
	}
	public List<String> getFirstPromotedDataframeIds() {
		return firstPromotedDataframeIds;
	}
	public void setFirstPromotedDataframeIds(List<String> firstPromotedDataframeIds) {
		this.firstPromotedDataframeIds = firstPromotedDataframeIds;
	}
	public long getAnalysisVersion() {
		return analysisVersion;
	}
	public void setAnalysisVersion(long analysisVersion) {
		this.analysisVersion = analysisVersion;
	}
	public long getParametersVersion() {
		return parametersVersion;
	}
	public void setParametersVersion(long parametersVersion) {
		this.parametersVersion = parametersVersion;
	}
	public long getConcentrationVersion() {
		return concentrationVersion;
	}
	public void setConcentrationVersion(long concentrationVersion) {
		this.concentrationVersion = concentrationVersion;
	}
	public long getKelFlagsVersion() {
		return kelFlagsVersion;
	}
	public void setKelFlagsVersion(long keyFlagsVersion) {
		this.kelFlagsVersion = keyFlagsVersion;
	}
}