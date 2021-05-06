package com.pfizer.pgrd.equip.dataframe.dto.atr;

public class Parameter {
	private String name;
	private String publishedName;
	private String manualName;
	private String openNcaName;
	private String units;
	private Integer significantFigures;
	private Integer decimalPlaces;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPublishedName() {
		return publishedName;
	}
	public void setPublishedName(String publishedName) {
		this.publishedName = publishedName;
	}
	public String getOpenNcaName() {
		return openNcaName;
	}
	public void setOpenNcaName(String openNcaName) {
		this.openNcaName = openNcaName;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public Integer getSignificantFigures() {
		return significantFigures;
	}
	public void setSignificantFigures(Integer significantFigures) {
		this.significantFigures = significantFigures;
	}
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}
	public String getManualName() {
		return manualName;
	}
	public void setManualName(String manualName) {
		this.manualName = manualName;
	}
}
