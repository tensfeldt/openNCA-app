package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.Date;

public interface EquipCreatable {
	public Date getCreated();
	public void setCreated(Date created);
	
	public String getCreatedBy();
	public void setCreatedBy(String createdBy);
}
