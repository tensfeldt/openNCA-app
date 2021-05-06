package com.pfizer.equip.services.input.dataframe;

import java.util.List;

public class MergeKeyJoin {
   String joinType;
   String fileL;
   String fileR;
   List<String> fileLColumnNames;
   List<String> fileRColumnNames;
   List<String> displayColumnNames;

   public String getJoinType() {
      return joinType;
   }
   public void setJoinType(String joinType) {
      this.joinType = joinType;
   }
   public String getFileL() {
      return fileL;
   }
   public void setFileL(String fileL) {
      this.fileL = fileL;
   }
   public String getFileR() {
      return fileR;
   }
   public void setFileR(String fileR) {
      this.fileR = fileR;
   }
   public List<String> getFileLColumnNames() {
      return fileLColumnNames;
   }
   public void setFileLColumnNames(List<String> fileLColumnNames) {
      this.fileLColumnNames = fileLColumnNames;
   }
   public List<String> getFileRColumnNames() {
      return fileRColumnNames;
   }
   public void setFileRColumnNames(List<String> fileRColumnNames) {
      this.fileRColumnNames = fileRColumnNames;
   }
   public List<String> getDisplayColumnNames() {
      return displayColumnNames;
   }
   public void setDisplayColumnNames(List<String> displayColumnNames) {
      this.displayColumnNames = displayColumnNames;
   }
}
