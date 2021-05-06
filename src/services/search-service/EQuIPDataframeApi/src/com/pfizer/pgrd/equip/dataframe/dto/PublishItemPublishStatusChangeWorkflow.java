package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

public class PublishItemPublishStatusChangeWorkflow extends EquipObject
		implements EquipCreatable, EquipModifiable, EquipMetadatable, EquipCommentable {
	
	private static final String ENTITY_TYPE = "Publish Item Publish Status Change Workflow";
	
	// PublishItemPublishStatusChangeWorkflow specific
	private String publishItemPublishStatusChangeDescription;
	private String publishStatus;
	private String publishItemId;
	
	// EquipCreatable
	private Date created;
	private String createdBy;

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipMetadatable
	private List<Metadatum> metadata;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	public PublishItemPublishStatusChangeWorkflow() {
		this.setEntityType(PublishItemPublishStatusChangeWorkflow.ENTITY_TYPE);
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getPublishItemId() {
		return publishItemId;
	}

	public void setPublishItemId(String publishItemId) {
		this.publishItemId = publishItemId;
	}

	public String getPublishItemPublishStatusChangeDescription() {
		return publishItemPublishStatusChangeDescription;
	}

	public void setPublishItemPublishStatusChangeDescription(String publishItemPublishStatusChangeDescription) {
		this.publishItemPublishStatusChangeDescription = publishItemPublishStatusChangeDescription;
	}

	public String getPublishStatus() {
		return publishStatus;
	}

	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
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

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
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
