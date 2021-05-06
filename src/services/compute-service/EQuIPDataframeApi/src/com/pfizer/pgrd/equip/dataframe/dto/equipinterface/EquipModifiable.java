package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.Date;

public interface EquipModifiable {
	public Date getModifiedDate();
	public void setModifiedDate(Date modifiedDate);
	
	public String getModifiedBy();
	public void setModifiedBy(String modifiedBy);
}