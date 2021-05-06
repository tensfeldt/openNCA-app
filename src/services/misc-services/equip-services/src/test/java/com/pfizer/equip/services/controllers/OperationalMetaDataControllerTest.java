package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.responses.opmeta.AttachmentVersionHistoryResponse;
import com.pfizer.equip.services.responses.opmeta.OperationalMetadataResponse;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OperationalMetaDataControllerTest {

   public final static String USERID = "atlamr-ncadev2";
   public final static String TESTPROGRAMCODE = "X999";
   public final static String TESTPROGRAMCODE2 = "X911";
   public final static String SYSTEMID = "nca";
   public final static String TESTSTUDYID = "X9991001";
   public final static String INVALIDPROGRAMCODE = "X990";
   public final static String INVALIDSTUDYID = "X9901001";
   public final static String SERVICEPATH = "/" + SYSTEMID + "/opmeta/nodes/programs";
   public final static String TESTPODSPROGRAMCODE = "A143";
   public final static String TESTSTUDYIDPODS = "A1431001";
   // TODO: Create Test users for CAG and PKA . Using Richards ID for now.
   public final static String CAG_USERID = "atlamr-ncadev2";
   public final static String PKA_USERID = "atlamr-ncadev3";
   public static final String USERID2 = "atlamr-ncadev2";
   public final static String TEST_USER1 = "atlamr-ncadev4";// TODO: This user should not have ALTER_PROGRAM , QUERY_PODS privilege.

   private MockMvc mockMvc;
   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private ResourceLoader resourceLoader;

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

   public void setUp() {

      try {
         // Delete the program (EQUIP) created.
         repositoryService.deleteNode("/Programs/" + TESTPROGRAMCODE);
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {
         repositoryService.deleteNode("/Programs/" + TESTPODSPROGRAMCODE);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgram() {
      try {
         // Cleanup from Previous Run
         setUp();
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE);
         inputs.put("opmeta:source", "EQUIP");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTPROGRAMCODE));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgramException() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE);
         inputs.put("opmeta:source", "EQUIP");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("already exists"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgramPods() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPODSPROGRAMCODE);
         inputs.put("opmeta:source", "PODS");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTPODSPROGRAMCODE));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgramPodsException() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPODSPROGRAMCODE);
         inputs.put("opmeta:source", "PODS");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("already exists"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgramException_SourceNull() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE2);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("SourceNotFound"));// Source should not be null
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addProgramException_InvalidSource() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE2);
         inputs.put("opmeta:source", "PROD");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("InvalidFormatException"));// Invalid Source
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // @Test
   // public void test01_addProgramException_Without_ALTER_PROGRAM_Privilege() {
   // try {
   // HttpHeaders headers1 = new HttpHeaders();
   // headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
   // headers1.set("IAMPFIZERUSERCN", TEST_USER1);
   // Map<String, String> inputs = new HashMap<>();
   // inputs.put("opmeta:programCode", TESTPROGRAMCODE2);
   // inputs.put("opmeta:source", "EQUIP");
   // ObjectMapper mapper = new ObjectMapper();
   // ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
   // .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
   // MvcResult result = resultAction.andReturn();
   // assert (result.getResponse().getContentAsString().contains("NotAuthorizedException"));
   // assert (result.getResponse().getContentAsString().contains("EQUIP source"));// No ALTER_PROGRAM priv
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }
   //
   // @Test
   // public void test01_addProgramException_Without_QUERY_PODS_Privilege() {
   // try {
   // HttpHeaders headers1 = new HttpHeaders();
   // headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
   // headers1.set("IAMPFIZERUSERCN", TEST_USER1);
   // Map<String, String> inputs = new HashMap<>();
   // inputs.put("opmeta:programCode", TESTPROGRAMCODE2);
   // inputs.put("opmeta:source", "PODS");
   // ObjectMapper mapper = new ObjectMapper();
   // ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(SERVICEPATH).headers(headers).content(mapper.writeValueAsString(inputs))
   // .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
   // MvcResult result = resultAction.andReturn();
   // assert (result.getResponse().getContentAsString().contains("NotAuthorizedException"));
   // assert (result.getResponse().getContentAsString().contains("PODS source"));// No QUERY_PODS priv
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }

   @Test
   public void test02_getProgram() {
      try {
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         ObjectMapper mapper = new ObjectMapper();
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assert (result.getResponse().getContentAsString().contains(TESTPROGRAMCODE));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_addProtocolByProgram() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         inputs.put("opmeta:source", "EQUIP");
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols")
               .headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYID));
         assertNotNull(operationalMetadataResponse.getNodeId());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_addProtocolByProgramException() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         inputs.put("opmeta:source", "EQUIP");
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + INVALIDPROGRAMCODE + "/protocols").headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         assert (resultAction.andReturn().getResponse().getContentAsString().contains("404"));// Not Found;

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_addProtocolByProgramPods() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYIDPODS);
         inputs.put("opmeta:source", "PODS");
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPODSPROGRAMCODE + "/protocols")
               .headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYIDPODS));
         assertNotNull(operationalMetadataResponse.getNodeId());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void test03_addProtocolByProgramPodsException() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYIDPODS);
         inputs.put("opmeta:source", "PODS");
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPODSPROGRAMCODE + "/protocols")
               .headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assertNotNull(result.getResponse().getContentAsString().contains("already exists"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_addProtocolByProgramException_Source_Null() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols").headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         assert (resultAction.andReturn().getResponse().getContentAsString().contains("SourceNotFound"));// source null

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_addProtocolByProgramException_Source_Invalid() {
      try {
         Map<String, Object> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         inputs.put("opmeta:source", "PROD");
         Map<String, String> milestonesInput = new HashMap<String, String>();
         milestonesInput.put("opmeta:sourceCreationTimestamp", "2016-02-02T20:08:08.000Z");
         milestonesInput.put("opmeta:milestoneId", "48");
         milestonesInput.put("opmeta:milestoneSequenceNumber", "1");
         milestonesInput.put("opmeta:actualDate", "2014-11-25T05:00:00.000Z");
         // inputs.put("milestones", milestonesInput);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols").headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         assert (resultAction.andReturn().getResponse().getContentAsString().contains("InvalidFormatException"));// source invalid

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_getProtocol() {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         ObjectMapper mapper = new ObjectMapper();
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assert (result.getResponse().getContentAsString().contains(TESTSTUDYID));
         assert (result.getResponse().getContentAsString().contains(TESTPROGRAMCODE));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_getProtocolException() {
      try {
         ResultActions resultAction = mockMvc
               .perform(
                     MockMvcRequestBuilders.get(SERVICEPATH + "/" + INVALIDPROGRAMCODE + "/protocols/" + TESTSTUDYID).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("not found"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_getProtocolsByProgram() {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         ObjectMapper mapper = new ObjectMapper();
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assert (result.getResponse().getContentAsString().contains(TESTSTUDYID));
         assert (result.getResponse().getContentAsString().contains(TESTPROGRAMCODE));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_updateProgram() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE);
         inputs.put("opmeta:source", "EQUIP");
         inputs.put("opmeta:compound", "PF-04971729");
         inputs.put("opmeta:compoundMechanismOfAction", "Sodium/glucose demo 2 (SGLT2) Inhibitor");
         inputs.put("opmeta:compoundName", "Examplefil");
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.put(SERVICEPATH + "/" + TESTPROGRAMCODE).headers(headers)
               .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTPROGRAMCODE));
         try {
            ResultActions resultAction1 = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH).headers(headers).accept(MediaType.APPLICATION_JSON))
                  .andExpect(MockMvcResultMatchers.status().isOk());
            MvcResult result1 = resultAction1.andReturn();
            assert (result1.getResponse().getContentAsString().contains("PF-04971729"));
            assert (result1.getResponse().getContentAsString().contains("Sodium/glucose demo 2 (SGLT2) Inhibitor"));
         } catch (Exception e) {
            e.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_updateProtocolByProgram() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         inputs.put("opmeta:source", "EQUIP");
         inputs.put("opmeta:studyTherapeuticArea", "METABOLIC DISEASE");
         inputs.put("opmeta:studyType", "INTERVENTIONAL");
         inputs.put("opmeta:subjectGender", "BOTH (MALES AND FEMALES)");

         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID).headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYID));
         // get the protocol to verify the change
         ResultActions resultAction1 = mockMvc
               .perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result1 = resultAction1.andReturn();
         assert (result1.getResponse().getContentAsString().contains("METABOLIC DISEASE"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_updateProtocolByProgramException() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYID);
         inputs.put("opmeta:source", "EQUIP");
         inputs.put("opmeta:studyTherapeuticArea", "METABOLIC DISEASE");
         inputs.put("opmeta:studyType", "INTERVENTIONAL");
         inputs.put("opmeta:subjectGender", "BOTH (MALES AND FEMALES)");

         ObjectMapper mapper = new ObjectMapper();
         mockMvc
               .perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + INVALIDSTUDYID).headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // TODO : Include Test Case for UpdateProtocol by User without ALTER_PROTOCOL_BLINDING priv.
   @Test
   public void test06_updateProtocolByProgramPODS() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:studyId", TESTSTUDYIDPODS);
         inputs.put("opmeta:studyBlindingStatus", StudyBlindingStatus.BLINDED.getValue());

         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPODSPROGRAMCODE + "/protocols/" + TESTSTUDYIDPODS).headers(headers)
                     .content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYIDPODS));
         // get the protocol to verify the change
         ResultActions resultAction1 = mockMvc.perform(
               MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPODSPROGRAMCODE + "/protocols/" + TESTSTUDYIDPODS).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result1 = resultAction1.andReturn();
         assert (result1.getResponse().getContentAsString().contains(Response.OK.getValue()));
         // TODO:check for blinding status change
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test07_getCagUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders
               .get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-cag-users").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getUsers().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test08_getPkaUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders
               .get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getUsers().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_addPkaUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         Set<String> users = new HashSet<>();
         users.add(PKA_USERID);
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users").headers(headers)
                     .accept(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(users)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users")
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult getResult = getAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse1 = mapper.readValue(getResult.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assertFalse(operationalMetadataResponse1.getUsers().isEmpty());
         assert (operationalMetadataResponse.getUsers().contains(PKA_USERID));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test10_addCagUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         Set<String> users = new HashSet<>();
         users.add(CAG_USERID);
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-cag-users").headers(headers)
                     .accept(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(users)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users")
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult getResult = getAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse1 = mapper.readValue(getResult.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assertFalse(operationalMetadataResponse1.getUsers().isEmpty());
         assert (operationalMetadataResponse.getUsers().contains(PKA_USERID));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test11_removePkaUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         Set<String> users = new HashSet<>();
         users.add(PKA_USERID);
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.delete(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users").headers(headers)
                     .accept(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(users)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users")
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult getResult = getAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse1 = mapper.readValue(getResult.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse1.getUsers().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test12_removeCagUsersByProtocol() {
      try {
         ObjectMapper mapper = new ObjectMapper();
         Set<String> users = new HashSet<>();
         users.add(CAG_USERID);
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.delete(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-cag-users").headers(headers)
                     .accept(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(users)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(MockMvcRequestBuilders.get(SERVICEPATH + "/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/assigned-pka-users")
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult getResult = getAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse1 = mapper.readValue(getResult.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse1.getUsers().isEmpty());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test13_updateMasterProtocolByProgram() {
      try {
         Map<String, String> inputs = new HashMap<>();
         inputs.put("modifiedBy", USERID2);
         ObjectMapper mapper = new ObjectMapper();
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/master")
                     .headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertNotNull(operationalMetadataResponse.getNodeId());
         assertNotNull(operationalMetadataResponse.getNodePath());
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYID));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // TODO : Tests for update program , protocol - by users without ALTER_PROGRAM privilege. Waiting for TEST USERS without the privilege.
   @Test
   public void test14_addAttachment() {
      MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
      requestContent.add("equipName", "ARD File Spec");
      ObjectMapper mapper = new ObjectMapper();

      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/attachments").file(file).headers(headers)
                     .params(requestContent))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertFalse(operationalMetadataResponse.getNodeId().isEmpty());
         assert (operationalMetadataResponse.getNodePath().contains(TESTPROGRAMCODE));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test14_attachments_with_protocol() {
      MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
      requestContent.add("equipName", "ARD File Spec");
      ObjectMapper mapper = new ObjectMapper();

      try {
         // Add attachment to protocol
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments")
                     .file(file).headers(headers).params(requestContent))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse operationalMetadataResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (operationalMetadataResponse.getResponse().equals(Response.OK));
         assertFalse(operationalMetadataResponse.getNodeId().isEmpty());
         assert (operationalMetadataResponse.getNodePath().contains(TESTPROGRAMCODE));
         assert (operationalMetadataResponse.getNodePath().contains(TESTSTUDYID));
         String attachmentId = operationalMetadataResponse.getNodeId();
         // get the attachment
         ResultActions getResult = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse getResponse = mapper.readValue(getResult.andReturn().getResponse().getContentAsString(), OperationalMetadataResponse.class);
         assert (getResponse.getResponse().equals(Response.OK));
         assert (getResult.andReturn().getResponse().getContentAsString().contains("ARD File Spec"));
         // Get Attachment version
         ResultActions getResultVesion = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/versions/1")
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse versionResponse = mapper.readValue(getResultVesion.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (versionResponse.getResponse().equals(Response.OK));
         // Trying to update the attachment
         resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_LCD_v3.0.xml");
         MockMultipartFile file1 = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result1 = mockMvc
               .perform(MockMvcRequestBuilders
                     .multipart("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).file(file1)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse operationalMetadataResponse1 = mapper.readValue(result1.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (operationalMetadataResponse1.getResponse().equals(Response.OK));
         // Get Attachment content
         ResultActions getContentResult = mockMvc.perform(MockMvcRequestBuilders
               .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/content")
               .headers(headers)).andExpect(MockMvcResultMatchers.status().isOk());
         byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent = new String(encoded, "UTF-8");
         assertTrue(getContentResult.andReturn().getResponse().getContentAsString().contains(fileContent));

         // Get attachment Content version
         ResultActions getContentResultVersion = mockMvc.perform(MockMvcRequestBuilders
               .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/versions/1/content")
               .headers(headers)).andExpect(MockMvcResultMatchers.status().isOk());
         resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         byte[] encoded_OlderVersion = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent_LCD = new String(encoded_OlderVersion, "UTF-8");
         assertTrue(getContentResultVersion.andReturn().getResponse().getContentAsString().contains(fileContent_LCD));
         // Getting attachment content for an invalid version
         mockMvc.perform(MockMvcRequestBuilders
               .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/versions/5/content")
               .headers(headers)).andExpect(MockMvcResultMatchers.status().isInternalServerError());
         // Trying to delete the attachment
         mockMvc.perform(MockMvcRequestBuilders
               .delete("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse operationalMetadataResponse2 = mapper.readValue(result1.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (operationalMetadataResponse2.getResponse().equals(Response.OK));
         // get the deleted attachment
         ResultActions getResult2 = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         assert (getResult2.andReturn().getResponse().getContentAsString().contains("InvalidAttachmentException"));
         // get the content for deleted attachment
         ResultActions getContentResult2 = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/content")
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         assert (getContentResult2.andReturn().getResponse().getContentAsString().contains("InvalidAttachmentException"));
         // get the version content for deleted attachment
         ResultActions getContentVersionResult2 = mockMvc
               .perform(MockMvcRequestBuilders.get(
                     "/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/versions/1/content")
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         assert (getContentVersionResult2.andReturn().getResponse().getContentAsString().contains("InvalidAttachmentException"));

         // Restore attachment
         mockMvc
               .perform(MockMvcRequestBuilders
                     .put("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse operationalMetadataResponse3 = mapper.readValue(result1.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (operationalMetadataResponse3.getResponse().equals(Response.OK));
         // Get the attachment after restoring
         ResultActions getAfterRestore = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         OperationalMetadataResponse responseAfterRestore = mapper.readValue(getAfterRestore.andReturn().getResponse().getContentAsString(),
               OperationalMetadataResponse.class);
         assert (responseAfterRestore.getResponse().equals(Response.OK));
         // Getting the version History
         ResultActions getVersionHistory = mockMvc
               .perform(MockMvcRequestBuilders
                     .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + attachmentId + "/versions")
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         AttachmentVersionHistoryResponse getResponseVersionHistory = mapper.readValue(getVersionHistory.andReturn().getResponse().getContentAsString(),
               AttachmentVersionHistoryResponse.class);
         assert (getResponseVersionHistory.getResponse().equals(Response.OK));
         assert (getResponseVersionHistory.getVersionHistory().length != 0);
         assert (getResponseVersionHistory.getVersionHistory()[0].get("attachment").get("name").asText().equals("ARD File Spec"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test14_addAttachmentException() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         mockMvc.perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/attachments").file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test15_updateAttachmentException() {
      Map<String, String> inputs = new HashMap<>();
      inputs.put("equipName", "ARD File Spec");
      ObjectMapper mapper = new ObjectMapper();
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         mockMvc
               .perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/attachments/12312312").file(file)
                     .headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test16_getAttachmentContentException() {
      try {
         ResultActions getResult2 = mockMvc.perform(MockMvcRequestBuilders
               .get("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID + "/attachments/" + "123123/content").headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         assert (getResult2.andReturn().getResponse().getContentAsString().contains("NodeNotFound"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
