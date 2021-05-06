package com.pfizer.equip.processors.framework;

import javax.servlet.ServletContext;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.processors.service.EmailService;

public class ProcessorListener implements JobListener {

   private static final String LISTENER_NAME = "LIFE_CYCLE_LISTENER";
   private static final Logger logger = LoggerFactory.getLogger(ProcessorListener.class);

   @Autowired
   private ProcessorsProperties processorProperties;
   
   @Autowired
   private EmailService email;
   
   public ProcessorListener(ServletContext context) {
      SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, context);
   }
   
   @Override
   public String getName() {
      return LISTENER_NAME;
   }

   @Override
   public void jobToBeExecuted(JobExecutionContext context) {
      String jobName = context.getJobDetail().getKey().getName();
      ProcessorEngine engine = ProcessorEngine.getInstance();
      if (engine.getStatus(jobName) == ProcessorStatus.ON || engine.getStatus(jobName) == ProcessorStatus.DONE
            || engine.getStatus(jobName) == ProcessorStatus.ERROR) {
         engine.setStatus(jobName, ProcessorStatus.RUNNING);
      }
   }

   @Override
   public void jobExecutionVetoed(JobExecutionContext context) {
   }

   @Override
   public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
      String jobName = context.getJobDetail().getKey().getName();
      if (jobException == null) {
         if (ProcessorEngine.getInstance().getStatus(jobName) == ProcessorStatus.RUNNING_ADHOC) {
            ProcessorEngine.getInstance().unschedule(jobName);
         } else if (ProcessorEngine.getInstance().getStatus(jobName) == ProcessorStatus.RUNNING) {
            ProcessorEngine.getInstance().setStatus(jobName, ProcessorStatus.DONE);
         }
      } else {
         if (ProcessorEngine.getInstance().getStatus(jobName) == ProcessorStatus.RUNNING_ADHOC) {
            ProcessorEngine.getInstance().unschedule(jobName);
         } else {
            ProcessorEngine.getInstance().setStatus(jobName, ProcessorStatus.ERROR);
         }
         if (jobException instanceof ProcessorException) {
            try {
               email.send(processorProperties.getMailServiceAccount(), processorProperties.getMailToSupport(), 
                     processorProperties.getMailSubjectPrefix() + " Processor - " + ((ProcessorException)jobException).getJobName() + " alert", 
                     "The Processor has stopped due to errors. Please check the logs for more detail.");
            } catch (Exception e) {
               logger.error(e.getMessage(), e);
            }
           
         }
      }

   }

}
