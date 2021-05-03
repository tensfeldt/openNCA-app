package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

public class QCWorkflowItem extends EquipObject implements EquipCommentable, EquipID, EquipCreatable, EquipModifiable, EquipMetadatable {
	public static final String ENTITY_TYPE = "QC Workflow Item";
	
	// QCWorkflowItem specific
	private String parentId;
	private String qcStatus;
	private String assignedReviewer;
	private String qcWorkflowStatus;
	
	// EquipID
	private String equipId;
	
	// EquipCreatable
	private Date created;
	private String createdBy;
	
	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;
	
	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();
	
	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	public QCWorkflowItem() {
		this.setEntityType(QCWorkflowItem.ENTITY_TYPE);
	}
	
	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public String getAssignedReviewer() {
		return assignedReviewer;
	}

	public void setAssignedReviewer(String assignedReviewer) {
		this.assignedReviewer = assignedReviewer;
	}

	public String getQcWorkflowStatus() {
		return qcWorkflowStatus;
	}

	public void setQcWorkflowStatus(String qcWorkflowStatus) {
		this.qcWorkflowStatus = qcWorkflowStatus;
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


	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
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