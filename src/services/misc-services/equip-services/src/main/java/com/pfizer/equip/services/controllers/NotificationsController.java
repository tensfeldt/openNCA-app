package com.pfizer.equip.services.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.notifications.NotificationTypeService;
import com.pfizer.equip.services.business.notifications.SubscriptionService;
import com.pfizer.equip.services.business.opmeta.OperationalMetadataService;
import com.pfizer.equip.services.input.notification.EventInput;
import com.pfizer.equip.services.input.notification.EventInputBuilder;
import com.pfizer.equip.services.input.notification.NotificationType;
import com.pfizer.equip.services.input.notification.SubscriptionInput;
import com.pfizer.equip.services.input.notification.SubscriptionInputBuilder;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.NotificationResponse;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.relational.entity.EventType;
import com.pfizer.equip.shared.relational.entity.Role;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.relational.repository.RoleRepository;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.service.user.UserInfo;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class NotificationsController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);
   private static final String STUDYSPECIFIC = "NCA Project/Study Specific";

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   private SubscriptionService subscriptionService;

   @Autowired
   private EventService eventService;

   @Autowired
   private NotificationTypeService notificationTypeService;

   @Autowired
   private RoleRepository roleRepository;

   @Autowired
   private AuditService auditService;

   // Used for validating user ID:
   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private OperationalMetadataService operationalMetadataService;

   /**
    * Publishes an event to the Notification system. This method consumes only "application/json" content types with the POST request. The input format is defined below.
    * 
    * <pre>
    * {"event_type": "",
    *  "entity_id": "", 
    *  "entity_type": "", 
    *  "study_id": "",
    *  "program_number": "",
    *  "component_name": "", 
    *  "event_detail": 
    *   {
    *      "system_initiated": true/false,
    *      "user_name": "",
    *      "comments": "",
    *      "requested_qc_due_date":"MM/dd/yyyy HH:mm:ss",
    *      "reporting_event_type": "",
    *      "reporting_event_id": "",
    *      "parameter_data_qc_status": "",
    *      "concentration_data_status": "",
    *      "publishing_event_expiration_date": "MM/dd/yyyy HH:mm:ss",
    *      "data_status": "",
    *      "blinding_status": "",
    *      "analyst_name": "",
    *      "qc_status": "",
    *      "number_record_data_load": 0,
    *      "number_subjects_data_load": 0,
    *      "number_skipped_records_data_load": 0
    *     }
    *  }
    * </pre>
    * 
    * @param inputs
    * 
    * @return
    */
   @RequestMapping(value = "{systemId}/event/publish", method = RequestMethod.POST, consumes = { "application/json" })
   public NotificationResponse publishEvent(@RequestBody Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         EventInput input = new EventInputBuilder().build(userId, inputs);
         log.info(
               "Publishing new event type '{}', for entity ID '{}', for entity Type '{}', for component name '{}', for study id '{}', for program number '{}', for event_detail '{}'...",
               input.getEventType(), input.getEntityId(), input.getEntityType(), input.getComponentName(), input.getStudyId(), input.getProgramNumber(),
               input.getDescription());
         eventService.createEvent(input.getComponentName(), new Date(), input.getEntityId(), input.getEntityType(), input.getEventType(), input.getStudyId(),
               input.getProgramNumber(), input.getDescription(), properties.getEventQueue());
         NotificationResponse response = new NotificationResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during publishing of event.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   /**
    * Creates a subscription the Notification system. This method consumes only "application/json" content types with the POST request. The input format is defined below.
    * 
    * <pre>
    *  {"notification_type": "",
    *   "study_id": "",
    *   "program_number": "",
    *   "event_type": "",
    *   "user_id": "",
    *   "email": ""
    *   }
    * </pre>
    * 
    * @param inputs
    * @return
    */
   @RequestMapping(value = "{systemId}/subscription", method = RequestMethod.POST, consumes = { "application/json" })
   public NotificationResponse addSubscription(@PathVariable String systemId, @RequestBody Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         SubscriptionInput input = new SubscriptionInputBuilder().build(userId, inputs);
         NotificationType notificationType = input.getNotificationType();
         String subscriptionUserId = StringUtils.defaultIfEmpty(input.getUserId(), "");
         log.info(
               "Adding new subscription for event type '{}', study ID '{}', program number '{}', artifact id '{}', notification type '{}', user ID '{}', and email '{}'...",
               input.getEventType(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(), input.getNotificationType(), input.getUserId(),
               input.getEmail());
         long subscriptionId = subscriptionService.createSubscription(notificationType.getValue(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(),
               input.getEventType(), subscriptionUserId, input.getEmail(), getUserId(request), systemId);
         NotificationResponse response = new NotificationResponse();
         response.setResponse(Response.OK);
         response.setSubscriptionId(subscriptionId);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Added subscription for User " + input.getUserId() + " for event type" + input.getEventType(),
               String.valueOf(response.getSubscriptionId()), EntityType.SUBSCRIPTION.getValue(), subscriptionUserId, ActionStatusType.SUCCESS, null));

         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   /**
    * Updates a subscription in the Notification system. This method consumes only "application/json" content types with the PUT request. The input format is defined
    * below.
    * 
    * <pre>
    *  {"notification_type": "",
    *   "study_id": "",
    *   "program_number": "",
    *   "event_type": "",
    *   "user_id": "",
    *   "email": ""
    *   }
    * </pre>
    * 
    * @param inputs
    * @return
    */
   @RequestMapping(value = "{systemId}/subscription/{subscriptionId}", method = RequestMethod.PUT, consumes = { "application/json" })
   public NotificationResponse updateSubscription(@PathVariable String systemId, @PathVariable String subscriptionId, @RequestBody Map<String, Object> inputs,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         SubscriptionInputBuilder builder = new SubscriptionInputBuilder();
         builder.setSubscriptionId(subscriptionId);
         SubscriptionInput input = builder.build(userId, inputs);
         NotificationType notificationType = input.getNotificationType();
         String subscriptionUserId = StringUtils.defaultIfEmpty(input.getUserId(), "");
         log.info("Updating subscription ID '{}' for event type '{}', study ID '{}', for program number '{}', notification type '{}', user ID '{}', and email '{}'...",
               subscriptionId, input.getEventType(), input.getStudyId(), input.getProgramNumber(), input.getNotificationType(), input.getUserId(), input.getEmail());

         subscriptionService.updateSubscription(Long.valueOf(subscriptionId), notificationType.getValue(), input.getStudyId(), input.getProgramNumber(),
               input.getEventType(), subscriptionUserId, input.getEmail(), getUserId(request), systemId);
         NotificationResponse response = new NotificationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Updated subscription for User " + input.getUserId() + " for event type" + input.getEventType(),
               subscriptionId, EntityType.SUBSCRIPTION.getValue(), subscriptionUserId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during update of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/subscription/{subscriptionId}", method = RequestMethod.DELETE)
   public NotificationResponse removeSubscription(@PathVariable String systemId, @PathVariable String subscriptionId, @RequestParam Map<String, Object> inputs,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Deleting subscription ID '{}'...", subscriptionId);
         String userId = getUserId(request);
         subscriptionService.deleteSubscription(Long.valueOf(subscriptionId), userId, systemId);
         NotificationResponse response = new NotificationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Removed subscription for User " + userId, subscriptionId, EntityType.SUBSCRIPTION.getValue(), userId,
               ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during deletion of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/subscription/user/{userId}/{studyId}", method = RequestMethod.POST)
   public NotificationResponse updateSubscriptionsToStudy(@PathVariable String systemId, @PathVariable String userId, @PathVariable String studyId,
         @RequestParam boolean active, @RequestParam String notificationType, @RequestParam List<String> eventTypes, @RequestParam String programNumber,
         @RequestParam String email) {
      Monitor monitor = null;
      try {
         List<Subscription> subs = subscriptionService.getSubscriptions(userId, systemId, studyId);
         int deletes = 0;
         int creates = 0;
         // If active is false, delete all and return empty list
         if (!active) {
            for (Subscription sub : subs) {
               subscriptionService.deleteSubscription(Long.valueOf(sub.getSubscriptionId()), userId, systemId);
               deletes++;
            }
            log.debug("deleted {} subs", deletes);
            NotificationResponse response = new NotificationResponse();
            response.setResponse(Response.OK);
            return response;
         }

         // For each sub of subs, if (sub.eventType is not present in eventTypes or sub.notificationType != notificationType) delete sub
         for (Subscription sub : subs) {
            if (!eventTypes.contains(sub.getEventType().getEventTypeName()) || !sub.getNotificationType().getNotificationTypeName().equals(notificationType)) {
               subscriptionService.deleteSubscription(Long.valueOf(sub.getSubscriptionId()), userId, systemId);
               deletes++;
            }
         }
         
         // Update subs (This is needed in case the notification type is changed)
         subs = subscriptionService.getSubscriptions(userId, systemId, studyId);
         
         // For each eventType of eventTypes, if eventType is not present in subs add new sub
         for (String eventType : eventTypes) {
            if (!subs.stream().filter(sub -> sub.getEventType().getEventTypeName().equals(eventType)).findFirst().isPresent()) {
               subscriptionService.createSubscription(notificationType, studyId, programNumber, null, eventType, userId, email, userId, systemId);
               creates++;
            }
         }
         
         log.debug("Deleted {} subs", deletes);
         log.debug("Created {} subs", creates);
         // get updated list of subs to return to front end
         subs = subscriptionService.getSubscriptions(userId, systemId, studyId);
         NotificationResponse response = new NotificationResponse();
         response.setResponse(Response.OK);
         response.setSubscriptions(subs);
         return response;
      } catch (Exception e) {
         log.error("Exception occured while updating subscription to a study.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/subscription/{subscriptionId}", method = RequestMethod.GET)
   public NotificationResponse getSubscription(@PathVariable String subscriptionId) {
      Monitor monitor = null;
      try {
         log.info("Retrieving subscription ID '{}'...", subscriptionId);
         Subscription sub = subscriptionService.getSubscription(Long.valueOf(subscriptionId));
         NotificationResponse response = new NotificationResponse();
         response.setSubscriptions(Arrays.asList(sub));
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during deletion of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/subscription/user/{userId}/{studyId}", method = RequestMethod.GET)
   public NotificationResponse getSubscriptionsList(@PathVariable String systemId, @PathVariable String userId, @PathVariable String studyId) {
      Monitor monitor = null;
      try {
         log.info("Retrieving subscription for user '{}'...", userId);
         List<Subscription> subs = subscriptionService.getSubscriptions(userId, systemId, studyId);
         NotificationResponse response = new NotificationResponse();
         response.setSubscriptions(subs);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during deletion of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/notification/types", method = RequestMethod.GET)
   public NotificationResponse getNotificationsTypes() {
      Monitor monitor = null;
      try {
         log.info("Retrieving notifications types");
         List<com.pfizer.equip.shared.relational.entity.NotificationType> notificationTypes = notificationTypeService.getNotificationTypes();
         NotificationResponse response = new NotificationResponse();
         response.setNotificationTypes(notificationTypes);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during deletion of subscription.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   /**
    * This method creates a global subscription to the given role name for the given user name
    * 
    * @param systemId
    * @param roleName
    * @param userName
    * @return
    */

   @RequestMapping(value = "{systemId}/roles/{roleName}/users/{userName}/default-subscriptions", method = RequestMethod.POST)
   public NotificationResponse defaultSubscriptionToRoles(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName,
         @PathVariable("userName") String userName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         NotificationResponse response = new NotificationResponse();
         log.info("Getting users for role '{}'", roleName);
         // Get Users by Role
         Set<String> users = userLookupService.getUsernamesByRole(systemId, roleName);
         if (users.contains(userName)) {
            UserInfo userInfo = userLookupService.lookupUser(userName);
            // Get Role Id
            Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);

            // Get Subscription event types from the role id
            if (!role.getEventTypes().isEmpty()) {
               for (EventType eventType : role.getEventTypes()) {
                  // Set the input values
                  Map<String, Object> inputs = new HashMap<>();
                  inputs.put("notification_type", NotificationType.REALTIME.toString());
                  inputs.put("user_id", userName);
                  inputs.put("email", userInfo.getEmailAddress());
                  inputs.put("event_type", eventType.getEventTypeName());

                  SubscriptionInput input = new SubscriptionInputBuilder().build(userId, inputs);
                  NotificationType notificationType = input.getNotificationType();
                  String subscriptionUserId = StringUtils.defaultIfEmpty(input.getUserId(), "");

                  log.info(
                        "Adding new subscription for event type '{}', study ID '{}', program number '{}', artifact id '{}', notification type '{}', user ID '{}', and email '{}'...",
                        input.getEventType(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(), input.getNotificationType(), input.getUserId(),
                        input.getEmail());
                  subscriptionService.createSubscription(notificationType.getValue(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(),
                        input.getEventType(), subscriptionUserId, input.getEmail(), getUserId(request), systemId);
                  response.setResponse(Response.OK);
                  // TODO : To add few more information to the response
               }
            } else {
               String message = String.format("No Event types found for the '%s' role under the system id '%s'.", roleName, systemId);
               log.error(message);
               throw new RuntimeException(message); // causes response status to be FAILED automatically but includes a message
            }
         } else {
            String message = String.format("User '%s' does not have the '%s' role. Try with the correct role name of the user.", userName, roleName);
            log.error(message);
            throw new RuntimeException(message); // causes response status to be FAILED automatically but includes a message
         }

         return response;

      } catch (Exception e) {
         log.error("Exception occured during creation of default subscriptions by role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   /**
    * This method creates a default study based subscription to the given role name, user name, and the study id
    * 
    * @param systemId
    * @param roleName
    * @param userName
    * @param studyId
    * @return
    */

   @RequestMapping(value = "{systemId}/roles/{roleName}/users/{userName}/programs/{programId}/protocols/{protocolId}/default-subscriptions", method = RequestMethod.POST)
   public NotificationResponse defaultSubscriptionToStudyId(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName,
         @PathVariable("userName") String userName, @PathVariable("programId") String programId, @PathVariable("protocolId") String protocolId,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         NotificationResponse response = new NotificationResponse();
         // Check the Prgram and Protocol exists
         Program program = operationalMetadataService.getProgramAndProtocol(programId, protocolId);
         if (program != null) {
            log.info("Getting users for role '{}'", roleName);
            // Get Users by Role
            Set<String> users = userLookupService.getUsernamesByRole(systemId, roleName);
            if (users.contains(userName)) {
               UserInfo userInfo = userLookupService.lookupUser(userName);
               // Get Role Id
               Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);

               // Get Subscription event types from the role id
               if (!role.getEventTypes().isEmpty()) {
                  for (EventType eventType : role.getEventTypes()) {
                     if (eventType.getEventCategory().equalsIgnoreCase(STUDYSPECIFIC)) {
                        // Set the input values
                        Map<String, Object> inputs = new HashMap<>();
                        inputs.put("notification_type", NotificationType.REALTIME.toString());
                        inputs.put("user_id", userName);
                        inputs.put("email", userInfo.getEmailAddress());
                        inputs.put("event_type", eventType.getEventTypeName());
                        inputs.put("program_number", programId);
                        inputs.put("study_id", protocolId);
                        String path = String.format("Programs/%s/Protocols/%s", programId, protocolId);
                        inputs.put("artifact_id", path);

                        SubscriptionInput input = new SubscriptionInputBuilder().build(userId, inputs);
                        NotificationType notificationType = input.getNotificationType();
                        String subscriptionUserId = StringUtils.defaultIfEmpty(input.getUserId(), "");

                        log.info(
                              "Adding new subscription for event type '{}', study ID '{}', program number '{}', artifact id '{}', notification type '{}', user ID '{}', and email '{}'...",
                              input.getEventType(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(), input.getNotificationType(), input.getUserId(),
                              input.getEmail());
                        subscriptionService.createSubscription(notificationType.getValue(), input.getStudyId(), input.getProgramNumber(), input.getArtifactId(),
                              input.getEventType(), subscriptionUserId, input.getEmail(), getUserId(request), systemId);
                        response.setResponse(Response.OK);
                        // TODO : To add few more information to the response
                     }
                  }
               } else {
                  String message = String.format("No Event types found for the '%s' role under the system id '%s'.", roleName, systemId);
                  log.error(message);
                  throw new RuntimeException(message); // causes response status to be FAILED automatically but includes a message
               }
            } else {
               String message = String.format("User '%s' does not have the '%s' role. Try with the correct role name of the user.", userName, roleName);
               log.error(message);
               throw new RuntimeException(message); // causes response status to be FAILED automatically but includes a message
            }
         }

         return response;

      } catch (Exception e) {
         log.error("Exception occured during creation of default subscriptions by role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

}
