package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public interface EquipIDDAO {
	public List<EquipObject> getItem(String equipId);
	public List<EquipObject> getItem(List<String> equipIds);
	public List<EquipObject> getItem(String[] equipIds);
}