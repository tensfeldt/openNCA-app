package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

public interface EquipLockable {
	public boolean isLocked();
	public String getLockedByUser();
}