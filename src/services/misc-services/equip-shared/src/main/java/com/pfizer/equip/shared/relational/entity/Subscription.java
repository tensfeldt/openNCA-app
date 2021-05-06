package com.pfizer.equip.shared.relational.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.Entity
@Table(name = "subscription", schema = "equip_owner")
public class Subscription {

   @Id
   @Column(name = "subscription_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long subscriptionId;

   @ManyToOne(optional = false)
   @JoinColumn(name = "event_type_id", nullable = false)
   private EventType eventType;

   @ManyToOne(optional = false)
   @JoinColumn(name = "notification_type_id", nullable = false)
   private NotificationType notificationType;

   @Column(name = "study_id")
   private String studyId;

   @Column(name = "program_number")
   private String programNumber;

   @Column(name = "artifact_id")
   private String artifactId;

   @Column(name = "user_id", nullable = false)
   private String userId;

   @Column(name = "email")
   private String email;

   @OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
   @JsonIgnore
   private List<Notification> notifications;

   public Subscription() {
   }

   public Subscription(Long subscriptionId, EventType eventType, NotificationType notificationType, String studyId, String programNumber,
         String artifactId, String userId, String email) {
      this.subscriptionId = subscriptionId;
      this.eventType = eventType;
      this.notificationType = notificationType;
      this.studyId = studyId;
      this.programNumber = programNumber;
      this.artifactId = artifactId;
      this.userId = userId;
      this.email = email;
   }

   public Long getSubscriptionId() {
      return subscriptionId;
   }

   public void setSubscriptionId(Long subscriptionId) {
      this.subscriptionId = subscriptionId;
   }

   public EventType getEventType() {
      return eventType;
   }

   public void setEventType(EventType eventType) {
      this.eventType = eventType;
   }

   public NotificationType getNotificationType() {
      return notificationType;
   }

   public void setNotificationType(NotificationType notificationType) {
      this.notificationType = notificationType;
   }

   public String getStudyId() {
      return studyId;
   }

   public void setStudyId(String studyId) {
      this.studyId = studyId;
   }

   public String getProgramNumber() {
      return programNumber;
   }

   public void setProgramNumber(String programNumber) {
      this.programNumber = programNumber;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getArtifactId() {
      return artifactId;
   }

   public void setArtifactId(String artifactId) {
      this.artifactId = artifactId;
   }
}
