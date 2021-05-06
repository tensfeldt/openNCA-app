package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipLock;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;

public class DataframeDTO extends ModeShapeNode implements EquipCreated, EquipDelete, EquipID, EquipVersion, EquipLock {
	public static final String PRIMARY_TYPE = "equip:dataframe";
	
	@Expose
	@SerializedName("equip:dataframeType")
	private String dataframeType;
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:modified")
	private Date modified;
	
	@Expose
	@SerializedName("equip:modifiedBy")
	private String modifiedBy;
	
	@Expose
	@SerializedName("equip:equipId")
	private String equipId;
	
	@Expose
	@SerializedName("equip:deleteFlag")
	private boolean isDeleted;
	
	@Expose
	@SerializedName("equip:obsoleteFlag")
	private boolean isObsolete;
	
	@Expose
	@SerializedName("equip:lockFlag")
	private boolean isLocked;
	
	@Expose
	@SerializedName("equip:lockedByUser")
	private String lockedByUser;
	
	@Expose
	@SerializedName("equip:batchId")
	private String batchId;
	
	@Expose
	@SerializedName("equip:protocolIds")
	private List<String> protocolIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:projectIds")
	private List<String> projectIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:programIds")
	private List<String> programIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:assemblyIds")
	private List<String> assemblyIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:dataframeIds")
	private List<String> parentDataframeIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:dataStatus")
	private String dataStatus;
	
	@Expose
	@SerializedName("equip:promotionStatus")
	private String promotionStatus;
	
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String restrictionStatus;
	
	@Expose
	@SerializedName("equip:dataBlindingStatus")
	private String dataBlindingStatus;
	
	@Expose
	@SerializedName("equip:qcStatus")
	private String qcStatus;
	
	@Expose
	@SerializedName("equip:versionNumber")
	private long versionNumber;
	
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private boolean isSuperseded;
	
	@Expose
	@SerializedName("equip:versionCommitted")
	private boolean isCommitted;
	
	@Expose
	@SerializedName("equip:studyId")
	private List<String> studyId = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:profileConfig")
	private List<String> profileConfig = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:name")
	private String name;
	
	@Expose
	@SerializedName("equip:itemType")
	private String itemType;
	
	@Expose
	@SerializedName("equip:description")
	private String description;
	
	@Expose
	@SerializedName("equip:published")
	private boolean isPublished;
	
	@Expose
	@SerializedName("equip:released")
	private boolean isReleased;
	
	@Expose
	@SerializedName("equip:subType")
	private String subType;
	
	
	public DataframeDTO() {
		this(null);
	}
	
