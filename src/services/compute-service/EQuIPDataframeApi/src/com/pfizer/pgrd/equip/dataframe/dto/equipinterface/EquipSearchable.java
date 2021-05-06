package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;



public interface EquipSearchable {
	public boolean isPublished();
	public void setPublished(boolean published);
	
	public boolean isReleased();
	public void setReleased(boolean released);

	public String getSubType();
	public void setSubType(String subType);
}