package com.pfizer.equip.services.input.search;

import java.util.List;

public class SearchGroup {
   private SearchMode mode;
   private List<SearchCriteria> criteria;
   private List<SearchGroup> subCriteriaGroups;

   public SearchMode getMode() {
      return mode;
   }

   public void setMode(SearchMode mode) {
      this.mode = mode;
   }

   public List<SearchCriteria> getCriteria() {
      return criteria;
   }

   public void setCriteria(List<SearchCriteria> criteria) {
      this.criteria = criteria;
   }

   public List<SearchGroup> getSubCriteriaGroups() {
      return subCriteriaGroups;
   }

   public void setSubCriteriaGroups(List<SearchGroup> subCriteriaGroups) {
      this.subCriteriaGroups = subCriteriaGroups;
   }
}
