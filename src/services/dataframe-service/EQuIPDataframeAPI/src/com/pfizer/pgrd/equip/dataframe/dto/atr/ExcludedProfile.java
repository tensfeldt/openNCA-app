package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;

public class ExcludedProfile {
	private String subjId;
	private String sdeId;
	private String periodUnit;
	private int period;
	private String visitUnit;
	private int visit;
	private String phase;
	
	private List<String> exclusionFlags = new ArrayList<>();
	private List<Comment> pkComments = new ArrayList<>();
	
	public String getSubjId() {
		return subjId;
	}
	public void setSubjId(String subjId) {
		this.subjId = subjId;
	}
	public String getSdeId() {
		return sdeId;
	}
	public void setSdeId(String sdeId) {
		this.sdeId = sdeId;
	}
	public String getPeriodUnit() {
		return periodUnit;
	}
	public void setPeriodUnit(String periodUnit) {
		this.periodUnit = periodUnit;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public String getVisitUnit() {
		return visitUnit;
	}
	public void setVisitUnit(String visitUnit) {
		this.visitUnit = visitUnit;
	}
	public int getVisit() {
		return visit;
	}
	public void setVisit(int visit) {
		this.visit = visit;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public List<String> getExclusionFlags() {
		return exclusionFlags;
	}
	public void setExclusionFlags(List<String> exclusionFlags) {
		this.exclusionFlags = exclusionFlags;
	}
	public List<Comment> getPkComments() {
		return pkComments;
	}
	public void setPkComments(List<Comment> pkComments) {
		this.pkComments = pkComments;
	}
}
