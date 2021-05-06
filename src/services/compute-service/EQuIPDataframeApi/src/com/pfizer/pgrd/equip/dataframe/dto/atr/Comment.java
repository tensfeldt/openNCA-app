package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
	private String body;
	private String createdBy;
	private Date createdDate;
	private String id;
	
	public static final List<Comment> fromComment(List<com.pfizer.pgrd.equip.dataframe.dto.Comment> comments) {
		List<Comment> list = new ArrayList<>();
		if(comments != null) {
			for(com.pfizer.pgrd.equip.dataframe.dto.Comment c : comments) {
				if(c != null) {
					list.add(Comment.fromComment(c));
				}
			}
		}
		
		return list;
	}
	
	public static final Comment fromComment(com.pfizer.pgrd.equip.dataframe.dto.Comment c) {
		Comment comment = null;
		if(c != null) {
			comment = new Comment();
			comment.setBody(c.getBody());
			comment.setCreatedDate(c.getCreated());
			comment.setCreatedBy(c.getCreatedBy());
			comment.setId(c.getId());
		}
		
		return comment;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date created) {
		this.createdDate = created;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
