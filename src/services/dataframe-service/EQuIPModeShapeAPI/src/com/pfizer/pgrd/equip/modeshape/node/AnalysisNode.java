package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public class AnalysisNode extends AssemblyNode {
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
	@SerializedName("equip:configurationTemplateId")
	private String configurationTemplateId;
	
	public AnalysisNode() {
		this(null);
	}
	
	public AnalysisNode(Analysis analysis) {
		super();
		this.setPrimaryType(AnalysisNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(analysis);
		this.setAssemblyType("Analysis");
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
		}
	}
	
	@Override
	public String generateNodeName() {
		return "AN-" + new Date().getTime();
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
		
		return analysis;
	}
	
	public Analysis toAnalysis() {
		return (Analysis) this.toEquipObject();
	}
	
	public static List<AnalysisNode> fromAnalysis(List<Analysis> analyses) {
		List<AnalysisNode> list = new ArrayList<>();
		for(Analysis a : analyses) {
			AnalysisNode dto = new AnalysisNode(a);
			list.add(dto);
		}
		
		return list;
	}
	
	public static List<Analysis> toAnalysis(List<AnalysisNode> analyses) {
		List<Analysis> list = new ArrayList<>();
		for(AnalysisNode dto : analyses) {
			list.add(dto.toAnalysis());
		}
		
		return list;
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
}
