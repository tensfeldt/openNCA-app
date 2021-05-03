package com.pfizer.equip.computeservice.containers;

import java.util.Date;

public class ContainerRun {
	private String containerId;
	private String userId;
	private Date startTime;
	
	public ContainerRun() {}
	
	public ContainerRun(String containerId, String userId) {
		this.containerId = containerId;
		this.userId = userId;
		startTime = new Date();
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

}
