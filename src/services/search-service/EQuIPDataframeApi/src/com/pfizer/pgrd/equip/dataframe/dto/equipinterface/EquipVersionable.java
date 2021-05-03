package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

public interface EquipVersionable {
	public Boolean getVersionSuperSeded();
	public void setVersionSuperSeded(Boolean versionSuperSeded);
	public long getVersionNumber();
	public void setVersionNumber(long versionNumber);
	public boolean isDeleteFlag();
	public void setDeleteFlag(boolean deleteFlag);
	public boolean isObsoleteFlag();
	public void setObsoleteFlag(boolean obsoleteFlag);
	public boolean isCommitted();
	public void setCommitted(boolean isCommitted);
}