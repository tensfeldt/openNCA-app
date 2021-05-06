package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

public class PublishItem extends EquipObject
		implements EquipVersionable, EquipID, EquipCommentable, EquipCreatable, EquipModifiable, EquipMetadatable {
	public static final String ENTITY_TYPE = "Publish Item";

	// PublishItem specific
	private String publishItemTemplateId; //parent
	private String reportingEventItemId;  //associated reporting event
	private Date   expirationDate;
	private String publishedViewTemplateId;
	private String publishedViewFilterCriteria;
	private String publishedViewSubFilter;
	private String publishStatus;
	private Date   publishedDate;
	private String publishEventId;

	private List<String> publishedTags = new ArrayList<>();
	private List<PublishItemPublishStatusChangeWorkflow> workflowItems = new ArrayList<>();
	
	// EquipCreatable
	private Date created;
	private String createdBy;

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();

	// EquipVersionable
	private long versionNumber = 1;
	private boolean obsoleteFlag;
	private boolean isCommitted;
	private boolean versionSuperSeded;
	private boolean deleteFlag;

	// EquipID
	private String equipId;

	private String name;
	
	public PublishItem() {
		this.setEntityType(PublishItem.ENTITY_TYPE);
	}
	
	public PublishItemPublishStatusChangeWorkflow getMostRecentWorkflowItem() {
		return this.getMostRecentWorkflowItem(null);
	}
	
	public PublishItemPublishStatusChangeWorkflow getMostRecentWorkflowItem(String status) {
		PublishItemPublishStatusChangeWorkflow latest = null;
		if(this.workflowItems != null) {
			this.workflowItems.sort(new Comparator<PublishItemPublishStatusChangeWorkflow>() {
				
				@Override
				public int compare(PublishItemPublishStatusChangeWorkflow arg0,
						PublishItemPublishStatusChangeWorkflow arg1) {
					if(arg0 == null) {
						return -1;
					}
					if(arg1 == null) {
						return 1;
					}
					
					long t0 = arg0.getCreated().getTime();
					long t1 = arg1.getCreated().getTime();
					if(t0 > t1) {
						return 1;
					}
					else if(t1 > t0) {
						return -1;
					}
					
					return 0;
				}
				
			});
			
			if(status == null) {
				latest = this.workflowItems.get(0);
			}
			else {
				for(PublishItemPublishStatusChangeWorkflow wfi : this.workflowItems) {
					if(wfi.getPublishStatus().equalsIgnoreCase(status)) {
						latest = wfi;
						break;
					}
				}
			}
		}
		
		return latest;
	}

	public String getPublishItemTemplateId() {
		return publishItemTemplateId;
	}

	public void setPublishItemTemplateId(String publishItemTemplateId) {
		this.publishItemTemplateId = publishItemTemplateId;
	}

	public String getReportingEventItemId() {
		return reportingEventItemId;
	}

	public void setReportingEventItemId(String reportingEventItemId) {
		this.reportingEventItemId = reportingEventItemId;
	}

	public Boolean getVersionSuperSeded() {
		return versionSuperSeded;
	}

	public void setVersionSuperSeded(Boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}
	
	public List<PublishItemPublishStatusChangeWorkflow> getWorkflowItems() {
		return workflowItems;
	}

	public void setWorkflowItems(List<PublishItemPublishStatusChangeWorkflow> workflowItems) {
		this.workflowItems = workflowItems;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<String> getPublishedTags() {
		return publishedTags;
	}

	public void setPublishedTags(List<String> publishedTags) {
		this.publishedTags = publishedTags;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public String getPublishEventId() {
		return publishEventId;
	}

	public void setPublishEventId(String publishEventId) {
		this.publishEventId = publishEventId;
	}

	public String getPublishStatus() {
		return publishStatus;
	}

	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
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

	public void setPublishedViewFilterCriteria(String viewFilterCriteria) {
		this.publishedViewFilterCriteria = viewFilterCriteria;
	}

	public String getPublishedViewSubFilter() {
		return publishedViewSubFilter;
	}

	public void setPublishedViewSubFilter(String publishedViewSubFilter) {
		this.publishedViewSubFilter = publishedViewSubFilter;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}

	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	@Override
	/**
	 * Returns a clone of this object. The clone is not a true clone as some objects are not cloned, merely their references are passed.
	 */
	public PublishItem clone() {
		PublishItem clone = new PublishItem();
		clone.setCommitted(this.isCommitted());
		clone.setCreated(this.getCreated());
		clone.setCreatedBy(this.getCreatedBy());
		clone.setDeleteFlag(this.isDeleteFlag());
		clone.setEntityType(this.getEntityType());
		clone.setEquipId(this.getEquipId());
		clone.setExpirationDate(this.getExpirationDate());
		clone.setId(this.getId());
		clone.setModifiedBy(this.getModifiedBy());
		clone.setModifiedDate(this.getModifiedDate());
		clone.setName(this.getName());
		clone.setObsoleteFlag(this.isObsoleteFlag());
		clone.setPublishedDate(this.getPublishedDate());
		clone.setPublishedTags(this.getPublishedTags());
		clone.setPublishedViewFilterCriteria(this.getPublishedViewFilterCriteria());
		clone.setPublishedViewSubFilter(this.getPublishedViewSubFilter());
		clone.setPublishedViewTemplateId(this.getPublishItemTemplateId());
		clone.setPublishEventId(this.getPublishEventId());
		clone.setPublishItemTemplateId(this.getPublishItemTemplateId());
		clone.setPublishStatus(this.getPublishStatus());
		clone.setReportingEventItemId(this.getReportingEventItemId());
		clone.setVersionNumber(this.getVersionNumber());
		clone.setVersionSuperSeded(this.getVersionSuperSeded());
		clone.setWorkflowItems(this.getWorkflowItems());
		
		for(Comment c : this.getComments()) {
			clone.getComments().add(c.clone());
		}
		for(Metadatum md : this.getMetadata()) {
			clone.getMetadata().add(md.clone());
		}
		
		return clone;
	}
}
