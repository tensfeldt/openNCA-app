package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;

public class GraabsStudyData {
   private String protocolId;
   private String clinicalDatablindingRequired;
   private String protocolBlindingDesignation;
   private String treatmentDatablindingRequired;
   private String studyBlindingStatus;
   private Date blindingStatusDate;

   public String getProtocolId() {
      return protocolId;
   }

   public void setProtocolId(String protocolId) {
      this.protocolId = protocolId;
   }

   public String getClinicalDatablindingRequired() {
      return clinicalDatablindingRequired;
   }

   public void setClinicalDatablindingRequired(String clinicalDatablindingRequired) {
      this.clinicalDatablindingRequired = clinicalDatablindingRequired;
   }

   public String getProtocolBlindingDesignation() {
      return protocolBlindingDesignation;
   }

   public void setProtocolBlindingDesignation(String protocolBlindingDesignation) {
      this.protocolBlindingDesignation = protocolBlindingDesignation;
   }

   public String getTreatmentDatablindingRequired() {
      return treatmentDatablindingRequired;
   }

   public void setTreatmentDatablindingRequired(String treatmentDatablindingRequired) {
      this.treatmentDatablindingRequired = treatmentDatablindingRequired;
   }

   public String getStudyBlindingStatus() {
      return studyBlindingStatus;
   }

   public void setStudyBlindingStatus(String studyBlindingStatus) {
      this.studyBlindingStatus = studyBlindingStatus;
   }

   public Date getBlindingStatusDate() {
      return blindingStatusDate;
   }

   public void setBlindingStatusDate(Date blindingStatusDate) {
      this.blindingStatusDate = blindingStatusDate;
   }
}
