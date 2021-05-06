package com.pfizer.pgrd.equip.dataframe.dto.lineage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

@XmlTransient
public abstract class LineageItem {
	private String nodeType;
	private String id;
	private String equipId;
	private long equipVersion = 1;
	private Date lastModifiedDate;
	private String lastModifiedBy;
	private Date createdDate;
	private String createdBy;
	private List<DataframeLineageItem> childDataframes = new ArrayList<>();
	private List<AssemblyLineageItem> childAssemblies = new ArrayList<>();
	private List<String> studyIds = new ArrayList<>();
	private boolean versionSuperseded;
	private boolean versionComitted;
	private boolean isLocked;
	private String lockedByUser;
	private boolean isDeleted;
	private List<Comment> comments;
	private String qcStatus;
	private String publishStatus = "Not Published";
	private String breadcrumb;
	private String legacyBreadcrumb;
	private String fullBreadcrumb;
	private String fullLegacyBreadcrumb;
	private String name;
	private List<Metadatum> metadata = new ArrayList<>();
	private String subType;
	private List<DataframeLineageItem> attachments = new ArrayList<>();
	private String itemType;
	private boolean userHasAccess = true;
	private List<String> parentAssemblyIds = new ArrayList<>();
	private List<String> parentDataframeIds = new ArrayList<>();
	
	public abstract LineageItem clone();
	public abstract LineageItem clone(boolean includeChildren);
	
	public abstract LineageItem deepClone();
	
	protected void populateClone(LineageItem item, boolean includeChildren, boolean isDeepClone) {
		item.setBreadcrumb(this.getBreadcrumb());
		item.setComments(this.getComments());
		item.setCreatedBy(this.getCreatedBy());
		item.setCreatedDate(this.getCreatedDate());
		item.setDeleted(this.isDeleted);
		item.setEquipId(this.getEquipId());
		item.setEquipVersion(this.getEquipVersion());
		item.setId(this.getId());
		item.setLastModifiedBy(this.getLastModifiedBy());
		item.setLastModifiedDate(this.getLastModifiedDate());
		item.setLegacyBreadcrumb(this.getLegacyBreadcrumb());
		item.setLocked(this.isLocked);
		item.setMetadata(this.getMetadata());
		item.setName(this.getName());
		item.setNodeType(this.getNodeType());
		item.setPublishStatus(this.getPublishStatus());
		item.setQcStatus(this.getQcStatus());
		item.setSubType(this.getSubType());
		item.setVersionComitted(this.isVersionComitted());
		item.setVersionSuperseded(this.isVersionSuperseded());
		item.setStudyIds(this.studyIds);
		item.setFullBreadcrumb(this.fullBreadcrumb);
		item.setFullLegacyBreadcrumb(this.fullLegacyBreadcrumb);
		item.setAttachments(this.attachments);
		item.setItemType(this.getItemType());
		item.setLockedByUser(this.getLockedByUser());
		item.setParentAssemblyIds(this.getParentAssemblyIds());
		item.setParentDataframeIds(this.getParentDataframeIds());
		
		if(includeChildren || isDeepClone) {
			for(AssemblyLineageItem child : this.getChildAssemblies()) {
				AssemblyLineageItem cclone = null;
				if(isDeepClone) {
					cclone = (AssemblyLineageItem)child.deepClone();
				}
				else {
					cclone = child.clone(false);
				}
				
				item.getChildAssemblies().add(cclone);
			}
			
			for(DataframeLineageItem child : this.getChildDataframes()) {
				DataframeLineageItem cclone = null;
				if(isDeepClone) {
					cclone = (DataframeLineageItem)child.deepClone();
				}
				else {
					cclone = child.clone(false);
				}
				
				item.getChildDataframes().add(cclone);
			}
		}
	}
	
	public boolean userHasAccess() {
		return userHasAccess;
	}
	public void setUserHasAccess(boolean userHasAccess) {
		this.userHasAccess = userHasAccess;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
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
	public long getEquipVersion() {
		return equipVersion;
	}
	public void setEquipVersion(long equipVersion) {
		this.equipVersion = equipVersion;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public List<DataframeLineageItem> getChildDataframes() {
		return childDataframes;
	}
	public void setChildDataframes(List<DataframeLineageItem> childDataframes) {
		this.childDataframes = childDataframes;
	}
	public boolean isVersionSuperseded() {
		return versionSuperseded;
	}
	public void setVersionSuperseded(boolean versionSuperseded) {
		this.versionSuperseded = versionSuperseded;
	}
	public boolean isVersionComitted() {
		return versionComitted;
	}
	public void setVersionComitted(boolean versionComitted) {
		this.versionComitted = versionComitted;
	}
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	public List<AssemblyLineageItem> getChildAssemblies() {
		return childAssemblies;
	}
	public void setChildAssemblies(List<AssemblyLineageItem> childAssemblies) {
		this.childAssemblies = childAssemblies;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getQcStatus() {
		return qcStatus;
	}
	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}
	public String getPublishStatus() {
		return publishStatus;
	}
	public void setPublishStatus(String publishStatus) {
		this.publishStatus = publishStatus;
	}
	public String getBreadcrumb() {
		return breadcrumb;
	}
	public void setBreadcrumb(String breadcrumb) {
		this.breadcrumb = breadcrumb;
	}
	public List<Metadatum> getMetadata() {
		return metadata;
	}
	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}
	public String getLegacyBreadcrumb() {
		return legacyBreadcrumb;
	}
	public void setLegacyBreadcrumb(String legacyBreadcrumb) {
		this.legacyBreadcrumb = legacyBreadcrumb;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public String getFullBreadcrumb() {
		return fullBreadcrumb;
	}
	public void setFullBreadcrumb(String fullBreadcrumb) {
		this.fullBreadcrumb = fullBreadcrumb;
	}
	public String getFullLegacyBreadcrumb() {
		return fullLegacyBreadcrumb;
	}
	public void setFullLegacyBreadcrumb(String fullLegacyBreadcrumb) {
		this.fullLegacyBreadcrumb = fullLegacyBreadcrumb;
	}
	public List<String> getStudyIds() {
		return studyIds;
	}
	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}
	public List<DataframeLineageItem> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<DataframeLineageItem> attachments) {
		this.attachments = attachments;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public String getMetadatum(String key) {
		String value = null;
		for(Metadatum md : this.getMetadata()) {
			if(md.getKey().equalsIgnoreCase(key)) {
				if(md.getValue() != null && !md.getValue().isEmpty()) {
					value = md.getValue().get(0);
				}
			}
		}
		
		return value;
	}
	public boolean addChild(LineageItem child) {
		if(child instanceof DataframeLineageItem) {
			return this.childDataframes.add((DataframeLineageItem) child);
		}
		else if(child instanceof AssemblyLineageItem) {
			return this.childAssemblies.add((AssemblyLineageItem) child);
		}
		
		return false;
	}
	public void addChildren(List<LineageItem> children) {
		for(LineageItem child : children) {
			this.addChild(child);
		}
	}
	
	public List<String> getParentIds() {
		return new ArrayList<>();
	}
	public String getLockedByUser() {
		return lockedByUser;
	}
	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}
	public List<String> getParentAssemblyIds() {
		return parentAssemblyIds;
	}
	public void setParentAssemblyIds(List<String> parentAssemblyIds) {
		this.parentAssemblyIds = parentAssemblyIds;
	}
	public List<String> getParentDataframeIds() {
		return parentDataframeIds;
	}
	public void setParentDataframeIds(List<String> parentDataframeIds) {
		this.parentDataframeIds = parentDataframeIds;
	}
}