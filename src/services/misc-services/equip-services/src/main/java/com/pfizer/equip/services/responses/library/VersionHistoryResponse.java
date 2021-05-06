package com.pfizer.equip.services.responses.library;

import java.util.List;

import com.pfizer.equip.shared.responses.AbstractResponse;

public class VersionHistoryResponse extends AbstractResponse {
   private List<VersionHistoryResponseItem> versionHistory;

   public List<VersionHistoryResponseItem> getVersionHistory() {
      return versionHistory;
   }

   public void setVersionHistory(List<VersionHistoryResponseItem> versionHistory) {
      this.versionHistory = versionHistory;
   }
}