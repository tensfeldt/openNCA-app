package com.pfizer.equip.shared.service.business.api;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportingEvent {
   // intentionally simple bean, only minimal fields
   String id;
   String reportingEventName;
   String reportingEventReleaseStatusKey;
   String equipId;
   Set<String> studyIds;
   Set<String> reportingEventItemIds;
   
   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }
   public String getReportingEventName() {
      return reportingEventName;
   }
   public void setReportingEventName(String reportingEventName) {
      this.reportingEventName = reportingEventName;
   }
   public String getReportingEventReleaseStatusKey() {
      return reportingEventReleaseStatusKey;
   }
   public void setReportingEventReleaseStatusKey(String reportingEventReleaseStatusKey) {
      this.reportingEventReleaseStatusKey = reportingEventReleaseStatusKey;
   }
   public String getEquipId() {
      return equipId;
   }
   public void setEquipId(String equipId) {
      this.equipId = equipId;
   }
   public Set<String> getStudyIds() {
      return studyIds;
   }
   public void setStudyIds(Set<String> studyIds) {
      this.studyIds = studyIds;
   }
   public Set<String> getReportingEventItemIds() {
      return reportingEventItemIds;
   }
   public void setReportingEventItemIds(Set<String> reportingEventItemIds) {
      this.reportingEventItemIds = reportingEventItemIds;
   }
}