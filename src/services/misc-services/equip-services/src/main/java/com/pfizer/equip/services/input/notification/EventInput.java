package com.pfizer.equip.services.input.notification;

import java.util.Map;

import com.pfizer.equip.services.input.AbstractInput;

public class EventInput extends AbstractInput {
   private final String eventType;
   private final String entityId;
   private final String entityType;
   private final String componentName;
   private final Map<String, Object> description;
   private final String studyId;
   private final String programNumber;

   /**
    * Using the builder design pattern here. Constructor is package only so that it
    * cannot be constructed publicly and the state of this remains immutable.
    */
   EventInput(final String eventType, final String entityId, final String entityType, final String componentName, final Map<String, Object> description,
         final String studyId, final String programNumber) {
      this.eventType = eventType;
      this.entityId = entityId;
      this.entityType = entityType;
      this.componentName = componentName;
      this.description = description;
      this.studyId = studyId;
      this.programNumber = programNumber;
   }

   public String getEventType() {
      return eventType;
   }

   public String getEntityId() {
      return entityId;
   }

   public String getComponentName() {
      return componentName;
   }

   public String getEntityType() {
      return entityType;
   }

   public Map<String, Object> getDescription() {
      return description;
   }

   public String getStudyId() {
      return studyId;
   }

   public String getProgramNumber() {
      return programNumber;
   }

}
