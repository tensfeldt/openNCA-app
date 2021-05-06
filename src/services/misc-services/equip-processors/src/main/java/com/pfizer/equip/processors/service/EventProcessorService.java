package com.pfizer.equip.processors.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.messages.MessagingConnector;
import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.shared.relational.entity.Event;
import com.pfizer.equip.shared.relational.entity.Notification;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.relational.repository.EventRepository;
import com.pfizer.equip.shared.relational.repository.SubscriptionRepository;

@Service
public class EventProcessorService {
   
   private static final Logger logger = LoggerFactory.getLogger(EventProcessorService.class);

   @Autowired
   private SubscriptionRepository subscriptionRepository;
   
   @Autowired
   private EventRepository eventRepository;

   @Autowired
   private MessagingConnector connector;

   @Autowired
   private ProcessorsProperties processorProperties;
   
   public List<Subscription> getGlobalSubscriptions(Long eventTypeId) {
      return subscriptionRepository.findByEventType_EventTypeIdAndEventType_GlobalFlag(eventTypeId, Boolean.TRUE);
   }
   
   public List<Subscription> getSubscriptionsByEventTypeIdAndStudyId(Long eventTypeId, String studyId) {
      return subscriptionRepository.findByEventType_EventTypeIdAndStudyId(eventTypeId, studyId);
   }

   public List<Subscription> getSubscriptionsByEventTypeIdAndProgramNumber(Long eventTypeId, String programNumber) {
      return subscriptionRepository.findByEventType_EventTypeIdAndProgramNumber(eventTypeId, programNumber);
   }
   
   public List<Subscription> getSubscriptionsByEventTypeIdAndArtifactId(Long eventTypeId, String artifactId) {
      return subscriptionRepository.findByEventType_EventTypeIdAndArtifactId(eventTypeId, artifactId);
   }
   
   @Transactional
   public void processEventAndNotifications(Event event, List<Notification> notifications) {
      event = eventRepository.save(event);
      logger.info("Event saved in database with Id {}", event.getEventId());
      logger.info("Sending {} notifications to Messaging Queue {}", notifications.size(), processorProperties.getNotificationQueue());
      for (Notification notification : notifications) {
         notification.setEvent(event);
         connector.sendMessage(notification, processorProperties.getNotificationQueue());
      }
   }
   
   public void saveEvent(Event event) {
      eventRepository.save(event);
   }
}
