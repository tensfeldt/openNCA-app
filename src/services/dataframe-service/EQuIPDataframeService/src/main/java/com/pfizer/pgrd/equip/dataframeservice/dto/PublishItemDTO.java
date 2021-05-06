package com.pfizer.pgrd.equip.dataframeservice.dto;

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
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;

public class PublishItemDTO extends ModeShapeNode implements EquipCreated, EquipDelete, EquipVersion, EquipID {
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

	public PublishItemDTO() {
		this(null);
	}
	
	public PublishItemDTO(PublishItem item) {
		super();
		this.setPrimaryType(PublishItemDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<PublishItem> toPublishedItem(List<PublishItemDTO> items) {
		List<PublishItem> list = new ArrayList<>();
		if(items != null) {
			for(PublishItemDTO dto : items) {
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

		List<Comment> comments = CommentDTO.toComment(this.getComments());
		item.setComments(comments);
		
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		item.setMetadata(metadata);
		
		List<PublishItemPublishStatusChangeWorkflow> swcs = PublishStatusChangeDTO.toPublishItemPublishStatusChangeWorkflow(this.getStatusChangeWorkflow());
		item.setWorkflowItems(swcs);
		
		return item;
	}
	
	public PublishItem toPublishedItem() {
		return (PublishItem) this.toEquipObject();
	}
	
	public static List<PublishItemDTO> fromPublishedItem(List<PublishItem> items) {
		List<PublishItemDTO> list = new ArrayList<>();
		if(items != null) {
			for(PublishItem item : items) {
				PublishItemDTO dto = new PublishItemDTO(item);
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
			
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(item.getMetadata());
			this.setMetadata(metadata);
			
			List<CommentDTO> comments = CommentDTO.fromComment(item.getComments());
			this.setComments(comments);

			List<PublishStatusChangeDTO> swcs = PublishStatusChangeDTO.fromPublishItemPublishStatusChangeWorkflow(item.getWorkflowItems());
			this.setStatusChangeWorkflow(swcs);
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
	
	public List<PublishStatusChangeDTO> getStatusChangeWorkflow() {
		return this.getChildren(PublishStatusChangeDTO.class);
	}
	
	public void setStatusChangeWorkflow(List<PublishStatusChangeDTO> workflow) {
		this.replaceChildren(PublishStatusChangeDTO.class, workflow);
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