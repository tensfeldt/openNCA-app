package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipLockable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipSearchable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipStudyable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

public class Dataframe extends EquipObject implements EquipVersionable, EquipCommentable, EquipID, EquipStudyable,
		EquipCreatable, EquipModifiable, EquipMetadatable, EquipLockable, EquipSearchable {
	
	public static final String ENTITY_TYPE = "Dataframe";

	public static final String DATASET_TYPE = "Dataset", DATA_TRANSFORMATION_TYPE = "Data Transformation", REPORT_TYPE = "Report",
			REPORT_ITEM_TYPE = "Report Item", SUBSET_TYPE = "Subset", KEL_FLAGS_TYPE = "KEL Flags",
			PRIMARY_PARAMETERS_TYPE = "Primary Parameters", DERIVED_PARAMETERS_TYPE = "Derived Parameters",
			MODEL_CONFIGURATION_TEMPLATE_TYPE = "Model Configuration Template",
			SECONDARY_CONFIGURATION_TEMPLATE_TYPE = "Secondary Configuration Template", DATA_PREP_PREFIX = "DataPrep",
			ANALYSIS_DATA_PREFIX = "AnalysisData", ATTACHMENT_TYPE = "Attachment",
			PROFILE_SETTINGS_TYPE = "Profile Settings", ATR_SUB_TYPE = "ATR", ANALYSIS_QC_REPORT_SUB_TYPE = "Analysis QC",
			CONCENTRATION_DATA_TYPE = "Concentration Data", ESTIMATED_CONCENTRATION_DATA_TYPE = "Estimated " + CONCENTRATION_DATA_TYPE;

	// Dataframe specific
	private Script script;
	private Dataset dataset;
	private String promotionStatus;
	private List<Promotion> promotions = new ArrayList<>();
	private String qcStatus = "Not QC'd";
	private List<String> dataframeIds = new ArrayList<>();
	private List<String> assemblyIds = new ArrayList<>();
	private List<String> studyIds = new ArrayList<>();
	private String restrictionStatus;
	private String dataBlindingStatus;
	private String dataStatus;
	private String dataframeType;
	private String name;
	private String itemType;
	private List<String> profileConfig = new ArrayList<>();
	private String batchId;


	// EquipCreatable
	private Date created;
	private String createdBy;

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();

	// EquipVersionable
	private long versionNumber;
	private boolean versionSuperSeded;
	private boolean deleteFlag = false;
	private boolean obsoleteFlag;
	private boolean isCommitted;
	
	//EquipSearchable
	private boolean published = false;
	private boolean released = false;
	private String description;
	private String subType;

	// EquipLockable
	private boolean isLocked;
	private String lockedByUser;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();

	// EquipStudyable
	private List<String> protocolIds = new ArrayList<>();
	private List<String> projectIds = new ArrayList<>();
	private List<String> programIds = new ArrayList<>();

	// EquipID
	private String equipId;
	
	public Dataframe() {
		this.setEntityType(Dataframe.ENTITY_TYPE);
	}
	
	public void setCEVersion(String ceVersion) {
		Metadatum md = this.getMetadatum(Metadatum.CE_VERSION_KEY);
		if(md == null) {
			md = new Metadatum();
			md.setKey(Metadatum.CE_VERSION_KEY);
			this.getMetadata().add(md);
		}
		
		md.setValueType(ceVersion);
	}
	
	public String getCEVersion() {
		return this.getMetadatumValue(Metadatum.CE_VERSION_KEY);
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getPromotionStatus() {
		return promotionStatus;
	}

	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public String getRestrictionStatus() {
		return restrictionStatus;
	}

	public void setRestrictionStatus(String restrictionStatus) {
		this.restrictionStatus = restrictionStatus;
	}

	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}

	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public List<Promotion> getPromotions() {
		return promotions;
	}

	public void setPromotions(List<Promotion> promotions) {
		this.promotions = promotions;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public List<String> getDataframeIds() {
		return dataframeIds;
	}

	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframeIds = dataframeIds;
	}

	public List<String> getAssemblyIds() {
		return assemblyIds;
	}

	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblyIds = assemblyIds;
	}

	public String getDataframeType() {
		return dataframeType;
	}

	public void setDataframeType(String dataframeType) {
		this.dataframeType = dataframeType;
	}

	@Override
	public Boolean getVersionSuperSeded() {
		return versionSuperSeded;
	}

	@Override
	public void setVersionSuperSeded(Boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}

	@Override
	public long getVersionNumber() {
		return versionNumber;
	}

	@Override
	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	@Override
	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	@Override
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	@Override
	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}

	@Override
	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}

	@Override
	public List<Comment> getComments() {
		return this.comments;
	}

	@Override
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	@Override
	public String getEquipId() {
		return this.equipId;
	}

	@Override
	public void setEquipId(String equipId) {
		boolean valid = true;
		if(equipId != null) {
			equipId = equipId.trim();
			if(equipId.isEmpty()) {
				valid = false;
			}
		}
		else {
			valid = false;
		}
		
		if(!valid) {
			this.equipId = "UNKNOWN";
		}
		else {
			this.equipId = equipId;
		}
	}
	
	public void overrideEquipId(String equipId) {
		this.equipId = equipId;
	}

	public List<String> getProtocolIds() {
		return protocolIds;
	}

	public void setProtocolIds(List<String> protocolIds) {
		this.protocolIds = protocolIds;
	}

	public List<String> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}

	public List<String> getProgramIds() {
		return programIds;
	}

	public void setProgramIds(List<String> programIds) {
		this.programIds = programIds;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public List<String> getStudyIds() {
		return studyIds;
	}

	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}

	public List<String> getProfileConfig() {
		return profileConfig;
	}

	public void setProfileConfig(List<String> profileConfig) {
		this.profileConfig = profileConfig;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser (String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	@Override
	public Metadatum getMetadatum(String key) {
		Metadatum metadatum = null;
		if (this.metadata != null && key != null) {
			for (Metadatum md : this.metadata) {
				if (md.getKey().equalsIgnoreCase(key)) {
					metadatum = md;
					break;
				}
			}
		}
		
		return metadatum;
	}
	
	@Override
	public String getMetadatumValue(String key) {
		String value = null;
		Metadatum md = this.getMetadatum(key);
		if(md != null && md.getValue() != null && !md.getValue().isEmpty()) {
			return md.getValue().get(0);
		}
		
		return value;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}
	
	public String getPrettyFileName() {
		return this.getPrettyFileName(true);
	}
	
	/**
	 * Returns the prettified version of the file name. This will be in the form {@code <Protocol>-<EquipID>-v<VersionNumber>}. If the output file name 
	 * metatdata value exists and does not begin with "OUTPUT" (case insensitive), then it will be appended to the end in the form {@code -<OutputFileName>}.
	 * Note: The file extension will not be added.
	 * @return {@link String} the prettified file name
	 */
	public String getPrettyFileName(boolean includeExt) {
		String fileName = null;
		if(this.equipId != null && !this.studyIds.isEmpty()) {
			String protocols = "";
			for(String studyId : this.studyIds) {
				if(studyId != null) {
					String protocol = studyId;
					if(protocol.contains(":") ) {
						String results[] = protocol.split(":");
						if(results.length > 1) {
							protocol = results[1];
						}
					}
					
					if(protocols != "") {
						protocols += "-";
					}
					protocols += protocol;
				}
			}
			
			fileName = protocols + "-" + this.equipId + "-v" + this.versionNumber;
			
			includeExt = true;
			String outputFileName = this.getOutputFileName(includeExt);
			if(outputFileName != null && !outputFileName.toLowerCase().startsWith("output")) {
				fileName += "-" + outputFileName;
			}
		}
		
		String[] invalidCharacters = new String[] { "\\\\", "/", "\\?", ":", "\\*", "<", ">", "\\|", ",", " " };
		for(String ic : invalidCharacters) {
			String rc = "-";
			if(ic.equals(" ")) {
				rc = "_";
			}
			
			fileName = fileName.replaceAll(ic, rc);
		}
		
		return fileName;
	}
	
	public String getOutputFileName() {
		return this.getMetadatumValue("output filename");
	}
	
	public String getOutputFileName(boolean includeExt) {
		String ofn = this.getOutputFileName();
		if(ofn != null && !includeExt && ofn.contains(".")) {
			String[] parts = ofn.split("\\.");
			ofn = "";
			for(int i = 0; i < parts.length - 1; i++) {
				if(i > 0) {
					ofn += ".";
				}
				ofn += parts[i];
			}
		}
		
		return ofn;
	}
	
	public void setOutputFileName(String outputFileName) {
		String key = "output filename";
		Metadatum md = this.getMetadatum(key);
		if(md == null) {
			md = new Metadatum();
			md.setKey(key);
			this.getMetadata().add(md);
		}
		
		md.getValue().add(outputFileName);
	}
	
	public String getOutputFileExtension() { 
		String ext = null;
		String ofn = this.getOutputFileName();
		if(ofn != null && ofn.contains(".")) {
			String[] parts = ofn.split("\\.");
			ext = parts[parts.length - 1];
		}
		
		return ext;
	}
	
	public String getFileName() {
		String fileName = null;
		Metadatum md = this.getMetadatum("File Name Full");
		if(md != null && md.getValue().size() > 0) {
			fileName = md.getValue().get(0);
		}
		else {
			md = this.getMetadatum("File Name Short");
			if(md != null && md.getValue().size() > 0) {
				fileName = md.getValue().get(0);
			}
			else if(this.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
				fileName = this.getName();
			}
		}
		
		return fileName;
	}

	@Override
	public String getSubType() {
		return this.subType;
	}

	@Override
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	public List<String> getParentIds() {
		List<String> parentIds = new ArrayList<>();
		if(this.assemblyIds != null) {
			parentIds.addAll(this.assemblyIds);
		}
		if(this.dataframeIds != null) {
			parentIds.addAll(this.dataframeIds);
		}
		
		return parentIds;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
}