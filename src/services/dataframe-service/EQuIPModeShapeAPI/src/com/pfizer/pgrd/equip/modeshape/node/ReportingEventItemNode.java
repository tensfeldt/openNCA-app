package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipDeleteMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipVersionMixin;

public class ReportingEventItemNode extends ModeShapeNode implements EquipCreatedMixin, EquipDeleteMixin, EquipVersionMixin, EquipIdMixin {
	public static final String PRIMARY_TYPE = "equip:reportingEventItem";
	
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
	@SerializedName("equip:deleteFlag")
	private boolean isDeleted;
	
	@Expose
	@SerializedName("equip:obsoleteFlag")
	private boolean isObsolete;
	
	@Expose
	@SerializedName("equip:versionNumber")
	private long versionNumber;
	
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private boolean isSuperseded;
	
	@Expose
	@SerializedName("equip:versionCommitted")
	private boolean isCommitted;

	@Expose
	@SerializedName("equip:included")
	private boolean included;

	@Expose
	@SerializedName("equip:name")
	private String name;
	
	@Expose
	@SerializedName("equip:assemblyId")
	private String assemblyId;

	@Expose
	@SerializedName("equip:parentReportingEventId")
	private String parentReportingEventId;	
	
	@Expose
	@SerializedName("equip:dataframeId")
	private String dataframeId;

	public ReportingEventItemNode() {
		this(null);
	}
	
	public ReportingEventItemNode(ReportingEventItem item) {
		super();
		this.setPrimaryType(ReportingEventItemNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	@Override
	public EquipObject toEquipObject() {
		ReportingEventItem item = new ReportingEventItem();
		item.setCreated(this.getCreated());
		item.setCreatedBy(this.getCreatedBy());
		item.setDeleteFlag(this.isDeleted());
		item.setEquipId(this.getEquipId());
		item.setId(this.getJcrId());
		item.setModifiedBy(this.getModifiedBy());
		item.setModifiedDate(this.getModified());
		item.setObsoleteFlag(this.isObsolete());
		item.setVersionNumber(this.getVersionNumber());
		item.setVersionSuperSeded(this.isSuperseded());
		item.setCommitted(this.isCommitted());
		item.setName(this.getName());
		item.setIncluded(this.isIncluded());
		item.setAssemblyId(this.getAssemblyId());
		item.setDataFrameId(this.getDataframeId());
		item.setReportingEventId(this.getAssemblyId());
		//item.setReportingEventId(this.getParentReportingEventId());
		
		List<PublishItem> publishedItems = PublishItemNode.toPublishedItem(this.getPublishedItems());
		if(!publishedItems.isEmpty()){
			item.setPublishItem(publishedItems.get(0));
		}
		
		List<Comment> comments = CommentNode.toComment(this.getComments());
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		item.setComments(comments);
		item.setMetadata(metadata);
		
		return item;
	}
	
	public ReportingEventItem toReportingItem() {
		return (ReportingEventItem) this.toEquipObject();
	}
	
	public static List<ReportingEventItemNode> fromReportingItem(List<ReportingEventItem> items) {
		List<ReportingEventItemNode> list = new ArrayList<>();
		if(items != null) {
			for(ReportingEventItem item : items) {
				ReportingEventItemNode dto = new ReportingEventItemNode(item);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(ReportingEventItem item) {
		if(item != null) {
			this.setCreated(item.getCreated());
			this.setCreatedBy(item.getCreatedBy());
			this.setCreatedBy(item.getCreatedBy());
			this.setDeleted(item.isDeleteFlag());
			this.setEquipId(item.getEquipId());
			this.setModified(item.getModifiedDate());
			this.setModifiedBy(item.getModifiedBy());
			this.setObsolete(item.isObsoleteFlag());
			this.setSuperseded(item.getVersionSuperSeded());
			this.setVersionNumber(item.getVersionNumber());
			this.setCommitted(item.isCommitted());
			this.setName(item.getName());
			this.setIncluded(item.isIncluded());
			this.setAssemblyId(item.getAssemblyId());
			this.setDataframeId(item.getDataFrameId());
			this.setParentReportingEventId(item.getReportingEventId());
			
			List<PublishItem> publishedItems = new ArrayList<>();
			publishedItems.add(item.getPublishItem());
			this.setPublishedItems(PublishItemNode.fromPublishedItem(publishedItems));
			
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(item.getMetadata());
			List<CommentNode> comments = CommentNode.fromComment(item.getComments());
			this.setMetadata(metadata);
			this.setComments(comments);
		}
	}
	
	@Override
	public String generateNodeName() {
		return "REI-" + new Date().getTime();
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

	public List<PublishItemNode> getPublishedItems() {
		return this.getChildren(PublishItemNode.class);
	}
	
	public void setPublishedItems(List<PublishItemNode> publishedItems) {
		this.replaceChildren(PublishItemNode.class, publishedItems);
	}
	
	public List<ReportingEventStatusChangeNode> getStatusChangeWorkflow() {
		return this.getChildren(ReportingEventStatusChangeNode.class);
	}
	
	public void setStatusChangeWorkflow(List<ReportingEventStatusChangeNode> workflow) {
		this.replaceChildren(ReportingEventStatusChangeNode.class, workflow);
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

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	public boolean isSuperseded() {
		return isSuperseded;
	}

	public void setSuperseded(boolean isSuperseded) {
		this.isSuperseded = isSuperseded;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public String getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}

	public String getDataframeId() {
		return dataframeId;
	}

	public void setDataframeId(String dataframeId) {
		this.dataframeId = dataframeId;
	}

	public String getParentReportingEventId() {
		return parentReportingEventId;
	}

	public void setParentReportingEventId(String parentReportingEventId) {
		this.parentReportingEventId = parentReportingEventId;
	}
}
