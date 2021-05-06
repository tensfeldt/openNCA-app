package com.pfizer.equip.shared.service.business.audit.input;

public class AuditFilterInput {
   private String[] actionFilter;
   private Integer currentVersion;

   public String[] getActionFilter() {
      return actionFilter;
   }

   public void setActionFilter(String[] actionFilter) {
      this.actionFilter = actionFilter;
   }

   public Integer getCurrentVersion() {
      return currentVersion;
   }

   public void setCurrentVersion(Integer currentVersion) {
      this.currentVersion = currentVersion;
   }
}
