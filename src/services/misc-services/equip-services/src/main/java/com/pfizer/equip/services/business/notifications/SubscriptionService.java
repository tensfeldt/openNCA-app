package com.pfizer.equip.services.business.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pfizer.equip.services.business.notifications.exceptions.SubscriptionAlreadyExistsException;
import com.pfizer.equip.shared.relational.entity.EventType;
import com.pfizer.equip.shared.relational.entity.NotificationType;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.relational.repository.EventTypeRepository;
import com.pfizer.equip.shared.relational.repository.NotificationTypeRepository;
import com.pfizer.equip.shared.relational.repository.SubscriptionRepository;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;

@Service
public class SubscriptionService {

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private SubscriptionRepository subscriptionRepository;

   @Autowired
   private NotificationTypeRepository notificationTypeRepo;

   @Autowired
   private EventTypeRepository eventTypeRepository;

   @Autowired
   private UserLookupService userLookupService;

   @Transactional
   public void deleteSubscription(Long id, String requestUser, String systemId) throws ExecutionException {
      Optional<Subscription> subOptional = subscriptionRepository.findById(id);
      if (subOptional.isPresent()) {
         Subscription sub = subOptional.get();
         if (subOptional != null && !StringUtils.equalsIgnoreCase(sub.getUserId(), requestUser)) {
            if (!canModifySubscription(requestUser, systemId)) {
               throw new RuntimeException("Request user " + requestUser + " is not authorized to delete subscriptions");
            }
         }
         subscriptionRepository.delete(sub);
      }
   }

   @Transactional
   public long createSubscription(String notificationTypeName, String studyId, String programNumber, String artifactId, String eventTypeName, String userName,
         String email, String requestUser, String systemId) throws JsonProcessingException, IOException, ExecutionException {
      log.debug("RequestUser = {}, Subscriber User = {}", requestUser, userName);
      userLookupService.validateUser(systemId, userName);
      if (!StringUtils.equalsIgnoreCase(requestUser, userName)) {
         // check if requestUser is an admin user. If no throw an exception
         if (!canModifySubscription(requestUser, systemId)) {
            throw new RuntimeException("Request user " + requestUser + " is not authorized to create subscriptions");
         }
      }
      NotificationType notificationType = notificationTypeRepo.findByNotificationTypeName(notificationTypeName);
      EventType eventType = eventTypeRepository.findByEventTypeName(eventTypeName);

      // Check if record already exists:
      // Must use defaultifEmpty to null or else the query doesn't work properly. `column is null` vs ` column = '' `
      List<Subscription> existingSubscriptions = subscriptionRepository.findByAll(eventType, notificationType, StringUtils.defaultIfEmpty(studyId, null),
            StringUtils.defaultIfEmpty(programNumber, null), StringUtils.defaultIfEmpty(artifactId, null), userName, StringUtils.defaultIfEmpty(email, null));
      if (existingSubscriptions.size() > 0) {
         String empty = "(blank)";
         String message = String.format(
               "Subscription with these attributes already exists: "
                     + "Event Type = %s, Notification Type = %s, Study ID = %s, Program Number = %s, Artifact ID = %s, Email = %s",
               eventTypeName, notificationTypeName, StringUtils.defaultIfBlank(studyId, empty), StringUtils.defaultIfBlank(programNumber, empty),
               StringUtils.defaultIfBlank(artifactId, empty), StringUtils.defaultIfBlank(email, empty));
         throw new SubscriptionAlreadyExistsException(message);
      }
      Subscription subscription = new Subscription(null, eventType, notificationType, studyId, programNumber, artifactId, userName, email);
      subscriptionRepository.save(subscription);
      return subscription.getSubscriptionId();
   }

   @Transactional
   public void updateSubscription(Long id, String notificationType, String studyId, String programNumber, String eventType, String userId, String email,
         String requestUser, String systemId) throws JsonProcessingException, IOException, ExecutionException {
      userLookupService.validateUser(systemId, userId);
      Optional<Subscription> subOptional = subscriptionRepository.findById(id);
      if (subOptional.isPresent()) {
         Subscription subscription = subOptional.get();
         if (!canModifySubscription(requestUser, systemId)) {
            if (!StringUtils.equalsIgnoreCase(requestUser, subscription.getUserId()) || !StringUtils.equalsIgnoreCase(requestUser, userId)
                  || !StringUtils.equalsIgnoreCase(subscription.getUserId(), userId)) {
               throw new RuntimeException("Request user " + requestUser + " is not authorized to update subscriptions");
            }
         }
         subscription.setNotificationType(notificationTypeRepo.findByNotificationTypeName(notificationType));
         subscription.setStudyId(studyId);
         subscription.setProgramNumber(programNumber);
         subscription.setEventType(eventTypeRepository.findByEventTypeName(eventType));
         subscription.setUserId(userId);
         subscription.setEmail(email);
         subscriptionRepository.save(subscription);
      } else {
         throw new RuntimeException("Subscription " + id + " does not exist");
      }
   }

   public Subscription getSubscription(Long id) {
      Optional<Subscription> subOptional = subscriptionRepository.findById(id);
      if (subOptional.isPresent()) {
         return subOptional.get();
      } else {
         throw new RuntimeException("Subscription " + id + " does not exist");
      }
   }

   public List<Subscription> getSubscriptions(String userId, String systemId, String studyId) throws JsonProcessingException, IOException, ExecutionException {
      userLookupService.validateUser(systemId, userId);
      return subscriptionRepository.findByUserIdAndStudyId(userId, studyId);
   }

   private boolean canModifySubscription(String user, String systemId) throws ExecutionException {
      try {
         Set<PrivilegeType> privs = userLookupService.getUserFunctions(systemId, user);
         return (!privs.isEmpty() && EnumSet.copyOf(privs).contains(PrivilegeType.ALTER_ANY_SUBSCRIPTION));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
