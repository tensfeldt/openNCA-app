package com.pfizer.equip.services.input.search;

import java.util.List;
import java.util.Map;

public class SavedSearchInput {
   private String name;
   private int pageSize;
   private boolean override = false;
   private Map<String, Object> searchCriteria;
   private List<Map<String, Object>> searchResults;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getPageSize() {
      return pageSize;
   }

   public void setPageSize(int pageSize) {
      this.pageSize = pageSize;
   }

   public boolean isOverride() {
      return override;
   }

   public void setOverride(boolean override) {
      this.override = override;
   }

   public Map<String, Object> getSearchCriteria() {
      return searchCriteria;
   }

   public void setSearchCriteria(Map<String, Object> searchCriteria) {
      this.searchCriteria = searchCriteria;
   }

   public List<Map<String, Object>> getSearchResults() {
      return searchResults;
   }

   public void setSearchResults(List<Map<String, Object>> searchResults) {
      this.searchResults = searchResults;
   }
}
