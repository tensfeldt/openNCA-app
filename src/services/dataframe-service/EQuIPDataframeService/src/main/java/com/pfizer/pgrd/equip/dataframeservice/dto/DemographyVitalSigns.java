package com.pfizer.pgrd.equip.dataframeservice.dto;

import com.pfizer.pgrd.cordis.cds.dto.pims.PimsDemography;

//This is a dto in the sense that the data is being transferred to a file that gets
//stored in a dataset
public class DemographyVitalSigns {
	PimsDemography demography;
	String height;
	String heightUnit;
	String weight;
	String weightUnit;
	
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getHeightUnit() {
		return heightUnit;
	}
	public void setHeightUnit(String heightUnit) {
		this.heightUnit = heightUnit;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getWeightUnit() {
		return weightUnit;
	}
	public void setWeightUnit(String weightUnit) {
		this.weightUnit = weightUnit;
	}
	public PimsDemography getDemography() {
		return demography;
	}
	public void setDemography(PimsDemography demography) {
		this.demography = demography;
	}
}
