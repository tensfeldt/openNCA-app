package com.pfizer.equip.services.responses;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.equip.shared.relational.entity.NotificationType;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.responses.AbstractResponse;

public class NotificationResponse extends AbstractResponse {
   private Long subscriptionId;
   private List<Subscription> subscriptions;
   private List<NotificationType> notificationTypes;

   public NotificationResponse() {
      subscriptions = new ArrayList<>();
      notificationTypes = new ArrayList<>();
      subscriptionId = 0L;// Initializing this value to help with Json to Object Mapping with Object Mapper in Junits
   }

   public List<Subscription> getSubscriptions() {
      return subscriptions;
   }

   public void setSubscriptions(List<Subscription> subscriptions) {
      this.subscriptions = subscriptions;
   }

   public List<NotificationType> getNotificationTypes() {
      return notificationTypes;
   }

   public void setNotificationTypes(List<NotificationType> notficationTypes) {
      this.notificationTypes = notficationTypes;
   }

   public long getSubscriptionId() {
      return subscriptionId;
   }

   public void setSubscriptionId(long subscriptionId) {
      this.subscriptionId = subscriptionId;
   }
}
