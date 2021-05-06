package com.pfizer.equip.shared.relational.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "operational_metadata_job", schema = "equip_owner")
public class OperationalMetadataJob {
   

   public OperationalMetadataJob() {
      this.startDate = new Date();
      this.status = "RUNNING";
   }

   @Id
   @Column(name = "operational_metadata_job_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long operationalMetadataJobId;

   @Column(name = "start_date")
   private Date startDate;

   @Column(name = "end_date")
   private Date endDate;
   
   @Column(name = "status")
   private String status;
   
   @Column(name = "error")
   private String error;

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

   public Long getOperationalMetadataJobId() {
      return operationalMetadataJobId;
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
}