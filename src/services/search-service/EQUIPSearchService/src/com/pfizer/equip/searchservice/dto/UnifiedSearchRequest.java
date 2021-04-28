package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Unified search request
 * 
 * @author HeinemanWP
 *
 */
public class UnifiedSearchRequest {
	private MetaDataSearchRequest metadataConditions;
	private FileDataSearchRequest fileDataConditions;
	private FileTextSearchRequest fileTextConditions;
	
	public UnifiedSearchRequest() {}

	public MetaDataSearchRequest getMetadataConditions() {
		return metadataConditions;
	}

	public void setMetadataConditions(MetaDataSearchRequest metadataConditions) {
		this.metadataConditions = metadataConditions;
		updateMetadataConditionsForStudyIds();
		updateMetadataConditionsForReportingEventItems();
	}
	
	private void updateMetadataConditionsForReportingEventItems() {
		if (metadataConditions.hasCondition("=", "jcr:primaryType", "equip:reportingEventItem") &&
				metadataConditions.hasCondition("=", "equip:equipId")) {
			List<BaseSearchCondition> matchingConditions = metadataConditions.findCondition("=", "equip:equipId");
			for (BaseSearchCondition matchingCondition : matchingConditions) {
				MetaDataSearchRequest newCondition = new MetaDataSearchRequest();
				newCondition.getConditions().add(new BaseSearchCondition(null, "=", "equip:equipId", matchingCondition.getValue()));
				newCondition.getConditions().add(new BaseSearchCondition(null, "=", "equip:parentEquipId", matchingCondition.getValue()));
				newCondition.getConditions().add(new BaseSearchCondition(null, "=", "equip:assemblyEquipIds", matchingCondition.getValue()));
				newCondition.getConditions().add(new BaseSearchCondition(null, "=", "equip:dataframeEquipIds", matchingCondition.getValue()));
				newCondition.setOperator("OR");
				metadataConditions.replaceCondition(matchingCondition, newCondition);
			}
		}
	}
	
	private void updateMetadataConditionsForStudyIds() {
		if (metadataConditions.hasCondition("=", "equip:studyId")) {
			List<BaseSearchCondition> matchingConditions = metadataConditions.findCondition("=", "equip:studyId");
			for (BaseSearchCondition matchingCondition : matchingConditions) {
				BaseSearchCondition newCondition = new BaseSearchCondition(null, "like", "equip:studyId", "*" + matchingCondition.getValue());
				metadataConditions.replaceCondition(matchingCondition, newCondition);
			}			
		}
	}
	
	public FileDataSearchRequest getFileDataConditions() {
		return fileDataConditions;
	}

	public void setFileDataConditions(FileDataSearchRequest fileDataConditions) {
		this.fileDataConditions = fileDataConditions;
	}

	public FileTextSearchRequest getFileTextConditions() {
		return fileTextConditions;
	}

	public void setFileTextConditions(FileTextSearchRequest fileTextConditions) {
		this.fileTextConditions = fileTextConditions;
	}
	
	public Predicate toElasticSearch() {
		List<Predicate> predicates = new ArrayList<>();
		String conditional = "must";
		if (metadataConditions != null) {
			predicates.add(metadataConditions.toElasticSearch());
		}
		if (fileDataConditions != null) {
			predicates.add(fileDataConditions.toElasticSearch());
		}
		if (fileTextConditions != null) {
			predicates.add(fileTextConditions.toElasticSearch());
		}
		return new Predicate("bool", conditional, predicates);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UnifiedSearchRequest(");
		if (metadataConditions != null) {
			sb.append(metadataConditions.toString());
			sb.append("|");
		}
		if (fileDataConditions != null) {
			sb.append(fileDataConditions.toString());
			sb.append("|");
		}
		if (fileTextConditions != null) {
			sb.append(fileTextConditions.toString());
		}
		sb.append(")");
		return sb.toString();
	}

}
