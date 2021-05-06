package com.pfizer.equip.services.responses.validation;

import java.util.List;

import com.pfizer.equip.services.business.validation.FileValidationLog;
import com.pfizer.equip.shared.responses.AbstractResponse;

public class ValidationResponse extends AbstractResponse {
   private String validationReportId;
   private List<FileValidationLog> fileValidationLog;

   public String getValidationReportId() {
      return validationReportId;
   }

   public void setValidationReportId(String reportId) {
      this.validationReportId = reportId;
   }

   public List<FileValidationLog> getFileValidationLog() {
      return fileValidationLog;
   }

   public void setFileValidationLog(List<FileValidationLog> fileValidationLog) {
      this.fileValidationLog = fileValidationLog;
   }
}
