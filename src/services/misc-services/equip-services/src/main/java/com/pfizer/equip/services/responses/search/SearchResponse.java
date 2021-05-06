package com.pfizer.equip.services.responses.search;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.shared.responses.AbstractResponse;

/**
 * This class is based off of SearchResultsNode. It's a separate class in case we need additional information beyond what the ModeShape search results
 * provide.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class SearchResponse extends AbstractResponse {
   private Map<String, String> columns;

   private List<Map<String, String>> rows;

   public Map<String, String> getColumns() {
      return columns;
   }

   public void setColumns(Map<String, String> columns) {
      this.columns = columns;
   }

   public List<Map<String, String>> getRows() {
      return rows;
   }

   public void setRows(List<Map<String, String>> rows) {
      this.rows = rows;
   }
}
