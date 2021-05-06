package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;

public class QCWorkflowItemNode extends ModeShapeNode implements EquipCreatedMixin, EquipIdMixin {
	public static final String PRIMARY_TYPE = "equip:qcworkflowitem";
	
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
	@SerializedName("equip:assignedReviewer")
	private String assignedReviewer;
	
	@Expose
	@SerializedName("equip:qcWorkflowStatus")
	private String qcWorkflowStatus;
	
	@Expose
	@SerializedName("equip:qcStatus")
	private String qcStatus;
	
	@Expose
	@SerializedName("equip:parentId")
	private String parentId;
	
	public QCWorkflowItemNode(QCWorkflowItem item) {
		super();
		this.setPrimaryType(QCWorkflowItemNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<QCWorkflowItem> toQCWorkflowItem(List<QCWorkflowItemNode> items) {
		List<QCWorkflowItem> list = new ArrayList<>();
		if(items != null) {
			for(QCWorkflowItemNode dto : items) {
				QCWorkflowItem item = dto.toQCWorkflowItem();
				list.add(item);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		QCWorkflowItem item = new QCWorkflowItem();
		item.setAssignedReviewer(this.getAssignedReviewer());
		item.setCreated(this.getCreated());
		item.setCreatedBy(this.getCreatedBy());
		item.setEquipId(this.getEquipId());
		item.setId(this.getJcrId());
		item.setModifiedBy(this.getModifiedBy());
		item.setModifiedDate(this.getModified());
		item.setQcStatus(this.getQcStatus());
		item.setQcWorkflowStatus(this.getQcWorkflowStatus());
		item.setParentId(this.getParentId());
		
		List<Comment> comments = CommentNode.toComment(this.getComments());
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		item.setComments(comments);
		item.setMetadata(metadata);
		
		return item;
	}
	
	public QCWorkflowItem toQCWorkflowItem() {
		return (QCWorkflowItem) this.toEquipObject();
	}
	
	public static List<QCWorkflowItemNode> fromQCWorkflowItem(List<QCWorkflowItem> items) {
		List<QCWorkflowItemNode> list = new ArrayList<>();
		if(items != null) {
			for(QCWorkflowItem item : items) {
				QCWorkflowItemNode dto = new QCWorkflowItemNode(item);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(QCWorkflowItem item) {
		if(item != null) {
			this.setAssignedReviewer(item.getAssignedReviewer());
			this.setCreated(item.getCreated());
			this.setCreatedBy(item.getCreatedBy());
			this.setEquipId(item.getEquipId());
			this.setModified(item.getModifiedDate());
			this.setModifiedBy(item.getModifiedBy());
			this.setQcStatus(item.getQcStatus());
			this.setQcWorkflowStatus(item.getQcWorkflowStatus());
			this.setParentId(item.getParentId());
			
			List<CommentNode> comments = CommentNode.fromComment(item.getComments());
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(item.getMetadata());
			this.setComments(comments);
			this.setMetadata(metadata);
		}
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<CommentNode> getComments() {
		return this.getChildren(CommentNode.class);
	}
	
	public void setComments(List<CommentNode> comments) {
		this.replaceChildren(CommentNode.class, comments);
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

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
}
