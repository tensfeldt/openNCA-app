package com.pfizer.equip.services.business.notifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfizer.equip.shared.relational.entity.NotificationType;
import com.pfizer.equip.shared.relational.repository.NotificationTypeRepository;

@Service
public class NotificationTypeService {

   @Autowired
   private NotificationTypeRepository notificationTypeRepository;
   
   public List<NotificationType> getNotificationTypes() {
      List<NotificationType> notificationTypes = new ArrayList<>();
      notificationTypeRepository.findAll().forEach(notificationTypes::add);
      return notificationTypes;
   }
}
