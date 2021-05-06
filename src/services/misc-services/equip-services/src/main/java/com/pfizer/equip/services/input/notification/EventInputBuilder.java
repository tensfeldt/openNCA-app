package com.pfizer.equip.services.input.notification;

import java.util.Map;

import com.pfizer.equip.services.input.InputBuilder;

public class EventInputBuilder implements InputBuilder {
   @Override
   public EventInput build(String userId, Map<String, Object> inputs) {
      String eventType = (String) inputs.get(KEY_EVENT_TYPE);
      String entityId = (String) inputs.get(KEY_ENTITY_ID);
      String entityType = (String) inputs.get(KEY_ENTITY_TYPE);
      String componentName = (String) inputs.get(KEY_COMPONENT_NAME);
      @SuppressWarnings("unchecked")
      Map<String, Object> description = (Map<String, Object>) inputs.get(KEY_EVENT_DETAIL);
      String studyId = (String) inputs.get(KEY_STUDY_ID);
      String programNumber = (String) inputs.get(KEY_PROGRAM_NUMBER);

      EventInput input = new EventInput(eventType, entityId, entityType, componentName, description, studyId, programNumber);
      return input;
   }
}
