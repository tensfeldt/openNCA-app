package com.pfizer.equip.shared.relational.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.Notification;

@Transactional(readOnly = true)
public interface NotificationRepository extends CrudRepository<Notification, Long> {

   List<Notification> findByNotificationType_NotificationTypeNameAndProcessedOrderBySubscription_SubscriptionId(String notificationTypeName,
         Boolean processed);

}
