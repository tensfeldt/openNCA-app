package com.pfizer.pgrd.equip.modeshape.node.mixin;

public interface EquipVersionMixin {
	public long getVersionNumber();
	public void setVersionNumber(long versionNumber);
	public boolean isSuperseded();
	public void setSuperseded(boolean isSuperseded);
	public boolean isCommitted();
	public void setCommitted(boolean isCommitted);
}