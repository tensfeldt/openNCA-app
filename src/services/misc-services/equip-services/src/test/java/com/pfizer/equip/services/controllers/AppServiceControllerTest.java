package com.pfizer.equip.services.controllers;

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
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.business.api.dataframe.Dataset;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.responses.UserAuthResponse;
import com.pfizer.equip.services.responses.dataframe.DataloadResponse;
import com.pfizer.equip.services.responses.validation.CreateUpdateDeleteSpecification;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.user.UserInfo;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppServiceControllerTest {
   // This user has the role of SYSADMIN
   public final static String USERID = "atlamr-ncadev1";
   public final static String INVALIDUSERID = "test123";
   public final static String TESTFOLDER = "/library/global/test/";
   public final static String TESTPROGRAMCODE = "X911";
   public final static String SYSTEMID = "nca";
   public final static String TESTSTUDYID = "X9111001";
   public final static String OPMETAURL = "/" + SYSTEMID + "/opmeta/nodes/programs";

   private MockMvc mockMvc;

   @Autowired
   private WebApplicationContext webApplicationContext;

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private DataframeService dataframeService;

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   private ResourceLoader resourceLoader;

   HttpHeaders headers = new HttpHeaders();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("IAMPFIZERUSERCN", USERID);

   }

   public void setUp() {
      // Delete Program
      try {
         // Delete the program (EQUIP) created.
         repositoryService.deleteNode("/Programs/" + TESTPROGRAMCODE);
      } catch (Exception e) {
         e.printStackTrace();
      }
      ObjectMapper mapper = new ObjectMapper();
      try {
         // 1. Create Program
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", TESTPROGRAMCODE);
         inputs.put("opmeta:source", "EQUIP");
         mockMvc.perform(MockMvcRequestBuilders.post(OPMETAURL).headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         // 2. Create Protocol
         Map<String, Object> protocolinputs = new HashMap<>();
         protocolinputs.put("opmeta:studyId", TESTSTUDYID);
         protocolinputs.put("opmeta:source", "EQUIP");
         mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols").headers(headers)
               .content(mapper.writeValueAsString(protocolinputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

         // Add specification LCD v1.1
         try {
            librarianService.addFolder("library/global/testvalidationlibraryfiles");
         } catch (Exception e) {
            e.printStackTrace();
         }
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_LCD_v1.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         mockMvc.perform(
               MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/specification").file(file).param("specificationType", "LCD").param("specificationVersion", "v1.1").headers(headers));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_userAuth() {

      try {

         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/api/user/auth").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         UserAuthResponse userAuthResponse = mapper.readValue(result.getResponse().getContentAsString(), UserAuthResponse.class);
         UserInfo userInfo = userAuthResponse.getUserInfo();
         assertTrue(userInfo.getFirstName() != null);
         assertTrue(userInfo.getLastName() != null);
         assertTrue(userInfo.getEmailAddress() != null);
         assertTrue(userAuthResponse.getPrivileges().size() > 0);
         assertTrue(userAuthResponse.getRoles().size() > 0);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_userAuthException() {

      try {
         headers.set("IAMPFIZERUSERCN", INVALIDUSERID);
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/api/user/auth").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains(Response.FAILED.getValue()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_createDataloadTest() {

      // Creating a new program and protocol first
      setUp();
      try {
         ObjectMapper mapper = new ObjectMapper();
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<>();
         inputs.add("description", "This is a test data load from Postman.");
         inputs.add("specificationType", "LCD");
         inputs.add("specificationVersion", "v1.1");
         inputs.add("numberRows", "1");
         inputs.add("mappingInformationList","[]");
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/A6301094_PLASMA_ANTIXA_29JUN2018_LCD.csv");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/api/dataload/" + TESTPROGRAMCODE + "/" + TESTSTUDYID).file(file).headers(headers).params(inputs))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         DataloadResponse dataLoadResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), DataloadResponse.class);
         assert (dataLoadResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test02_createDataloadTestException() {

      try {
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
         inputs.add("specificationType", "LCD");
         inputs.add("numberRows", "1");
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/A6301094_PLASMA_ANTIXA_29JUN2018_LCD.csv");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.multipart("/" + SYSTEMID + "/api/dataload/" + TESTPROGRAMCODE + "/" + TESTSTUDYID).file(file).headers(headers).params(inputs))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         assert (result.andReturn().getResponse().getContentAsString().contains("Specification type and version are required to create data loads"));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }
   // TODO: Check for Authorization
   // @Test
   // public void test02_createDataloadTestException_UnAuthorized() {
   //
   // try {
   // MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
   // inputs.add("specificationType", "LCD");
   // inputs.add("numberRows", "1");
   // Resource resource = resourceLoader.getResource("classpath:/inputFiles/A6301094_PLASMA_ANTIXA_29JUN2018_LCD.csv");
   // MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
   // ResultActions result = mockMvc
   // .perform(MockMvcRequestBuilders.multipart("/api/dataload/" + TESTPROGRAMCODE + "/" + TESTSTUDYID).file(file).headers(headers).params(inputs))
   // .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   //
   // assert (result.andReturn().getResponse().getContentAsString().contains("client error"));
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   //
   // }

   @Test
   public void test03_getDataset() {

      // Creating a dataframe and complex data
      String complexDataID = setUp2();
      try {
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<>();
         inputs.add("maxvalues", "1");
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/api/dataset/" + complexDataID).headers(headers).params(inputs))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         assert (result.andReturn().getResponse().getContentAsString().contains("cars"));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test03_getDatasetException() {

      // Creating a dataframe and complex data
      try {
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<>();
         inputs.add("maxvalues", "AB");
         mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/api/dataset/" + "qweqew123123").headers(headers).params(inputs))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private String setUp2() {

      try {
         // 1. Create Dataframe
         Dataframe dataframe = new Dataframe();
         dataframe.setDataframeType("Reporting Item");
         dataframe.setPromotionStatus("PromotionStatus");
         dataframe.setDataBlindingStatus(StudyBlindingStatus.BLINDED.getValue());
         dataframe.setRestrictionStatus("Not Restricted");
         dataframe.setStudyBlindingStatus(StudyBlindingStatus.BLINDED.getValue());
         dataframe.setDataStatus("Draft");

         Set<String> studyId = new HashSet<>();
         studyId.add(TESTPROGRAMCODE + ":" + TESTSTUDYID);
         dataframe.setProgramStudyIdsConcatenated(studyId);
         String dataframeID = dataframeService.addDataframe(USERID, dataframe);// perfect Dataframe
         dataframeService.commitEntity(USERID, dataframeID);
         // Create DataSet
         Dataset dataset = new Dataset();
         dataset.setData("test");
         dataset.setStdErr("string");
         dataset.setStdIn("string");
         dataset.setStdOut("string");
         dataset.setType("dataset");
         String datasetID = dataframeService.addDataset(USERID, dataframeID, dataset);
         // Upload Dataset content
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/reportInputFiles/test-reporting-item-dataset.csv");
         String contentId = dataframeService.addDatasetContent(USERID, datasetID, Files.readAllBytes(Paths.get(resource.getURI())));
         return contentId;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }
}
