package com.pfizer.pgrd.equip.modeshape.node;

import com.google.gson.annotations.SerializedName;

public class NTResourceNode extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "nt:resource";
	
	@SerializedName("jcr:mimeType")
	private String jcrMimeType;
	
	@SerializedName("jcr:data")
	private String jcrData;

	public String getJcrMimeType() {
		return jcrMimeType;
	}

	protected void setJcrMimeType(String jcrMimeType) {
		this.jcrMimeType = jcrMimeType;
	}

	public String getJcrData() {
		return jcrData;
	}

	protected void setJcrData(String jcrData) {
		this.jcrData = jcrData;
	}
}
