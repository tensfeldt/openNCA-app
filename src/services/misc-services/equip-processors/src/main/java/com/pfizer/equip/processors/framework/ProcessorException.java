package com.pfizer.equip.processors.framework;

import org.quartz.JobExecutionException;

@SuppressWarnings("serial")
public class ProcessorException extends JobExecutionException {
   private String jobName;
   
   public ProcessorException(String jobName) {
      super();
      this.jobName = jobName;
   }
   
   public ProcessorException(Throwable t, String jobName) {
      super(t);
      this.jobName = jobName;
   }
   
   public String getJobName() {
      return jobName;
   }

   public void setJobName(String jobName) {
      this.jobName = jobName;
   }
   
}
