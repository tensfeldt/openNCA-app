package com.pfizer.equip.computeservice.dto;

public class DataframeCreationReportEntry {
	private String dataframeId;
	private String datasetId;
	private String complexDataId;
	private int statusCode;
	private String message;
	private String fileName;
	private boolean rolledBack;
	
	
	public String getDataframeId() {
		return dataframeId;
	}
	public void setDataframeId(String dataframeId) {
		this.dataframeId = dataframeId;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public String getComplexDataId() {
		return complexDataId;
	}
	public void setComplexDataId(String complexDataId) {
		this.complexDataId = complexDataId;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isRolledBack() {
		return rolledBack;
	}
	public void setRolledBack(boolean rolledBack) {
		this.rolledBack = rolledBack;
	}
	
	@Override
	public String toString() {
		return "DataframeCreationReportEntry [dataframeId=" + dataframeId + ", datasetId=" + datasetId
				+ ", complexDataId=" + complexDataId + ", statusCode=" + statusCode + ", message=" + message
				+ ", fileName=" + fileName + ", rolledBack=" + rolledBack + "]";
	}
	
}
