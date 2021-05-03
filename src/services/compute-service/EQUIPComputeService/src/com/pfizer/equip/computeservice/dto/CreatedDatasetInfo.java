package com.pfizer.equip.computeservice.dto;

import java.util.Date;

public class CreatedDatasetInfo {
	private String filename;
	private Date lastModified;
	private long size;
	private byte[] data;
	private boolean systemReport;
	
	protected CreatedDatasetInfo() {}
	
	public CreatedDatasetInfo(String filename, Date lastModified, long size) {
		this.filename = filename;
		this.lastModified = lastModified;
		this.size = size;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isSystemReport() {
		return systemReport;
	}

	public void setSystemReport(boolean systemReport) {
		this.systemReport = systemReport;
	}

}
