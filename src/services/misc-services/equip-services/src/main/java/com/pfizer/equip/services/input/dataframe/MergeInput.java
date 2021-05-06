package com.pfizer.equip.services.input.dataframe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeInput {
   private boolean doPimsLoad;
   private String mappingInformationList;
   private List<MergeKeyJoin> joins;
   private List<Integer> fileIndexes;
   private String pkterm;
   private String analytes;
   private String programId;
   private String protocolId;

   public List<MergeKeyJoin> getJoins() {
      return joins;
   }

   public void setJoins(List<MergeKeyJoin> joins) {
      this.joins = joins;
   }

   public String getMappingInformationList() {
      return mappingInformationList;
   }

   public void setMappingInformationList(String mappingInformationList) {
      this.mappingInformationList = mappingInformationList;
   }

   public boolean isDoPimsLoad() {
      return doPimsLoad;
   }

   public void setDoPimsLoad(boolean doPimsLoad) {
      this.doPimsLoad = doPimsLoad;
   }

   public List<Integer> getFileIndexes() {
      return fileIndexes;
   }

   public void setFileIndexes(List<Integer> fileIndexes) {
      this.fileIndexes = fileIndexes;
   }

   public String getPkterm() {
      return pkterm;
   }

   public void setPkterm(String pkterm) {
      this.pkterm = pkterm;
   }

   public String getAnalytes() {
      return analytes;
   }

   public void setAnalytes(String analytes) {
      this.analytes = analytes;
   }

   public String getProgramId() {
      return programId;
   }

   public void setProgramId(String programId) {
      this.programId = programId;
   }

   public String getProtocolId() {
      return protocolId;
   }

   public void setProtocolId(String protocolId) {
      this.protocolId = protocolId;
   }
}