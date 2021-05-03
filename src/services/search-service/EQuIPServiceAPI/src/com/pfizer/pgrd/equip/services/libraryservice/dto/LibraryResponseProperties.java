package com.pfizer.pgrd.equip.services.libraryservice.dto;

import java.util.Date;

public class LibraryResponseProperties {
	private String equipName;
	private boolean deleted;
	private String primaryType;
	private String description;
	private String subType;
	private String equipCreatedBy;
	private Date equipCreated;
	
	public String getEquipName() {
		return equipName;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public String getPrimaryType() {
		return primaryType;
	}
	public String getDescription() {
		return description;
	}
	public String getSubType() {
		return subType;
	}
	public String getEquipCreatedBy() {
		return equipCreatedBy;
	}
	public Date getEquipCreated() {
		return equipCreated;
	}
}
