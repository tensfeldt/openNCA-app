package com.pfizer.equip.services.input.validation.types;

public enum ValidationStatusTypes {
   SUCCESS("SUCCESSFUL"),
   INFO("INFO"),
   WARNING("WARNING"),
   ERROR("FAILED_WITH_ERRORS");

   ValidationStatusTypes(String statusType) {
      this.setStatusType(statusType);
   }

   private String statusType;

   public String getStatusType() {
      return statusType;
   }

   public void setStatusType(String statusType) {
      this.statusType = statusType;
   }
   
   public static ValidationStatusTypes mostSevere(ValidationStatusTypes status1, ValidationStatusTypes status2) {
      return status1.ordinal() > status2.ordinal() ? status1 : status2;
   }
}
