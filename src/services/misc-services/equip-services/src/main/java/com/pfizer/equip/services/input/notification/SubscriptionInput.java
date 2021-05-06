package com.pfizer.equip.services.input.notification;

import com.pfizer.equip.services.input.AbstractInput;

public class SubscriptionInput extends AbstractInput {
   private final String subscriptionId;
   private final String eventType;
   private final NotificationType notificationType;
   private final String studyId;
   private final String programNumber;
   private final String userId;
   private final String email;
   private final String artifactId;

   /**
    * Using the builder design pattern here. Constructor is package only so that it
    * cannot be constructed publicly and the state of this remains immutable.
    */
   SubscriptionInput(final String subscriptionId, final String eventType, final String studyId, final String programNumber, final String artifactId,
         final NotificationType notificationType, final String userId, String email) {
      this.subscriptionId = subscriptionId;
      this.eventType = eventType;
      this.studyId = studyId;
      this.programNumber = programNumber;
      this.notificationType = notificationType;
      this.userId = userId;
      this.email = email;
      this.artifactId = artifactId;
   }

   public String getSubscriptionId() {
      return subscriptionId;
   }

   public String getEventType() {
      return eventType;
   }

   public NotificationType getNotificationType() {
      return notificationType;
   }

   public String getUserId() {
      return userId;
   }

   public String getEmail() {
      return email;
   }

   public String getStudyId() {
      return studyId;
   }

   public String getProgramNumber() {
      return programNumber;
   }

   public String getArtifactId() {
      return artifactId;
   }

}
