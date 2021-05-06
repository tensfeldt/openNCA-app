package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;

public class PublishStatusChangeDTO extends ModeShapeNode implements EquipCreated, EquipID {
	public static final String PRIMARY_TYPE = "equip:publishedItemPublishStatusChangeWorkflow";
	
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
	@SerializedName("equip:publishedStatusChangeDescription")
	private String statusChangeDescription;
	
	@Expose
	@SerializedName("equip:publishStatus")
	private String publishStatus;
	
	public PublishStatusChangeDTO() {
		this(null);
	}
	
	public PublishStatusChangeDTO(PublishItemPublishStatusChangeWorkflow statusChange) {
		super();
		this.setPrimaryType(PublishStatusChangeDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(statusChange);
	}
	
	public static List<PublishItemPublishStatusChangeWorkflow> toPublishItemPublishStatusChangeWorkflow(List<PublishStatusChangeDTO> changes) {
		List<PublishItemPublishStatusChangeWorkflow> list = new ArrayList<>();
		if(changes != null) {
			for(PublishStatusChangeDTO dto : changes) {
				PublishItemPublishStatusChangeWorkflow c = dto.toPublishItemPublishStatusChangeWorkflow();
				list.add(c);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		PublishItemPublishStatusChangeWorkflow c = new PublishItemPublishStatusChangeWorkflow();
		c.setCreated(this.getCreated());
		c.setCreatedBy(this.getCreatedBy());
		c.setId(this.getJcrId());
		c.setModifiedBy(this.getModifiedBy());
		c.setModifiedDate(this.getModified());
		c.setPublishItemPublishStatusChangeDescription(this.getStatusChangeDescription());
		c.setPublishStatus(this.getPublishStatus());
			
		List<Comment> comments = CommentDTO.toComment(this.getComments());
		c.setComments(comments);
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		c.setMetadata(metadata);
		
		return c;
	}
	
	public PublishItemPublishStatusChangeWorkflow toPublishItemPublishStatusChangeWorkflow() {
		return (PublishItemPublishStatusChangeWorkflow) this.toEquipObject();
	}
	
	public static List<PublishStatusChangeDTO> fromPublishItemPublishStatusChangeWorkflow(List<PublishItemPublishStatusChangeWorkflow> changes) {
		List<PublishStatusChangeDTO> list = new ArrayList<>();
		if(changes != null) {
			for(PublishItemPublishStatusChangeWorkflow c : changes) {
				PublishStatusChangeDTO dto = new PublishStatusChangeDTO(c);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(PublishItemPublishStatusChangeWorkflow statusChange) {
		if(statusChange != null) {
			this.setCreated(statusChange.getCreated());
			this.setCreatedBy(statusChange.getCreatedBy());
			this.setModified(statusChange.getModifiedDate());
			this.setModifiedBy(statusChange.getModifiedBy());
			this.setPublishStatus(statusChange.getPublishStatus());
			this.setStatusChangeDescription(statusChange.getPublishItemPublishStatusChangeDescription());
			
			List<CommentDTO> comments = CommentDTO.fromComment(statusChange.getComments());
			this.setComments(comments);			
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(statusChange.getMetadata());
			this.setMetadata(metadata);
		}
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

	public String getStatusChangeDescription() {
		return statusChangeDescription;
	}

	public void setStatusChangeDescription(String statusChangeDescription) {
		this.statusChangeDescription = statusChangeDescription;
	}

	public String getPublishStatus() {
		return publishStatus;
	}

	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
	}
}