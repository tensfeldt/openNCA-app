package com.pfizer.equip.processors.impl;

import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.pfizer.equip.messages.AsyncMessage;
import com.pfizer.equip.messages.AsyncMessageConsumer;
import com.pfizer.equip.messages.GetMessagesResultAsync;
import com.pfizer.equip.messages.MessagingConnector;
import com.pfizer.equip.processors.framework.Processor;
import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.processors.service.NotificationProcessorService;
import com.pfizer.equip.shared.relational.entity.Notification;

@DisallowConcurrentExecution
public class RealtimeEmailProcessor extends Processor {

   private static final Logger logger = LoggerFactory.getLogger(RealtimeEmailProcessor.class);
   private volatile GetMessagesResultAsync async;
   private volatile boolean stop = false;
   private volatile boolean errorStop = false;
   private static final int MAX_ERRORS = 10; // consecutive errors

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
      async = connector.getMesages(processorProperties.getNotificationEmailRealtimeQueue(), new AsyncMessageConsumer<Notification>() {
         private int consecutiveErrorCount = 0;

         @Override
         public void consume(AsyncMessage<Notification> message) {
            try {
               Notification notification = message.getContent(Notification.class);
               logger.info("Realtime-email notification received");
               if (notificationService.subscriptionExists(notification.getSubscription())) {
                  notificationService.sendRealtimeEmailAndFlagNotification(notification);
               } else {
                  logger.info("Subscription not found. Skipping.");
               }
               consecutiveErrorCount = 0; // reset error count
               message.acknowledge();
            } catch (MailException e) {
               logger.error(e.getMessage(), e);
               message.reject(false);
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
            logger.info("Canceling the Realtime Email Processor");

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
