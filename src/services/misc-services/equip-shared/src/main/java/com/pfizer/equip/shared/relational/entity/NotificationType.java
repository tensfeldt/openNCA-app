package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "notification_type", schema = "equip_owner")
public class NotificationType {

   @Id
   @Column(name = "notification_type_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long notificationTypeId;

   @Column(name = "notification_type_name", nullable = false)
   private String notificationTypeName;

   public NotificationType() {
   }

   public NotificationType(Long notificationTypeId, String notificationTypeName) {
      this.notificationTypeId = notificationTypeId;
      this.notificationTypeName = notificationTypeName;
   }

   public Long getNotificationTypeId() {
      return notificationTypeId;
   }

   public void setNotificationTypeId(Long notificationTypeId) {
      this.notificationTypeId = notificationTypeId;
   }

   public String getNotificationTypeName() {
      return notificationTypeName;
   }

   public void setNotificationTypeName(String notificationTypeName) {
      this.notificationTypeName = notificationTypeName;
   }

}