	public DataframeDTO(Dataframe dataframe) {
		super();
		this.setPrimaryType(DataframeDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(dataframe);
	}
	
	public static List<Dataframe> toDataframe(List<DataframeDTO> dataframes) {
		List<Dataframe> list = new ArrayList<>();
		if(dataframes != null) {
			for(DataframeDTO dto : dataframes) {
				Dataframe df = dto.toDataframe();
				list.add(df);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Dataframe dataframe = new Dataframe();
		dataframe.setAssemblyIds(this.getAssemblyIds());
		dataframe.setCreated(this.getCreated());
		dataframe.setCreatedBy(this.getCreatedBy());
		dataframe.setDataBlindingStatus(this.getDataBlindingStatus());
		dataframe.setDataframeIds(this.getParentDataframeIds());
		dataframe.setDataframeType(this.getDataframeType());
		dataframe.setDataStatus(this.getDataStatus());
		dataframe.setDeleteFlag(this.isDeleted());
		dataframe.setDescription(this.getDescription());
		dataframe.setEquipId(this.getEquipId());
		dataframe.setId(this.getJcrId());
		dataframe.setModifiedBy(this.getModifiedBy());
		dataframe.setModifiedDate(this.getModified());
		dataframe.setObsoleteFlag(this.isObsolete());
		dataframe.setLocked(this.isLocked());
		dataframe.setLockedByUser(this.lockedByUser);
		dataframe.setProgramIds(this.getProgramIds());
		dataframe.setProjectIds(this.getProjectIds());
		dataframe.setPromotionStatus(this.getPromotionStatus());
		dataframe.setProtocolIds(this.getProtocolIds());
		dataframe.setPublished(this.isPublished());
		dataframe.setQcStatus(this.getQcStatus());
		dataframe.setReleased(this.isReleased());
		dataframe.setRestrictionStatus(this.getRestrictionStatus());
		dataframe.setVersionNumber(this.getVersionNumber());
		dataframe.setVersionSuperSeded(this.isSuperseded());
		dataframe.setCommitted(this.isCommitted());
		dataframe.setStudyIds(this.getStudyId());
		dataframe.setProfileConfig(this.getProfileConfig());
		dataframe.setName(this.getName());
		dataframe.setItemType(this.getItemType());
		dataframe.setSubType(this.getSubType());
		dataframe.setBatchId(this.getBatchId());
		
		List<Comment> comments = CommentDTO.toComment(this.getComments());
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		List<Promotion> promotions = PromotionDTO.toPromotion(this.getPromotions());
		dataframe.setComments(comments);
		dataframe.setMetadata(metadata);
		dataframe.setPromotions(promotions);
		
		ModeShapeNode dataset = this.getChild("equip:dataset");
		if(dataset != null && dataset instanceof DatasetDTO) {
			Dataset ds = ((DatasetDTO)dataset).toDataset();
			dataframe.setDataset(ds);
		}
		
		ModeShapeNode script = this.getChild("equip:script");
		if(script != null && script instanceof ScriptDTO) {
			Script s = ((ScriptDTO)script).toScript();
			dataframe.setScript(s);
		}
		
		return dataframe;
	}
	
	private String getItemType() {
		return itemType;
	}

	private String getName() {
		return name;
	}

	public Dataframe toDataframe() {
		return (Dataframe) this.toEquipObject();
	}
	
	public static List<DataframeDTO> fromDataframe(List<Dataframe> dataframes) {
		List<DataframeDTO> list = new ArrayList<>();
		if(dataframes != null) {
			for(Dataframe df : dataframes) {
				DataframeDTO dto = new DataframeDTO(df);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Dataframe dataframe) {
		if(dataframe != null) {
			this.setAssemblyIds(dataframe.getAssemblyIds());
			this.setCreated(dataframe.getCreated());
			this.setCreatedBy(dataframe.getCreatedBy());
			this.setDataBlindingStatus(dataframe.getDataBlindingStatus());
			this.setDataframeType(dataframe.getDataframeType());
			this.setDataStatus(dataframe.getDataStatus());
			this.setDeleted(dataframe.isDeleteFlag());
			this.setDescription(dataframe.getDescription());
			this.setEquipId(dataframe.getEquipId());
			this.setModified(dataframe.getModifiedDate());
			this.setModifiedBy(dataframe.getModifiedBy());
			this.setObsolete(dataframe.isObsoleteFlag());
			this.setLocked(dataframe.isLocked());
			this.setLockedByUser(dataframe.getLockedByUser());
			this.setParentDataframeIds(dataframe.getDataframeIds());;
			this.setProgramIds(dataframe.getProgramIds());
			this.setProjectIds(dataframe.getProjectIds());
			this.setPromotionStatus(dataframe.getPromotionStatus());
			this.setProtocolIds(dataframe.getProtocolIds());
			this.setPublished(dataframe.isPublished());
			this.setQcStatus(dataframe.getQcStatus());
			this.setReleased(dataframe.isReleased());
			this.setRestrictionStatus(dataframe.getRestrictionStatus());
			this.setVersionNumber(dataframe.getVersionNumber());
			this.setSuperseded(dataframe.getVersionSuperSeded());
			this.setCommitted(dataframe.isCommitted());
			this.setStudyId(dataframe.getStudyIds());
			this.setProfileConfig(dataframe.getProfileConfig());
			String test = dataframe.getName();
			this.setName(test);
			this.setName(dataframe.getName());
			this.setItemType(dataframe.getItemType());
			this.setSubType(dataframe.getSubType());
			this.setBatchId(dataframe.getBatchId());
			
			List<CommentDTO> comments = CommentDTO.fromComment(dataframe.getComments());
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(dataframe.getMetadata());
			List<PromotionDTO> promotions = PromotionDTO.fromPromotion(dataframe.getPromotions());
			this.setComments(comments);
			this.setMetadata(metadata);
			this.setPromotions(promotions);
			
			if(dataframe.getDataset() != null) {
				DatasetDTO dataset = new DatasetDTO(dataframe.getDataset());
				this.setDataset(dataset);
			}
			
			if(dataframe.getScript() != null) {
				ScriptDTO script = new ScriptDTO(dataframe.getScript());
				this.setScript(script);
			}
		}
	}
	

	private void setName(String name) {
		this.name = name;
		
	}

	private void setItemType(String itemType) {
		this.itemType = itemType;
		
	}

	@Override
	public String generateNodeName() {
		return "DF-" + new Date().getTime() + "-" + Thread.currentThread().getId();
	}
	
	public String getDataframeType() {
		return dataframeType;
	}

	public void setDataframeType(String dataframeType) {
		this.dataframeType = dataframeType;
	}
	
	public List<MetadatumDTO> getMetadata() {
		return this.getChildren(MetadatumDTO.class);
	}
	
	public void setMetadata(List<MetadatumDTO> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<CommentDTO> getComments() {
		return this.getChildren(CommentDTO.class);
	}
	
	public void setComments(List<CommentDTO> comments) {
		this.replaceChildren(CommentDTO.class, comments);
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

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
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

	public List<String> getAssemblyIds() {
		return assemblyIds;
	}

	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblyIds = assemblyIds;
	}

	public List<String> getParentDataframeIds() {
		return parentDataframeIds;
	}

	public void setParentDataframeIds(List<String> parentDataframeIds) {
		this.parentDataframeIds = parentDataframeIds;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getPromotionStatus() {
		return promotionStatus;
	}
	
	public List<String> getProfileConfig() {
return profileConfig;
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

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}
	
	public ScriptDTO getScript() {
		return (ScriptDTO) this.getChild(ScriptDTO.PRIMARY_TYPE);
	}
	
	public void setScript(ScriptDTO script) {
		this.replaceChild(ScriptDTO.PRIMARY_TYPE, script);
	}
	
	public List<PromotionDTO> getPromotions() {
		return this.getChildren(PromotionDTO.class);
	}
	
	public void setPromotions(List<PromotionDTO> promotions) {
		this.replaceChildren(PromotionDTO.class, promotions);
	}
	
	public DatasetDTO getDataset() {
		return (DatasetDTO) this.getChild(DatasetDTO.PRIMARY_TYPE);
	}
	
	public void setDataset(DatasetDTO dataset) {
		this.replaceChild(DatasetDTO.PRIMARY_TYPE, dataset);
	}

	private void setProfileConfig(List<String> profileConfig) {
		this.profileConfig = profileConfig;
		
	}
	
	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	public boolean isSuperseded() {
		return isSuperseded;
	}

	public void setSuperseded(boolean isSuperseded) {
		this.isSuperseded = isSuperseded;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
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

	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}
	
	
	public List<String> getStudyId() {
		return studyId;
	}

	public void setStudyId(List<String> studyId) {
		this.studyId = studyId;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isPublished() {
		return isPublished;
	}

	public void setPublished(boolean published) {
		this.isPublished = published;
	}
	public boolean isReleased() {
		return isReleased;
	}

	public void setReleased(boolean released) {
		this.isReleased = released;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	public String getBatchId() {
		return this.batchId;
	}
	
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
}
