package com.pfizer.equip.services.input.dataframe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeKey {
   List<MergeKeyVariable> files;
   List<MergeKeyJoin> joins;

   public List<MergeKeyVariable> getFiles() {
      return files;
   }
   public void setFiles(List<MergeKeyVariable> files) {
      this.files = files;
   }
   public List<MergeKeyJoin> getJoins() {
      return joins;
   }
   public void setJoins(List<MergeKeyJoin> joins) {
      this.joins = joins;
   }
}