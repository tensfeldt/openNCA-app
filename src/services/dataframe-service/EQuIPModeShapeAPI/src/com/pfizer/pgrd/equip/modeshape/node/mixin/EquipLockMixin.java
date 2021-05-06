package com.pfizer.pgrd.equip.modeshape.node.mixin;

public interface EquipLockMixin {
	public boolean isLocked();
	public void setLocked(boolean isLocked);
	public String getLockedByUser();
	public void setLockedByUser(String lockedByUser);
}
