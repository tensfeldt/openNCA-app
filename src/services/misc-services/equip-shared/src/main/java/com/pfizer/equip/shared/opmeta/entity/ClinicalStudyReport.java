package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.folder.ClinicalStudyReportFolder;

@SuppressWarnings("serial")
@Entity
@BatchSize(size=1000)
@Table(schema = "podsdal", name = "pods_ods_study_report_v")
@JsonInclude(Include.ALWAYS)
public class ClinicalStudyReport extends BaseProtocolChildNode {
   
   
   public ClinicalStudyReport() {
      nodeFolder = new ClinicalStudyReportFolder();
      primaryType = "opmeta:clinicalStudyReport";
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
   @Column(name = "csr_category")
   @JsonProperty("opmeta:csrCategory")
   String csrCategory;

   @Id 
   @Column(name = "csr_dt")
   @JsonProperty("opmeta:csrDate")
   Date csrDate;

   @Id 
   @Column(name = "csr_type")
   @JsonProperty("opmeta:csrType")
   String csrType;
   
   @Override
   public String getPath() {
      return String.format("%s/%s", ClinicalStudyReportFolder.NAME, ClinicalStudyReportFolder.CHILD_NODE_NAME);
   }
}