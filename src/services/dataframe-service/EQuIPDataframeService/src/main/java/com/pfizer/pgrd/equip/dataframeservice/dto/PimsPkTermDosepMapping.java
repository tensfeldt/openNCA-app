package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.List;

public class PimsPkTermDosepMapping {
	String pkTerm;
	List<String> dosep = new ArrayList<String>();
	public String getPkTerm() {
		return pkTerm;
	}
	public void setPkTerm(String pkTerm) {
		this.pkTerm = pkTerm;
	}
	public List<String> getDosep() {
		return dosep;
	}
	public void setDosep(List<String> dosep) {
		this.dosep = dosep;
	}
}
