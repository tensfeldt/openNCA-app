package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;

public class ReportingEventStatusChangeNode extends ModeShapeNode implements EquipCreatedMixin, EquipIdMixin {
	public static final String PRIMARY_TYPE = "equip:reportingEventStatusChangeWorkflow";
	
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
	@SerializedName("equip:reportingEventStatusChangeDescription")
	private String statusChangeDescription;
	
	@Expose
	@SerializedName("equip:reportingEventReleaseStatus")
	private String releaseStatus;
	
	@Expose
	@SerializedName("equip:reportingEventReopenReason")
	private String reopenReason;

	@Expose
	@SerializedName("equip:reportingEventReopenReasonAttachmentId")
	private String reopenReasonAttachmentId;
	
	public ReportingEventStatusChangeNode() {
		this(null);
	}
	
	public ReportingEventStatusChangeNode(ReportingEventStatusChangeWorkflow statusChange) {
		super();
		this.setPrimaryType(ReportingEventStatusChangeNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(statusChange);
	}
	
	public static List<ReportingEventStatusChangeWorkflow> toReportingEventStatusChangeWorkflow(List<ReportingEventStatusChangeNode> changes) {
		List<ReportingEventStatusChangeWorkflow> list = new ArrayList<>();
		if(changes != null) {
			for(ReportingEventStatusChangeNode dto : changes) {
				ReportingEventStatusChangeWorkflow c = dto.toReportingEventStatusChangeWorkflow();
				list.add(c);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		ReportingEventStatusChangeWorkflow c = new ReportingEventStatusChangeWorkflow();
		c.setCreated(this.getCreated());
		c.setCreatedBy(this.getCreatedBy());
		c.setCreated(this.getCreated());
		c.setId(this.getJcrId());
		c.setModifiedBy(this.getModifiedBy());
		c.setModifiedDate(this.getModified());
		c.setReportingEventReleaseStatusKey(this.getReleaseStatus());
		c.setReportingEventReopenReasonKey(this.getReopenReason());
		c.setReportingEventStatusWorkflowDescription(this.getStatusChangeDescription());
		
		List<Comment> comments = CommentNode.toComment(this.getComments());
		c.setComments(comments);
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		c.setMetadata(metadata);
		
		return c;
	}
	
	public ReportingEventStatusChangeWorkflow toReportingEventStatusChangeWorkflow() {
		return (ReportingEventStatusChangeWorkflow) this.toEquipObject();
	}
	
	public static List<ReportingEventStatusChangeNode> fromReportingEventStatusChangeWorkflow(List<ReportingEventStatusChangeWorkflow> changes) {
		List<ReportingEventStatusChangeNode> list = new ArrayList<>();
		if(changes != null) {
			for(ReportingEventStatusChangeWorkflow c : changes) {
				ReportingEventStatusChangeNode dto = new ReportingEventStatusChangeNode(c);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(ReportingEventStatusChangeWorkflow statusChange) {
		if(statusChange != null) {
			this.setCreated(statusChange.getCreated());
			this.setCreatedBy(statusChange.getCreatedBy());
			this.setModified(statusChange.getModifiedDate());
			this.setModifiedBy(statusChange.getModifiedBy());
			this.setReleaseStatus(statusChange.getReportingEventReleaseStatus());
			this.setReopenReason(statusChange.getReportingEventReopenReasonKey());
			this.setStatusChangeDescription(statusChange.getReportingEventStatusWorkflowDescription());
			
			List<CommentNode> comments = CommentNode.fromComment(statusChange.getComments());
			this.setComments(comments);
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(statusChange.getMetadata());
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

	public String getStatusChangeDescription() {
		return statusChangeDescription;
	}

	public void setStatusChangeDescription(String statusChangeDescription) {
		this.statusChangeDescription = statusChangeDescription;
	}

	public String getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public String getReopenReason() {
		return reopenReason;
	}

	public void setReopenReason(String reopenReason) {
		this.reopenReason = reopenReason;
	}
}
