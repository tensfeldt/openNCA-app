package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;

public class ReportingEventStatusChangeDTO extends ModeShapeNode implements EquipCreated, EquipID {
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
	

	public ReportingEventStatusChangeDTO() {
		this(null);
	}
	
	public ReportingEventStatusChangeDTO(ReportingEventStatusChangeWorkflow statusChange) {
		super();
		this.setPrimaryType(ReportingEventStatusChangeDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(statusChange);
	}
	
	public static List<ReportingEventStatusChangeWorkflow> toReportingEventStatusChangeWorkflow(List<ReportingEventStatusChangeDTO> changes) {
		List<ReportingEventStatusChangeWorkflow> list = new ArrayList<>();
		if(changes != null) {
			for(ReportingEventStatusChangeDTO dto : changes) {
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
		c.setReportingEventReopenReasonAttachmentId(this.getReopenReasonAttachmentId());
		List<Comment> comments = CommentDTO.toComment(this.getComments());
		c.setComments(comments);
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		c.setMetadata(metadata);
		
		return c;
	}
	
	public ReportingEventStatusChangeWorkflow toReportingEventStatusChangeWorkflow() {
		return (ReportingEventStatusChangeWorkflow) this.toEquipObject();
	}
	
	public static List<ReportingEventStatusChangeDTO> fromReportingEventStatusChangeWorkflow(List<ReportingEventStatusChangeWorkflow> changes) {
		List<ReportingEventStatusChangeDTO> list = new ArrayList<>();
		if(changes != null) {
			for(ReportingEventStatusChangeWorkflow c : changes) {
				ReportingEventStatusChangeDTO dto = new ReportingEventStatusChangeDTO(c);
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
			this.setReopenReasonAttachmentId(statusChange.getReportingEventReopenReasonAttachmentId());
			
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
	
	public String getReopenReasonAttachmentId() {
		return reopenReasonAttachmentId;
	}

	public void setReopenReasonAttachmentId(String reopenReasonAttachmentId) {
		this.reopenReasonAttachmentId = reopenReasonAttachmentId;
	}
	
}
