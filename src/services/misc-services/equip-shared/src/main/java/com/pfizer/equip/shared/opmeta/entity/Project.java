package com.pfizer.equip.shared.opmeta.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProjectFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_candidate_project_v")
public class Project extends BaseNode {

   public Project() {
      primaryType = "opmeta:project";
      nodeFolder = new ProjectFolder();
   }

   public Project(String programCode, String projectCode) {
      primaryType = "opmeta:project";
      nodeFolder = new ProjectFolder();
      this.programCode = programCode;
      this.projectCode = projectCode;
   }

   @Transient
   @JsonProperty("opmeta:currentSnapshot")
   String currentSnapshot;

   @JsonProperty("id")
   @Transient
   String uuid;
   
   @Id 
   @Column(name = "cand_code")
   @JsonProperty("opmeta:candidateCode")
   @JsonInclude(Include.ALWAYS)
   String projectCode;

   @Column(name = "drug_program_cd")
   @JsonProperty("opmeta:programCode")
   String programCode;
   
   @Column(name = "cand_disease_area")
   @JsonProperty("opmeta:candidateDiseaseArea")
   @JsonInclude(Include.ALWAYS)
   String candidateDiseaseArea;
   
   @Column(name = "cand_finance_code")
   @JsonProperty("opmeta:compoundSource")
   @JsonInclude(Include.ALWAYS)
   String compoundSource;
   
   @Column(name = "cand_medium_desc")
   @JsonProperty("opmeta:candidateMediumDescription")
   @JsonInclude(Include.ALWAYS)
   String candidateMediumDescription;
   
   @Column(name = "cand_name")
   @JsonProperty("opmeta:candidateName")
   @JsonInclude(Include.ALWAYS)
   String candidateName;
   
   @Column(name = "cand_phase")
   @JsonProperty("opmeta:candidatePhase")
   @JsonInclude(Include.ALWAYS)
   String candidatePhase;
   
   @Column(name = "cand_prmry_formulation")
   @JsonProperty("opmeta:candidatePrimaryFormulation")
   @JsonInclude(Include.ALWAYS)
   String candidatePrimaryFormulation;
   
   @Column(name = "cand_prmry_ind")
   @JsonProperty("opmeta:candidatePrimaryIndication")
   @JsonInclude(Include.ALWAYS)
   String candidatePrimaryIndication;
   
   @Column(name = "cand_short_desc")
   @JsonProperty("opmeta:candidateShortDescription")
   @JsonInclude(Include.ALWAYS)
   String candidateShortDescription;
   
   @Column(name = "cand_status")
   @JsonProperty("opmeta:candidateStatus")
   @JsonInclude(Include.ALWAYS)
   String candidateStatus;
   
   @Column(name = "cand_sub_type")
   @JsonProperty("opmeta:candidateSubType")
   @JsonInclude(Include.ALWAYS)
   String candidateSubType;
   
   @Column(name = "cand_therapeutic_area")
   @JsonProperty("opmeta:candidateTherapeuticArea")
   @JsonInclude(Include.ALWAYS)
   String candidateTherapeuticArea;
   
   @Column(name = "cand_type")
   @JsonProperty("opmeta:candidateType")
   @JsonInclude(Include.ALWAYS)
   String candidateType;
   
   @Column(name = "cand_unit")
   @JsonProperty("opmeta:candidateUnit")
   @JsonInclude(Include.ALWAYS)
   String candidateUnit;
   
   @Column(name = "cand_zone")
   @JsonProperty("opmeta:candidateZone")
   @JsonInclude(Include.ALWAYS)
   String candidateZone;

   @JsonProperty("opmeta:source")
   @Transient
   protected SourceType source;

   @JsonProperty("opmeta:comments")
   @Transient
   String comments;

   public String getCurrentSnapshot() {
      return currentSnapshot;
   }

   public void setCurrentSnapshot(String currentSnapshot) {
      this.currentSnapshot = currentSnapshot;
   }

   public String getUuid() {
      return uuid;
   }

   @Override
   public String getPath() {
      return String.format("/%s/%s/%s/%s", ProgramFolder.NAME, programCode, ProjectFolder.NAME, projectCode);
   }

   public String getProjectCode() {
      return projectCode;
   }

   public String getProgramCode() {
      return programCode;
   }

   public String getComments() {
      return comments;
   }

   public void setComments(String comments) {
      this.comments = comments;
   }

   public SourceType getSource() {
      return source;
   }

   public void setSource(SourceType source) {
      this.source = source;
   }
}
