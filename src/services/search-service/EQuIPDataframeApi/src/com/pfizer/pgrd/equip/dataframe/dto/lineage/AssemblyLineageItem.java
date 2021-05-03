package com.pfizer.pgrd.equip.dataframe.dto.lineage;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;

public class AssemblyLineageItem extends LineageItem {
	private String assemblyType;
	private List<DataframeLineageItem> memberDataframes = new ArrayList<>();
	private List<String> parentIds = new ArrayList<>();
	private String modelConfigurationDataframeId;
	private String kelFlagsDataframeId;
	private String parametersDataframeId;
	private String parametersEquipId;
	private String estimatedConcDataframeId;
	private String analysisSaveError;
	private String memberDataframeType;

	private DataframeLineageItem primaryParameters;
	private DataframeLineageItem mct;
	private DataframeLineageItem kel;
	private DataframeLineageItem estimatedConcentraction;
	
	public AssemblyLineageItem() {
		this.setNodeType("Assembly");
	}
	
	public static AssemblyLineageItem fromAssembly(Assembly assembly) {
		AssemblyLineageItem item = null;
		if(assembly != null) {
			item = new AssemblyLineageItem();
			item.setEquipId(assembly.getEquipId());
			item.setEquipVersion(assembly.getVersionNumber());
			item.setId(assembly.getId());
			item.setLastModifiedBy(assembly.getModifiedBy());
			item.setLastModifiedDate(assembly.getModifiedDate());
			item.setAssemblyType(assembly.getAssemblyType());
			item.setVersionComitted(assembly.isCommitted());
			item.setVersionSuperseded(assembly.getVersionSuperSeded());
			item.setLocked(assembly.isLocked());
			item.setParentIds(assembly.getParentIds());
			item.setStudyIds(assembly.getStudyIds());
			item.setCreatedBy(assembly.getCreatedBy());
			item.setCreatedDate(assembly.getCreated());
			item.setDeleted(assembly.isDeleteFlag());
			item.setComments(assembly.getComments());
			item.setQcStatus(assembly.getQcStatus());
			item.setMetadata(assembly.getMetadata());
			item.setName(assembly.getName());
			item.setSubType(assembly.getSubType());
			item.setItemType(assembly.getItemType());
			
			if(!assembly.getMemberDataframes().isEmpty()) {
				Dataframe mem = assembly.getMemberDataframes().get(0);
				item.setMemberDataframeType(mem.getDataframeType());
			}
			
			if(assembly.getLockedByUser() != null) {
				String lbu = assembly.getLockedByUser().trim();
				if(!lbu.isEmpty()) {
					item.setLockedByUser(lbu);
				}
			}
			
			if(assembly.isPublished()) {
				item.setPublishStatus("Published");
			}
			if(item.getLastModifiedBy() == null) {
				item.setLastModifiedBy(assembly.getCreatedBy());
			}
			if(item.getLastModifiedDate() == null) {
				item.setLastModifiedDate(assembly.getCreated());
			}
			
			if(assembly instanceof Analysis) {
				Analysis an = (Analysis) assembly;
				item.setModelConfigurationDataframeId(an.getModelConfigurationDataframeId());
				item.setKelFlagsDataframeId(an.getKelFlagsDataframeId());
				item.setParametersDataframeId(an.getParametersDataframeId());
				item.setEstimatedConcDataframeId(an.getEstimatedConcDataframeId());
				
				Comment c = an.getSaveErrorComment();
				if(c != null) {
					item.setAnalysisSaveError(c.getBody());
				}
			}
		}
		
		return item;
	}
	
	public AssemblyLineageItem clone() {
		return this.clone(true);
	}
	
	public AssemblyLineageItem clone(boolean includeChildren) {
		AssemblyLineageItem clone = this.createClone();
		this.populateClone(clone, includeChildren, false);
		return clone;
	}
	
