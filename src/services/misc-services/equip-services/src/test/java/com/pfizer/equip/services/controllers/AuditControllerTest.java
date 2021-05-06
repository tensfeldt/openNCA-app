package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.responses.AuditEntryResponse;
import com.pfizer.equip.shared.relational.entity.AuditEntry;
import com.pfizer.equip.shared.relational.repository.AuditEntryRepository;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuditControllerTest {

   // The user atlamr-ncadev2 has been assigned to a group EQUIP-NCA-CAG and has the role CAG.
   // The user should have VIEW_AUDIT privilege to access the AuditServiceController methods
   public final static String USERID = "atlamr-ncadev2";
   public final static String SYSTEMID = "nca";

   @Autowired
   private AuditEntryRepository auditEntryRepository;

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
   public void testSearchAuditEntries() {
      // The searchAuditEntries method in the Audit Controller is not implemented yet, hence we are checking the response as OK alone
      try {
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/audit-entries").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         AuditEntryResponse auditEntryResponse = mapper.readValue(result.getResponse().getContentAsString(), AuditEntryResponse.class);
         assertTrue(auditEntryResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void testaddAuditEntry() {
      try {
         AuditEntryInput input = new AuditEntryInput();
         input.setAction("Some auditable test event");
         input.setEntityId("AuditControllerTest_TestEntity123");
         input.setEntityType("DataFrame");
         input.setUserId(USERID);
         input.setActionStatus(ActionStatusType.SUCCESS);

         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/audit-entry").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(input))
                     .headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapperResult = new ObjectMapper();
         AuditEntryResponse auditEntryResponse = mapperResult.readValue(result.getResponse().getContentAsString(), AuditEntryResponse.class);
         assertTrue(auditEntryResponse.getResponse().equals(Response.OK));
         assertTrue(auditEntryResponse.getAuditEntryId() != null);
         AuditEntry auditEntry = auditEntryRepository.findByEntityId("AuditControllerTest_TestEntity123");
         assertTrue(auditEntry.getEntityId().equalsIgnoreCase("AuditControllerTest_TestEntity123"));
         assertTrue(auditEntry.getAction().equalsIgnoreCase("Some auditable test event"));
         assertTrue(auditEntry.getEntityType().equalsIgnoreCase("DataFrame"));
         assertTrue(auditEntry.getUserId().equalsIgnoreCase(USERID));
         assertTrue(auditEntry.getAuditEntryId().equals(auditEntryResponse.getAuditEntryId()));
         assertTrue(auditEntry.getActionStatus().equals(ActionStatusType.SUCCESS));

         // Delete the above audit entry inserted for the testing purpose
         // Cleanup
         auditEntryRepository.deleteById(auditEntry.getAuditEntryId());

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void testaddAuditEntryException() {
      try {

         ResultActions resultAction = mockMvc.perform(
               MockMvcRequestBuilders.post("/" + SYSTEMID + "/audit-entry").contentType(MediaType.APPLICATION_JSON_UTF8).content("{}").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assertTrue(result.getResponse().getContentAsString().contains(Response.FAILED.getValue()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
