package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Condition for metadata search.
 * 
 * @author HeinemanWP
 *
 */
public class MetaDataSearchCondition extends BaseSearchCondition {
	
	public MetaDataSearchCondition() {
		super("metadataConditions");
	}

	public static List<Predicate> toElasticSearch(List<MetaDataSearchCondition> conditions) {
		List<Predicate> returnValue = new ArrayList<>();
		for(MetaDataSearchCondition condition : conditions) {
			returnValue.add(condition.toElasticSearch());
		}
		return returnValue;
	}
}
