package com.pfizer.equip.services.business.validation;

import java.util.LinkedList;
import java.util.List;

import com.pfizer.equip.services.input.validation.CrossFileLevelLogReport;
import com.pfizer.equip.services.input.validation.FieldLevelLogReport;
import com.pfizer.equip.services.input.validation.FieldSetLevelLogReport;
import com.pfizer.equip.services.input.validation.FileLevelLog;
import com.pfizer.equip.services.input.validation.FileLevelLogReport;
import com.pfizer.equip.services.input.validation.GlobalInfoReport;
import com.pfizer.equip.services.input.validation.SdeidLevelLogReport;
import com.pfizer.equip.services.input.validation.UniqueSetLevelLogReport;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class FileValidationLog {

   /**
    * File Validation Log to be used to display the list of errors/warnings/info in file validation
    */

   private GlobalInfoReport globalInfoReport;
   private FileLevelLog fileNameError;
   private String delimiterError;
   private String errorOrException;
   private FileLevelLogReport fileLevelLogReport = new FileLevelLogReport();
   private List<FieldLevelLogReport> fieldLevelLogReportList = new LinkedList<>();
   private List<FieldSetLevelLogReport> fieldSetLevelLogReportList = new LinkedList<>();
   private List<SdeidLevelLogReport> sdeIdLevelLogReportList = new LinkedList<>();
   // Duplicate Records uses the same structure of attributes as SDEID
   private List<SdeidLevelLogReport> duplicateRecordsLevelLogReportList = new LinkedList<>();
   private List<CrossFileLevelLogReport> crossFileLevelLogReportList = new LinkedList<>();
   private List<UniqueSetLevelLogReport> uniqueSetLevelLogReportList = new LinkedList<>();
   private ValidationStatusTypes fieldLevelValidationStatus;
   private ValidationStatusTypes fileLevelValidationStatus;
   private ValidationStatusTypes fieldSetLevelValidationStatus;
   private ValidationStatusTypes duplicateRecordsValidationStatus;
   private ValidationStatusTypes crossFileFieldValidationStatus;
   private ValidationStatusTypes uniquenessValidationStatus;

   public GlobalInfoReport getGlobalInfoReport() {
      return globalInfoReport;
   }

   public void setGlobalInfoReport(GlobalInfoReport globalInfoReport) {
      this.globalInfoReport = globalInfoReport;
   }

   public FileLevelLog getFileNameError() {
      return fileNameError;
   }

   public void setFileNameError(FileLevelLog fileNameError) {
      this.fileNameError = fileNameError;
   }

   public String getDelimiterError() {
      return delimiterError;
   }

   public void setDelimiterError(String delimiterError) {
      this.delimiterError = delimiterError;
   }

   public String getErrorOrException() {
      return errorOrException;
   }

   public void setErrorOrException(String errorOrException) {
      this.errorOrException = errorOrException;
   }

   public FileLevelLogReport getFileLevelLogReport() {
      return fileLevelLogReport;
   }

   public void setFileLevelLogReport(FileLevelLogReport fileLevelLogReport) {
      this.fileLevelLogReport = fileLevelLogReport;
   }

   public List<FieldLevelLogReport> getFieldLevelLogReportList() {
      return fieldLevelLogReportList;
   }

   public void setFieldLevelLogReportList(List<FieldLevelLogReport> fieldLevelLogReportList) {
      this.fieldLevelLogReportList = fieldLevelLogReportList;
   }

   public List<FieldSetLevelLogReport> getFieldSetLevelLogReportList() {
      return fieldSetLevelLogReportList;
   }

   public void setFieldSetLevelLogReportList(List<FieldSetLevelLogReport> fieldSetLevelLogReportList) {
      this.fieldSetLevelLogReportList = fieldSetLevelLogReportList;
   }

   public List<SdeidLevelLogReport> getSdeIdLevelLogReportList() {
      return sdeIdLevelLogReportList;
   }

   public void setSdeIdLevelLogReportList(List<SdeidLevelLogReport> sdeIdLevelLogReportList) {
      this.sdeIdLevelLogReportList = sdeIdLevelLogReportList;
   }

   public ValidationStatusTypes getFieldLevelValidationStatus() {
      return fieldLevelValidationStatus;
   }

   public void setFieldLevelValidationStatus(ValidationStatusTypes fieldLevelValidationStatus) {
      this.fieldLevelValidationStatus = fieldLevelValidationStatus;
   }

   public ValidationStatusTypes getFileLevelValidationStatus() {
      return fileLevelValidationStatus;
   }

   public void setFileLevelValidationStatus(ValidationStatusTypes fileLevelValidationStatus) {
      this.fileLevelValidationStatus = fileLevelValidationStatus;
   }

   public ValidationStatusTypes getFieldSetLevelValidationStatus() {
      return fieldSetLevelValidationStatus;
   }

   public void setFieldSetLevelValidationStatus(ValidationStatusTypes fieldSetLevelValidationStatus) {
      this.fieldSetLevelValidationStatus = fieldSetLevelValidationStatus;
   }

   public ValidationStatusTypes getUniquenessValidationStatus() {
      return uniquenessValidationStatus;
   }

   public void setUniquenessValidationStatus(ValidationStatusTypes uniquenessValidationStatus) {
      this.uniquenessValidationStatus = uniquenessValidationStatus;
   }

   public List<UniqueSetLevelLogReport> getUniqueSetLevelLogReportList() {
      return uniqueSetLevelLogReportList;
   }

   public void setUniqueSetLevelLogReportList(List<UniqueSetLevelLogReport> uniqueSetLevelLogReportList) {
      this.uniqueSetLevelLogReportList = uniqueSetLevelLogReportList;
   }

   public List<CrossFileLevelLogReport> getCrossFileLevelLogReportList() {
      return crossFileLevelLogReportList;
   }

   public void setCrossFileLevelLogReportList(List<CrossFileLevelLogReport> crossFileLevelLogReportList) {
      this.crossFileLevelLogReportList = crossFileLevelLogReportList;
   }

   public ValidationStatusTypes getCrossFileFieldValidationStatus() {
      return crossFileFieldValidationStatus;
   }

   public void setCrossFileFieldValidationStatus(ValidationStatusTypes crossFileFieldValidationStatus) {
      this.crossFileFieldValidationStatus = crossFileFieldValidationStatus;
   }

   public ValidationStatusTypes getDuplicateRecordsValidationStatus() {
      return duplicateRecordsValidationStatus;
   }

   public void setDuplicateRecordsValidationStatus(ValidationStatusTypes duplicateRecordsValidationStatus) {
      this.duplicateRecordsValidationStatus = duplicateRecordsValidationStatus;
   }

   public List<SdeidLevelLogReport> getDuplicateRecordsLevelLogReportList() {
      return duplicateRecordsLevelLogReportList;
   }

   public void setDuplicateRecordsLevelLogReportList(List<SdeidLevelLogReport> duplicateRecordsLevelLogReportList) {
      this.duplicateRecordsLevelLogReportList = duplicateRecordsLevelLogReportList;
   }

}
