package com.pfizer.pgrd.equip.dataframeservice.copyutils;

public class CopyValidation {
	private boolean canBeCopied;
	private String copyFailureReason;
	
	public boolean canBeCopied() {
		return canBeCopied;
	}
	public void setCanBeCopied(boolean canBeCopied) {
		this.canBeCopied = canBeCopied;
	}
	public String getCopyFailureReason() {
		return copyFailureReason;
	}
	public void setCopyFailureReason(String copyFailureReason) {
		this.copyFailureReason = copyFailureReason;
	}
}
