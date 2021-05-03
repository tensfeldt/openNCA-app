package com.pfizer.pgrd.equip.services.opmeta.client;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OpmetaResponseBody {

	// this does not get the entire response, just the properties we need
	private List<Program> programs;

	public List<Program> getPrograms() {
		return programs;
	}

	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}

}
