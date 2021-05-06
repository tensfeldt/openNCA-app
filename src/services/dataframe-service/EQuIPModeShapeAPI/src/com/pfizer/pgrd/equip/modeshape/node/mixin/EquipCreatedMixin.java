package com.pfizer.pgrd.equip.modeshape.node.mixin;

import java.util.Date;

public interface EquipCreatedMixin {
	public Date getCreated();
	public void setCreated(Date created);
	public String getCreatedBy();
	public void setCreatedBy(String createdBy);
	public Date getModified();
	public void setModified(Date modified);
	public String getModifiedBy();
	public void setModifiedBy(String modifiedBy);
}
