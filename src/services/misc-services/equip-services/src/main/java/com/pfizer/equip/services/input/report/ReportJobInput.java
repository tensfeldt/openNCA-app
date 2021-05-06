package com.pfizer.equip.services.input.report;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pfizer.equip.services.business.api.input.KeyValuePairInput;

public class ReportJobInput {
   // Must be ordered:
   String equipId;
   List<String> dataframeIds;
   List<String> assemblyIds;
   Set<KeyValuePairInput> parameters = new HashSet<KeyValuePairInput>();

   public List<String> getDataframeIds() {
      return dataframeIds;
   }
   public void setDataframeIds(List<String> dataframeIds) {
      this.dataframeIds = dataframeIds;
   }

   public Set<KeyValuePairInput> getParameters() {
      return parameters;
   }

   public void setParameters(Set<KeyValuePairInput> parameters) {
      this.parameters = parameters;
   }
   
   public void addParameter(KeyValuePairInput parameter) {
      this.parameters.add(parameter);
   }

   public String getEquipId() {
      return equipId;
   }
   public void setEquipId(String equipId) {
      this.equipId = equipId;
   }
   public List<String> getAssemblyIds() {
      return assemblyIds;
   }
   public void setAssemblyIds(List<String> assemblyIds) {
      this.assemblyIds = assemblyIds;
   }
}