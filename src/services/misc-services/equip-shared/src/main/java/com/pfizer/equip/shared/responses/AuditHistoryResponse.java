package com.pfizer.equip.shared.responses;

import java.util.List;

public class AuditHistoryResponse extends AbstractResponse {
   private List<AuditHistoryResponseItem> auditHistory;

   public List<AuditHistoryResponseItem> getAuditHistory() {
      return auditHistory;
   }

   public void setAuditHistory(List<AuditHistoryResponseItem> auditHistory) {
      this.auditHistory = auditHistory;
   }
}
