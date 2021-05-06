package com.pfizer.equip.shared.relational.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.EventType;
import com.pfizer.equip.shared.relational.entity.NotificationType;
import com.pfizer.equip.shared.relational.entity.Subscription;

@Transactional(readOnly = true)
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
   
     List<Subscription> findByEventType_EventTypeIdAndEventType_GlobalFlag(Long eventTypeId, Boolean globalFlag);
     
     List<Subscription> findByEventType_EventTypeIdAndStudyId(Long eventTypeId, String studyId);

     List<Subscription> findByEventType_EventTypeIdAndProgramNumber(Long eventTypeId, String programNumber);
     
     List<Subscription> findByEventType_EventTypeIdAndArtifactId(Long eventTypeId, String artifactId);

     List<Subscription> findByUserId(String userId);
     
     List<Subscription> findByUserIdAndStudyId(String userId, String studyId);

     List<Subscription> findByEventTypeAndNotificationTypeAndStudyIdAndProgramNumberAndArtifactIdAndUserIdAndEmail(
           EventType eventType, NotificationType notificationType, String studyId, String programNumber, String artifactId, String userId, String email);
     
     default List<Subscription> findByAll(
           EventType eventType, NotificationType notificationType, String studyId, String programNumber, String artifactId, String userId, String email) {
           return this.findByEventTypeAndNotificationTypeAndStudyIdAndProgramNumberAndArtifactIdAndUserIdAndEmail(
                 eventType, notificationType, studyId, programNumber, artifactId, userId, email);
     }
}