	public AssemblyLineageItem deepClone() {
		AssemblyLineageItem clone = this.createClone();
		this.populateClone(clone, true, true);
		return clone;
	}
	
	private AssemblyLineageItem createClone() {
		AssemblyLineageItem clone = new AssemblyLineageItem();
		clone.setAssemblyType(this.getAssemblyType());
		clone.setParentIds(this.getParentIds());
		clone.setMemberDataframes(this.getMemberDataframes());
		clone.setModelConfigurationDataframeId(this.getModelConfigurationDataframeId());
		clone.setKelFlagsDataframeId(this.getKelFlagsDataframeId());
		clone.setParametersDataframeId(this.getParametersDataframeId());
		clone.setParametersEquipId(this.getParametersEquipId());
		clone.setEstimatedConcDataframeId(this.getEstimatedConcDataframeId());
		clone.setMct(this.getMct());
		clone.setPrimaryParameters(this.getPrimaryParameters());
		clone.setKel(this.getKel());
		clone.setEstimatedConcentraction(this.getEstimatedConcentraction());
		clone.setAnalysisSaveError(this.getAnalysisSaveError());
		clone.setMemberDataframeType(this.getMemberDataframeType());
		
		return clone;
	}
	
	public String getAnalysisSaveError() {
		return analysisSaveError;
	}

	public void setAnalysisSaveError(String analysisSaveError) {
		this.analysisSaveError = analysisSaveError;
	}
	public String getAssemblyType() {
		return assemblyType;
	}
	public void setAssemblyType(String assemblyType) {
		this.assemblyType = assemblyType;
	}
	public List<DataframeLineageItem> getMemberDataframes() {
		return memberDataframes;
	}
	public void setMemberDataframes(List<DataframeLineageItem> memberDataframes) {
		this.memberDataframes = memberDataframes;
	}
	@Override
	public List<String> getParentIds() {
		return this.parentIds;
	}
	public void setParentIds(List<String> parentIds) {
		this.parentIds = parentIds;
	}
	public String getModelConfigurationDataframeId() {
		return modelConfigurationDataframeId;
	}
	public void setModelConfigurationDataframeId(String modelConfigurationDataframeId) {
		this.modelConfigurationDataframeId = modelConfigurationDataframeId;
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
	public String getParametersEquipId() {
		return parametersEquipId;
	}
	public void setParametersEquipId(String parametersEquipId) {
		this.parametersEquipId = parametersEquipId;
	}
	public String getEstimatedConcDataframeId() {
		return estimatedConcDataframeId;
	}
	public void setEstimatedConcDataframeId(String estimatedConcDataframeId) {
		this.estimatedConcDataframeId = estimatedConcDataframeId;
	}
	public List<String> getDataframeIds() {
		List<String> ids = new ArrayList<>();
		for(DataframeLineageItem dli : this.memberDataframes) {
			ids.add(dli.getId());
		}
		return ids;
	}

	public DataframeLineageItem getPrimaryParameters() {
		return primaryParameters;
	}

	public void setPrimaryParameters(DataframeLineageItem primaryParameters) {
		this.primaryParameters = primaryParameters;
	}

	public DataframeLineageItem getMct() {
		return mct;
	}

	public void setMct(DataframeLineageItem mct) {
		this.mct = mct;
	}
	
	public DataframeLineageItem getKel() {
		return kel;
	}

	public void setKel(DataframeLineageItem kel) {
		this.kel = kel;
	}
	
	public DataframeLineageItem getEstimatedConcentraction() {
		return estimatedConcentraction;
	}
	
	public void setEstimatedConcentraction(DataframeLineageItem estimatedConcentraction) {
		this.estimatedConcentraction = estimatedConcentraction;
	}

	public String getMemberDataframeType() {
		return memberDataframeType;
	}

	public void setMemberDataframeType(String memberDataframeType) {
		this.memberDataframeType = memberDataframeType;
	}
}
