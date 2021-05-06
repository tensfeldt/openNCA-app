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
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipDeleteMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipVersionMixin;

public class PublishItemNode extends ModeShapeNode implements EquipCreatedMixin, EquipDeleteMixin, EquipVersionMixin, EquipIdMixin {
	public static final String PRIMARY_TYPE = "equip:publishedItem";
	
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
	@SerializedName("equip:publishStatus")
	private String publishStatus;
	
	@Expose
	@SerializedName("equip:publishedDate")
	private Date publishedDate;
	
	@Expose
	@SerializedName("equip:expirationDate")
	private Date expirationDate;
	
	@Expose
	@SerializedName("equip:publishedViewTemplateId")
	private String publishedViewTemplateId;
	
	@Expose
	@SerializedName("equip:publishedViewFilterCriteria")
	private String publishedViewFilterCriteria;
	
	@Expose
	@SerializedName("equip:publishedViewSubfilter")
	private String publishedViewSubfilter;
	
	@Expose
	@SerializedName("equip:publishedTags")
	private List<String> publishedTags;
	
	@Expose
	@SerializedName("equip:versionNumber")
	private long versionNumber;
	
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private boolean isSuperseded;
	
	@Expose
	@SerializedName("equip:versionCommitted")
	private boolean isCommitted;

	//uncomment this once the CND is updated and installed
//	@Expose
//	@SerializedName("equip:name")
	private String name;

	public PublishItemNode() {
		this(null);
	}
	
	public PublishItemNode(PublishItem item) {
		super();
		this.setPrimaryType(PublishItemNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<PublishItem> toPublishedItem(List<PublishItemNode> items) {
		List<PublishItem> list = new ArrayList<>();
		if(items != null) {
			for(PublishItemNode dto : items) {
				PublishItem item = dto.toPublishedItem();
				list.add(item);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		PublishItem item = new PublishItem();
		item.setCreated(this.getCreated());
		item.setCreatedBy(this.getCreatedBy());
		item.setDeleteFlag(this.isDeleted());
		item.setEquipId(this.getEquipId());
		item.setExpirationDate(this.getExpirationDate());
		item.setId(this.getJcrId());
		item.setModifiedBy(this.getModifiedBy());
		item.setModifiedDate(this.getModified());
		item.setObsoleteFlag(this.isObsolete());
		item.setPublishedDate(this.getPublishedDate());
		item.setPublishedViewSubFilter(this.getPublishedViewSubfilter());
		item.setPublishedViewTemplateId(this.getPublishedViewTemplateId());
		item.setPublishedViewFilterCriteria(this.getPublishedViewFilterCriteria());
		item.setVersionNumber(this.getVersionNumber());
		item.setVersionSuperSeded(this.isSuperseded());
		item.setPublishStatus(this.getPublishStatus());
		item.setCommitted(this.isCommitted);
		item.setPublishedTags(this.getPublishedTags());
		item.setName(this.getName());

		List<Comment> comments = CommentNode.toComment(this.getComments());
		item.setComments(comments);
		
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		item.setMetadata(metadata);
		
		List<PublishItemPublishStatusChangeWorkflow> swcs = PublishStatusChangeNode.toPublishItemPublishStatusChangeWorkflow(this.getStatusChangeWorkflow());
		item.setWorkflowItems(swcs);
		
		return item;
	}
	
	public PublishItem toPublishedItem() {
		return (PublishItem) this.toEquipObject();
	}
	
	public static List<PublishItemNode> fromPublishedItem(List<PublishItem> items) {
		List<PublishItemNode> list = new ArrayList<>();
		if(items != null) {
			for(PublishItem item : items) {
				PublishItemNode dto = new PublishItemNode(item);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(PublishItem item) {
		if(item != null) {
			this.setCreated(item.getCreated());
			this.setCreatedBy(item.getCreatedBy());
			this.setDeleted(item.isDeleteFlag());
			this.setExpirationDate(item.getExpirationDate());
			this.setEquipId(item.getEquipId());
			this.setModified(item.getModifiedDate());
			this.setModifiedBy(item.getModifiedBy());
			this.setObsolete(item.isObsoleteFlag());
			this.setPublishedViewSubfilter(item.getPublishedViewSubFilter());
			this.setPublishedViewTemplateId(item.getPublishItemTemplateId());
			this.setPublishedViewFilterCriteria(item.getPublishedViewFilterCriteria());
			this.setVersionNumber(item.getVersionNumber());
			this.setSuperseded(item.getVersionSuperSeded());
			this.setCommitted(item.isCommitted());
			this.setPublishStatus(item.getPublishStatus());
			this.setPublishedDate(item.getPublishedDate());
			this.setName(item.getName());
			
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(item.getMetadata());
			this.setMetadata(metadata);
			
			List<CommentNode> comments = CommentNode.fromComment(item.getComments());
			this.setComments(comments);

			List<PublishStatusChangeNode> swcs = PublishStatusChangeNode.fromPublishItemPublishStatusChangeWorkflow(item.getWorkflowItems());
			this.setStatusChangeWorkflow(swcs);
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
	
	public List<PublishStatusChangeNode> getStatusChangeWorkflow() {
		return this.getChildren(PublishStatusChangeNode.class);
	}
	
	public void setStatusChangeWorkflow(List<PublishStatusChangeNode> workflow) {
		this.replaceChildren(PublishStatusChangeNode.class, workflow);
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

	public String getPublishStatus() {
		return publishStatus;
	}

	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getPublishedViewTemplateId() {
		return publishedViewTemplateId;
	}

	public void setPublishedViewTemplateId(String publishedViewTemplateId) {
		this.publishedViewTemplateId = publishedViewTemplateId;
	}

	public String getPublishedViewFilterCriteria() {
		return publishedViewFilterCriteria;
	}

	public void setPublishedViewFilterCriteria(String publishedViewFilterCriteria) {
		this.publishedViewFilterCriteria = publishedViewFilterCriteria;
	}

	public String getPublishedViewSubfilter() {
		return publishedViewSubfilter;
	}

	public void setPublishedViewSubfilter(String publishedViewSubfilter) {
		this.publishedViewSubfilter = publishedViewSubfilter;
	}

	public List<String> getPublishedTags() {
		return publishedTags;
	}

	public void setPublishedTags(List<String> publishedTags) {
		this.publishedTags = publishedTags;
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
}