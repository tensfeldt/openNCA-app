package com.pfizer.pgrd.equip.dataframe.dto.lineage;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class DataframeLineageItem extends LineageItem {
	private String dataframeType;
	private String promotionStatus;
	private String dataBlindingStatus;
	private String releaseStatus = "Not Released";
	private List<String> memberOfAssemblyIds = new ArrayList<>();
	private String dataStatus;
	private String batchId;
	
	public DataframeLineageItem() {
		this.setNodeType("Dataframe");
	}
	
	public static final List<DataframeLineageItem> fromDataframe(List<Dataframe> dfs) {
		List<DataframeLineageItem> list = new ArrayList<>();
		if(dfs != null) {
			for(Dataframe df : dfs) {
				DataframeLineageItem dli = DataframeLineageItem.fromDataframe(df);
				list.add(dli);
			}
		}
		
		return list;
	}
	
	public static DataframeLineageItem fromDataframe(Dataframe df) {
		return DataframeLineageItem.fromDataframe(df, true);
	}
	
	public static DataframeLineageItem fromDataframe(Dataframe df, boolean userHasAccess) {
		DataframeLineageItem item = null;
		if(df != null) {
			item = new DataframeLineageItem();
			item.setDataframeType(df.getDataframeType());
			item.setEquipId(df.getEquipId());
			item.setEquipVersion(df.getVersionNumber());
			item.setId(df.getId());
			item.setLastModifiedBy(df.getModifiedBy());
			item.setLastModifiedDate(df.getModifiedDate());
			item.setParentDataframeIds(df.getDataframeIds());
			item.setParentAssemblyIds(df.getAssemblyIds());
			item.setStudyIds(df.getStudyIds());
			item.setPromotionStatus(df.getPromotionStatus());
			item.setVersionComitted(df.isCommitted());
			item.setVersionSuperseded(df.getVersionSuperSeded());
			item.setParentAssemblyIds(df.getAssemblyIds());
			item.setParentDataframeIds(df.getDataframeIds());
			item.setLocked(df.isLocked());
			item.setDeleted(df.isDeleteFlag());
			item.setCreatedBy(df.getCreatedBy());
			item.setCreatedDate(df.getCreated());
			item.setDataStatus(df.getDataStatus());
			item.setQcStatus(df.getQcStatus());
			item.setName(df.getName());
			item.setSubType(df.getSubType());
			item.setDataBlindingStatus(df.getDataBlindingStatus());
			item.setItemType(df.getItemType());
			item.setBatchId(df.getBatchId());
			
			if(df.getLockedByUser() != null) {
				String lbu = df.getLockedByUser().trim();
				if(!lbu.isEmpty()) {
					item.setLockedByUser(lbu);
				}
			}
			
			if(df.isReleased()) {
				item.setReleaseStatus("Released");
			}
			if(df.isPublished()) {
				item.setPublishStatus("Published");
			}
			
			if(item.getLastModifiedBy() == null) {
				item.setLastModifiedBy(df.getCreatedBy());
			}
			if(item.getLastModifiedDate() == null) {
				item.setLastModifiedDate(df.getCreated());
			}
			
			item.setUserHasAccess(userHasAccess);
			if(item.userHasAccess()) {
				item.setComments(df.getComments());
				item.setMetadata(df.getMetadata());
			}
		}
		
		return item;
	}
	
	public DataframeLineageItem clone() {
		return this.clone(true);
	}
	
	public DataframeLineageItem clone(boolean includeChildren) {
		DataframeLineageItem clone = this.createClone();
		this.populateClone(clone, includeChildren, false);
		return clone;
	}
	
	public DataframeLineageItem deepClone() {
		DataframeLineageItem clone = this.createClone();
		this.populateClone(clone, true, true);
		return clone;
	}
	
	private DataframeLineageItem createClone() {
		DataframeLineageItem clone = new DataframeLineageItem();
		clone.setDataframeType(this.getDataframeType());
		clone.setPromotionStatus(this.getPromotionStatus());
		clone.setReleaseStatus(this.getReleaseStatus());
		clone.setMemberOfAssemblyIds(this.getMemberOfAssemblyIds());
		clone.setParentAssemblyIds(this.getParentAssemblyIds());
		clone.setParentDataframeIds(this.getParentDataframeIds());
		clone.setDataStatus(this.getDataStatus());
		clone.setDataBlindingStatus(this.getDataBlindingStatus());
		clone.setBatchId(this.getBatchId());
		
		return clone;
	}
	
	public String getDataframeType() {
		return dataframeType;
	}
	public void setDataframeType(String dataframeType) {
		this.dataframeType = dataframeType;
	}
	public String getPromotionStatus() {
		return promotionStatus;
	}
	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}
	public List<String> getMemberOfAssemblyIds() {
		return memberOfAssemblyIds;
	}
	public void setMemberOfAssemblyIds(List<String> memberOfAssemblyIds) {
		this.memberOfAssemblyIds = memberOfAssemblyIds;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}

	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}
	
	public String getFileName() {
		String fileName = this.getMetadatum("File Name Full");
		if(fileName == null) {
			fileName = this.getMetadatum("File Name Short");
		}
		
		return fileName;
	}
	
	@Override
	public List<String> getParentIds() {
		List<String> pids = new ArrayList<>();
		if(this.getParentAssemblyIds() != null) {
			pids.addAll(this.getParentAssemblyIds());
		}
		if(this.getParentDataframeIds() != null) {
			pids.addAll(this.getParentDataframeIds());
		}
		
		return pids;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	public String getOutputFileName() {
		return this.getMetadatum("output filename");
	}
}