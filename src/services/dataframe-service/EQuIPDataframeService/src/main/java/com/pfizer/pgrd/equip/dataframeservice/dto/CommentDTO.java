package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;

public class CommentDTO extends ModeShapeNode implements EquipCreated, EquipDelete {
	public static final String PRIMARY_TYPE = "equip:comment";
	
	@Expose
	@SerializedName("equip:commentType")
	private String commentType;
	
	@Expose
	@SerializedName("equip:body")
	private String body;
	
	@Expose
	@SerializedName("equip:deleteFlag")
	private boolean isDeleted;
	
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
	
	public CommentDTO() {
		this(null);
	}
	
	public CommentDTO(Comment comment) {
		super();
		this.setPrimaryType(CommentDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(comment);
	}
	
	public static List<Comment> toComment(List<CommentDTO> comments) {
		List<Comment> list = new ArrayList<>();
		if(comments != null) {
			for(CommentDTO dto : comments) {
				Comment c = dto.toComment();
				list.add(c);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Comment comment = new Comment();
		comment.setBody(this.getBody());
		comment.setCommentType(this.getCommentType());
		comment.setCreated(this.getCreated());
		comment.setCreatedBy(this.createdBy);
		comment.setId(this.getJcrId());
		comment.setModifiedBy(this.getModifiedBy());
		comment.setModifiedDate(this.getModified());
		comment.setDeleted(this.isDeleted);
		
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		comment.setMetadata(metadata);
		
		return comment;
	}
	
	public Comment toComment() {
		return (Comment) this.toEquipObject();
	}
	
	public static List<CommentDTO> fromComment(List<Comment> comments) {
		List<CommentDTO> list = new ArrayList<>();
		if(comments != null) {
			for(Comment c : comments) {
				CommentDTO dto = new CommentDTO(c);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Comment comment) {
		if(comment != null) {
			this.setBody(comment.getBody());
			this.setCommentType(comment.getCommentType());
			this.setCreated(comment.getCreated());
			this.setCreatedBy(comment.getCreatedBy());
			this.setModified(comment.getModifiedDate());
			this.setModifiedBy(comment.getModifiedBy());
			this.setDeleted(comment.isDeleted());
			
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(comment.getMetadata());
			this.setMetadata(metadata);
		}
	}
	
	public List<MetadatumDTO> getMetadata() {
		return this.getChildren(MetadatumDTO.class);
	}
	
	public void setMetadata(List<MetadatumDTO> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	@Override
	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
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

	@Override
	public boolean isObsolete() {
		return false;
	}

	@Override
	public void setObsolete(boolean isObsolete) {
		// do nothing
	}
}
