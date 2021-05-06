package com.pfizer.pgrd.equip.dataframe.dto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipLockable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipSearchable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipStudyable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;

public class Assembly extends EquipObject implements EquipVersionable, EquipCommentable, EquipID, EquipStudyable,
		EquipCreatable, EquipModifiable, EquipMetadatable, EquipLockable, EquipSearchable {
	
	public static final String ENTITY_TYPE = "Assembly";

	public static final String ANALYSIS_TYPE = "Analysis", REPORTING_EVENT_TYPE = "Reporting Event", DATA_LOAD_TYPE = "Data Load", BATCH_TYPE = "Batch";
	private static final String RELEASE_STATUS_KEY = "reportingEventReleaseStatusKey",
								RELEASE_DATE_KEY = "reportingEventReleaseDate";
	private static final SimpleDateFormat RELEASE_DATE_FORMAT = new SimpleDateFormat("");
	
	// Assembly specific
	private List<String> parentIds = new ArrayList<>();
	private List<String> reportingItemIds = new ArrayList<>();
	private List<String> publishItemIds = new ArrayList<>();
	private List<String> dataframeIds = new ArrayList<>();
	private List<String> assemblyIds = new ArrayList<>();
	private List<String> studyIds = new ArrayList<>();
	private List<String> libraryReferences = new ArrayList<>();
	private List<ReportingEventStatusChangeWorkflow> reportingEventStatusChangeWorkflows = new ArrayList<>();
	private List<ReportingEventItem> reportingItems = new ArrayList<>();
	private Boolean atrIsCurrent;
	
	private List<String> parentDataframeIds = new ArrayList<>();
	private List<String> parentAssemblyIds = new ArrayList<>();
	
	private String loadStatus;
	private String qcStatus;
	private String assemblyType;
	private List<Script> scripts = new ArrayList<>();
	private String name;
	private String itemType;
	
	// EquipID
	private String equipId;

	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();

	// EquipCreatable
	private Date created;
	private String createdBy;

	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();

	// EquipLockable
	private boolean isLocked;
	private String lockedByUser;

	// EquipStudyable
	private List<String> protocolIds = new ArrayList<>();
	private List<String> projectIds = new ArrayList<>();
	private List<String> programIds = new ArrayList<>();
	
	//EquipSearchable
	private boolean published = false;
	private boolean released = false;
	private String description;
	private String subType;

	// EquipVersionable
	private long versionNumber;
	private boolean versionSuperSeded;
	private boolean deleteFlag = false;
	private boolean obsoleteFlag;
	private boolean isCommitted;
	
	// Virtual properties. These are not persisted in Modeshape.
	private List<Dataframe> memberDataframes = new ArrayList<>();
	private List<Assembly> memberAssemblies = new ArrayList<>();
	
	public List<ReportingEventStatusChangeWorkflow> getReportingEventStatusChangeWorkflows() {
		return reportingEventStatusChangeWorkflows;
	}

	public void setReportingEventStatusChangeWorkflows(
			List<ReportingEventStatusChangeWorkflow> reportingEventStatusChangeWorkflows) {
		this.reportingEventStatusChangeWorkflows = reportingEventStatusChangeWorkflows;
	}

	public List<String> getReportingItemIds() {
		return reportingItemIds;
	}

	public void setReportingItemIds(List<String> reportingItemIds) {
		this.reportingItemIds = reportingItemIds;
	}

	public List<String> getPublishItemIds() {
		return publishItemIds;
	}

	public void setPublishItemIds(List<String> publishItemIds) {
		this.publishItemIds = publishItemIds;
	}

	public Assembly() {
		this.libraryReferences = new ArrayList<String>();
		if (this.getDataframeIds() == null)
			this.setDataframeIds(new ArrayList<String>());
		if (this.getAssemblyIds() == null)
			this.setAssemblyIds(new ArrayList<String>());
		if (this.getQcStatus() == null)
			this.setQcStatus("Not QC'd");
		this.comments = new ArrayList<Comment>();
		this.setAssemblyType(Assembly.ENTITY_TYPE);
		this.setEntityType(Assembly.ENTITY_TYPE);
	}
	
	public void setCEVersion(String ceVersion) {
		Metadatum md = this.getMetadatum(Metadatum.CE_VERSION_KEY);
		if(md == null) {
			md = new Metadatum();
			md.setKey(Metadatum.CE_VERSION_KEY);
			this.getMetadata().add(md);
		}
		
		md.setValue(Arrays.asList(ceVersion));
	}
	
	public String getCEVersion() {
		return this.getMetadatumValue(Metadatum.CE_VERSION_KEY);
	}

	public List<String> getLibraryReferences() {
		return libraryReferences;
	}

	public void setLibraryReferences(List<String> libraryReferences) {
		this.libraryReferences = libraryReferences;
	}

	public String getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public List<String> getDataframeIds() {
		return dataframeIds;
	}

	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframeIds = dataframeIds;
	}

	public List<String> getAssemblyIds() {
		return assemblyIds;
	}

	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblyIds = assemblyIds;
	}

	public String getAssemblyType() {
		return assemblyType;
	}

	public void setAssemblyType(String assemblyType) {
		this.assemblyType = assemblyType;
	}

	@Override
	public Boolean getVersionSuperSeded() {
		return versionSuperSeded;
	}

	@Override
	public void setVersionSuperSeded(Boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}

	@Override
	public long getVersionNumber() {
		return versionNumber;
	}

	@Override
	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}

	@Override
	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	@Override
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	@Override
	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}

	@Override
	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}

	public List<String> getProtocolIds() {
		return protocolIds;
	}

	public void setProtocolIds(List<String> protocolIds) {
		this.protocolIds = protocolIds;
	}

	public List<String> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}

	public List<String> getProgramIds() {
		return programIds;
	}

	public void setProgramIds(List<String> programIds) {
		this.programIds = programIds;
	}

	@Override
	public List<Comment> getComments() {
		return this.comments;
	}

	@Override
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	@Override
	public String getEquipId() {
		return this.equipId;
	}

	@Override
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public List<String> getParentIds() {
		return parentIds;
	}

	public void setParentIds(List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public List<String> getStudyIds() {
		return studyIds;
	}

	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser (String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}
	
	public String getReleaseStatus() {
		String status = null;
		Metadatum md = this.getMetadatum(RELEASE_STATUS_KEY);
		if(md != null) {
			status = md.getValue().get(0);
		}
		
		return status;
	}
	
	public void setReleaseStatus(String releaseStatus) {
		List<String> status = new ArrayList<>();
		status.add(releaseStatus);
		
		Metadatum md = this.getMetadatum(RELEASE_STATUS_KEY);
		if(md == null) {
			md = new Metadatum();
			md.setKey(RELEASE_STATUS_KEY);
			md.setValueType("STRING");
			this.getMetadata().add(md);
		}
		
		md.setValue(status);
	}
	
	public Date getReleaseDate() {
		Date d = null;
		String rds = this.getMetadatumValue(RELEASE_DATE_KEY);
		if(rds != null) {
			try {
				d = RELEASE_DATE_FORMAT.parse(rds);
			}
			catch(Exception e) { }
		}
		
		return d;
	}
	
	public void setReleaseDate(Date d) {
		if(d != null) {
			String rds = RELEASE_DATE_FORMAT.format(d);
			List<String> v = new ArrayList<>();
			v.add(rds);
			Metadatum md = this.getMetadatum(RELEASE_DATE_KEY);
			if(md == null) {
				md = new Metadatum();
				md.setKey(RELEASE_DATE_KEY);
				md.setValueType("STRING");
				this.getMetadata().add(md);
			}
			
			md.setValue(v);
		}
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

	public String getDescription() {
		if(this.description != null) {
			return this.description;
		}
		else {
			return this.getMetadatumValue("description");
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isPublished() {
		return published;
	}

	@Override
	public void setPublished(boolean published) {
		this.published = published;
	}

	@Override
	public boolean isReleased() {
		return released;
	}

	@Override
	public void setReleased(boolean released) {
	this.released = released;
		
	}

	@Override
	public String getSubType() {
		return this.subType;
	}

	@Override
	public void setSubType(String subType) {
		this.subType = subType;
	}

	public List<ReportingEventItem> getReportingItems() {
		return reportingItems;
	}

	public void setReportingEventItems(List<ReportingEventItem> reportingItems) {
		this.reportingItems = reportingItems;
	}

	public List<Dataframe> getMemberDataframes() {
		return memberDataframes;
	}

	public void setMemberDataframes(List<Dataframe> memberDataframes) {
		this.memberDataframes = memberDataframes;
	}

	public List<Assembly> getMemberAssemblies() {
		return memberAssemblies;
	}

	public void setMemberAssemblies(List<Assembly> memberAssemblies) {
		this.memberAssemblies = memberAssemblies;
	}

	public List<Script> getScripts() {
		return scripts;
	}

	public void setScripts(List<Script> scripts) {
		this.scripts = scripts;
	}

	public Boolean atrIsCurrent() {
		return atrIsCurrent;
	}

	public void setAtrIsCurrent(Boolean atrIsCurrent) {
		this.atrIsCurrent = atrIsCurrent;
	}

	public List<String> getParentDataframeIds() {
		return parentDataframeIds;
	}

	public void setParentDataframeIds(List<String> parentDataframeIds) {
		this.parentDataframeIds = parentDataframeIds;
	}

	public List<String> getParentAssemblyIds() {
		return parentAssemblyIds;
	}

	public void setParentAssemblyIds(List<String> parentAssemblyIds) {
		this.parentAssemblyIds = parentAssemblyIds;
	}
}
