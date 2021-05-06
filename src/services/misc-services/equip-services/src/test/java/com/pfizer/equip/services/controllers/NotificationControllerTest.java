package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.responses.NotificationResponse;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationControllerTest {
   //The user is a SUPER USER
   public final static String USERID = "atlamr-ncadev8";
   public final static String ADDITIONALUSERID = "ravicr";
   public final static String INVALIDUSERID = "testInvalid";
   public final static String TESTSTUDYID = "testStudy123";
   public final static String UPDATEDTESTSTUDYID = "testStudy12345";
   public final static String EVENT_TYPE = "qc_request_sent";
   public final static String INVALID_EVENT_TYPE = "test";
   public final static String NOTIFICATION_TYPE = "realtime_email";
   public final static String SYSTEMID = "nca";

   private MockMvc mockMvc;

   @Autowired
   private WebApplicationContext webApplicationContext;
   HttpHeaders headers = new HttpHeaders();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("IAMPFIZERUSERCN", USERID);

   }

   @Test
   public void test01_addSubscription_GetSubscription() {
      try {

         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", EVENT_TYPE);
         inputs.put("study_id", TESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", USERID);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/subscription").contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         assertNotNull(notificationResponse.getSubscriptionId());
         // Confirming creation of subscription using GetSubscription with subscription ID
         ResultActions getResult = mockMvc.perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/subscription/" + notificationResponse.getSubscriptionId())
               .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(getResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addSubscription_Invalid_Event_Type_Exception() {
      try {

         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", INVALID_EVENT_TYPE);
         inputs.put("study_id", TESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", USERID);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/subscription").contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addSubscription_SubscriptionForDifferentUser() {
      try {

         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", EVENT_TYPE);
         inputs.put("study_id", TESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", ADDITIONALUSERID);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/subscription").contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         // Confirming creation of subscription using GetSubscription with subscription ID
         ResultActions getResult = mockMvc.perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/subscription/" + notificationResponse.getSubscriptionId())
               .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(getResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addSubscription_SubscriptionForDifferentUserByNonAdminUser_Exception() {
      try {
         // TODO: Require a Non Admin Test - Test user accounts not active yet

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_getSubscriptionsList() {
      try {
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/user/" + USERID).headers(headers).accept(MediaType.APPLICATION_JSON));
         System.out.println(result.andReturn().getResponse());
         ObjectMapper mapper = new ObjectMapper();
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         assertFalse(notificationResponse.getSubscriptions().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_getSubscriptionsListException() {
      try {
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/user/" + INVALIDUSERID).headers(headers).accept(MediaType.APPLICATION_JSON));
         assert (result.andReturn().getResponse().getContentAsString().contains(Response.FAILED.getValue()));
         assert (result.andReturn().getResponse().getContentAsString().contains("NoSuchUserException"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_getNotificationsTypes() {
      try {
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/notification/types").headers(headers).accept(MediaType.APPLICATION_JSON));
         ObjectMapper mapper = new ObjectMapper();
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assertFalse(notificationResponse.getNotificationTypes().isEmpty());
         assert (notificationResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_updateSubscription() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         // Get all subscriptions by user
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/user/" + USERID).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         String subscriptionId = "";
         List<Subscription> subscriptions = notificationResponse.getSubscriptions();
         for (Subscription s : subscriptions) {
            if (s.getStudyId().equals(TESTSTUDYID)) {
               subscriptionId = s.getSubscriptionId().toString();
               break;
            }
         }
         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", EVENT_TYPE);
         inputs.put("study_id", UPDATEDTESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", USERID);
         // Update the subscription
         ResultActions updateResult = mockMvc.perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/subscription/" + subscriptionId).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(updateResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));

         ResultActions getUpdatedResult = mockMvc
               .perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/" + subscriptionId).headers(headers).accept(MediaType.APPLICATION_JSON));

         NotificationResponse notificationResponse2 = mapper.readValue(getUpdatedResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse2.getResponse().equals(Response.OK));
         assert (notificationResponse2.getSubscriptions().get(0).getStudyId().equals(UPDATEDTESTSTUDYID));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // TODO: Add tests with user authentication checks - Require Test Users without ALTER ANY SUBSCRIPTION
   @Test
   public void test04_updateSubscriptionException() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/user/" + ADDITIONALUSERID).headers(headers).accept(MediaType.APPLICATION_JSON));

         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         String subscriptionId = "";
         List<Subscription> subscriptions = notificationResponse.getSubscriptions();
         for (Subscription s : subscriptions) {
            if ((s.getStudyId() != null) && (s.getStudyId().equals(TESTSTUDYID))) {
               subscriptionId = s.getSubscriptionId().toString();
               break;
            }
         }

         // Delete the subscription
         ResultActions deleteResult = mockMvc
               .perform(MockMvcRequestBuilders.delete("/" + SYSTEMID + "/subscription/" + subscriptionId).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(deleteResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));

         // Trying to update the deleted Subscription
         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", EVENT_TYPE);
         inputs.put("study_id", UPDATEDTESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", USERID);
         mockMvc.perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/subscription/" + subscriptionId).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(inputs))
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isInternalServerError());

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_deleteSubscription() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/subscription/user/" + USERID).headers(headers).accept(MediaType.APPLICATION_JSON));

         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         String subscriptionId = "";
         List<Subscription> subscriptions = notificationResponse.getSubscriptions();
         for (Subscription s : subscriptions) {
            if (s.getStudyId().equals(UPDATEDTESTSTUDYID)) {
               subscriptionId = s.getSubscriptionId().toString();
               break;
            }
         }
         ResultActions deleteResult = mockMvc
               .perform(MockMvcRequestBuilders.delete("/" + SYSTEMID + "/subscription/" + subscriptionId).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(deleteResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_deleteSubscriptionException() {
      try {

         Map<String, String> inputs = new HashMap<>();
         inputs.put("event_type", EVENT_TYPE);
         inputs.put("study_id", TESTSTUDYID);
         inputs.put("notification_type", NOTIFICATION_TYPE);
         inputs.put("user_id", USERID);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/subscription").contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         assertNotNull(notificationResponse.getSubscriptionId());

         // Delete the subscription
         ResultActions deleteResult = mockMvc
               .perform(MockMvcRequestBuilders.delete("/" + SYSTEMID + "/subscription/" + notificationResponse.getSubscriptionId()).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse1 = mapper.readValue(deleteResult.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse1.getResponse().equals(Response.OK));

         // Delete the subscription again - trying to delete a non existing subscription
         ResultActions deleteResult1 = mockMvc
               .perform(MockMvcRequestBuilders.delete("/" + SYSTEMID + "/subscription/" + notificationResponse.getSubscriptionId()).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse2 = mapper.readValue(deleteResult1.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse2.getResponse().equals(Response.OK));
         // Delete is idempotent. Should return OK .

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_publishEvent() {
      try {

         Map<String, Object> inputs = new HashMap<>();
         inputs.put("component_name", this.getClass().toString());
         inputs.put("entity_type", "TESTS");
         inputs.put("event_type", "qc_request_sent");

         Map<String, Object> description = new HashMap<>();
         description.put("comments", "Event generated from unit Tests ");
         description.put("user_name", USERID);
         description.put("system_initiated", "false");
         ObjectMapper mapper = new ObjectMapper();

         inputs.put("event_detail", description);

         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/event/publish").contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(inputs)).headers(headers).accept(MediaType.APPLICATION_JSON));
         NotificationResponse notificationResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationResponse.class);
         assert (notificationResponse.getResponse().equals(Response.OK));
         assertNotNull(notificationResponse.getSubscriptionId());

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test06_publishEventException() {
      try {

         Map<String, Object> inputs = new HashMap<>();
         inputs.put("component_name", this.getClass().toString());
         inputs.put("entity_type", "TESTS");
         inputs.put("event_type", INVALID_EVENT_TYPE);

         Map<String, Object> description = new HashMap<>();
         description.put("comments", "Event generated from unit Tests ");
         description.put("user_name", USERID);
         description.put("system_initiated", "false");
         ObjectMapper mapper = new ObjectMapper();

         inputs.put("event_detail", description);

         mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/event/publish").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(inputs)).headers(headers)
               .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isInternalServerError());

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   // TODO: Need to update the below test method with assert and expectations
   // @Test
   public void test07_defaultSubscriptionToRoles() {
      try {

         mockMvc
               .perform(MockMvcRequestBuilders.post("/{systemId}/roles/{roleName}/users/{userName}/default-subscriptions", "nca", "SYSADMIN", "ravicr").headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // TODO: Need to update the below test method with assert and expectations
   // @Test
   public void test08_defaultSubscriptionToStudyId() {
      try {

         mockMvc
               .perform(MockMvcRequestBuilders.post("/{systemId}/roles/{roleName}/users/{userName}/programs/{programId}/protocols/{protocolId}/default-subscriptions",
                     "nca", "SYSADMIN", "ravicr", "X111", "X1110011").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
