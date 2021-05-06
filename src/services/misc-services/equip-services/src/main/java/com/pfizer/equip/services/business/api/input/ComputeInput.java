package com.pfizer.equip.services.business.api.input;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
public class ComputeInput {
   String user;
   String computeContainer;
   String environment;
   String scriptId;
   String dataframeType;
   String equipId;
   String subType;
   Set<? extends GenericInput> parameters;
   List<String> dataframes;
   List<String> assemblies;

   public ComputeInput(String user, String computeContainer, String environment, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframes,
         List<String> assemblies, String dataframeType, String equipId) {
      this.user = user;
      this.computeContainer = computeContainer;
      this.environment = environment;
      this.scriptId = scriptId;
      this.dataframes = dataframes;
      this.assemblies = assemblies;
      this.dataframeType = dataframeType;
      this.parameters = parameters;
      this.equipId = equipId;
   }

   public ComputeInput(String user, String computeContainer, String environment, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframes,
         List<String> assemblies, String dataframeType, String equipId, String subType) {
      this.user = user;
      this.computeContainer = computeContainer;
      this.environment = environment;
      this.scriptId = scriptId;
      this.dataframes = dataframes;
      this.assemblies = assemblies;
      this.dataframeType = dataframeType;
      this.parameters = parameters;
      this.equipId = equipId;
      this.subType = subType;
   }

   // http://equip-services-dev.pfizer.com:8080/EQUIPComputeService/ComputeSwagger/index.html#!/14632Compute/computePost
   // {
   //    "user": "heinemanwp",
   //    "computeContainer": "R",
   //    "environment": "Server",
   //    "scriptId": "1",
   //    "parameters": [{
   //       "key": "Study ID",
   //       "type": "string",
   //       "value": "12345"
   //    }],
   //    "dataframes": [
   //       "7b4cd47f-f20f-4684-8b93-3c7d3eda2f24"
   //    ],
   //    "assemblies": [
   //       "GUID HERE"
   //    ]
   // }

   public String getUser() {
      return user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public String getComputeContainer() {
      return computeContainer;
   }

   public void setComputeContainer(String computeContainer) {
      this.computeContainer = computeContainer;
   }

   public String getEnvironment() {
      return environment;
   }

   public void setEnvironment(String environment) {
      this.environment = environment;
   }

   public String getScriptId() {
      return scriptId;
   }

   public void setScriptId(String scriptId) {
      this.scriptId = scriptId;
   }

   public Set<? extends GenericInput> getParameters() {
      return parameters;
   }

   public void setParameters(Set<? extends GenericInput> parameters) {
      this.parameters = parameters;
   }

   public List<String> getDataframes() {
      return dataframes;
   }

   public void setDataframes(List<String> dataframes) {
      this.dataframes = dataframes;
   }

   public List<String> getAssemblies() {
      return assemblies;
   }

   public void setAssemblies(List<String> assemblies) {
      this.assemblies = assemblies;
   }

   public String getDataframeType() {
      return dataframeType;
   }

   public void setDataframeType(String dataframeType) {
      this.dataframeType = dataframeType;
   }

   public String getEquipId() {
      return equipId;
   }

   public void setEquipId(String equipId) {
      this.equipId = equipId;
   }

   public String getSubType() {
      return subType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }
}
