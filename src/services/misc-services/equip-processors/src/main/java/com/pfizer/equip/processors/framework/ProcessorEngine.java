package com.pfizer.equip.processors.framework;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.calendar.AnnualCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.processors.framework.ProcessorDetail.Interval;
import com.pfizer.equip.processors.framework.ProcessorSettings.Holiday;

/**
 * 
 * @author plee
 * 
 */
public class ProcessorEngine {

   private static final ProcessorEngine instance = new ProcessorEngine();
   private static final Logger logger = LoggerFactory.getLogger(ProcessorEngine.class);
   private Map<String, ProcessorContext> processorContextMap;
   private List<ProcessorContext> processorContextList;

   private Scheduler scheduler;

   private ProcessorEngine() {
      processorContextMap = new ConcurrentHashMap<>();
      processorContextList = new ArrayList<>();
   }

   /**
    * Handle by the framework initializer class.
    * 
    * @param scheduler
    * @param settings
    */
   protected void initialize(Scheduler scheduler, ProcessorSettings settings) {
      this.scheduler = scheduler;
      addHolidays(settings.getHolidays());
      addProcessorContext(settings.getProcessorDetails());
   }

   public static ProcessorEngine getInstance() {
      return instance;
   }

   public void setStatus(String name, ProcessorStatus status) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      logger.info("Processor {} status is now {}", name, status);
      context.setStatus(status);
   }

   public ProcessorStatus getStatus(String name) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      return context.getStatus();
   }

   public void scheduleProcessor(String name) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      synchronized (context) {
         if (context.getStatus() == ProcessorStatus.OFF) {
            try {
               logger.info("Scheduling Processor {}", name);
               scheduler.addJob(context.getJobDetail(), true);
            } catch (SchedulerException e) {
               throw new RuntimeException(e.getMessage(), e);
            }
            for (Trigger trigger : context.getTriggers()) {
               try {
                  scheduler.scheduleJob(trigger);
               } catch (SchedulerException e) {
                  throw new RuntimeException(e.getMessage(), e);
               }
            }
            context.setStatus(ProcessorStatus.ON);
            logger.info("Processor {} status is now {}", name, ProcessorStatus.ON);
         } else {
            logger.info("Processor {} could not be scheduled. Current status is {}", name, context.getStatus());
         }
      }
   }

   public void runProcessorNow(String name) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      synchronized (context) {
         if (context.getStatus() == ProcessorStatus.OFF) {
            try {
               logger.info("Run Now Processor {}", name);
               scheduler.addJob(context.getJobDetail(), true);
            } catch (SchedulerException e) {
               throw new RuntimeException(e.getMessage(), e);
            }

            try {
               scheduler.triggerJob(JobKey.jobKey(name));
            } catch (SchedulerException e) {
               throw new RuntimeException(e.getMessage(), e);
            }
            context.setStatus(ProcessorStatus.RUNNING_ADHOC);
            logger.info("Processor {} status is now {}", name, ProcessorStatus.RUNNING_ADHOC);
         } else {
            logger.info("Processor {} could not be started. Current status is {}", name, context.getStatus());
         }
      }
   }

   public void unschedule(String name) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      try {
         JobKey key = JobKey.jobKey(name);
         logger.info("Unscheduling Prcoessor {}", name);
         if (scheduler.checkExists(key)) {
            context.setStatus(ProcessorStatus.TRANSITION_OFF);
            try {
               scheduler.deleteJob(key);
            } catch (Exception e) {
               logger.error("The state of the scheduler is unknown. A restart is recommended", e);
            }
            if (isRunning(name)) {
               scheduler.interrupt(key);
            }
         }
      } catch (SchedulerException e) {
         throw new RuntimeException(e.getMessage(), e);
      } finally {
         context.setStatus(ProcessorStatus.OFF);
         logger.info("Processor {} status is now {}", name, ProcessorStatus.OFF);
      }

   }

   public boolean isRunning(String name) {
      ProcessorContext context = processorContextMap.get(name);
      if (context == null) {
         throw new NullPointerException("Processor " + name + " is not defined");
      }
      try {
         for (JobExecutionContext jobContext : scheduler.getCurrentlyExecutingJobs()) {
            if (jobContext.getJobDetail().getKey().getName().equals(name)) {
               return true;
            }
         }
         return false;
      } catch (SchedulerException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   public List<ProcessorContext> getProcessorContextList() {
      return Collections.unmodifiableList(processorContextList);
   }

   private void addProcessorContext(List<ProcessorDetail> processorDetails) {
      logger.info("Loading Processor JobDetail and Trigger to internal map");
      for (ProcessorDetail detail : processorDetails) {
         Class<? extends Processor> processorClass;
         try {
            processorClass = Class.forName(detail.getClassName()).asSubclass(Processor.class);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
         }
         JobBuilder jobBuilder = JobBuilder.newJob(processorClass).withIdentity(detail.getName()).withDescription(detail.getDescription())
               .storeDurably();
         for (String key : detail.getProperties().keySet()) {
            jobBuilder.usingJobData(key, detail.getProperties().get(key));
         }
         JobDetail jobDetail = jobBuilder.build();
         logger.info("JobDetail created for Processor {}", detail.getName());
         List<Trigger> triggers = new ArrayList<>();
         int counter = 0;
         boolean hasCronExpression = false;
         for (String cronExpression : detail.getCronExpressions()) {
            if (StringUtils.isEmpty(cronExpression)) {
               continue;
            }
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(detail.getName() + "_trigger_" + counter)
                  .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
                  .modifiedByCalendar("holidays").forJob(jobDetail).build();
            triggers.add(trigger);
            counter++;
            hasCronExpression = true;
            logger.info("Cron Trigger created for Processor {}", detail.getName());
         }
         for (Interval interval : detail.getIntervals()) {
            TimeOfDay startTime = TimeOfDay.hourAndMinuteFromDate(interval.getStartTime().getTime());
            TimeOfDay endTime = TimeOfDay.hourAndMinuteFromDate(interval.getEndTime().getTime());
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(detail.getName() + "_trigger_" + counter)
                  .withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInSeconds(interval.getSeconds())
                        .onDaysOfTheWeek(interval.getDays()).startingDailyAt(startTime).endingDailyAt(endTime)
                        .withMisfireHandlingInstructionDoNothing())
                  .modifiedByCalendar("holidays").forJob(jobDetail).build();
            triggers.add(trigger);
            counter++;
            logger.info("DailyTimeIntervalTrigger created for Processor {}", detail.getName());
         }
         if (triggers.size() >= 0 && !hasCronExpression) {
            // This means we have zero or more intervals.
            // We build a one time run trigger so the processor starts to
            // run once after the Turn On button is pressed.
            // This also handles Processors that do no have any triggers. In that case
            // the Turn On and Run Now button execute a run once trigger every time.
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(detail.getName() + "_trigger_" + counter).forJob(jobDetail).build();
            triggers.add(trigger);
            counter++;
         }
         ProcessorContext context = new ProcessorContext(detail, jobDetail, triggers);
         processorContextMap.put(detail.getName(), context);
         processorContextList.add(context);
      }
   }

   private void addHolidays(List<Holiday> holidays) {
      logger.info("Loading holidays to internal scheduler");
      ArrayList<Calendar> excludedDays = new ArrayList<>();
      for (Holiday holiday : holidays) {
         int month = holiday.getMonth();
         int day = holiday.getDay();
         logger.info("Loading month = {}, day = {} as holiday", month, day);
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.MONTH, month - 1); // calendar month starts at 0
                                             // lol
         cal.set(Calendar.DAY_OF_MONTH, day);
         excludedDays.add(cal);
      }
      AnnualCalendar annualCalendar = new AnnualCalendar();
      annualCalendar.setDaysExcluded(excludedDays);
      try {
         scheduler.addCalendar("holidays", annualCalendar, true, true);
      } catch (SchedulerException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

}
