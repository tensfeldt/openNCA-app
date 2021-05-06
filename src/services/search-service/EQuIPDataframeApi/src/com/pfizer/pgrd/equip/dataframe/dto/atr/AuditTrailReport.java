package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

public class AuditTrailReport {
	private List<AnalysisData> analysisData = new ArrayList<>();
	private List<DataframeInfo> dataframes = new ArrayList<>();
	private List<DataframeInfo> dataframesInfo = new ArrayList<>();
	private Program program;
	private List<Analysis> primaryAnalyses = new ArrayList<>();
	private List<Analysis> secondaryAnalyses = new ArrayList<>();
	private List<Analysis> legacyAnalyses = new ArrayList<>();
	
	private ReportingEvent reportingEvent;
	
	private List<DataLoad> dataLoads = new ArrayList<>();
	private List<PromotedDataTransformation> promotedDataTransformations = new ArrayList<>();
	private List<DataTransformation> dataTransformations = new ArrayList<>();
	private List<ExcludedSubject> excludedSubjects = new ArrayList<>();
	private List<ExcludedProfile> excludedProfiles = new ArrayList<>();
	
	private String clientName;
	private String clientVersion;
	
	public Program getProgram() {
		return program;
	}
	public void setProgram(Program program) {
		this.program = program;
	}
	public List<DataLoad> getDataLoads() {
		return dataLoads;
	}
	public void setDataLoads(List<DataLoad> dataLoads) {
		this.dataLoads = dataLoads;
	}
	public List<PromotedDataTransformation> getPromotedDataTransformations() {
		return promotedDataTransformations;
	}
	public void setPromotedDataTransformations(List<PromotedDataTransformation> promotedDataTransformations) {
		this.promotedDataTransformations = promotedDataTransformations;
	}
	public List<DataTransformation> getDataTransformations() {
		return dataTransformations;
	}
	public void setDataTransformations(List<DataTransformation> dataTransformations) {
		this.dataTransformations = dataTransformations;
	}
	public List<ExcludedSubject> getExcludedSubjects() {
		return excludedSubjects;
	}
	public void setExcludedSubjects(List<ExcludedSubject> excludedSubjects) {
		this.excludedSubjects = excludedSubjects;
	}
	public List<ExcludedProfile> getExcludedProfiles() {
		return excludedProfiles;
	}
	public void setExcludedProfiles(List<ExcludedProfile> excludedProfiles) {
		this.excludedProfiles = excludedProfiles;
	}
	public ReportingEvent getReportingEvent() {
		return reportingEvent;
	}
	public void setReportingEvent(ReportingEvent reportingEvent) {
		this.reportingEvent = reportingEvent;
	}
	public List<Analysis> getPrimaryAnalyses() {
		return primaryAnalyses;
	}
	public void setPrimaryAnalyses(List<Analysis> primaryAnalyses) {
		this.primaryAnalyses = primaryAnalyses;
	}
	public List<Analysis> getSecondaryAnalyses() {
		return secondaryAnalyses;
	}
	public void setSecondaryAnalyses(List<Analysis> secondaryAnalyses) {
		this.secondaryAnalyses = secondaryAnalyses;
	}
	
	public DataLoad getDataLoad(String equipId) {
		for(DataLoad dl : this.dataLoads) {
			if(dl.getEquipId().equalsIgnoreCase(equipId)) {
				return dl;
			}
		}
		
		return null;
	}
	
	public DataTransformation getDataTransformation(String equipId) {
		for(DataTransformation dt : this.dataTransformations) {
			if(dt.getEquipId().equalsIgnoreCase(equipId)) {
				return dt;
			}
		}
		
		return null;
	}
	
	public Analysis getPrimaryAnalysis(String equipId) {
		for(Analysis a : this.primaryAnalyses) {
			if(a.getEquipId().equalsIgnoreCase(equipId)) {
				return a;
			}
		}
		
		return null;
	}
	
	public Analysis getSecondaryAnalysis(String equipId) {
		for(Analysis a : this.secondaryAnalyses) {
			if(a.getEquipId().equalsIgnoreCase(equipId)) {
				return a;
			}
		}
		
		return null;
	}
	
	public Analysis getLegacyAnalysis(String equipId) {
		for(Analysis a : this.legacyAnalyses) {
			if(a.getEquipId().equalsIgnoreCase(equipId)) {
				return a;
			}
		}
		
		return null;
	}
	
	public List<AnalysisData> getAnalysisData() {
		return analysisData;
	}
	public void setAnalysisData(List<AnalysisData> analysisData) {
		this.analysisData = analysisData;
	}
	public List<DataframeInfo> getDataframes() {
		return dataframes;
	}
	public void setDataframes(List<DataframeInfo> dataframes) {
		this.dataframes = dataframes;
	}
	public List<Analysis> getLegacyAnalyses() {
		return legacyAnalyses;
	}
	public void setLegacyAnalyses(List<Analysis> legacyAnalyses) {
		this.legacyAnalyses = legacyAnalyses;
	}
	public List<DataframeInfo> getDataframesInfo() {
		return dataframesInfo;
	}
	public void setDataframesInfo(List<DataframeInfo> dataframesInfo) {
		this.dataframesInfo = dataframesInfo;
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
	public void setClientVersion(String clientVerison) {
		this.clientVersion = clientVerison;
	}
}