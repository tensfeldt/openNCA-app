package com.pfizer.pgrd.equip.services.opmeta.client;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to parse aliases endpoint which returns a single study id matching an alias
 * @author DeanDP
 *
 */

@XmlRootElement
public class OpmetaSimpleResponseBody {
	
	//this does not get the entire response, just the properties we need
	private String studyId;

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}
}




