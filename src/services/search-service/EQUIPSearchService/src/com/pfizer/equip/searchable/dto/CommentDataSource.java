package com.pfizer.equip.searchable.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * Used to retrieve equip:comments from Elasticsearch index
 * 
 * @author HeinemanWP
 *
 */
public class CommentDataSource {
	@Expose
	@SerializedName("_index")
	private String index;
	@Expose
	@SerializedName("_type")
	private String type;
	@Expose
	@SerializedName("_id")
	private String id;
	@Expose
	@SerializedName("_score")
	private String score;
	@Expose
	@SerializedName("_source")
	private CommentData commentData;
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public CommentData getCommentData() {
		return commentData;
	}
	public void setCommentData(CommentData commentData) {
		this.commentData = commentData;
	}
	@Override
	public String toString() {
		return "CommentDataSource [index=" + index + ", type=" + type + ", id=" + id + ", score=" + score
				+ ", commentData=" + commentData + "]";
	}
		
}
