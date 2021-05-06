package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.folder.MilestoneFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_study_milestone_v")
@JsonInclude(Include.ALWAYS)
public class Milestone extends BaseProtocolChildNode {

   public Milestone() {
      primaryType = "opmeta:milestone";
      nodeFolder = new MilestoneFolder();
   }

   @ManyToOne
   @JoinColumn(name="study_id", insertable=false, updatable=false)
   @JsonIgnore
   Protocol protocol;

   @Id 
   @Column(name = "study_id")
   @JsonIgnore
   String studyId;
   
   @Id 
   @Column(name = "study_milestone_id")
   @JsonProperty("opmeta:milestoneId")
   Long milestoneId;
   
   @Id 
   @Column(name = "study_milestone_seq_num")
   @JsonProperty("opmeta:milestoneSequenceNumber")
   Long milestoneSequenceNumber;
   
   @Column(name = "study_milestone_actual_dt")
   @JsonProperty("opmeta:actualDate")
   Date actualDate;
   
   @Column(name = "study_milestone_base_dt")
   @JsonProperty("opmeta:baselineDate")
   Date baseDate;
   
   @Column(name = "study_milestone_planned_dt")
   @JsonProperty("opmeta:plannedDate")
   Date plannedDate;

   @Column(name = "code")
   @JsonProperty("opmeta:studyTaskActivityCode")
   String studyTaskActivityCode;

   @Column(name = "value")
   @JsonProperty("opmeta:studyTaskActivityValue")
   String studyTaskActivityValue;

   @Column(name = "lov_desc")
   @JsonProperty("opmeta:lovDescription")
   String listOfValuesDescription;

   @Column(name = "epm_flag")
   @JsonProperty("opmeta:epmFlag")
   Boolean epmFlag;

   public Protocol getProtocol() {
      return protocol;
   }


   public String getStudyId() {
      return studyId;
   }


   public Long getMilestoneId() {
      return milestoneId;
   }


   public Long getMilestoneSequenceNumber() {
      return milestoneSequenceNumber;
   }


   public Date getActualDate() {
      return actualDate;
   }


   public Date getBaseDate() {
      return baseDate;
   }


   public Date getPlannedDate() {
      return plannedDate;
   }


   public String getStudyTaskActivityCode() {
      return studyTaskActivityCode;
   }


   public String getStudyTaskActivityValue() {
      return studyTaskActivityValue;
   }


   public String getListOfValuesDescription() {
      return listOfValuesDescription;
   }


   public Boolean getEpmFlag() {
      return epmFlag;
   }

   @Override
   public String getPath() {
      return String.format("%s/%s", MilestoneFolder.NAME, MilestoneFolder.CHILD_NODE_NAME);
   }

}
