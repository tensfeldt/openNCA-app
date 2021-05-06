package com.pfizer.pgrd.equip.dataframe.dto.analysisqc;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryMCT;

public class MCT extends Dataframe {
	private LibraryMCT configurationTemplateDetails;
	
	public MCT() {
		this(null);
	}
	public MCT(Dataframe mct) {
		super();
		this.populate(mct);
	}
	
	public void populate(Dataframe mct) {
		if(mct == null) {
			return;
		}
		
		this.setAssemblyIds(mct.getAssemblyIds());
		this.setBatchId(mct.getBatchId());
		this.setComments(mct.getComments());
		this.setCommitted(mct.isCommitted());
		this.setCreated(mct.getCreated());
		this.setCreatedBy(mct.getCreatedBy());
		this.setDataBlindingStatus(mct.getDataBlindingStatus());
		this.setDataframeIds(mct.getDataframeIds());
		this.setDataframeType(mct.getDataframeType());
		this.setDataset(mct.getDataset());
		this.setDataStatus(mct.getDataStatus());
		this.setDeleteFlag(mct.isDeleteFlag());
		this.setDescription(mct.getDescription());
		this.setEntityType(mct.getEntityType());
		this.setEquipId(mct.getEquipId());
		this.setId(mct.getId());
		this.setItemType(mct.getItemType());
		this.setLocked(mct.isLocked());
		this.setLockedByUser(mct.getLockedByUser());
		this.setMetadata(mct.getMetadata());
		this.setModifiedBy(mct.getModifiedBy());
		this.setModifiedDate(mct.getModifiedDate());
		this.setName(mct.getName());
		this.setObsoleteFlag(mct.isObsoleteFlag());
		this.setOutputFileName(mct.getOutputFileName());
		this.setProfileConfig(mct.getProfileConfig());
		this.setProgramIds(mct.getProgramIds());
		this.setProjectIds(mct.getProjectIds());
		this.setPromotions(mct.getPromotions());
		this.setPromotionStatus(mct.getPromotionStatus());
		this.setProtocolIds(mct.getProtocolIds());
		this.setPublished(mct.isPublished());
		this.setQcStatus(mct.getQcStatus());
		this.setReleased(mct.isReleased());
		this.setRestrictionStatus(mct.getRestrictionStatus());
		this.setScript(mct.getScript());
		this.setStudyIds(mct.getStudyIds());
		this.setSubType(mct.getSubType());
		this.setVersionNumber(mct.getVersionNumber());
		this.setVersionSuperSeded(mct.getVersionSuperSeded());
	}
	public LibraryMCT getConfigurationTemplateDetails() {
		return configurationTemplateDetails;
	}
	public void setConfigurationTemplateDetails(LibraryMCT configurationTemplateDetails) {
		this.configurationTemplateDetails = configurationTemplateDetails;
	}
}
