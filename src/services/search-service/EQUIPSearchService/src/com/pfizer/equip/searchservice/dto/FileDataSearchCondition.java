package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Condition for file data search.
 * 
 * @author HeinemanWP
 *
 */
public class FileDataSearchCondition extends BaseSearchCondition {

	public FileDataSearchCondition() {
		super("filedataConditions");
	}

	public static List<Predicate> toElasticSearch(List<FileDataSearchCondition> conditions) {
		List<Predicate> returnValue = new ArrayList<>();
		for(FileDataSearchCondition condition : conditions) {
			returnValue.add(condition.toElasticSearch());
		}
		return returnValue;
	}
}
