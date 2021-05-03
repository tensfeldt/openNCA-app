package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublishedParameter {
	private Date publishedDate;
	private String publishedBy;
	
	private List<Parameter> parameters = new ArrayList<>();
	
	public Date getPublishedDate() {
		return publishedDate;
	}
	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
	public String getPublishedBy() {
		return publishedBy;
	}
	public void setPublishedBy(String publishedBy) {
		this.publishedBy = publishedBy;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}
