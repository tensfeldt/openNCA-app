package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "report_job_output", schema = "equip_owner")
public class ReportJobOutput {
   
   @ManyToOne
   @JoinColumn(name="report_job_id", insertable=false, updatable=false)
   private ReportJob reportJob;

   @Id
   @Column(name = "report_job_output_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long reportJobOutputId;

   @Column(name = "output_id")
   private String outputId;
   
   @Column(name = "output_type")
   private String outputType;

   @Column(name = "report_job_id")
   private Long reportJobId;

   public Long getReportJobOutputId() {
      return reportJobOutputId;
   }

   public void setReportJobOutputId(Long reportJobOutputId) {
      this.reportJobOutputId = reportJobOutputId;
   }

   public String getOutputId() {
      return outputId;
   }

   public void setOutputId(String outputId) {
      this.outputId = outputId;
   }

   public String getOutputType() {
      return outputType;
   }

   public void setOutputType(String outputType) {
      this.outputType = outputType;
   }

   public Long getReportJobId() {
      return reportJobId;
   }

   public void setReportJobId(Long reportJobId) {
      this.reportJobId = reportJobId;
   }

   public ReportJob getReportJob() {
      return reportJob;
   }

   public void setReportJob(ReportJob reportJob) {
      this.reportJob = reportJob;
   }
   
}