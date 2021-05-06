package com.pfizer.pgrd.equip.services.authorization.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthorizationRequestBody {
	private String id;
	private String dataBlindingStatus;
	private String dataframeType;
	private String promotionStatus;
	private String restrictionStatus;
	private List<String> studyIds;
	
	//JAXB requires a constructor
	public AuthorizationRequestBody() {}
	
	public String getId() {
		return id;
	}
	
	public void setDataframeId(String id) {
		this.id = id;
	}
	
	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}

	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}

	public String getDataframeType() {
		return dataframeType;
	}

	public void setDataframeType(String dataframeType) {
		this.dataframeType = dataframeType;
	}

	public String getPromotionStatus() {
		return promotionStatus;
	}

	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public String getRestrictionStatus() {
		return restrictionStatus;
	}

	public void setRestrictionStatus(String restrictionStatus) {
		this.restrictionStatus = restrictionStatus;
	}


	public List<String> getstudyIds() {
		return studyIds;
	}

	public void setstudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}


}

