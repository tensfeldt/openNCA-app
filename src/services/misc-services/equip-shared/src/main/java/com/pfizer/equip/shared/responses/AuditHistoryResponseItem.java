package com.pfizer.equip.shared.responses;

import java.util.Date;

public class AuditHistoryResponseItem {
   private Long id;
   private String equipId;
   private String versionNumber;
   private String actionBy;
   private Date actionDate;
   private String action;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getEquipId() {
      return equipId;
   }

   public void setEquipId(String equipId) {
      this.equipId = equipId;
   }

   public String getVersionNumber() {
      return versionNumber;
   }

   public void setVersionNumber(String versionNumber) {
      this.versionNumber = versionNumber;
   }

   public String getActionBy() {
      return actionBy;
   }

   public void setActionBy(String actionBy) {
      this.actionBy = actionBy;
   }

   public Date getActionDate() {
      return actionDate;
   }

   public void setActionDate(Date actionDate) {
      this.actionDate = actionDate;
   }

   public String getAction() {
      return action;
   }

   public void setAction(String action) {
      this.action = action;
   }
}
