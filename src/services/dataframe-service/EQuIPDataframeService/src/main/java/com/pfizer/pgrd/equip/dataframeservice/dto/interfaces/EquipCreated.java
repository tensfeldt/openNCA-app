package com.pfizer.pgrd.equip.dataframeservice.dto.interfaces;

import java.util.Date;

public interface EquipCreated {
	public Date getCreated();
	public void setCreated(Date created);
	public String getCreatedBy();
	public void setCreatedBy(String createdBy);
	public Date getModified();
	public void setModified(Date modified);
	public String getModifiedBy();
	public void setModifiedBy(String modifiedBy);
}
