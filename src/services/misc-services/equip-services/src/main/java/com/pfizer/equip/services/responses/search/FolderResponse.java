package com.pfizer.equip.services.responses.search;

import java.util.List;
import java.util.Map;

import com.pfizer.equip.shared.responses.AbstractResponse;

public class FolderResponse extends AbstractResponse {
   private Map<String, String> columns;
   private List<Map<String, Object>> rows;

   public Map<String, String> getColumns() {
      return columns;
   }

   public void setColumns(Map<String, String> columns) {
      this.columns = columns;
   }

   public List<Map<String, Object>> getRows() {
      return rows;
   }

   public void setRows(List<Map<String, Object>> rows) {
      this.rows = rows;
   }
}
