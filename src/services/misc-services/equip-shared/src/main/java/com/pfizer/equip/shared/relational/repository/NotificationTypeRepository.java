package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.NotificationType;

@Transactional(readOnly = true)
public interface NotificationTypeRepository extends CrudRepository<NotificationType, Long> {

   NotificationType findByNotificationTypeName(String notificationTypeName);
}
