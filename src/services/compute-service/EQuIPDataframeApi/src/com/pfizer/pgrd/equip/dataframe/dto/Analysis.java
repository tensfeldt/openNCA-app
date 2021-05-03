package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Analysis extends Assembly {
	public static final String ENTITY_TYPE = Assembly.ANALYSIS_TYPE;
	
	private String analysisType;
	private List<String> subsetDataframeIds = new ArrayList<>();
	private String kelFlagsDataframeId;
	private String parametersDataframeId;
	private String modelConfigurationDataframeId;
	private String configurationTemplateId;
	private String estimatedConcDataframeId;
	private String analysisSaveError;
	
	public Analysis() {
		super();
		this.setAssemblyType(Assembly.ANALYSIS_TYPE);
		this.setEntityType(Analysis.ENTITY_TYPE);
	}
	
	public String getAnalysisSaveError() {
		return this.analysisSaveError;
	}
	public void setAnalysisSaveError(String analysisSaveError) {
		this.analysisSaveError = analysisSaveError;
	}
	public String getAnalysisType() {
		return analysisType;
	}
	public void setAnalysisType(String analysisType) {
		this.analysisType = analysisType;
	}
	public List<String> getSubsetDataframeIds() {
		return subsetDataframeIds;
	}
	public void setSubsetDataframeIds(List<String> subsetDataframeIds) {
		this.subsetDataframeIds = subsetDataframeIds;
	}
	public String getKelFlagsDataframeId() {
		return kelFlagsDataframeId;
	}
	public void setKelFlagsDataframeId(String kelFlagsDataframeId) {
		this.kelFlagsDataframeId = kelFlagsDataframeId;
	}
	public String getParametersDataframeId() {
		return parametersDataframeId;
	}
	public void setParametersDataframeId(String parametersDataframeId) {
		this.parametersDataframeId = parametersDataframeId;
	}
	public String getModelConfigurationDataframeId() {
		return modelConfigurationDataframeId;
	}
	public void setModelConfigurationDataframeId(String modelConfigurationDataframeId) {
		this.modelConfigurationDataframeId = modelConfigurationDataframeId;
	}
	public String getConfigurationTemplateId() {
		return configurationTemplateId;
	}
	public void setConfigurationTemplateId(String configurationTemplateId) {
		this.configurationTemplateId = configurationTemplateId;
	}
	public String getEstimatedConcDataframeId() {
		return estimatedConcDataframeId;
	}
	public void setEstimatedConcDataframeId(String estimatedConcDataframeId) {
		this.estimatedConcDataframeId = estimatedConcDataframeId;
	}
	public Comment getSaveErrorComment() {
		for(Comment c : this.getComments()) {
			if(c.getCommentType().equalsIgnoreCase(Comment.ANALYSIS_SAVE_ERROR_TYPE)) {
				return c;
			}
		}
		
		return null;
	}
	public void setSaveErrorComment(String body) {
		Comment c = null;
		if(body != null) {
			c = new Comment();
			c.setBody(body);
			c.setCreatedBy("System");
			c.setCreated(new Date());
		}
		
		this.setSaveErrorComment(c);
	}
	public void setSaveErrorComment(Comment c) {
		Comment comment = this.getSaveErrorComment();
		if(comment != null) {
			if(c == null) {
				this.getComments().remove(comment);
			}
			else {
				comment.setBody(c.getBody());
			}
		}
		else if(c != null) {
			c.setCommentType(Comment.ANALYSIS_SAVE_ERROR_TYPE);
			this.getComments().add(c);
		}
	}
	
	public void populate(Assembly assembly) {
		if(assembly != null) {
			this.setAssemblyIds(assembly.getAssemblyIds());
			this.setAssemblyType(assembly.getAssemblyType());
			this.setComments(assembly.getComments());
			this.setCommitted(assembly.isCommitted());
			this.setCreated(assembly.getCreated());
			this.setCreatedBy(assembly.getCreatedBy());
			this.setDataframeIds(assembly.getDataframeIds());
			this.setDeleteFlag(assembly.isDeleteFlag());
			this.setEquipId(assembly.getEquipId());
			this.setId(assembly.getId());
			this.setItemType(assembly.getItemType());
			this.setLibraryReferences(assembly.getLibraryReferences());
			this.setLoadStatus(assembly.getLoadStatus());
			this.setMetadata(assembly.getMetadata());
			this.setModifiedBy(assembly.getModifiedBy());
			this.setModifiedDate(assembly.getModifiedDate());
			this.setName(assembly.getName());
			this.setObsoleteFlag(assembly.isObsoleteFlag());
			this.setParentIds(assembly.getParentIds());
			this.setProgramIds(assembly.getProgramIds());
			this.setProjectIds(assembly.getProjectIds());
			this.setProtocolIds(assembly.getProtocolIds());
			this.setPublishItemIds(assembly.getPublishItemIds());
			this.setQcStatus(assembly.getQcStatus());
			this.setReportingEventStatusChangeWorkflows(assembly.getReportingEventStatusChangeWorkflows());
			this.setReportingItemIds(assembly.getReportingItemIds());
			this.setScripts(assembly.getScripts());
			this.setStudyIds(assembly.getStudyIds());
			this.setVersionNumber(assembly.getVersionNumber());
			this.setVersionSuperSeded(assembly.getVersionSuperSeded());
			this.setLocked(assembly.isLocked());
			this.setLockedByUser(assembly.getLockedByUser());
			
			if(assembly instanceof Analysis) {
				Analysis an = (Analysis) assembly;
				this.setModelConfigurationDataframeId(an.getModelConfigurationDataframeId());
				this.setKelFlagsDataframeId(an.getKelFlagsDataframeId());
				this.setParametersDataframeId(an.getParametersDataframeId());
				this.setConfigurationTemplateId(an.getConfigurationTemplateId());
				this.setSubsetDataframeIds(an.getSubsetDataframeIds());
				this.setAnalysisType(an.getAnalysisType());
				this.setEstimatedConcDataframeId(an.getEstimatedConcDataframeId());
				
				Comment ase = an.getSaveErrorComment();
				if(ase != null) {
					this.analysisSaveError = ase.getBody();
				}
			}
		}
	}
}