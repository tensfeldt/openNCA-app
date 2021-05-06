package com.pfizer.equip.services.input.validation;

import java.util.LinkedList;
import java.util.List;

public class FileLevelLogReport {
   private List<FileLevelLog> extraneousFieldList = new LinkedList<>();
   private List<FileLevelLog> unorderedFieldList = new LinkedList<>();
   private List<FileLevelLog> missingMandatoryFieldList = new LinkedList<>();
   private List<FileLevelLog> caseMismatchFieldList = new LinkedList<>();
   private List<FileLevelLog> leadingTrailingSpacesColumnsList = new LinkedList<>();
   private List<EmptyLinesLog> emptyLinesList = new LinkedList<>();
   private List<FileLevelLog> nonUniqueColumnList = new LinkedList<>();
   private List<FileLevelLog>  uniqueColumnList = new LinkedList<>();
   private List<FileLevelLog>  mandatoryIfAvailableList = new LinkedList<>();
   private List<FileLevelLog>  emptyColumnsList = new LinkedList<>();

   public List<FileLevelLog> getExtraneousFieldList() {
      return extraneousFieldList;
   }

   public void setExtraneousFieldList(List<FileLevelLog> extraneousFieldList) {
      this.extraneousFieldList = extraneousFieldList;
   }

   public List<FileLevelLog> getUnorderedFieldList() {
      return unorderedFieldList;
   }

   public void setUnorderedFieldList(List<FileLevelLog> unorderedFieldList) {
      this.unorderedFieldList = unorderedFieldList;
   }

   public List<FileLevelLog> getMissingMandatoryFieldList() {
      return missingMandatoryFieldList;
   }

   public void setMissingMandatoryFieldList(List<FileLevelLog> missingMandatoryFieldList) {
      this.missingMandatoryFieldList = missingMandatoryFieldList;
   }

   public List<FileLevelLog> getCaseMismatchFieldList() {
      return caseMismatchFieldList;
   }

   public void setCaseMismatchFieldList(List<FileLevelLog> caseMismatchFieldList) {
      this.caseMismatchFieldList = caseMismatchFieldList;
   }

   public List<FileLevelLog> getLeadingTrailingSpacesColumnsList() {
      return leadingTrailingSpacesColumnsList;
   }

   public void setLeadingTrailingSpacesColumnsList(List<FileLevelLog> leadingTrailingSpacesColumnsList) {
      this.leadingTrailingSpacesColumnsList = leadingTrailingSpacesColumnsList;
   }

   public List<EmptyLinesLog> getEmptyLinesList() {
      return emptyLinesList;
   }

   public void setEmptyLinesList(List<EmptyLinesLog> emptyLinesList) {
      this.emptyLinesList = emptyLinesList;
   }

   public List<FileLevelLog> getNonUniqueColumnList() {
      return nonUniqueColumnList;
   }

   public void setNonUniqueColumnList(List<FileLevelLog> nonUniqueColumnList) {
      this.nonUniqueColumnList = nonUniqueColumnList;
   }

   public List<FileLevelLog> getUniqueColumnList() {
      return uniqueColumnList;
   }

   public void setUniqueColumnList(List<FileLevelLog> uniqueColumnList) {
      this.uniqueColumnList = uniqueColumnList;
   }

   public List<FileLevelLog> getMandatoryIfAvailableList() {
      return mandatoryIfAvailableList;
   }

   public void setMandatoryIfAvailableList(List<FileLevelLog> mandatoryIfAvailableList) {
      this.mandatoryIfAvailableList = mandatoryIfAvailableList;
   }

   public List<FileLevelLog> getEmptyColumnsList() {
      return emptyColumnsList;
   }

   public void setEmptyColumnsList(List<FileLevelLog> emptyColumnsList) {
      this.emptyColumnsList = emptyColumnsList;
   }
   
}
