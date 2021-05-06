package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.business.api.dataframe.Dataset;
import com.pfizer.equip.services.business.api.input.KeyValuePairInput;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.input.report.ReportJobInput;
import com.pfizer.equip.services.responses.ReportResponse;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReportControllerTest {
   // The user atlamr-ncadev1 has been assigned to a group EQUIP-NCA-SystemAdmin and has the role SYSADMIN.
   public final static String USERID = "atlamr-ncadev1";
   public final static String TESTPROGRAMCODE = "X919";
   public final static String SYSTEMID = "nca";
   public final static String TESTSTUDYID = "X9191001";
   public final static String OPMETAURL = "/" + SYSTEMID + "/opmeta/nodes/programs";
   public final static String REPORTSERVICESURL = "/" + SYSTEMID + "/reports";
   public final static String DATAFRAMESERVICEURL = "http://equip-services-dev.pfizer.com:8080/EQuIPDataframeService/";
   public final static String REPORTFOLDER = "report-artifacts";
   public final static String PATHTOREPORTFOLDER = "/library/global/" + REPORTFOLDER;
   public static final String TESTSTUDYID2 = "X9191002";
   public final static String LIBRARYBASE = "/" + SYSTEMID + "/librarian";
   // TODO:Privilege check with Test Users are not done yet. Waiting for Test users.

   private MockMvc mockMvc;
   @Autowired
   private RepositoryService repositoryService;
   @Autowired
   private ResourceLoader resourceLoader;
   @Autowired
   private DataframeService dataframeService;

   @Autowired
   private WebApplicationContext webApplicationContext;
   HttpHeaders headers = new HttpHeaders();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("IAMPFIZERUSERCN", USERID);
      headers.set("Content-Type", "application/json");

   }

   public List<String> setUpDataFrames() {
      List<String> dataframeList = new LinkedList<>();
      // 0. Clean up from Previous Run
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
         // Map<String, Object> protocolinputs1 = new HashMap<>();
         // protocolinputs.put("opmeta:studyId", TESTSTUDYID2);
         // protocolinputs.put("opmeta:source", "EQUIP");
         // mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + TESTPROGRAMCODE + "/protocols").headers(headers)
         // .content(mapper.writeValueAsString(protocolinputs1)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
         // TODO: Check when a study ID will not be present in the Protocol
         // 3. Create Dataframe
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
         String dataframe2 = dataframeService.addDataframe(USERID, dataframe);// uncommited Dataframe
         String dataframe3 = dataframeService.addDataframe(USERID, dataframe);// Deleted Dataframe
         dataframeService.deleteEntity(USERID, dataframe3);
         // TODO:Obsolete and Version Superseceding To be tested - warning is logged in console but no change in response.
         // Creating an Obsolete Dataframe for Testing
         // dataframe.setObsoleteFlag(true);
         // String dataframe4 = dataframeService.addDataframe(USERID, dataframe);// Obsolete Dataframe
         // // Creating an version superseded dataframe
         // dataframe.setObsoleteFlag(false);
         // dataframe.setVersionSuperSeded(true);
         // String dataframe5 = dataframeService.addDataframe(USERID, dataframe);// Version Superseded Dataframe
         dataframeList.add(dataframeID);
         dataframeList.add(dataframe2);
         dataframeList.add(dataframe3);
         // dataframeList.add(dataframe4);
         // dataframeList.add(dataframe5);
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
         dataframeService.addDatasetContent(USERID, datasetID, Files.readAllBytes(Paths.get(resource.getURI())));

      } catch (JsonProcessingException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return dataframeList;

   }

   public void cleanUpReportArtifacts() {
      // Delete existing Artifacts - Clean up from previous Run
      try {
         // Delete the program (EQUIP) created.
         repositoryService.deleteNode(PATHTOREPORTFOLDER + "/test-reporting-item-template.R");
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {
         repositoryService.deleteNode(PATHTOREPORTFOLDER + "/test-report-template.rmd");
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public String addReportTemplate() {
      String artifactId = null;
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/reportInputFiles/test-report-template.rmd");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
         inputs.add("primaryType", "equipLibrary:reportTemplate");
         inputs.add("equipName", "Report template Test");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact/" + PATHTOREPORTFOLDER).file(file).params(inputs).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         artifactId = libraryArtifactResponse.getArtifactId();
         assertTrue(artifactId != null);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return artifactId;
   }

   public String addReportingItem() {
      String artifactId = null;
      // Add the reporting Item
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/reportInputFiles/test-reporting-item-template.R");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
         inputs.add("primaryType", "equipLibrary:reportingItemTemplate");
         inputs.add("equipName", "Reporting item template Test");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + PATHTOREPORTFOLDER).file(file).params(inputs).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         artifactId = libraryArtifactResponse.getArtifactId();
         assertTrue(artifactId != null);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return artifactId;
   }

   @Test
   public void test01() {
      // 1. Create the Dataframes.
      List<String> dataFrameIDList = setUpDataFrames();
      // 2. Set up the required report artifacts
      cleanUpReportArtifacts();
      String reportingItemTemplateId = addReportingItem();
      String reportTemplateId = addReportTemplate();
      // 3. Test begins
      // Create Reporting Item from Uncommitted Dataframe
      generateReportingItemFromTemplateException_Uncommited(dataFrameIDList.get(1), reportingItemTemplateId);
      // Create Reporting Item from Deleted Dataframe
      generateReportingItemFromTemplateException_DataframeDeleted(dataFrameIDList.get(2), reportingItemTemplateId);
      // Create Reporting Item
      String reportingItemId = generateReportingItemFromTemplate(dataFrameIDList.get(0), reportingItemTemplateId);
      // Generate report from uncommitted Reporting Item
      generateReportException(reportingItemId, reportTemplateId);
      // Commit the Reporting Item
      commitReportingItem(reportingItemId);
      // Generating Report
      String reportId = generateReport(reportingItemId, reportTemplateId);
      // Get Report MetaData
      Long dataSize = getReportOutput(reportId, reportingItemId);
      // Get Report Content
      getReportOutputContent(reportId, reportingItemId, dataSize.toString());
      // Delete Reporting Item
      deleteReportingItem(reportingItemId);
      generateReportException_DeletedReportingItem(reportingItemId, reportTemplateId);
      generateReportException_InValidReportingItem(reportingItemId, reportTemplateId);
      generateReportException_NoReportingItem(reportingItemId, reportTemplateId);

   }

   public void generateReportingItemFromTemplateException_DataframeDeleted(String dataframeId, String reportingItemTemplateId) {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ReportJobInput reportJobInput = new ReportJobInput();
         reportJobInput.setDataframeIds(Arrays.asList(dataframeId));
         Set<KeyValuePairInput> parameters = new HashSet<>();
         KeyValuePairInput kvp = new KeyValuePairInput();
         kvp.setKey("test");
         kvp.setValue(Arrays.asList("val"));
         kvp.setType("String");
         parameters.add(kvp);
         reportJobInput.setParameters(parameters);
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/template/" + reportingItemTemplateId)
               .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).headers(headers).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("deleted"));
         assert (result.getResponse().getContentAsString().contains("InvalidReportingItem"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void generateReportingItemFromTemplateException_Uncommited(String dataframeId, String reportingItemTemplateId) {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ReportJobInput reportJobInput = new ReportJobInput();
         reportJobInput.setDataframeIds(Arrays.asList(dataframeId));
         Set<KeyValuePairInput> parameters = new HashSet<>();
         KeyValuePairInput kvp = new KeyValuePairInput();
         kvp.setKey("test");
         kvp.setValue(Arrays.asList("val"));
         kvp.setType("String");
         parameters.add(kvp);
         reportJobInput.setParameters(parameters);
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/template/" + reportingItemTemplateId)
               .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).headers(headers).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("uncommitted"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public String generateReportingItemFromTemplate(String dataframeId, String reportingItemTemplateId) {
      try {
         ObjectMapper mapper = new ObjectMapper();
         ReportJobInput reportJobInput = new ReportJobInput();
         reportJobInput.setDataframeIds(Arrays.asList(dataframeId));
         Set<KeyValuePairInput> parameters = new HashSet<>();
         KeyValuePairInput kvp = new KeyValuePairInput();
         kvp.setKey("test");
         kvp.setValue(Arrays.asList("val"));
         kvp.setType("String");
         parameters.add(kvp);
         reportJobInput.setParameters(parameters);
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/template/" + reportingItemTemplateId)
               .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).headers(headers).accept(MediaType.APPLICATION_JSON));
         MvcResult result = resultAction.andReturn();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assert (reportResponse.getResponse().equals(Response.OK));
         assertNotNull(reportResponse.getReportingItemIds());
         List<String> reportingItemList = new ArrayList<>();
         reportingItemList.addAll(reportResponse.getReportingItemIds());
         return reportingItemList.get(0);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public void commitReportingItem(String reportingItemId) {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.put(REPORTSERVICESURL + "/output/" + reportingItemId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assert (reportResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test03_commitReportingItemException_InvalidArtifactId() {
      try {
         mockMvc.perform(MockMvcRequestBuilders.put(REPORTSERVICESURL + "/output/" + "12312").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void deleteReportingItem(String reportingItemId) {
      // String reportingItemId = "4a442655-067b-4f93-82bf-d693d8a4135a";
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.delete(REPORTSERVICESURL + "/output/" + reportingItemId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assert (reportResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test2_generateReportingItemFromNode() {
      String protocolId = repositoryService.getIdByPath("/Programs/" + TESTPROGRAMCODE + "/Protocols/" + TESTSTUDYID);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/node/" + protocolId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assert (reportResponse.getResponse().equals(Response.OK));
         // assert (reportResponse.getReportingItemIds().contains(dataframeId));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test2_generateReportingItemFromNodeException_NotOfTypeProtocol() {
      String programId = repositoryService.getIdByPath("/Programs/" + TESTPROGRAMCODE);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/node/" + programId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("Report service can only convert nodes of type protocol"));
         // assert (reportResponse.getReportingItemIds().contains(dataframeId));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   // TODO : When will STUDY ID be null ?
   // @Test
   // public void test2_generateReportingItemFromNodeException_StudyIdNull() {
   // String protocolId = repositoryService.getIdByPath("/Programs/" + TESTPROGRAMCODE + "/Protocols/" + TESTPROGRAMCODE);
   // try {
   // ResultActions resultAction = mockMvc
   // .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/reporting-item/node/" + protocolId).headers(headers).accept(MediaType.APPLICATION_JSON))
   // .andExpect(MockMvcResultMatchers.status().isInternalServerError());
   // MvcResult result = resultAction.andReturn();
   // assert (result.getResponse().getContentAsString().contains("Report service can only convert nodes of type protocol"));
   // // assert (reportResponse.getReportingItemIds().contains(dataframeId));
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   //
   // }

   @Test
   public void test3_getReportOutputByProtocol() {
      try {
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.get(REPORTSERVICESURL + "/output/programs/" + TESTPROGRAMCODE + "/protocols/" + TESTSTUDYID)
               .headers(headers).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assertFalse(reportResponse.getReportOutputs().isEmpty());
         assert (result.getResponse().getContentAsString().contains(TESTPROGRAMCODE));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public Long getReportOutput(String reportId, String reportItemId) {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(REPORTSERVICESURL + "/output/" + reportId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assertNotNull(reportResponse.getReportOutput());
         assert (result.getResponse().getContentAsString().contains(TESTPROGRAMCODE));
         assert (reportResponse.getReportOutput().getDataframeIds().contains(reportItemId));
         assertNotNull(reportResponse.getReportOutput().getDataset().getDataSize());
         Long dataSize = reportResponse.getReportOutput().getDataset().getDataSize();
         return dataSize;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;

   }

   public void getReportOutputContent(String reportId, String reportItemId, String dataSize) {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(REPORTSERVICESURL + "/output/" + reportId + "/content").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         Integer length = result.getResponse().getContentAsByteArray().length;
         assert (length.toString().equals(dataSize));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public String generateReport(String reportingItemId, String reportTemplateId) {
      ObjectMapper mapper = new ObjectMapper();
      ReportJobInput reportJobInput = new ReportJobInput();
      reportJobInput.setDataframeIds(Arrays.asList(reportingItemId));
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput kvp1 = new KeyValuePairInput();
      kvp1.setKey("TEST");
      kvp1.setValue(Arrays.asList("test"));
      kvp1.setType("String");
      parameters.add(kvp1);
      reportJobInput.setParameters(parameters);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/report/template/" + reportTemplateId).headers(headers)
                     .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         MvcResult result = resultAction.andReturn();
         ReportResponse reportResponse = mapper.readValue(result.getResponse().getContentAsString(), ReportResponse.class);
         assert (reportResponse.getResponse().equals(Response.OK));
         assertNotNull(reportResponse.getReportId());
         return reportResponse.getReportId();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public void generateReportException(String reportingItemId, String reportTemplateId) {
      ObjectMapper mapper = new ObjectMapper();
      ReportJobInput reportJobInput = new ReportJobInput();
      reportJobInput.setDataframeIds(Arrays.asList(reportingItemId));
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput kvp1 = new KeyValuePairInput();
      kvp1.setKey("TEST");
      kvp1.setValue(Arrays.asList("test"));
      kvp1.setType("String");
      parameters.add(kvp1);
      reportJobInput.setParameters(parameters);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/report/template/" + reportTemplateId).headers(headers)
                     .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("InvalidReportingItemException"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void generateReportException_DeletedReportingItem(String reportingItemId, String reportTemplateId) {
      ObjectMapper mapper = new ObjectMapper();
      ReportJobInput reportJobInput = new ReportJobInput();
      reportJobInput.setDataframeIds(Arrays.asList(reportingItemId));
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput kvp1 = new KeyValuePairInput();
      kvp1.setKey("TEST");
      kvp1.setValue(Arrays.asList("test"));
      kvp1.setType("String");
      parameters.add(kvp1);
      reportJobInput.setParameters(parameters);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/report/template/" + reportTemplateId).headers(headers)
                     .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("deleted"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void generateReportException_InValidReportingItem(String reportingItemId, String reportTemplateId) {
      ObjectMapper mapper = new ObjectMapper();
      ReportJobInput reportJobInput = new ReportJobInput();
      List<String> dataframeIds = new ArrayList<>();
      dataframeIds.add(reportingItemId);
      dataframeIds.add("123123123");
      reportJobInput.setDataframeIds(dataframeIds);
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput kvp1 = new KeyValuePairInput();
      kvp1.setKey("TEST");
      kvp1.setValue(Arrays.asList("test"));
      kvp1.setType("String");
      parameters.add(kvp1);
      reportJobInput.setParameters(parameters);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/report/template/" + reportTemplateId).headers(headers)
                     .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains("ReportingItemNotFoundException"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void generateReportException_NoReportingItem(String reportingItemId, String reportTemplateId) {
      ObjectMapper mapper = new ObjectMapper();
      ReportJobInput reportJobInput = new ReportJobInput();
      Set<KeyValuePairInput> parameters = new HashSet<>();
      KeyValuePairInput kvp1 = new KeyValuePairInput();
      kvp1.setKey("TEST");
      kvp1.setValue(Arrays.asList("test"));
      kvp1.setType("String");
      parameters.add(kvp1);
      reportJobInput.setParameters(parameters);
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(REPORTSERVICESURL + "/report/template/" + reportTemplateId).headers(headers)
                     .content(mapper.writeValueAsString(reportJobInput)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
         MvcResult result = resultAction.andReturn();
         assert (result.getResponse().getContentAsString().contains(Response.FAILED.toString()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
