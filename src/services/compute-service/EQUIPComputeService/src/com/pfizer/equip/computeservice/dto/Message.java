package com.pfizer.equip.computeservice.dto;

import java.util.List;

public class Message {
	
	private List<Long> parentDataFrameIds;
	private long containerId;
	private long scriptId;
	
	
	public List<Long> getParentDataFrameIds() {
		return parentDataFrameIds;
	}
	public void setParentDataFrameIds(List<Long> parentDataFrameIds) {
		this.parentDataFrameIds = parentDataFrameIds;
	}
	public long getContainerId() {
		return containerId;
	}
	public void setContainerId(long containerId) {
		this.containerId = containerId;
	}
	public long getScriptId() {
		return scriptId;
	}
	public void setScriptId(long scriptId) {
		this.scriptId = scriptId;
	}
}
