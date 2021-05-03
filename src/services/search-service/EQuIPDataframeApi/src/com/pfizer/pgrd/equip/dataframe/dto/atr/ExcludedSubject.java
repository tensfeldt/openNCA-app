package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;

public class ExcludedSubject {
	private String subjid;
	private List<String> exclusionFlags = new ArrayList<>();
	private List<Comment> comments = new ArrayList<>();
	
	public String getSubjid() {
		return subjid;
	}
	public void setSubjid(String subjid) {
		this.subjid = subjid;
	}
	public List<String> getExclusionFlags() {
		return exclusionFlags;
	}
	public void setExclusionFlags(List<String> exclusionFlags) {
		this.exclusionFlags = exclusionFlags;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
}
