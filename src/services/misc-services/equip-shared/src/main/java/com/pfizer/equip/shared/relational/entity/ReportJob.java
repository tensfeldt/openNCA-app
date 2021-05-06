package com.pfizer.equip.shared.relational.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "report_job", schema = "equip_owner")
public class ReportJob {
   

   public ReportJob() {
      this.startDate = new Date();
      this.status = "RUNNING";
   }
   
   @OneToMany(mappedBy = "reportJob", cascade = CascadeType.ALL) 
   private Set<ReportJobOutput> reportJobOutputs = new HashSet<ReportJobOutput>();

   @Id
   @Column(name = "report_job_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long reportJobId;

   @Column(name = "start_date")
   private Date startDate;

   @Column(name = "end_date")
   private Date endDate;
   
   @Column(name = "status")
   private String status;
   
   @Column(name = "error")
   private String error;

   @Column(name = "compute_stdin")
   private String computeStdin;

   @Column(name = "compute_stdout")
   private String computeStdout;

   @Column(name = "compute_stderr")
   private String computeStderr;

   public Long getReportJobId() {
      return reportJobId;
   }

   public Date getStartDate() {
      return startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public Date getEndDate() {
      return endDate;
   }

   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

   public String getComputeStdin() {
      return computeStdin;
   }

   public void setComputeStdin(String computeStdin) {
      this.computeStdin = computeStdin;
   }

   public String getComputeStdout() {
      return computeStdout;
   }

   public void setComputeStdout(String computeStdout) {
      this.computeStdout = computeStdout;
   }

   public String getComputeStderr() {
      return computeStderr;
   }

   public void setComputeStderr(String computeStderr) {
      this.computeStderr = computeStderr;
   }

   public Set<ReportJobOutput> getReportJobOutputs() {
      return reportJobOutputs;
   }

   public void setReportJobOutputs(Set<ReportJobOutput> reportJobOutputs) {
      this.reportJobOutputs = reportJobOutputs;
   }

   public void addReportJobOutput(ReportJobOutput reportJobOutput) {
      reportJobOutput.setReportJob(this);
      reportJobOutput.setReportJobId(this.reportJobId);
      this.reportJobOutputs.add(reportJobOutput);
   }
}