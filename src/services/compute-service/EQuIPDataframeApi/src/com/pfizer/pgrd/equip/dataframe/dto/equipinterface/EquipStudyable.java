package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.List;

public interface EquipStudyable {
	public List<String> getProgramIds();
	public void setProgramIds(List<String> programIds);
	
	public List<String> getProtocolIds();
	public void setProtocolIds(List<String> protocolIds);
	
	public List<String> getProjectIds();
	public void setProjectIds(List<String> projectIds);
}
