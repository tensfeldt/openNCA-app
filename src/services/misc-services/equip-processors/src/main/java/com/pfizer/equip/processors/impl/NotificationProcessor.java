package com.pfizer.equip.processors.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pfizer.equip.messages.AsyncMessage;
import com.pfizer.equip.messages.AsyncMessageConsumer;
import com.pfizer.equip.messages.GetMessagesResultAsync;
import com.pfizer.equip.messages.MessagingConnector;
import com.pfizer.equip.processors.framework.Processor;
import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.processors.service.NotificationProcessorService;
import com.pfizer.equip.shared.relational.entity.Notification;

@DisallowConcurrentExecution
public class NotificationProcessor extends Processor {

   private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);
   private volatile GetMessagesResultAsync async;
   private volatile boolean stop = false;
   private volatile boolean errorStop = false;
   private static final int MAX_ERRORS = 10; // consecutive errors
   private static final String REAL_TIME_EMAIL = "realtime_email";

   @Autowired
   private MessagingConnector connector;

   @Autowired
   private ProcessorsProperties processorProperties;

   @Autowired
   private NotificationProcessorService notificationService;

   @Override
   public void stop() {
      if (async != null) {
         async.cancel();
         async = null;
      }
      stop = true;
   }

   @Override
   public void run(Map<String, Object> properties) {
      stop = false;
      errorStop = false;
      async = connector.getMesages(processorProperties.getNotificationQueue(), new AsyncMessageConsumer<Notification>() {
         private int consecutiveErrorCount = 0;

         @Override
         public void consume(AsyncMessage<Notification> message) {
            try {
               Notification notification = message.getContent(Notification.class);
               logger.info("Notification received with Notification Type '{}'", notification.getNotificationType().getNotificationTypeName());
               if (!StringUtils.equalsIgnoreCase(REAL_TIME_EMAIL, notification.getNotificationType().getNotificationTypeName())) {
                  if (notificationService.subscriptionExists(notification.getSubscription())) {
                     logger.info("Saving non-realtime email notification to database");
                     notificationService.saveNotification(notification);
                  } else {
                     logger.info("Subscription not found. Skipping");
                  }
               } else {
                  logger.info("Sending notification to the Realtime Email Notification Queue");
                  connector.sendMessage(notification, processorProperties.getNotificationEmailRealtimeQueue());
               }
               consecutiveErrorCount = 0; // reset error count
               message.acknowledge();
            } catch (Exception e) {
               logger.error(e.getMessage(), e);
               consecutiveErrorCount++; // increase consecutive error count
               message.reject(true);
               if (MAX_ERRORS == consecutiveErrorCount) {
                  errorStop = true;
                  stop();
               }
            }
         }

         @Override
         public void handleCancel() {
            logger.info("Canceling the Notification Processor");

         }

         @Override
         public void handleShutdown(boolean isError) {
            logger.info("The channel or connection has received a shutdown notification");

         }

      });
      // keep alive.
      while (stop == false) {
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            break;
         }
      }
      if (errorStop) {
         throw new RuntimeException("Max error count has been reached. Stopping the consumer.");
      }

   }

}
