package com.pfizer.equip.services.business.api.dataframe;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.services.business.api.input.CommentInput;
import com.pfizer.equip.shared.service.business.api.Metadatum;

// {
//   "type": "assembly",
//   "assemblyType": "Data Load",
//   "dataframeIds": [
//     "df1d2105-5020-42d6-8b56-41f1b17ad333"
//   ],
//   "studyIds": [
//     "B152:B1521057"
//   ]
// }
// Class for storing assemblies returned or passed from Dataframe Service
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assembly {
   private String type = "assembly";
   private String assemblyType;
   private Set<String> dataframeIds;
   private Set<String> studyIds;
   private List<Metadatum> metadata;
   private List<CommentInput> comments;
   
   // Added for analysis
   private String equipId;
   private String kelFlagsDataframeId;
   private String modelConfigurationDataframeId;
   private String parametersDataframeId;
   private String profileSettingsDataframeId;
   private String estimatedConcDataframeId;

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getAssemblyType() {
      return assemblyType;
   }

   public void setAssemblyType(String assemblyType) {
      this.assemblyType = assemblyType;
   }

   public Set<String> getDataframeIds() {
      return dataframeIds;
   }

   public void setDataframeIds(Set<String> dataframeIds) {
      this.dataframeIds = dataframeIds;
   }

   public Set<String> getStudyIds() {
      return studyIds;
   }

   public void setStudyIds(Set<String> studyIds) {
      this.studyIds = studyIds;
   }

   public List<Metadatum> getMetadata() {
      return metadata;
   }

   public void setMetadata(List<Metadatum> metadata) {
      this.metadata = metadata;
   }

   public List<CommentInput> getComments() {
      return comments;
   }

   public void setComments(List<CommentInput> comments) {
      this.comments = comments;
   }

   public String getKelFlagsDataframeId() {
      return kelFlagsDataframeId;
   }

   public void setKelFlagsDataframeId(String kelFlagsDataframeId) {
      this.kelFlagsDataframeId = kelFlagsDataframeId;
   }

   public String getModelConfigurationDataframeId() {
      return modelConfigurationDataframeId;
   }

   public void setModelConfigurationDataframeId(String modelConfigurationDataframeId) {
      this.modelConfigurationDataframeId = modelConfigurationDataframeId;
   }

   public String getParametersDataframeId() {
      return parametersDataframeId;
   }

   public void setParametersDataframeId(String parametersDataframeId) {
      this.parametersDataframeId = parametersDataframeId;
   }

   public String getEquipId() {
      return equipId;
   }

   public void setEquipId(String equipId) {
      this.equipId = equipId;
   }

   public String getProfileSettingsDataframeId() {
      return profileSettingsDataframeId;
   }

   public void setProfileSettingsDataframeId(String profileSettingsDataframeId) {
      this.profileSettingsDataframeId = profileSettingsDataframeId;
   }

	public String getEstimatedConcDataframeId() {
		return estimatedConcDataframeId;
	}
	
	public void setEstimatedConcDataframeId(String estimatedConcDataframeId) {
		this.estimatedConcDataframeId = estimatedConcDataframeId;
	}
}