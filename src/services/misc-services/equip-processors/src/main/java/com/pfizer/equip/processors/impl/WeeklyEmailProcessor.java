package com.pfizer.equip.processors.impl;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.pfizer.equip.processors.framework.Processor;
import com.pfizer.equip.processors.service.NotificationProcessorService;
import com.pfizer.equip.shared.relational.entity.Notification;

@DisallowConcurrentExecution
public class WeeklyEmailProcessor extends Processor {

   private static final String WEEKLY_EMAIL = "weekly_email";
   private static final Logger logger = LoggerFactory.getLogger(WeeklyEmailProcessor.class);
   private volatile boolean stop = false;
   private static final int MAX_ERRORS = 10; // consecutive errors

   @Autowired
   private NotificationProcessorService notificationService;

   @Override
   public void stop() {
      stop = true;
   }

   @Override
   public void run(Map<String, Object> properties) {
      stop = false;
      if (notificationService.lock(WEEKLY_EMAIL)) {
         try {
            List<Notification> notifications = notificationService.getNotifications(WEEKLY_EMAIL, Boolean.FALSE);
            Map<Long, List<Notification>> groupedNotifications = notifications.stream().collect(Collectors.groupingBy(n -> n.getSubscription().getSubscriptionId()));
            // lambda expression would have been hard to read. Going old school.
            groupedNotifications.forEach(new BiConsumer<Long, List<Notification>>() {
               private int consecutiveErrorCount = 0;

               @Override
               public void accept(Long t, List<Notification> u) {
                  try {
                     if (stop)
                        return;
                     notificationService.sendWeeklyEmailAndFlagNotification(u);
                     consecutiveErrorCount = 0;
                  } catch (MailException e) {
                     logger.error(e.getMessage(), e);
                  } catch (Exception e) {
                     logger.error(e.getMessage(), e);
                     consecutiveErrorCount++;
                     if (consecutiveErrorCount == MAX_ERRORS) {
                        throw new RuntimeException("Max consecutive error count reached");
                     }
                  }

               }
            });

         } finally {
            notificationService.unlock(WEEKLY_EMAIL);
         }
      } else {
         logger.info("Another Processor is working on these notifications");
      }

   }

}
