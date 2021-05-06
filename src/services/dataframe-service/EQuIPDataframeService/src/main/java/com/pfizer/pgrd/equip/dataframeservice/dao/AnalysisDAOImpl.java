package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframeservice.dto.AnalysisDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.extractor.AnalysisExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.ResultSetExtractor;

public class AnalysisDAOImpl extends ModeShapeDAO implements AnalysisDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisDAOImpl.class);

	private static final String ALIAS = "analysis";
	private static final String NODE = "equip:analysis";
	
	@Override
	public Analysis getAnalysis(String analysisId) {
		Analysis a = null;
		if(analysisId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			AnalysisDTO dto = client.getNode(AnalysisDTO.class, analysisId);
			if(dto != null) {
				a = dto.toAnalysis();
				
				a.setKelFlagsDataframeId(this.fetchId(a.getKelFlagsDataframeId()));
				a.setLibraryReferences(this.fetchId(a.getLibraryReferences()));
				a.setModelConfigurationDataframeId(this.fetchId(a.getModelConfigurationDataframeId()));
				a.setParametersDataframeId(this.fetchId(a.getParametersDataframeId()));
				a.setSubsetDataframeIds(this.fetchId(a.getSubsetDataframeIds()));
				a.setEstimatedConcDataframeId(this.fetchId(a.getEstimatedConcDataframeId()));
				
				a.setAssemblyIds(this.fetchId(a.getAssemblyIds()));
				a.setDataframeIds(this.fetchId(a.getDataframeIds()));
				a.setReportingItemIds(this.fetchId(a.getReportingItemIds()));
				
				a.setProgramIds(this.fetchId(a.getProgramIds()));
				a.setParentIds(this.fetchId(a.getParentIds()));
				a.setProjectIds(this.fetchId(a.getProjectIds()));
				a.setProtocolIds(this.fetchId(a.getProtocolIds()));
				
				a.setConfigurationTemplateId(this.fetchId(a.getConfigurationTemplateId()));
			}
		}
		
		return a;
	}

	@Override
	public List<Analysis> getAnalysis(List<String> analysisIds) {
		List<Analysis> list = new ArrayList<>();
		if(analysisIds != null) {
			list = this.getAnalysis(analysisIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<Analysis> getAnalysis(String[] analysisIds) {
		List<Analysis> list = new ArrayList<>();
		if(analysisIds != null) {
			for(String id : analysisIds) {
				Analysis a = this.getAnalysis(id);
				if(a != null) {
					list.add(a);
				}
			}
		}
		
		return list;
	}

	@Override
	public List<Analysis> getAnalysisByStudyId(String studyId) {
		return this.getAnalysisByStudyId(new String[] { studyId });
	}

	@Override
	public List<Analysis> getAnalysisByStudyId(List<String> studyIds) {
		List<Analysis> list = new ArrayList<>();
		if(studyIds != null) {
			list = this.getAnalysisByStudyId(studyIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<Analysis> getAnalysisByStudyId(String[] studyIds) {
		return this.getByStringProperty("equip:studyId", studyIds, NODE, ALIAS, this.getResultSetExtractor());
	}

	@Override
	public Analysis insertAnalysis(Analysis analysis) {
		Analysis newAnalysis = null;
		if(analysis != null && analysis.getStudyIds() != null && !analysis.getStudyIds().isEmpty()) {
			String studyId = analysis.getStudyIds().get(0);
			if(studyId != null) {
				String path = this.constructPath(studyId, "Analyses");
				if(path != null) {
					AnalysisDTO dto = new AnalysisDTO(analysis);
					dto.setNodeName(dto.generateNodeName());
					
					ModeShapeClient client = this.getModeShapeClient();
					path += dto.getNodeName();
					try {
						dto = client.postNode(dto, path, true);
						if(dto != null) {
							newAnalysis = this.getAnalysis(dto.getJcrId());
						}
					}
					catch(ModeShapeAPIException maie) {
						LOGGER.error("", maie);
						throw new RuntimeException("Persistence layer exception upon analysis insertion");
					}
				}
			}
		}
		
		return newAnalysis;
	}

	@Override
	public boolean checkProgramPath(String programCode) {
		boolean result = false;

		ModeShapeClient client = this.getModeShapeClient();
			String walkingPath = client.getBaseUri() + "/items/Programs/" + programCode;
			ModeShapeNode programFolder = client.getNodeByPath(walkingPath, false);
			if (programFolder != null) {
				result = true;
			}
		
		return result;
	}
	
	@Override
	public void updateAnalysis(Analysis analysis) {
		if(analysis != null && analysis.getId() != null) {
			AnalysisDTO dto = new AnalysisDTO(analysis);
		}
	}
	
	@Override
	public List<Analysis> getAnalysisByEquipId(String equipId) {
		return this.getAnalysisByEquipId(new String[] { equipId });
	}
	
	@Override
	public List<Analysis> getAnalysisByEquipId(List<String> equipIds) {
		List<Analysis> list = new ArrayList<>();
		if(equipIds != null) {
			list = this.getAnalysisByEquipId(equipIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<Analysis> getAnalysisByEquipId(String[] equipIds) {
		return this.getByStringProperty("equip:equipId", equipIds, NODE, ALIAS, this.getResultSetExtractor());
	}
	
	private ResultSetExtractor<Analysis> getResultSetExtractor() {
		AnalysisExtractor ae = new AnalysisExtractor();
		ae.setAlias(ALIAS);
		return ae;
	}
}