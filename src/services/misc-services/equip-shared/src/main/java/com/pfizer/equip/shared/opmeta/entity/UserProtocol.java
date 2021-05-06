package com.pfizer.equip.shared.opmeta.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatusSource;
import com.pfizer.equip.shared.opmeta.StudyRestrictionStatus;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;

@SuppressWarnings("serial")
@MappedSuperclass
public class UserProtocol extends BaseNode {

   @Transient
   @JsonIgnore
   public final String PRIMARY_TYPE = "opmeta:protocol";

   public UserProtocol() {
      this.primaryType = "opmeta:protocol";
   }

   public UserProtocol(String programCode, String studyId) {
      primaryType = PRIMARY_TYPE;
      nodeFolder = new ProtocolFolder();
      this.programCode = programCode;
      this.studyId = studyId;
   }

   @Id
   @Column(name = "study_id")
   @JsonProperty("opmeta:studyId")
   @JsonInclude(Include.ALWAYS)
   String studyId;

   @Column(name = "drug_program_cd")
   @JsonProperty("opmeta:programCode")
   @JsonInclude(Include.ALWAYS)
   String programCode;

   @JsonProperty("opmeta:studyBlindingStatus")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String studyBlindingStatus;

   @JsonProperty("opmeta:studyBlindingStatusSource")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String studyBlindingStatusSource;

   @JsonProperty("opmeta:studyRestrictionStatus")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String studyRestrictionStatus;

   @JsonProperty("opmeta:studyBlindingDescription")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String studyBlindingDescription;

   @JsonProperty("opmeta:clinicalDataBlindingRequired")
   @Transient
   @JsonInclude(Include.ALWAYS)
   Boolean clinicalDataBlindingRequired;

   @JsonProperty("opmeta:treatmentDataBlindingRequired")
   @Transient
   @JsonInclude(Include.ALWAYS)
   Boolean treatmentDataBlindingRequired;

   @JsonProperty("opmeta:studyBlindingStatusDate")
   @Transient
   @JsonInclude(Include.ALWAYS)
   Date studyBlindingStatusDate;

   @JsonProperty("opmeta:compoundOrSaltForm")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String compoundOrSaltForm;

   @JsonProperty("opmeta:analysisNotes")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String analysisNotes;

   @JsonProperty("opmeta:food")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String food;

   @JsonProperty("opmeta:foodComments")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String foodComments;

   @JsonProperty("opmeta:sdComments")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String sdComments;

   @JsonProperty("opmeta:crfDataStatus")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String crfDataStatus;

   @JsonProperty("opmeta:assignedCagUsers")
   @Transient
   @JsonInclude(Include.ALWAYS) // TODO: conditional include
   Set<String> assignedCagUsers;

   @JsonProperty("opmeta:assignedPkaUsers")
   @Transient
   @JsonInclude(Include.ALWAYS) // TODO: conditional include
   Set<String> assignedPkaUsers;

   @JsonProperty("opmeta:setupDate")
   @Transient
   @JsonInclude(Include.ALWAYS)
   Date setupDate;

   @JsonProperty("opmeta:setupBy")
   @Transient
   @JsonInclude(Include.ALWAYS)
   String setupBy;

   @Transient
   @JsonProperty("customAttributes")
   Set<KeyValuePair> customAttributes;

   public void setStudyId(String studyId) {
      this.studyId = studyId;
   }

   public String getStudyId() {
      return studyId;
   }

   public void setProgramCode(String programCode) {
      this.programCode = programCode;
   }

   public String getProgramCode() {
      return programCode;
   }

   public String getStudyBlindingStatus() {
      return (studyBlindingStatus == null) ? StudyBlindingStatus.BLINDED.getValue() : studyBlindingStatus;
   }

   void setStudyBlindingStatus(String studyBlindingStatus) {
      this.studyBlindingStatus = studyBlindingStatus;
   }

   public String getStudyBlindingStatusSource() {
      return (studyBlindingStatusSource == null) ? StudyBlindingStatusSource.MANUAL.getValue() : studyBlindingStatusSource;
   }

   public void setStudyBlindingStatusSource(String studyBlindingStatusSource) {
      this.studyBlindingStatusSource = studyBlindingStatusSource;
   }
   
   public String getStudyRestrictionStatus() {
      return (studyRestrictionStatus == null) ? StudyRestrictionStatus.NOT_RESTRICTED.getValue() : studyRestrictionStatus;
   }
   
   void setStudyRestrictionStatus(String studyRestrictionStatus) {
      this.studyRestrictionStatus = studyRestrictionStatus;
   }

   @JsonIgnore
   public Boolean getIsStudyBlinded() {
      if (studyBlindingStatus != null) {
         if (studyBlindingStatus.equalsIgnoreCase(StudyBlindingStatus.BLINDED.getValue())) {
            return true;
         } else {
            return false;
         }
      }
      return true; // Default to blinded if null
   }

   public void setIsStudyBlinded(boolean isStudyBlinded) {
      if (isStudyBlinded) {
         studyBlindingStatus = StudyBlindingStatus.BLINDED.getValue();
      } else {
         studyBlindingStatus = StudyBlindingStatus.UNBLINDED.getValue();
      }
   }

   @JsonIgnore
   public Boolean getIsStudyRestricted() {
      if (studyRestrictionStatus != null) {
         if (studyRestrictionStatus.equalsIgnoreCase(StudyRestrictionStatus.RESTRICTED.getValue())) {
            return true;
         } else {
            return false;
         }
      }
      return false; // Default to not restricted if null
   }

