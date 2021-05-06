package com.pfizer.equip.services.input.validation;

import java.util.Date;

public class GlobalInfoReport {
   private String toolName;
   private String specificationType;
   private String specificationVersion;
   private String artifactVersion;
   private String tester;
   private String hostName;
   private String platForm;
   private String fileName;
   private Date validationDoneDate;
   private String validationStatus;
   private String numberRows;
   private String numberSubjects;
   private String pkterm;
   private String warning;
   private String error;

   public String getToolName() {
      return toolName;
   }

   public void setToolName(String toolName) {
      this.toolName = toolName;
   }

   public String getSpecificationType() {
      return specificationType;
   }

   public void setSpecificationType(String specificationType) {
      this.specificationType = specificationType;
   }

   public String getSpecificationVersion() {
      return specificationVersion;
   }

   public void setSpecificationVersion(String specificationVersion) {
      this.specificationVersion = specificationVersion;
   }

   public String getArtifactVersion() {
      return artifactVersion;
   }

   public void setArtifactVersion(String artifactVersion) {
      this.artifactVersion = artifactVersion;
   }

   public String getTester() {
      return tester;
   }

   public void setTester(String tester) {
      this.tester = tester;
   }

   public String getHostName() {
      return hostName;
   }

   public void setHostName(String hostName) {
      this.hostName = hostName;
   }

   public String getPlatForm() {
      return platForm;
   }

   public void setPlatForm(String platForm) {
      this.platForm = platForm;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public Date getValidationDoneDate() {
      return validationDoneDate;
   }

   public void setValidationDoneDate(Date validationDoneDate) {
      this.validationDoneDate = validationDoneDate;
   }

   public String getValidationStatus() {
      return validationStatus;
   }

   public void setValidationStatus(String validationStatus) {
      this.validationStatus = validationStatus;
   }

   public String getNumberRows() {
      return numberRows;
   }

   public void setNumberRows(String numberRows) {
      this.numberRows = numberRows;
   }

   public String getNumberSubjects() {
      return numberSubjects;
   }

   public void setNumberSubjects(String numberSubjects) {
      this.numberSubjects = numberSubjects;
   }

   public String getPkterm() {
      return pkterm;
   }

   public void setPkterm(String pkterm) {
      this.pkterm = pkterm;
   }

   public String getWarning() {
      return warning;
   }

   public void setWarning(String warning) {
      this.warning = warning;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }
}
