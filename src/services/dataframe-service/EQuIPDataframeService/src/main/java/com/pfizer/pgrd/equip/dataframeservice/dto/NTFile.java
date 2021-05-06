package com.pfizer.pgrd.equip.dataframeservice.dto;

public class NTFile extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "nt:file";
	
	public NTResource getContent() {
		return this.getChild(NTResource.class);
	}
	
	protected void setContent(NTResource content) {
		this.replaceChild(NTResource.class, content);
	}
}
