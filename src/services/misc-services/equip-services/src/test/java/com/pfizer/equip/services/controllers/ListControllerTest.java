package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.shared.relational.entity.ListName;
import com.pfizer.equip.shared.relational.entity.ListValue;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListControllerTest {
   //The user is a SUPER USER
   public final static String USERID = "atlamr-ncadev8";
   private static final String INVALIDLISTNAME = "testlist";
   private static final String SYSTEMID = "nca";

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
   public void test01_getAllLists() {

      try {

         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/list").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         List<ListName> listName = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ListName>>() {});
         assertFalse(listName.isEmpty());
         assertFalse(listName.get(0).getName().isEmpty());
         assertFalse(listName.get(0).getValues().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_getListValuesByName() {

      try {

         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/list").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         List<ListName> listName = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ListName>>() {});
         assertFalse(listName.isEmpty());
         assertFalse(listName.get(0).getName().isEmpty());
         assertFalse(listName.get(0).getValues().isEmpty());
         String name = listName.get(0).getName();
         ResultActions resultAction1 = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/list/" + name).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result1 = resultAction1.andReturn();
         List<ListValue> listValue = mapper.readValue(result1.getResponse().getContentAsString(), new TypeReference<List<ListValue>>() {});
         assertFalse(listValue.isEmpty());
         assertTrue(listValue.size() == listName.get(0).getValues().size());

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_getListValuesByNameException() {

      try {

         ResultActions resultAction1 = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/list/" + INVALIDLISTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         assert (resultAction1.andReturn().getResponse().getContentAsString().contains("ListNotFoundException"));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
