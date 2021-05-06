package com.pfizer.equip.services.responses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.response.ComputeResponse;
import com.pfizer.equip.services.business.modeshape.nodes.BaseReportArtifactNode;
import com.pfizer.equip.shared.responses.AbstractResponse;

@JsonInclude(Include.NON_NULL)
public class ReportResponse extends AbstractResponse {
   Set<String> reportingItemIds;
   String artifactId;
   String artifactPath;
   Set<Dataframe> reportOutputs;
   Dataframe reportOutput;
   BaseReportArtifactNode reportArtifact;
   String reportId;
   ComputeResponse jobInfo;
   List<String> warningMessages;
   String equipID;
   String equipVersion;

   public Set<String> getReportingItemIds() {
      return reportingItemIds;
   }

   public void setReportingItemIds(Set<String> reportingItemIds) {
      this.reportingItemIds = reportingItemIds;
   }

   public void addReportingItemId(String reportingItemId) {
      if (reportingItemIds == null) {
         reportingItemIds = new HashSet<String>();
      }
      this.reportingItemIds.add(reportingItemId);
   }

   public String getReportId() {
      return reportId;
   }

   public void setReportId(String reportId) {
      this.reportId = reportId;
   }

   public BaseReportArtifactNode getReportArtifact() {
      return reportArtifact;
   }

   public void setReportArtifact(BaseReportArtifactNode reportArtifact) {
      this.reportArtifact = reportArtifact;
   }

   public String getArtifactId() {
      return artifactId;
   }

   public void setArtifactId(String artifactId) {
      this.artifactId = artifactId;
   }

   public ComputeResponse getJobInfo() {
      return jobInfo;
   }

   public void setJobInfo(ComputeResponse jobInfo) {
      this.jobInfo = jobInfo;
   }

   public String getArtifactPath() {
      return artifactPath;
   }

   public void setArtifactPath(String artifactPath) {
      this.artifactPath = artifactPath;
   }

   public Set<Dataframe> getReportOutputs() {
      return reportOutputs;
   }

   public void setReportOutputs(Set<Dataframe> reportOutputs) {
      this.reportOutputs = reportOutputs;
   }

   public Dataframe getReportOutput() {
      return reportOutput;
   }

   public void setReportOutput(Dataframe reportOutput) {
      this.reportOutput = reportOutput;
   }

   public List<String> getWarningMessages() {
      return warningMessages;
   }

   public void setWarningMessages(List<String> warningMessages) {
      this.warningMessages = warningMessages;
   }

   public void addWarningMessage(String warningMessage) {
      if (warningMessages == null) {
         warningMessages = new ArrayList<String>();
      }
      this.warningMessages.add(warningMessage);
   }

   public String getEquipID() {
      return equipID;
   }

   public void setEquipID(String equipID) {
      this.equipID = equipID;
   }

   public String getEquipVersion() {
      return equipVersion;
   }

   public void setEquipVersion(String equipVersion) {
      this.equipVersion = equipVersion;
   }
}