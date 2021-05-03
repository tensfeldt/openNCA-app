package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.utils.EquipIdCalculator;

public class QCRequest extends EquipObject implements EquipCommentable, EquipID, EquipCreatable, EquipModifiable, EquipMetadatable {
	public static final String ENTITY_TYPE = "QC Request";
	
	// QCRequest specific
	private Date qcDueDate;
	private LibraryReference checklistTemplateId;
	private String dataframeId;
	private String assemblyId;
	private String qcStatus;
	private List<QCWorkflowItem> qcWorkflowItems = new ArrayList<>();
	private List<QCChecklistSummaryItem> qcChecklistSummaryItems = new ArrayList<>();
	private List<QCChecklistItem> qcChecklistItems = new ArrayList<>();
	
	// EquipCreatable
	private Date created;
	private String createdBy;
	
	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;
	
	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();
	
	// EquipID
	private String equipId = "";
	
	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();

	public QCRequest() {
		this.setEntityType(QCRequest.ENTITY_TYPE);
	}
	
	public Date getQcDueDate() {
		return qcDueDate;
	}

	public void setQcDueDate(Date qcDueDate) {
		this.qcDueDate = qcDueDate;
	}

	public LibraryReference getChecklistTemplateId() {
		return checklistTemplateId;
	}

	public void setChecklistTemplateId(LibraryReference checklistTemplateId) {
		this.checklistTemplateId = checklistTemplateId;
	}

	public String getDataframeId() {
		return dataframeId;
	}

	public void setDataframeId(String dataframeId) {
		this.dataframeId = dataframeId;
	}

	public String getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public List<QCWorkflowItem> getQcWorkflowItems() {
		return qcWorkflowItems;
	}

	public void setQcWorkflowItems(List<QCWorkflowItem> qcWorkflowItems) {
		this.qcWorkflowItems = qcWorkflowItems;
	}

	public List<QCChecklistSummaryItem> getQcChecklistSummaryItems() {
		return qcChecklistSummaryItems;
	}

	public void setQcChecklistSummaryItems(List<QCChecklistSummaryItem> qcChecklistSummaryItems) {
		this.qcChecklistSummaryItems = qcChecklistSummaryItems;
	}

	public List<QCChecklistItem> getQcChecklistItems() {
		return qcChecklistItems;
	}

	public void setQcChecklistItems(List<QCChecklistItem> qcChecklistItems) {
		this.qcChecklistItems = qcChecklistItems;
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
		this.equipId = equipId;
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
}