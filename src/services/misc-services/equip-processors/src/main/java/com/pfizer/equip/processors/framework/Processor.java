package com.pfizer.equip.processors.framework;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * 
 * @author plee
 *
 */
@DisallowConcurrentExecution
public abstract class Processor implements InterruptableJob {
   private volatile Thread runningThread;
   private static final Logger logger = LoggerFactory.getLogger(Processor.class);
   private ReentrantLock lock = new ReentrantLock();
   private Condition interruptedCondition = lock.newCondition();
   private volatile boolean interrupted = false;
   private static ServletContext context;

   public Processor() {
      SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, context);
   }

   @Override
   public void execute(JobExecutionContext ctx) throws JobExecutionException {
      JobDataMap map = ctx.getMergedJobDataMap();
      String jobName = ctx.getJobDetail().getKey().getName();
      try {
         runningThread = Thread.currentThread();
         run(map.getWrappedMap());
         logger.info("Processor {} was successful", jobName);
      } catch (Exception e) {
         logger.error(e.getMessage(), e);
         throw new ProcessorException(e, jobName);
      } finally {
         if (interrupted) {
            lock.lock();
            try {
               interruptedCondition.signal();
            } finally {
               lock.unlock();
            }
         }
         runningThread = null;
      }

   }

   /**
    * Monitor and Condition is used to have the interrupt method wait for the
    * execute method to finish. This guarantees the status displayed in the
    * Processor status is accurate.
    */
   @Override
   public void interrupt() throws UnableToInterruptJobException {
      interrupted = true;
      stop();
      if (runningThread != null) {
         runningThread.interrupt();
         lock.lock();
         try {
            while (runningThread != null) {
               if (interruptedCondition.await(2, TimeUnit.SECONDS)) {
                  break;
               }
            }
         } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
         } finally {
            lock.unlock();
         }

      }
   }

   static void setServletContext(ServletContext c) {
      context = c;
   }

   public abstract void stop();

   public abstract void run(Map<String, Object> properties);

}
