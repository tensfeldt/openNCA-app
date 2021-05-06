package com.pfizer.pgrd.equip.dataframe.dto.equipinterface;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;

public interface EquipCommentable {
	/**
	 * Returns a {@link List} of {@link Comment} objects belonging to this object.
	 * @return
	 */
	public List<Comment> getComments();
	
	/**
	 * Sets this object's {@link List} of {@link Comment} objects to the provided list.
	 * @param comments
	 */
	public void setComments(List<Comment> comments);
}