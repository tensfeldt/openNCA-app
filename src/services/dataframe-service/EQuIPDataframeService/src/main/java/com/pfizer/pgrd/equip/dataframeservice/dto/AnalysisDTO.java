package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public class AnalysisDTO extends AssemblyDTO {
	public static final String PRIMARY_TYPE = "equip:analysis";
	
	@Expose
	@SerializedName("equip:analysisType")
	private String analysisType;
	
	@Expose
	@SerializedName("equip:subsetDataframeIds")
	private List<String> subsetDataframeIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:kelFlagsDataframeId")
	private String kelFlagsDataframeId;
	
	@Expose
	@SerializedName("equip:parametersDataframeId")
	private String parametersDataframeId;
	
	@Expose
	@SerializedName("equip:modelConfigurationDataframeId")
	private String modelConfigurationDataframeId;
	
	@Expose
	@SerializedName("equip:estimatedConcDataframeId")
	private String estimatedConcDataframeId;
	
	@Expose
	@SerializedName("equip:configurationTemplateId")
	private String configurationTemplateId;
	
	public AnalysisDTO() {
		this(null);
	}
	
	public AnalysisDTO(Analysis analysis) {
		super();
		this.setPrimaryType(AnalysisDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(analysis);
		this.setAssemblyType(Assembly.ANALYSIS_TYPE);
	}
	
	public void populate(Analysis analysis) {
		if(analysis != null) {
			this.populate((Assembly)analysis);
			
			this.setAnalysisType(analysis.getAnalysisType());
			this.setSubsetDataframeIds(analysis.getSubsetDataframeIds());
			this.setKelFlagsDataframeId(analysis.getKelFlagsDataframeId());
			this.setParametersDataframeId(analysis.getParametersDataframeId());
			this.setModelConfigurationDataframeId(analysis.getModelConfigurationDataframeId());
			this.setConfigurationTemplateId(analysis.getConfigurationTemplateId());
			this.setEstimatedConcDataframeId(analysis.getEstimatedConcDataframeId());
		}
	}
	
	@Override
	public String generateNodeName() {
		return "AN-" + new Date().getTime() + "-" + Thread.currentThread().getId();
	}
	
	@Override
	public EquipObject toEquipObject() {
		Analysis analysis = new Analysis();
		this.populateAssembly(analysis);
		
		analysis.setAnalysisType(this.getAnalysisType());
		analysis.setSubsetDataframeIds(this.getSubsetDataframeIds());
		analysis.setKelFlagsDataframeId(this.getKelFlagsDataframeId());
		analysis.setParametersDataframeId(this.getParametersDataframeId());
		analysis.setModelConfigurationDataframeId(this.getModelConfigurationDataframeId());
		analysis.setConfigurationTemplateId(this.getConfigurationTemplateId());
		analysis.setEstimatedConcDataframeId(this.getEstimatedConcDataframeId());
		
		CommentDTO c = this.getAnalysisSaveErrorComment();
		if(c != null) {
			analysis.setAnalysisSaveError(c.getBody());
		}
		
		return analysis;
	}
	
	public Analysis toAnalysis() {
		return (Analysis) this.toEquipObject();
	}
	
	public static List<AnalysisDTO> fromAnalysis(List<Analysis> analyses) {
		List<AnalysisDTO> list = new ArrayList<>();
		for(Analysis a : analyses) {
			AnalysisDTO dto = new AnalysisDTO(a);
			list.add(dto);
		}
		
		return list;
	}
	
	public static List<Analysis> toAnalysis(List<AnalysisDTO> analyses) {
		List<Analysis> list = new ArrayList<>();
		for(AnalysisDTO dto : analyses) {
			list.add(dto.toAnalysis());
		}
		
		return list;
	}
	
	public CommentDTO getAnalysisSaveErrorComment() {
		for(CommentDTO c : this.getComments()) {
			if(c.getCommentType().equalsIgnoreCase(Comment.ANALYSIS_SAVE_ERROR_TYPE)) {
				return c;
			}
		}
		
		return null;
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
}
