package com.pfizer.pgrd.equip.dataframeservice.dto.interfaces;

public interface EquipLock {
	public boolean isLocked();
	public void setLocked(boolean isLocked);
	public String getLockedByUser();
	public void setLockedByUser(String lockedByUser);
}
