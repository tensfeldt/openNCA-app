package com.pfizer.equip.services.input.notification;

import java.util.Map;

import com.pfizer.equip.services.input.InputBuilder;

public class SubscriptionInputBuilder implements InputBuilder {
   private String subscriptionId;

   @Override
   public SubscriptionInput build(String userId, Map<String, Object> inputs) {
      String eventType = (String) inputs.get(KEY_EVENT_TYPE);
      String studyId = (String) inputs.get(KEY_STUDY_ID);
      String subscriptionuserId = (String) inputs.get("user_id");
      String programNumber = (String) inputs.get(KEY_PROGRAM_NUMBER);
      NotificationType notificationType = NotificationType.fromString((String) inputs.get(KEY_NOTIFICATION_TYPE));
      String email = (String) inputs.get(KEY_EMAIL);
      String artifactId = (String) inputs.get(KEY_ARTIFACT_ID);
      
      SubscriptionInput input = new SubscriptionInput(subscriptionId, eventType, studyId, programNumber, artifactId, notificationType, subscriptionuserId, email);
      return input;
   }

   public void setSubscriptionId(String subscriptionId) {
      this.subscriptionId = subscriptionId;
   }
}
