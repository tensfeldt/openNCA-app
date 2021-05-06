package com.pfizer.pgrd.equip.dataframe.dto.analysisqc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;

public class AnalysisQCReport {
	private List<String> protocols = new ArrayList<>();
	private String analysisEquipId;
	private long analysisVersion;
	private String analysisSaveError;
	private List<String> profileConfig = new ArrayList<>();
	private Date createdDate;
	private String createdBy;
	private List<AnalysisQCLineageItem> lineage = new ArrayList<>();
	private Analysis analysis;
	private String clientName;
	private String clientVersion;
	private String computeEngineVersion;
	private Integer wrapperScriptVersion;
	private String wrapperScriptName;
	private MCT mct;
	
	public List<String> getProtocols() {
		return protocols;
	}
	public void setProtocols(List<String> protocols) {
		this.protocols = protocols;
	}
	public String getAnalysisEquipId() {
		return analysisEquipId;
	}
	public void setAnalysisEquipId(String analysisEquipId) {
		this.analysisEquipId = analysisEquipId;
	}
	public long getAnalysisVersion() {
		return analysisVersion;
	}
	public void setAnalysisVersion(long analysisVersion) {
		this.analysisVersion = analysisVersion;
	}
	public List<String> getProfileConfig() {
		return profileConfig;
	}
	public void setProfileConfig(List<String> profileConfig) {
		this.profileConfig = profileConfig;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public List<AnalysisQCLineageItem> getLineage() {
		return lineage;
	}
	public void setLineage(List<AnalysisQCLineageItem> lineage) {
		this.lineage = lineage;
	}
	public String getAnalysisSaveError() {
		return analysisSaveError;
	}
	public void setAnalysisSaveError(String analysisSaveError) {
		this.analysisSaveError = analysisSaveError;
	}
	public Analysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	public Dataframe getMct() {
		return mct;
	}
	public void setMct(MCT mct) {
		this.mct = mct;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientVersion() {
		return clientVersion;
	}
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}
	public String getComputeEngineVersion() {
		return computeEngineVersion;
	}
	public void setComputeEngineVersion(String computeEngineVersion) {
		this.computeEngineVersion = computeEngineVersion;
	}
	public Integer getWrapperScriptVersion() {
		return wrapperScriptVersion;
	}
	public void setWrapperScriptVersion(Integer wrapperScriptVersion) {
		this.wrapperScriptVersion = wrapperScriptVersion;
	}
	public String getWrapperScriptName() {
		return wrapperScriptName;
	}
	public void setWrapperScriptName(String wrapperScriptName) {
		this.wrapperScriptName = wrapperScriptName;
	}
}