   public void setIsStudyRestricted(boolean isStudyRestricted) {
      if (isStudyRestricted) {
         studyRestrictionStatus = StudyRestrictionStatus.RESTRICTED.getValue();
      } else {
         studyRestrictionStatus = StudyRestrictionStatus.NOT_RESTRICTED.getValue();
      }
   }

   @JsonIgnore
   public Boolean getIsStudyBlindingStatusSet() {
      return studyBlindingStatus != null;
   }

   @JsonIgnore
   public Boolean getIsStudyBlindingStatusSourceSet() {
      return studyBlindingStatusSource != null;
   }
   
   @JsonIgnore
   public Boolean getIsStudyRestrictionStatusSet() {
      return studyRestrictionStatus != null;
   }

   @Override
   public String getPath() {
      return String.format("/%s/%s/%s/%s", ProgramFolder.NAME, programCode, ProtocolFolder.NAME, studyId);
   }

   // START auto-generated getters and setters from BeanUtils property copy
   public String getCompoundOrSaltForm() {
      return compoundOrSaltForm;
   }

   public void setCompoundOrSaltForm(String compoundOrSaltForm) {
      this.compoundOrSaltForm = compoundOrSaltForm;
   }

   public String getAnalysisNotes() {
      return analysisNotes;
   }

   public void setAnalysisNotes(String analysisNotes) {
      this.analysisNotes = analysisNotes;
   }

   public String getFood() {
      return food;
   }

   public void setFood(String food) {
      this.food = food;
   }

   public String getFoodComments() {
      return foodComments;
   }

   public void setFoodComments(String foodComments) {
      this.foodComments = foodComments;
   }

   public String getSdComments() {
      return sdComments;
   }

   public void setSdComments(String sdComments) {
      this.sdComments = sdComments;
   }

   public String getCrfDataStatus() {
      return crfDataStatus;
   }

   public void setCrfDataStatus(String crfDataStatus) {
      this.crfDataStatus = crfDataStatus;
   }
   // END auto-generated getters and setters from BeanUtils property copy

   public void addAssignedCagUsers(Collection<String> assignedUsers) {
      if (assignedCagUsers == null) {
         assignedCagUsers = new HashSet<String>();
      }
      this.assignedCagUsers.addAll(assignedUsers);
   }

   public void addAssignedPkaUsers(Collection<String> assignedUsers) {
      if (assignedPkaUsers == null) {
         assignedPkaUsers = new HashSet<String>();
      }
      this.assignedPkaUsers.addAll(assignedUsers);
   }

   public Set<String> getAssignedCagUsers() {
      return assignedCagUsers;
   }

   @JsonIgnore // forces list even if null
   public Set<String> getAssignedCagUsersList() {
      if (assignedCagUsers == null) {
         assignedCagUsers = new HashSet<String>();
      }
      return assignedCagUsers;
   }

   public Set<String> getAssignedPkaUsers() {
      return assignedPkaUsers;
   }

   @JsonIgnore // forces list even if null
   public Set<String> getAssignedPkaUsersList() {
      if (assignedPkaUsers == null) {
         assignedPkaUsers = new HashSet<String>();
      }
      return assignedPkaUsers;
   }

   public void setAssignedCagUsers(Set<String> assignedCagUsers) {
      this.assignedCagUsers = assignedCagUsers;
   }

   public void setAssignedPkaUsers(Set<String> assignedPkaUsers) {
      this.assignedPkaUsers = assignedPkaUsers;
   }

   public void removeAssignedCagUsers(Collection<String> assignedUsers) {
      if (assignedCagUsers == null) {
         assignedCagUsers = new HashSet<String>();
      }
      this.assignedCagUsers.removeAll(assignedUsers);
   }

   public void removeAssignedPkaUsers(Collection<String> assignedUsers) {
      if (assignedPkaUsers == null) {
         assignedPkaUsers = new HashSet<String>();
      }
      this.assignedPkaUsers.removeAll(assignedUsers);
   }

   public String getStudyBlindingDescription() {
      return studyBlindingDescription;
   }

   public void setStudyBlindingDescription(String studyBlindingDescription) {
      this.studyBlindingDescription = studyBlindingDescription;
   }

   public Boolean getClinicalDataBlindingRequired() {
      return clinicalDataBlindingRequired;
   }

   public void setClinicalDataBlindingRequired(Boolean clinicalDataBlindingRequired) {
      this.clinicalDataBlindingRequired = clinicalDataBlindingRequired;
   }

   public Boolean getTreatmentDataBlindingRequired() {
      return treatmentDataBlindingRequired;
   }

   public void setTreatmentDataBlindingRequired(Boolean treatmentDataBlindingRequired) {
      this.treatmentDataBlindingRequired = treatmentDataBlindingRequired;
   }

   public Date getStudyBlindingStatusDate() {
      return studyBlindingStatusDate;
   }

   public void setStudyBlindingStatusDate(Date studyBlindingStatusDate) {
      this.studyBlindingStatusDate = studyBlindingStatusDate;
   }

   public Set<KeyValuePair> getCustomAttributes() {
      return customAttributes;
   }

   public void setCustomAttributes(Set<KeyValuePair> customAttributes) {
      this.customAttributes = customAttributes;
   }

   public Date getSetupDate() {
      return setupDate;
   }

   public void setSetupDate(Date setupDate) {
      this.setupDate = setupDate;
   }

   public String getSetupBy() {
      return setupBy;
   }

   public void setSetupBy(String setupBy) {
      this.setupBy = setupBy;
   }
}