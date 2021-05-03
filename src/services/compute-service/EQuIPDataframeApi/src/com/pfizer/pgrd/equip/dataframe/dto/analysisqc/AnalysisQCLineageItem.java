package com.pfizer.pgrd.equip.dataframe.dto.analysisqc;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;

public class AnalysisQCLineageItem {
	private String nodeType;
	private String equipId;
	private long version;
	private String legacyName;
	private String createdBy;
	private Date createdDate;
	private String modifiedBy;
	private Date modifiedDate;
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public String getLegacyName() {
		return legacyName;
	}
	public void setLegacyName(String legacyName) {
		this.legacyName = legacyName;
	}
	
	public static final AnalysisQCLineageItem fromLineageItem(LineageItem lineageItem) {
		AnalysisQCLineageItem item = null;
		if(lineageItem != null) {
			item = new AnalysisQCLineageItem();
			item.setEquipId(lineageItem.getEquipId());
			item.setLegacyName(lineageItem.getName());
			item.setVersion(lineageItem.getEquipVersion());
			item.setCreatedBy(lineageItem.getCreatedBy());
			item.setCreatedDate(lineageItem.getCreatedDate());
			item.setModifiedBy(lineageItem.getLastModifiedBy());
			item.setModifiedDate(lineageItem.getLastModifiedDate());
			
			String name = null;
			if(lineageItem instanceof AssemblyLineageItem) {
				name = ((AssemblyLineageItem) lineageItem).getAssemblyType();
			}
			else if(lineageItem instanceof DataframeLineageItem) {
				name = ((DataframeLineageItem) lineageItem).getDataframeType();
			}
			item.setNodeType(name);
		}
		
		return item;
	}
}
