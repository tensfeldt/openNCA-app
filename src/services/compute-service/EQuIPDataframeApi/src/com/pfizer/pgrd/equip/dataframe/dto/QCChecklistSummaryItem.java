package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

public class QCChecklistSummaryItem extends EquipObject
		implements EquipCommentable, EquipID, EquipMetadatable, EquipCreatable, EquipModifiable {
	
	public static final String ENTITY_TYPE = "QC Checklist Summary Item";
	
	// QCChecklistSummaryItem specific
	private String checklistSummaryItemAnswer;
	private LibraryReference checklistTemplateSummaryItemId;

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

	// EquipID
	private String equipId;
	
	public QCChecklistSummaryItem() {
		this.setEntityType(QCChecklistSummaryItem.ENTITY_TYPE);
	}

	public String getChecklistSummaryItemAnswer() {
		return checklistSummaryItemAnswer;
	}

	public void setChecklistSummaryItemAnswer(String checklistSummaryItemAnswer) {
		this.checklistSummaryItemAnswer = checklistSummaryItemAnswer;
	}

	public LibraryReference getChecklistTemplateSummaryItemId() {
		return checklistTemplateSummaryItemId;
	}

	public void setChecklistTemplateSummaryItemId(LibraryReference checklistTemplateSummaryItemId) {
		this.checklistTemplateSummaryItemId = checklistTemplateSummaryItemId;
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