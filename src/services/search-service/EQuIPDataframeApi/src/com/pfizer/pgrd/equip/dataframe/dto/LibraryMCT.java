package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LibraryMCT extends EquipObject {
	public static final String ENTITY_TYPE = "LibraryMCT";
	
	private String description;
	private Date created;
	private String createdBy;
	private String name;
	private String subType;
	private boolean isDeleted;
	private String derivedDataStatus;
	private List<String> customTags = new ArrayList<>();
	private List<Comment> comments = new ArrayList<>();
	
	public LibraryMCT() {
		super();
		this.setEntityType(LibraryMCT.ENTITY_TYPE);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getDerivedDataStatus() {
		return derivedDataStatus;
	}

	public void setDerivedDataStatus(String derivedDataStatus) {
		this.derivedDataStatus = derivedDataStatus;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<String> getCustomTags() {
		return customTags;
	}

	public void setCustomTags(List<String> customTags) {
		this.customTags = customTags;
	}
}