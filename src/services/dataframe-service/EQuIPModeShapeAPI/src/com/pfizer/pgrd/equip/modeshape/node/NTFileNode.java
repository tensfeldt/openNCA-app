package com.pfizer.pgrd.equip.modeshape.node;

public class NTFileNode extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "nt:file";
	
	public NTResourceNode getContent() {
		return this.getChild(NTResourceNode.class);
	}
	
	protected void setContent(NTResourceNode content) {
		this.replaceChild(NTResourceNode.class, content);
	}
}
