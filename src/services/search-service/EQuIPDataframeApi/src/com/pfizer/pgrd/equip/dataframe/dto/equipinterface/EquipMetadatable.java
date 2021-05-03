package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public interface EquipMetadatable {
	public List<Metadatum> getMetadata();
	public void setMetadata(List<Metadatum> metadata);
	public Metadatum getMetadatum(String key);
	public String getMetadatumValue(String key);
}
