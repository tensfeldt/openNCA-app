package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.Date;

public class ProtocolContact {
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String role;
	private Date dateAdded;
	
	public ProtocolContact() {}
	public ProtocolContact(String firstName, String lastName, String role, Date dateAdded) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.dateAdded = dateAdded;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Date getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
}
