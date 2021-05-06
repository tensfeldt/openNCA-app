package com.pfizer.equip.shared.service.business.notifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.messages.MessagingConnector;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.relational.entity.Event;
import com.pfizer.equip.shared.relational.entity.EventType;
import com.pfizer.equip.shared.relational.repository.EventTypeRepository;
import com.pfizer.equip.shared.service.business.notifications.exceptions.InvalidEventInputException;

@Service
public class EventService {

   @Autowired
   private MessagingConnector messagingConnector;

   @Autowired
   private EventTypeRepository eventTypeRepository;
   
   @Autowired 
   private SharedApplicationProperties applicationProperties;

   private ObjectMapper mapper = new ObjectMapper();

   public void createEvent(String componentName, Date createdOn, String entityId, String entityType, String eventType, String studyId, String programNumber,
         Map<String, Object> description, String queueName) {
      createEvent(componentName, createdOn, entityId, entityType, eventType, studyId, programNumber, description, queueName, null);
   }

   public void createEvent(String componentName, Date createdOn, String entityId, String entityType, String eventType, String studyId, String programNumber,
         Map<String, Object> description, String queueName, ContentInfo attachment) {
      
      if (applicationProperties.isStandaloneMode()) {
         return;         
      }
      
      Event event = new Event();
      event.setComponentName(componentName);
      event.setCreatedOn(createdOn);
      event.setEntityId(entityId);
      event.setEntityType(entityType);
      event.setStudyId(studyId);
      event.setProgramNumber(programNumber);
      if (attachment != null) {
         event.setAttachmentContent(attachment.getContent());
         event.setAttachmentMimeType(attachment.getMimeType());
         event.setAttachmentFilename(attachment.getFileName());
      }
      EventType eventTypeEntity = eventTypeRepository.findByEventTypeName(eventType);
      if (eventTypeEntity != null && eventTypeEntity.getActive() == Boolean.TRUE) {
         event.setEventType(eventTypeRepository.findByEventTypeName(eventType));
         
         if (componentName == null || componentName.isEmpty()) {
            throw new InvalidEventInputException("Component Name is required.");
         }
         
         if (description != null && description.size() > 0) {
            try {
               event.setDescription(mapper.writeValueAsString(description));
            } catch (JsonProcessingException e) {
               throw new RuntimeException();
            }
         } else {
            throw new InvalidEventInputException("Description is required.");
         }

         messagingConnector.sendMessage(event, queueName);
         // TODO: see if we can return the eventId. RVG 28-Jun-2018.
      } else {
         throw new InvalidEventInputException(String.format("Event type '%s' doesn't exist or is not active.", eventType));
      }
   }

   public Map<String, Object> createEventDescription(String comments, String userName, String systemInitiated) {
      Map<String, Object> description = new HashMap<>();
      description.put("comments", comments);
      description.put("user_name", userName);
      description.put("system_initiated", systemInitiated);
      return description;
   }
}
