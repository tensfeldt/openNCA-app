package com.pfizer.equip.processors.framework;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author plee
 *
 */
public class ProcessorInitializer implements ServletContextListener {

   private static final Logger logger = LoggerFactory.getLogger(ProcessorInitializer.class);

   @Override
   public void contextDestroyed(ServletContextEvent ctx) {
   }

   @Override
   public void contextInitialized(ServletContextEvent ctx) {
      Processor.setServletContext(ctx.getServletContext());
      try {
         ProcessorSettingsBuilder builder = new FileProcessorSettingsBuilder();
         ProcessorSettings settings = builder.get();
         StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getServletContext().getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
         Scheduler scheduler = factory.getScheduler("ProcessorScheduler");
         scheduler.getListenerManager().addJobListener(new ProcessorListener(ctx.getServletContext()));
         ProcessorEngine engine = ProcessorEngine.getInstance();
         logger.info("Initializing processor engine");
         engine.initialize(scheduler, settings);
         logger.info("Initialization complete");
         for (ProcessorDetail processorDetail : settings.getProcessorDetails()) {
            if (processorDetail.isAutoStart()) {
               logger.info("Auto starting {}", processorDetail.getName());
               engine.scheduleProcessor(processorDetail.getName());
            }
         }
      } catch (Exception e) {
         logger.error(e.getMessage(), e);
         throw new RuntimeException(e.getMessage(), e); // blow up, bad things happened
      }
   }
}
