package com.pfizer.equip.shared.relational.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "notification", schema = "equip_owner")
public class Notification {

   @Id
   @Column(name = "notification_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long notificationId;

   @ManyToOne(optional = false)
   @JoinColumn(name = "event_id", nullable = false)
   private Event event;

   @ManyToOne(optional = false)
   @JoinColumn(name = "notification_type_id", nullable = false)
   private NotificationType notificationType;

   @Column(name = "processed", nullable = false)
   private Boolean processed;

   @ManyToOne(optional = false)
   @JoinColumn(name = "subscription_id", nullable = false)
   private Subscription subscription;

   @Column(name = "processed_on")
   private Date processedOn;

   public Notification() {
   }

   public Notification(Long notificationId, Event event, NotificationType notificationType, Boolean processed, Subscription subscription,
         Date processedOn) {
      this.notificationId = notificationId;
      this.event = event;
      this.notificationType = notificationType;
      this.processed = processed;
      this.subscription = subscription;
      this.processedOn = processedOn;
   }

   public Boolean getProcessed() {
      return processed;
   }

   public void setProcessed(Boolean processed) {
      this.processed = processed;
   }

   public Long getNotificationId() {
      return notificationId;
   }

   public void setNotificationId(Long notificationId) {
      this.notificationId = notificationId;
   }

   public Event getEvent() {
      return event;
   }

   public void setEvent(Event event) {
      this.event = event;
   }

   public NotificationType getNotificationType() {
      return notificationType;
   }

   public void setNotificationType(NotificationType notificationType) {
      this.notificationType = notificationType;
   }

   public Subscription getSubscription() {
      return subscription;
   }

   public void setSubscription(Subscription subscription) {
      this.subscription = subscription;
   }

   public Date getProcessedOn() {
      return processedOn;
   }

   public void setProcessedOn(Date processedOn) {
      this.processedOn = processedOn;
   }

}
