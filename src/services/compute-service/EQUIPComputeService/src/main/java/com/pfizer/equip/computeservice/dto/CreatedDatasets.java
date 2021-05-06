package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.NONE)
public class CreatedDatasets {
	@XmlElement
	private List<CreatedDatasetInfo> datasetInfo = new ArrayList<>();
	@XmlTransient
	private byte[] tarData;
	
	public List<CreatedDatasetInfo> getDatasetInfo() {
		return datasetInfo;
	}
	public void setDatasetInfo(List<CreatedDatasetInfo> datasetInfo) {
		this.datasetInfo = datasetInfo;
	}
	public byte[] getTarData() {
		return tarData;
	}
	public void setTarData(byte[] tarData) {
		this.tarData = tarData;
	}
	
}
