package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class File {
	private String equipId;
	private String location;
	private String server;
	private String sourceFile;
	private Date loadDate;
	private String loadedBy;
	private int loadedRecords;
	private int skippedRecords;
	private int totalRecords;
	private String name;
	private String id;
	private boolean userHasAccess;
	
	private List<String> loadErrors = new ArrayList<>();
	private List<Comment> comments = new ArrayList<>();
	
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	public Date getLoadDate() {
		return loadDate;
	}
	public void setLoadDate(Date loadDate) {
		this.loadDate = loadDate;
	}
	public String getLoadedBy() {
		return loadedBy;
	}
	public void setLoadedBy(String loadedBy) {
		this.loadedBy = loadedBy;
	}
	public int getLoadedRecords() {
		return loadedRecords;
	}
	public void setLoadedRecords(int loadedRecord) {
		this.loadedRecords = loadedRecord;
	}
	public int getSkippedRecords() {
		return skippedRecords;
	}
	public void setSkippedRecords(int skippedRecords) {
		this.skippedRecords = skippedRecords;
	}
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public List<String> getLoadErrors() {
		return loadErrors;
	}
	public void setLoadErrors(List<String> loadErrors) {
		this.loadErrors = loadErrors;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isUserHasAccess() {
		return userHasAccess;
	}
	public void setUserHasAccess(boolean userHasAccess) {
		this.userHasAccess = userHasAccess;
	}
}