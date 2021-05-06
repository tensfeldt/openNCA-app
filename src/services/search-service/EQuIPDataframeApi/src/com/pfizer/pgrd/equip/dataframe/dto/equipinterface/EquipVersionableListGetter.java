package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.List;

public interface EquipVersionableListGetter {
	List<EquipVersionable> get(String equipId);   
}
