package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import javax.json.JsonObject;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;

/**
 * Implementing classes expose methods to retrieve, create, and modify comments.
 * @author QUINTJ16
 *
 */
public interface CommentDAO {
	/**
	 * Returns the comment matching the provided comment ID.
	 * @param commentId
	 * @return {@link Comment} the comment
	 */
	public Comment getComment(String commentId);
	
	/**
	 * Returns a collection of comments matching the provided comment IDs.
	 * @param commentIds
	 * @return {@link List}<{@link Comment}> the comments
	 */
	public List<Comment> getComment(List<String> commentIds);
	
	/**
	 * Returns a collection of comments matching the provided comment IDs.
	 * @param commentIds
	 * @return {@link List}<{@link Comment}> the comments
	 */
	public List<Comment> getComment(String[] commentIds);
	
	/**
	 * Returns a collection of comments matching the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link List}<{@link Comment}> the comments
	 */
	public List<Comment> getCommentByDataframe(String dataframeId);

	/**
	 * Inserts the provided {@link Comment} object, using the provided parent ID. Returns the newly created comment.
	 * @param comment
	 * @param parentId
	 * @return
	 */
	public Comment insertComment(Comment comment, String parentId);
	
	/**
	 * Inserts the provided {@link Comment} objects, using the provided parent ID. Returns the newly created comments.
	 * @param comment
	 * @param parentId
	 * @return
	 */
	public List<Comment> insertComment(List<Comment> comment, String parentId);

	/**
	 * Updates the comment matching the provided comment ID and using the fields in the provided {@link Comment} object.
	 * @param comment
	 * @param commentId
	 * @return
	 */
	public boolean updateComment(Comment comment, String commentId);
}
