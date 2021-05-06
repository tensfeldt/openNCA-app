package com.pfizer.pgrd.equip.services.libraryservice.dto;

import java.util.Date;

public class LibraryResponse {
	private String response;
	private String artifactId;
	private String artifactPath;
	private Date created;
	private String createdBy;
	private String primaryType;
	private boolean deleted;
	private String comments;
	private LibraryResponseProperties properties;
	
	public String getResponse() {
		return response;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public String getArtifactPath() {
		return artifactPath;
	}
	public Date getCreated() {
		return created;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public String getPrimaryType() {
		return primaryType;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public String getComments() {
		return comments;
	}
	public LibraryResponseProperties getProperties() {
		return properties;
	}
}
