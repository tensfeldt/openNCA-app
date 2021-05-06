package com.pfizer.equip.processors.framework;

import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * 
 * @author plee
 *
 */
public class ProcessorContext {
   private ProcessorDetail detail;
   private volatile ProcessorStatus status;
   private JobDetail jobDetail;
   private List<Trigger> triggers; // support multiple triggers in case the scheduling is complex

   public ProcessorContext(ProcessorDetail detail, JobDetail jobDetail, List<Trigger> triggers) {
      this.detail = detail;
      status = ProcessorStatus.OFF;
      this.jobDetail = jobDetail;
      this.triggers = triggers;
   }

   public ProcessorDetail getDetail() {
      return detail;
   }

   public void setDetail(ProcessorDetail detail) {
      this.detail = detail;
   }

   public ProcessorStatus getStatus() {
      return status;
   }

   public void setStatus(ProcessorStatus status) {
      this.status = status;
   }

   public JobDetail getJobDetail() {
      return jobDetail;
   }

   public void setJobDetail(JobDetail jobDetail) {
      this.jobDetail = jobDetail;
   }

   public List<Trigger> getTriggers() {
      return triggers;
   }

   public void setTriggers(List<Trigger> triggers) {
      this.triggers = triggers;
   }
}
