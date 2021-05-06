package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.input.validation.FieldLevelLogReport;
import com.pfizer.equip.services.input.validation.FileLevelLogReport;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.UniqueSetLevelLogReport;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.responses.validation.CreateUpdateDeleteSpecification;
import com.pfizer.equip.services.responses.validation.GetSpecificationsResponse;
import com.pfizer.equip.services.responses.validation.ValidationResponse;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValidationControllerTest {
   // This user has the role of SYSADMIN
   public final static String USERID = "atlamr-ncadev1";
   public final static String PROGRAMID = "X001";
   public final static String PROTOCOLID = "X0011001";
   public final static String PROGRAMSFOLDER = "Programs";
   public final static String PROTOCOLSFOLDER = "Protocols";
   public final static String SYSTEMID = "nca";
   public final static String SPECIFICATIONBASE = "/" + SYSTEMID + "/specification";
   public final static String CROSSFILEVALIDATIONPATH = SPECIFICATIONBASE + "/crossfilevalidate/";
   public final static String VALIDATIONPATH = SPECIFICATIONBASE + "/validate/";
   public final static String OPMETAURL = "/" + SYSTEMID + "/opmeta/nodes/programs";
   public final static String MODESHAPEFILESPECPATH = SYSTEMID + "/library/global/testvalidationlibraryfiles/"; // expect leading slash in concat

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   private ResourceLoader resourceLoader;

   @Autowired
   private RepositoryService repositoryService;

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

   public void setup_cleanupSpecificationsFromPreviousRun() {
      try {
         try {
            repositoryService.deleteNode("/library/global/testvalidationlibraryfiles");
         } catch (NullPointerException npe) {
            // Do nothing - Happens when first time testvalidationlibraryfiles does not exist
         }
         // mockMvc.perform(MockMvcRequestBuilders.get("/specifications").headers(headers).accept(MediaType.APPLICATION_JSON))
         // .andExpect(MockMvcResultMatchers.status().isOk());
         // TODO:Get these paths from the properties file
         librarianService.addFolder("library/global/testvalidationlibraryfiles");

         // Setup - Create Programs and Protocols that will be used for file validation below
         createProgramsProtocols();

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void createProgramsProtocols() throws Exception {
      try {
         // Add Programs and Protocols
         ObjectMapper mapper = new ObjectMapper();
         // 1. Create Program
         Map<String, String> inputs = new HashMap<>();
         inputs.put("opmeta:programCode", PROGRAMID);
         inputs.put("opmeta:source", "EQUIP");
         mockMvc.perform(MockMvcRequestBuilders.post(OPMETAURL).headers(headers).content(mapper.writeValueAsString(inputs)).contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON));
         // 2. Create Protocol
         Map<String, Object> protocolinputs = new HashMap<>();
         protocolinputs.put("opmeta:studyId", PROTOCOLID);
         protocolinputs.put("opmeta:source", "EQUIP");
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + PROGRAMID + "/protocols").headers(headers)
               .content(mapper.writeValueAsString(protocolinputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   @Test
   public void test01_AddSpecification() {
      // do some cleanup
      setup_cleanupSpecificationsFromPreviousRun();
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "ARD").param("specificationVersion", "3.1")
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         CreateUpdateDeleteSpecification createUpdateDeleteSpecification = mapper.readValue(result.andReturn().getResponse().getContentAsString(),
               CreateUpdateDeleteSpecification.class);
         assert (createUpdateDeleteSpecification.getResponse().equals(Response.OK));
         assertNotNull(createUpdateDeleteSpecification.getId());
         assertNotNull(createUpdateDeleteSpecification.getPath());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_AddSpecificationException() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/FieldDataValidation_ARD_v3.1.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
         mockMvc.perform(
               MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "ARD").param("specificationVersion", "3.1").headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_GetSpecifications() {
      try {
         ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/specifications").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         GetSpecificationsResponse getSpecificationResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(),
               GetSpecificationsResponse.class);
         assertFalse(getSpecificationResponse.getSpecifications().isEmpty());
         assert (getSpecificationResponse.getSpecifications().get(0).getPath().contains("FieldDataValidation_ARD_v3.1.xml"));
         assert (getSpecificationResponse.getSpecifications().get(0).getVersion().equals("3.1"));
         assert (getSpecificationResponse.getSpecifications().get(0).getType().equals("ARD"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecification() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_17oct2014.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultActions = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ValidationResponse.class);
         assert (validationResponse.getResponse().equals(Response.OK));
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getTester().equals(USERID));
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getFileName().equals(resource.getFilename()));
         assertFalse(validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getHostName().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getValidationStatus().equals(ValidationStatusTypes.ERROR.getStatusType()));
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(1).getName().equalsIgnoreCase("DRGDATE")); // Date pattern check failed
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(1).getMessage()
               .equalsIgnoreCase(FileValidationMessages.EQUIP_FV_ERR04.getErrMessageCode()));
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(0).getName().equalsIgnoreCase("DOSETIM"));// Time pattern check failed
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(0).getMessage()
               .equalsIgnoreCase(FileValidationMessages.EQUIP_FV_ERR18.getErrMessageCode()));
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(5).getName().equalsIgnoreCase("AGEDERU"));// Allowed values check
         // failed
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(5).getMessage()
               .startsWith(FileValidationMessages.EQUIP_FV_ERR03.getErrMessageCode()));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecification_ColumnAliasCheck() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_28oct2014.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultActions = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ValidationResponse.class);
         assert (validationResponse.getResponse().equals(Response.OK));
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getTester().equals(USERID));
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getFileName().equals(resource.getFilename()));
         assertFalse(validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getHostName().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getValidationStatus().equals(ValidationStatusTypes.ERROR.getStatusType()));
         assert (validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList().get(0).getName().equalsIgnoreCase("DOSE1"));// Column Alias Check
         assert (validationResponse.getFileValidationLog().get(0).getFieldSetLevelLogReportList().get(0).getMessage()
               .equalsIgnoreCase(FileValidationMessages.EQUIP_FV_ERR32.getErrMessageCode()));// Conditions Check
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_DelimiterError_DelimitorNotInList() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2018.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", "$");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getDelimiterError().contains(FileValidationMessages.EQUIP_FV_ERR11.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_DelimiterError_DifferentDelimitorUsedInFile() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2012.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ";");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getDelimiterError().equals(FileValidationMessages.EQUIP_FV_ERR21.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_EmptyCSV() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2018.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains(FileValidationMessages.EQUIP_FV_ERR29.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_FileExtensionMismatchTest() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2018.cav");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains(FileValidationMessages.EQUIP_FV_ERR27.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_FileNameError() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ArD_3151A2_PLASMA_DVS333_14jun20.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultActions = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ValidationResponse.class);
         assert (validationResponse.getResponse().equals(Response.OK));
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getFileNameError().getMessage().contains(FileValidationMessages.EQUIP_FV_ERR07.getErrMessageCode()));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_MissingHeaders() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_01jun2018.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains(FileValidationMessages.EQUIP_FV_ERR23.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_ValidateFileToSpecificationException_SpecificationNotFound() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2012.csv");
         MockMultipartFile file = new MockMultipartFile("file", "ARD_33151A2_PLASMA_DVS333_14jun2012.csv", "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.2.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultActions = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getResponse().equals(Response.FAILED));
         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains("Specification Not Found"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_ValidateFileToSpecification_FileLevelValidations_ARD() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2012.csv");
         MockMultipartFile file = new MockMultipartFile("file", "ARD_33151A2_PLASMA_DVS333_14jun2012.csv", "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         // File Level Validations
         FileLevelLogReport fileLevelLogReport = validationResponse.getFileValidationLog().get(0).getFileLevelLogReport();
         assert (fileLevelLogReport.getExtraneousFieldList().get(0).getName().equalsIgnoreCase("Extra1"));// Extraneous column Validation
         assert (fileLevelLogReport.getUnorderedFieldList().get(0).getName().equalsIgnoreCase("SUBJID"));// Column Ordering validation
         assert (fileLevelLogReport.getEmptyLinesList().get(0).getRowIndex() == 3);// Empty Lines validation
         assert (fileLevelLogReport.getCaseMismatchFieldList().get(0).getName().substring(1, 9).equalsIgnoreCase("COLLDATE"));// Case mismatch validation
         assert (fileLevelLogReport.getLeadingTrailingSpacesColumnsList().get(0).getName().trim().equalsIgnoreCase("PKSMND"));// Leading Space Validation
         assert (fileLevelLogReport.getMissingMandatoryFieldList().get(0).getName().equals("TRTCD"));// Mandatory Column Missing Validation
         // Duplicate Record
         assert (validationResponse.getFileValidationLog().get(0).getDuplicateRecordsLevelLogReportList().isEmpty());// Conditions Check
         // Field Set Validation
         assertFalse(validationResponse.getFileValidationLog().get(0).getFieldSetLevelLogReportList().isEmpty());
         // File Level Rules
         assert (fileLevelLogReport.getMandatoryIfAvailableList().get(0).getName().equalsIgnoreCase("UDSDEID"));// Mandatory If Available Column
         assert (fileLevelLogReport.getUniqueColumnList().get(0).getName().equalsIgnoreCase("PKUSMID"));// Unique Column File Level Rule
         // Unique Sets Validation
         List<UniqueSetLevelLogReport> uniqueSetLevelLogReportList = validationResponse.getFileValidationLog().get(0).getUniqueSetLevelLogReportList();
         Map<String, UniqueSetLevelLogReport> uniqueResultMap = uniqueSetLevelLogReportList.stream()
               .collect(Collectors.toMap(UniqueSetLevelLogReport::getName, Function.identity()));
         assert (uniqueResultMap.containsKey("HTUNI"));
         assert (uniqueResultMap.get("HTUNI").getScope().equals("SUBJID"));
         assert (uniqueResultMap.containsKey("PROGRAM"));
         assert (uniqueResultMap.get("PROGRAM").getScope().equals("FILE"));// Unique with FILE SCOPE
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_UpdateSpecification() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_ARD_v3.0.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", "FieldDataValidation_ARD_v3.0.xml", "text/plain", resource.getInputStream());
         MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(SPECIFICATIONBASE);
         requestBuilder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
               request.setMethod("PUT");
               return request;
            }
         });
         ResultActions updateResult = mockMvc
               .perform(requestBuilder.file(file).param("fileName", "FieldDataValidation_ARD_v3.1.xml/").param("specificationVersion", "3.2").headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = updateResult.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         CreateUpdateDeleteSpecification validationResponse = mapper.readValue(result.getResponse().getContentAsString(), CreateUpdateDeleteSpecification.class);
         ResultActions resultActionGetContent = mockMvc
               .perform(MockMvcRequestBuilders.get("/" + SYSTEMID + "/librarian/artifact/content/current/id/{id}", validationResponse.getId()).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
         MvcResult resultGetContent = resultActionGetContent.andReturn();
         // Check File content

         byte[] encoded = Files.readAllBytes(Paths.get("classpath:/config/filevalidatorlibrary/FieldDataValidation_ARD_v3.0.xml"));
         String fileContent = new String(encoded, "UTF-8");
         @SuppressWarnings("unchecked")
         ResponseEntity<byte[]> response = mapper.readValue(resultGetContent.getResponse().getContentAsString(), ResponseEntity.class);
         assertTrue(response.getBody().toString().contains(fileContent));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_ValidateFileToSpecification_FieldValidation_ARD() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/ARD_33151A2_PLASMA_DVS333_14jun2013.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_ARD_v3.1.xml/");
         requestContent.add("specificationType", "ARD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         // Field Level Validations
         List<FieldLevelLogReport> fieldLevelLogReportList = validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList();
         Map<String, FieldLevelLogReport> fieldResultMap = fieldLevelLogReportList.stream().collect(Collectors.toMap(FieldLevelLogReport::getName, Function.identity()));
         assert (fieldResultMap.containsKey("PKSAMQA"));// IsNULL Validation
         assert (fieldResultMap.containsKey("SUBJID"));// Length Validation
         assert (fieldResultMap.containsKey("TREATSEQ"));// Pattern violation validation
         assert (fieldResultMap.containsKey("VISITU"));// Allowed Values Validation
         // Specification Version
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getArtifactVersion().equalsIgnoreCase("v3.1"));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test07_ValidateFileToSpecification_FieldValidation_LCD() {
      try {

         // Add the LCD File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_LCD_v1.1.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            mockMvc
                  .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "LCD").param("specificationVersion", "1.1")
                        .headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/A6301094_PLASMA_ANTIXA_29JUN2018_LCD.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.1");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_LCD_v1.1.xml/");
         requestContent.add("specificationType", "LCD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         // Field Level Validations
         List<FieldLevelLogReport> fieldLevelLogReportList = validationResponse.getFileValidationLog().get(0).getFieldLevelLogReportList();
         Map<String, FieldLevelLogReport> fieldResultMap = fieldLevelLogReportList.stream().collect(Collectors.toMap(FieldLevelLogReport::getName, Function.identity()));
         assert (fieldResultMap.containsKey("SITEID"));// MAX length Validation
         assert (fieldResultMap.containsKey("ACTTRT"));// Leading Space Validation
         assert (fieldResultMap.containsKey("AGEDER"));// Number Range validation
         assert (fieldResultMap.containsKey("AGEDERU"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test08_ValidateFileToSpecification_FieldValidation_LCD3_DoubleQuotesInFile() {
      try {

         // Add the LCD3.0 File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_LCD_v3.0.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            mockMvc
                  .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "LCD").param("specificationVersion", "3.0")
                        .headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }

         Resource resource = resourceLoader.getResource("classpath:/inputFiles/3151A2_PLASMA_DVS233_27APR2012_LCD.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_LCD_v3.0.xml/");
         requestContent.add("specificationType", "LCD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         // System should be able to process quoted files.
         assertNull(validationResponse.getFileValidationLog().get(0).getErrorOrException());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_ValidateFileToSpecificationException_FPKDEF_CheckNonASCIICharacterInHeader() {
      try {

         // Add the Final PK Def File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_FinalPKDef_v1.0.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            ResultActions resultAction = mockMvc
                  .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "LCD").param("specificationVersion", "1.0")
                        .headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200PLASMA_DVS233_13JUN2012_dft.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_FinalPKDef_v1.0.xml/");
         requestContent.add("specificationType", "FinalPKDef");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().isEmpty());// Non ASCII character in Header
         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains(FileValidationMessages.EQUIP_FV_ERR26.getErrMessageCode()));
         assert (validationResponse.getResponse().equals(Response.FAILED));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_ValidateFileToSpecificationException_FPKDEF_ColumnAliasCheck() {
      try {

         Resource resource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200PLASMA_DVS233_13JUN2014_dft.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_FinalPKDef_v1.0.xml/");
         requestContent.add("specificationType", "FinalPKDef");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getValidationStatus().equals(ValidationStatusTypes.ERROR.getStatusType()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_ValidateFileToSpecificationException_FPKDEF_WarningValidationStatus() {
      try {

         // Update the Final PK Def File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_FinalPKDef_v1.1.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(SPECIFICATIONBASE);
            requestBuilder.with(new RequestPostProcessor() {
               @Override
               public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                  request.setMethod("PUT");
                  return request;
               }
            });
            mockMvc.perform(requestBuilder.file(file).param("fileName", "FieldDataValidation_FinalPKDef_v1.0.xml/").param("specificationVersion", "1.1").headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200PLASMA_DVS233_13JUN2014_dft.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_FinalPKDef_v1.0.xml/");
         requestContent.add("specificationType", "FinalPKDef");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(0).getGlobalInfoReport().getValidationStatus().equals(ValidationStatusTypes.WARNING.getStatusType()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test10_ValidateFileToSpecification_FieldValidation_LPD_Exception_ASCIICharacterInValues() {
      try {

         // Add the LPD File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_LPD_v3.0.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            ResultActions resultAction = mockMvc
                  .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "LPD").param("specificationVersion", "3.0")
                        .headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }
         Resource resource = resourceLoader.getResource("classpath:/inputFiles/C6301094_PLASMA_ANTIXA_29JUN2017_LPD.csv");
         MockMultipartFile file = new MockMultipartFile("file", resource.getFilename(), "text/plain", resource.getInputStream());
         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v3.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_LPD_v3.0.xml/");
         requestContent.add("specificationType", "LPD");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(VALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(file).params(requestContent)
                     .headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().isEmpty());// ASCII character in values
         assert (validationResponse.getResponse().equals(Response.FAILED));

         assert (validationResponse.getFileValidationLog().get(0).getErrorOrException().contains(FileValidationMessages.EQUIP_FV_ERR26.getErrMessageCode()));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test11_DeleteSpecification() {
      try {
         mockMvc.perform(MockMvcRequestBuilders.delete(SPECIFICATIONBASE + "/FieldDataValidation_ARD_v3.1.xml").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test11_DeleteSpecificationException() {
      try {
         mockMvc.perform(MockMvcRequestBuilders.delete(SPECIFICATIONBASE + "/FieldDataValidation_ARD_v3.2.xml").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test12_UpdateSpecificationException() {
      try {
         Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_ARD_v3.0.xml");
         MockMultipartFile file = new MockMultipartFile("fileContent", "FieldDataValidation_ARD_v3.0.xml", "text/plain", resource.getInputStream());
         MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(SPECIFICATIONBASE);
         requestBuilder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
               request.setMethod("PUT");
               return request;
            }
         });
         mockMvc.perform(requestBuilder.file(file).param("specificationVersion", "3.0").headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test10_validateCrossFileToSpecification() {
      try {

         // Add the PKS File
         try {
            Resource resource = resourceLoader.getResource("classpath:/config/filevalidatorlibrary/FieldDataValidation_PKS_v1.0.xml");
            MockMultipartFile file = new MockMultipartFile("fileContent", resource.getFilename(), "text/plain", resource.getInputStream());
            ResultActions resultAction = mockMvc
                  .perform(MockMvcRequestBuilders.multipart(SPECIFICATIONBASE).file(file).param("specificationType", "PKS").param("specificationVersion", "1.0")
                        .headers(headers))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         } catch (Exception e) {
            e.printStackTrace();
         }

         Resource finalPKDefFileResource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200_PLASMA_DVS_233_13JUN2012_DEF.csv");
         MockMultipartFile finalPKDefFile = new MockMultipartFile("file", finalPKDefFileResource.getFilename(), "text/plain", finalPKDefFileResource.getInputStream());

         Resource pksFileResource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200_PLASMA_DVS_233_13JUN2018_PKS.csv");
         MockMultipartFile pksFile = new MockMultipartFile("pksFile", pksFileResource.getFilename(), "text/plain", pksFileResource.getInputStream());

         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_FinalPKDef_v1.0.xml/");
         requestContent.add("specificationType", "FinalPKDef");
         requestContent.add("pksSpecificationType", "PKS");
         requestContent.add("pksSspecificationVersion", "v1.0");
         requestContent.add("pksSpecificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_PKS_v1.0.xml/");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(CROSSFILEVALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(finalPKDefFile).file(pksFile)
                     .params(requestContent).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().isEmpty());
         assert (validationResponse.getFileValidationLog().get(1).getCrossFileFieldValidationStatus().equals(ValidationStatusTypes.WARNING));
         assert (validationResponse.getFileValidationLog().get(1).getCrossFileLevelLogReportList().size() > 0);
         assert (validationResponse.getFileValidationLog().get(1).getFileNameError().getMessage().contains(FileValidationMessages.EQUIP_FV_ERR07.getErrMessageCode()));
         assert (validationResponse.getResponse().equals(Response.OK));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test10_validateCrossFileToSpecification_Exception() {
      try {

         Resource finalPKDefFileResource = resourceLoader.getResource("classpath:/inputFiles/3151A2_1200_PLASMA_DVS_233_13JUN2012_DEF.csv");
         MockMultipartFile finalPKDefFile = new MockMultipartFile("file", finalPKDefFileResource.getFilename(), "text/plain", finalPKDefFileResource.getInputStream());

         MultiValueMap<String, String> requestContent = new LinkedMultiValueMap<>();
         requestContent.add("delimiter", ",");
         requestContent.add("specificationVersion", "v1.0");
         requestContent.add("specificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_FinalPKDef_v1.0.xml/");
         requestContent.add("specificationType", "FinalPKDef");
         requestContent.add("pksSpecificationType", "PKS");
         requestContent.add("pksSspecificationVersion", "v1.0");
         requestContent.add("pksSpecificationPath", MODESHAPEFILESPECPATH + "FieldDataValidation_PKS_v1.0.xml/");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(CROSSFILEVALIDATIONPATH + "{programId}/{protocolId}", PROGRAMID, PROTOCOLID).file(finalPKDefFile)
                     .params(requestContent).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         ValidationResponse validationResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationResponse.class);
         assertFalse(validationResponse.getFileValidationLog().get(0).getErrorOrException().equalsIgnoreCase("Specification Not Found"));
         assert (validationResponse.getFileValidationLog().get(1).getGlobalInfoReport().getValidationStatus()
               .equalsIgnoreCase(ValidationStatusTypes.ERROR.getStatusType()));
         assert (validationResponse.getResponse().equals(Response.FAILED));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
