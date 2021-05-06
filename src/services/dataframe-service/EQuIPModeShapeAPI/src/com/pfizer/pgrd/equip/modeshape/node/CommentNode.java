package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;

public class CommentNode extends ModeShapeNode implements EquipCreatedMixin {
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
	
	public CommentNode() {
		this(null);
	}
	
	public CommentNode(Comment comment) {
		super();
		this.setPrimaryType(CommentNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(comment);
	}
	
	public static List<Comment> toComment(List<CommentNode> comments) {
		List<Comment> list = new ArrayList<>();
		if(comments != null) {
			for(CommentNode dto : comments) {
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
		
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		comment.setMetadata(metadata);
		
		return comment;
	}
	
	public Comment toComment() {
		return (Comment) this.toEquipObject();
	}
	
	public static List<CommentNode> fromComment(List<Comment> comments) {
		List<CommentNode> list = new ArrayList<>();
		if(comments != null) {
			for(Comment c : comments) {
				CommentNode dto = new CommentNode(c);
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
			
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(comment.getMetadata());
			this.setMetadata(metadata);
		}
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
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

	public boolean isDeleted() {
		return isDeleted;
	}

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
}
