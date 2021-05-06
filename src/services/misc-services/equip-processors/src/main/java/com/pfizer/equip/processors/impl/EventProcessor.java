package com.pfizer.equip.processors.impl;

import java.util.ArrayList;
import java.util.List;
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
import com.pfizer.equip.processors.service.EventProcessorService;
import com.pfizer.equip.shared.relational.entity.Event;
import com.pfizer.equip.shared.relational.entity.Notification;
import com.pfizer.equip.shared.relational.entity.Subscription;

@DisallowConcurrentExecution
public class EventProcessor extends Processor {

   private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);
   private volatile GetMessagesResultAsync async;
   private volatile boolean stop = false;
   private volatile boolean errorStop = false;
   private static final int MAX_ERRORS = 10; // consecutive errors

   @Autowired
   private MessagingConnector connector;

   @Autowired
   private ProcessorsProperties processorProperties;

   @Autowired
   private EventProcessorService eventProcessorService;

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
      async = connector.getMesages(processorProperties.getEventQueue(), new AsyncMessageConsumer<Event>() {
         private int consecutiveErrorCount = 0;

         @Override
         public void consume(AsyncMessage<Event> message) {
            try {
               Event event = message.getContent(Event.class);
               logger.info(
                     "Event received with Component Name '{}', Event Type Id '{}', 'Entity Id '{}', Entity Type '{}', Study Id '{}', Program Number '{}', Description '{}', Created On '{}'",
                     event.getComponentName(), event.getEventType(), event.getEntityId(), event.getEntityType(), event.getStudyId(), event.getProgramNumber(),
                     event.getDescription(), event.getCreatedOn());
               List<Subscription> subscriptions = new ArrayList<>();
               if (event.getEventType().getGlobalFlag()) {
                  List<Subscription> s = eventProcessorService.getGlobalSubscriptions(event.getEventType().getEventTypeId());
                  if (s != null) {
                     subscriptions.addAll(s);
                  }
               } else {
                  // Check global library artifacts
                  if (StringUtils.equalsIgnoreCase(event.getEntityType(), "artifact") && StringUtils.isNotBlank(event.getEntityId())) {
                     List<Subscription> s = eventProcessorService.getSubscriptionsByEventTypeIdAndArtifactId(event.getEventType().getEventTypeId(), event.getEntityId());
                     if (s != null) {
                        subscriptions.addAll(s);
                     }
                  } else {
                     if ((StringUtils.isNotBlank(event.getProgramNumber()) && StringUtils.isNotBlank(event.getStudyId())) || StringUtils.isNotBlank(event.getStudyId())) {
                        List<Subscription> s = eventProcessorService.getSubscriptionsByEventTypeIdAndStudyId(event.getEventType().getEventTypeId(), event.getStudyId());
                        if (s != null) {
                           subscriptions.addAll(s);
                        }
                     } else {
                        List<Subscription> s = eventProcessorService.getSubscriptionsByEventTypeIdAndProgramNumber(event.getEventType().getEventTypeId(), event.getProgramNumber());
                        if (s != null) {
                           subscriptions.addAll(s);
                        }   
                     }
                  }

               }
               if (subscriptions.size() > 0) {
                  List<Notification> notifications = new ArrayList<>();
                  logger.info("Found {} subscriptions", subscriptions.size());
                  for (Subscription subscription : subscriptions) {
                     Notification notification = new Notification();
                     notification.setNotificationType(subscription.getNotificationType());
                     notification.setProcessed(Boolean.FALSE);
                     notification.setSubscription(subscription);
                     notifications.add(notification);
                     logger.info("Notification created for Subscription Id {}", subscription.getSubscriptionId());
                  }
                  eventProcessorService.processEventAndNotifications(event, notifications);
               } else {
                  eventProcessorService.saveEvent(event);
                  logger.info("No subscriptions found for this event");
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
            logger.info("Canceling the Event Processor");

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
