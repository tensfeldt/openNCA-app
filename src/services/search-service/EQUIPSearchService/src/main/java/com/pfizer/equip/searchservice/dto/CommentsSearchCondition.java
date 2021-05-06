package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Comments search condition
 * 
 * @author HeinemanWP
 *
 */
public class CommentsSearchCondition extends BaseSearchCondition {
	
	public CommentsSearchCondition() {
		super("commentsConditions");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CommentsSearchCondition(");
		sb.append(condition);
		sb.append("|");
		sb.append(property);
		sb.append("|");
		sb.append(value);
		sb.append(")");
		return sb.toString();
	}
	
	public static List<Predicate> toElasticSearch(List<CommentsSearchCondition> conditions) {
		List<Predicate> returnValue = new ArrayList<>();
		for(CommentsSearchCondition condition : conditions) {
			returnValue.add(condition.toElasticSearch());
		}
		return returnValue;
	}
}
