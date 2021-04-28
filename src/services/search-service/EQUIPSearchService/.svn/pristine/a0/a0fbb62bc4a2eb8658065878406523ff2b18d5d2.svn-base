package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;
import com.pfizer.elasticsearch.dto.PropertyValuePair;

/**
 * File full text search request
 * 
 * @author HeinemanWP
 *
 */
public class FileTextSearchRequest extends BaseSearchRequest {
	private List<String> texts;

	public List<String> getTexts() {
		return texts;
	}

	public void setTexts(List<String> texts) {
		this.texts = texts;
	}

	@Override
	public Predicate toElasticSearch() {
		List<Predicate> basePredicates = new ArrayList<>();
		for (String text : texts) {
			basePredicates.add(new Predicate("=", new PropertyValuePair("jcr:data", text)));
		}
		PropertyValuePair mustPvp = new PropertyValuePair("must", basePredicates);
		return new Predicate("bool", mustPvp);
	}
	
}
