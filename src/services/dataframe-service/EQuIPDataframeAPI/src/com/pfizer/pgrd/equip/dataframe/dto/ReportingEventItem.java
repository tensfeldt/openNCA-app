package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

public class ReportingEventItem extends EquipObject
		implements EquipVersionable, EquipCreatable, EquipModifiable, EquipMetadatable, EquipCommentable, EquipID {

	public static final String ENTITY_TYPE = "Reporting Event Item";
	
	// ReportingItem specific
	private String reportingEventId;
	
	@SerializedName(value="dataFrameId", alternate="dataframeId")
	private String dataframeId;
	private String assemblyId;
	
	private String name;
	
	// EquipCreatable
	private Date created;
	private String createdBy;

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipMetadatable
	private List<Metadatum> metadata;

	// EquipID
	private String equipId;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();

	// EquipVersionable
	private boolean obsoleteFlag;
	private boolean versionSuperSeded;
	private long versionNumber = 1;
	private boolean isCommitted;
	private boolean deleteFlag;
	
	private boolean included;
	
	private PublishItem publishItem;
	
	public ReportingEventItem() {
		this.setEntityType(ReportingEventItem.ENTITY_TYPE);
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getEquipId() {
		return this.equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getReportingEventId() {
		return reportingEventId;
	}

	public void setReportingEventId(String reportingEventId) {
		this.reportingEventId = reportingEventId;
	}

	public String getDataFrameId() {
		return dataframeId;
	}

	public void setDataFrameId(String dataFrameId) {
		this.dataframeId = dataFrameId;
	}

	public String getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}

	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}

	public void setObsoleteFlag(boolean obsolete) {
		this.obsoleteFlag = obsolete;
	}

	public Boolean getVersionSuperSeded() {
		return versionSuperSeded;
	}

	public void setVersionSuperSeded(Boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIncluded() {
		return true;
		//return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public PublishItem getPublishItem() {
		return publishItem;
	}

	public void setPublishItem(PublishItem publishItem) {
		this.publishItem = publishItem;
	}
	
	@Override
	public Metadatum getMetadatum(String key) {
		Metadatum metadatum = null;
		if(this.metadata != null && key != null) {
			for(Metadatum md : this.metadata) {
				if(md.getKey().equalsIgnoreCase(key)) {
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
	
	@Override
	public ReportingEventItem clone() {
		ReportingEventItem clone = new ReportingEventItem();
		clone.setAssemblyId(this.getAssemblyId());
		clone.setCommitted(this.isCommitted());
		clone.setCreated(this.getCreated());
		clone.setCreatedBy(this.getCreatedBy());
		clone.setDataFrameId(this.getDataFrameId());
		clone.setDeleteFlag(this.isDeleteFlag());
		clone.setEntityType(this.getEntityType());
		clone.setEquipId(this.getEquipId());
		clone.setId(this.getId());
		clone.setIncluded(this.isIncluded());
		clone.setModifiedBy(this.getModifiedBy());
		clone.setModifiedDate(this.getModifiedDate());
		clone.setName(this.getName());
		clone.setObsoleteFlag(this.isObsoleteFlag());
		clone.setReportingEventId(this.getReportingEventId());
		clone.setVersionNumber(this.getVersionNumber());
		clone.setVersionSuperSeded(this.getVersionSuperSeded());
		
		for(Comment c : this.getComments()) {
			clone.getComments().add(c.clone());
		}
		for(Metadatum md : this.getMetadata()) {
			clone.getMetadata().add(md.clone());
		}
		
		clone.setPublishItem(this.getPublishItem().clone());
		
		return clone;
	}
}
