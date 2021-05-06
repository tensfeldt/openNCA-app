package com.pfizer.equip.shared.opmeta.entity;

public class GraabsStudiesList {
   private Long numberOfRecordsFound;
   private GraabsStudyData[] studyBlindingStatuses;

   public Long getNumberOfRecordsFound() {
      return numberOfRecordsFound;
   }

   public void setNumberOfRecordsFound(Long numberOfRecordsFound) {
      this.numberOfRecordsFound = numberOfRecordsFound;
   }

   public GraabsStudyData[] getStudyBlindingStatuses() {
      return studyBlindingStatuses;
   }

   public void setStudyBlindingStatuses(GraabsStudyData[] studyBlindingStatuses) {
      this.studyBlindingStatuses = studyBlindingStatuses;
   }
}
