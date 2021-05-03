package com.pfizer.pgrd.equip.dataframe.dto.atr;

public class DataframeInfo {
	private String equipId;
	private String id;
	private long version;
	
	public DataframeInfo() {
		this(null, null, 0);
	}
	
	public DataframeInfo(String id, String equipId, long version) {
		this.id = id;
		this.equipId = equipId;
		this.version = version;
	}
	
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getId() {
		return id;
	}
	public void setId(String uuid) {
		this.id = uuid;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
}