package com.pfizer.equip.searchable.dto;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores for data for nt:resource type nodes.
 * 
 * @author HeinemanWP
 *
 */
public class NtResource {
	private String self;
	private String up;
	private String id;
	@Expose
	@SerializedName("jcr:primaryType")
	private String jcrPrimaryType;
	@Expose
	@SerializedName("jcr:lastModifiedBy")
	private String jcrLastModifiedBy;
	@Expose
	@SerializedName("jcr:lastModified")
	private Instant jcrLastModified;
	@Expose
	@SerializedName("jcr:mimeType")
	private String jcrMimeType;
	@Expose
	@SerializedName("jcr:data")
	private String jcrData;
	
	
	
	public String getSelf() {
		return self;
	}
	public void setSelf(String self) {
		this.self = self;
	}
	public String getUp() {
		return up;
	}
	public void setUp(String up) {
		this.up = up;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJcrPrimaryType() {
		return jcrPrimaryType;
	}
	public void setJcrPrimaryType(String jcrPrimaryType) {
		this.jcrPrimaryType = jcrPrimaryType;
	}
	public String getJcrLastModifiedBy() {
		return jcrLastModifiedBy;
	}
	public void setJcrLastModifiedBy(String jcrLastModifiedBy) {
		this.jcrLastModifiedBy = jcrLastModifiedBy;
	}
	public Instant getJcrLastModified() {
		return jcrLastModified;
	}
	public void setJcrLastModified(Instant jcrLastModified) {
		this.jcrLastModified = jcrLastModified;
	}
	public String getJcrMimeType() {
		return jcrMimeType;
	}
	public void setJcrMimeType(String jcrMimeType) {
		this.jcrMimeType = jcrMimeType;
	}
	public String getJcrData() {
		return jcrData;
	}
	public void setJcrData(String jcrData) {
		this.jcrData = jcrData;
	}
	
	
}
