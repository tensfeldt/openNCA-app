package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.Date;

public class Program {
	private String program;
	private String compoundNumber;
	private String tradeName;
	private Date setupDate;
	private String setupBy;
	private Protocol protocol;
	
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getCompoundNumber() {
		return compoundNumber;
	}
	public void setCompoundNumber(String compoundNumber) {
		this.compoundNumber = compoundNumber;
	}
	public String getTradeName() {
		return tradeName;
	}
	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}
	public Date getSetupDate() {
		return setupDate;
	}
	public void setSetupDate(Date setupDate) {
		this.setupDate = setupDate;
	}
	public String getSetupBy() {
		return setupBy;
	}
	public void setSetupBy(String setupBy) {
		this.setupBy = setupBy;
	}
	public Protocol getProtocol() {
		return protocol;
	}
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}	
}
