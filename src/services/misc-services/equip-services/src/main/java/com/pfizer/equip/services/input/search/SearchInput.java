package com.pfizer.equip.services.input.search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
public class SearchInput {
   private List<String> typesToSearch;
   private String childFolder;
   private List<String> descendantFolders;
   private SearchFullText fullTextQuery;
   private List<SearchGroup> criteriaGroups;
   private List<String> excludedTypes;
   private List<SearchOrdering> ordering;

   public List<String> getTypesToSearch() {
      return typesToSearch;
   }

   public void setTypesToSearch(List<String> typesToSearch) {
      this.typesToSearch = typesToSearch;
   }

   public String getChildFolder() {
      return childFolder;
   }

   public void setChildFolder(String childFolder) {
      this.childFolder = childFolder;
   }

   public List<String> getDescendantFolders() {
      return descendantFolders;
   }

   public void setDescendantFolders(List<String> descendantFolders) {
      this.descendantFolders = descendantFolders;
   }

   public SearchFullText getFullTextQuery() {
      return fullTextQuery;
   }

   public void setFullTextQuery(SearchFullText fullTextQuery) {
      this.fullTextQuery = fullTextQuery;
   }

   public List<SearchGroup> getCriteriaGroups() {
      return criteriaGroups;
   }

   public void setCriteriaGroups(List<SearchGroup> criteriaGroups) {
      this.criteriaGroups = criteriaGroups;
   }

   public List<String> getExcludedTypes() {
      return excludedTypes;
   }

   public void setExcludedTypes(List<String> excludedTypes) {
      this.excludedTypes = excludedTypes;
   }

   public List<SearchOrdering> getOrdering() {
      return ordering;
   }

   public void setOrdering(List<SearchOrdering> ordering) {
      this.ordering = ordering;
   }
}