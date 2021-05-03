package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

public class Comment extends EquipObject implements EquipCreatable, EquipModifiable, EquipMetadatable {
	public static final String ENTITY_TYPE = "Comment";
	public static final String ANALYSIS_SAVE_ERROR_TYPE = "Analysis Save Error";
	public static final String QC_TYPE = "QC";
	
	// Comment specific
	private String body;
	private String commentType;

	// EquipCreatable
	private Date created;
	private String createdBy;
	private boolean isDeleted;
	
	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;
	
	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();
	
	public Comment() { this(null, null, null, null); }
	public Comment(Date createdDate, String createdBy, String body, String commentType) {
		this.created = createdDate;
		this.createdBy = createdBy;
		this.body = body;
		this.commentType = commentType;
		this.setEntityType(Comment.ENTITY_TYPE);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
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
	
	public boolean isDeleted() {
		return isDeleted;
	}
	
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if ((obj instanceof Comment)) {
			Comment other = (Comment) obj;
			return
				equals(this.body, other.body) &&
				equals(this.commentType, other.commentType) &&
				equals(this.createdBy, other.createdBy) &&
				(this.isDeleted == other.isDeleted) &&
				equals(this.modifiedBy, other.modifiedBy) &&
				equals(this.metadata, other.metadata);
		}
		return false;
	}

	private boolean equals(Object o1, Object o2) {
		return ((o1 == null) && (o2 == null)) ||
				o1 != null &&
				o1.equals(o2);
	}
	
	@Override
	public Comment clone() {
		Comment clone = new Comment();
		clone.setBody(this.getBody());
		clone.setCommentType(this.getCommentType());
		clone.setCreated(this.getCreated());
		clone.setCreatedBy(this.getCreatedBy());
		clone.setDeleted(this.isDeleted());
		clone.setEntityType(this.getEntityType());
		clone.setId(this.getId());
		clone.setModifiedBy(this.getModifiedBy());
		clone.setModifiedDate(this.getModifiedDate());
		
		for(Metadatum md : this.getMetadata()) {
			clone.getMetadata().add(md.clone());
		}
		
		return clone;
	}
}
