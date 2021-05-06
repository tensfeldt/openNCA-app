package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.StringContains;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactNode;
import com.pfizer.equip.services.business.notifications.SubscriptionService;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.input.notification.NotificationType;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.library.MCTArtifactResponse;
import com.pfizer.equip.services.responses.library.VersionHistoryResponse;
import com.pfizer.equip.services.responses.library.VersionHistoryResponseItem;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.relational.entity.ListName;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.relational.repository.EventTypeRepository;
import com.pfizer.equip.shared.relational.repository.NotificationTypeRepository;
import com.pfizer.equip.shared.relational.repository.SubscriptionRepository;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibrarianControllerTest {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   // The user atlamr-ncadev7 has been assigned to a group EQUIP-NCA-Librarian and has the role Librarian.
   // The user should have all Library related privileges to access the LibrarianController methods
   public final static String USERID = "atlamr-ncadev7";
   public final static String TESTFOLDER = "/library/global/test/";
   public final static String MCTTESTFOLDER = "/library/global/mct/";
   public final static String TESTARTIFACTNAME = "TestFieldDataValidation_LCD_v1.1.xml";
   public final static String TESTARTIFACTPATH = "classpath:/config/filevalidatorlibrary/TestFieldDataValidation_LCD_v1.1.xml";
   public final static String TESTUPDATEARTIFACTPATH = "classpath:/config/filevalidatorlibrary/updateartifactcheck/TestFieldDataValidation_LCD_v1.1.xml";
   public final static String ARTIFACTVERSION = "1";
   public final static String INVALIDARTIFACTVERSION = "10";
   public final static String HIDDENFOLDERPATH = "/library/hidden/";
   public final static String HIDDENARTIFACTPATH = "classpath:/config/filevalidatorlibrary/TestFieldDataValidation_LCD_v1.2.xml";
   public final static String HIDDENARTIFACTNAME = "TestFieldDataValidation_LCD_v1.2.xml";
   public final static String MOVESOURCEFOLDERPATH = "testmove/";
   public final static String MOVEARTIFACTPATH = "classpath:/config/filevalidatorlibrary/FieldDataValidation_ARD_v3.0.xml";
   public final static String MOVEARTIFACTNAME = "FieldDataValidation_ARD_v3.0.xml";
   public final static String USERFOLDER = "/library/users/";
   public final static String GLOBALFOLDER = "/global/";
   public final static String MODESHAPEHIDDENARTIFACTPATH = "/nca/items/library/hidden/";
   public final static String SYSTEMID = "nca";
   public final static String LIBRARYBASE = "/" + SYSTEMID + "/librarian";

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private SubscriptionRepository subscriptionRepository;

   @Autowired
   private NotificationTypeRepository notificationTypeRepo;

   @Autowired
   private EventTypeRepository eventTypeRepository;

   @Autowired
   private SubscriptionService subscriptionService;

   private MockMvc mockMvc;

   @Autowired
   private WebApplicationContext webApplicationContext;

   @Autowired
   private ResourceLoader resourceLoader;

   HttpHeaders headers = new HttpHeaders();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      // The current user should have access to the WRITE_GLOBAL_LIBRARY privilege, to do any operation in /library/global/ directory
      headers.set("IAMPFIZERUSERCN", USERID);
      // TODO Test user accounts still pending per Richard. Need to add few more test cases for the user without WRITE_GLOBAL_LIBRARY privilege

   }

   /**
    * Deleting the node created for testing the methods in the LibrarianController. The TESTFOLDER will be deleted, which was created for the previous run. The deleteNode
    * method call is just the cleanup activity and not the part of the Unit test.
    */
   public void setUp_deleteNode() {
      try {
         repositoryService.deleteNode(TESTFOLDER);
      // before test - delete the test artifact that was promoted earlier if it already exists
         repositoryService.deleteNode("/library/global/" + TESTARTIFACTNAME);
      } catch (Exception e) {
         log.info("The Node '{}' may not exists. Error message : '{}'", TESTFOLDER, e.getMessage());
      }
   }

   @Test
   public void test01_addFolder() {
      // Do cleanup activity
      setUp_deleteNode();
      try {
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + TESTFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue((libraryArtifactResponse.getArtifactPath() + "/").contains(TESTFOLDER));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_addFolderException() {
      // Try to create a folder that is already available in the same directory - 400 Bad Request Check
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Bad Request (400), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + TESTFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("400 Bad Request")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_addArtifact() {

      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         MockMultipartFile file = new MockMultipartFile("fileContent", TESTARTIFACTNAME, "text/plain", resource.getInputStream());
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
         inputs.add("comments", "Test Artifact used for Unit Testing");
         inputs.add("customTags", "My Custom Tag Info");
         inputs.add("equipName", "File Validation Rules for LCD v1.1");
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + TESTFOLDER).file(file).params(inputs).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactId = libraryArtifactResponse.getArtifactId();
         assertTrue(artifactId != null);
         assertTrue(libraryArtifactResponse.getArtifactPath().contains(TESTFOLDER + TESTARTIFACTNAME));
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test02_addArtifactException() {
      // Try to add the same folder which was created in the previous method, So exception occurs that the folder already exists
      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         MockMultipartFile file = new MockMultipartFile("fileContent", TESTARTIFACTNAME, "text/plain", resource.getInputStream());
         mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + TESTFOLDER).file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains("\"response\":\"FAILED\"")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_getArtifact() {

      try {

         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactPath = libraryArtifactResponse.getArtifactPath();
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue(("/" + artifactPath).equalsIgnoreCase(TESTFOLDER + TESTARTIFACTNAME));
         assertTrue(libraryArtifactResponse.getPrimaryType().equalsIgnoreCase("equipLibrary:attachment"));
         assertTrue(libraryArtifactResponse.getMimeType().equalsIgnoreCase("application/xml"));
         assertTrue(libraryArtifactResponse.getProperties().get("equipCreatedBy").equals(USERID));
         assertTrue(libraryArtifactResponse.getProperties().get("comments").equals("Test Artifact used for Unit Testing"));
         assertTrue(libraryArtifactResponse.getProperties().get("customTags").toString().equalsIgnoreCase("[My Custom Tag Info]"));
         assertTrue(libraryArtifactResponse.getProperties().get("equipName").toString().equalsIgnoreCase("File Validation Rules for LCD v1.1"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test03_getArtifactException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Bad Request (400), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + TESTFOLDER + "test").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_getArtifactById() {

      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactId = libraryArtifactResponse.getArtifactId();
         ResultActions resultActionGetArtifactById = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current/id/{id}", artifactId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult resultGetArtifactById = resultActionGetArtifactById.andReturn();
         ObjectMapper mapperGetArtifactById = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponseGetArtifactById = mapperGetArtifactById.readValue(resultGetArtifactById.getResponse().getContentAsString(),
               LibraryArtifactResponse.class);
         String artifactPath = libraryArtifactResponseGetArtifactById.getArtifactPath();
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponseGetArtifactById.getArtifactId() != null);
         assertTrue(("/" + artifactPath).equalsIgnoreCase(TESTFOLDER + TESTARTIFACTNAME));
         assertTrue(libraryArtifactResponseGetArtifactById.getPrimaryType().equalsIgnoreCase("equipLibrary:attachment"));
         assertTrue(libraryArtifactResponseGetArtifactById.getMimeType().equalsIgnoreCase("application/xml"));
         assertTrue(libraryArtifactResponseGetArtifactById.getProperties().get("equipCreatedBy").equals(USERID));
         assertTrue(libraryArtifactResponse.getProperties().get("comments").equals("Test Artifact used for Unit Testing"));
         assertTrue(libraryArtifactResponse.getProperties().get("customTags").toString().equalsIgnoreCase("[My Custom Tag Info]"));
         assertTrue(libraryArtifactResponse.getProperties().get("equipName").toString().equalsIgnoreCase("File Validation Rules for LCD v1.1"));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test04_getArtifactByIdException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current/id/{id}", "123").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_getArtifactContent() {

      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent = new String(encoded, "UTF-8");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/current" + TESTFOLDER + TESTARTIFACTNAME).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
         MvcResult result = resultAction.andReturn();
         assertTrue(result.getResponse().getContentAsString().contains(fileContent));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test05_getArtifactContentException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/current" + TESTFOLDER + "test").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_getArtifactContentById() {

      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent = new String(encoded, "UTF-8");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactId = libraryArtifactResponse.getArtifactId();
         ResultActions resultActionGetContent = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/current/id/{id}", artifactId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
         MvcResult resultGetContent = resultActionGetContent.andReturn();
         assertTrue(resultGetContent.getResponse().getContentAsString().contains(fileContent));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test06_getArtifactContentByIdException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/current/id/{id}", "123").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test07_getVersionHistory() {

      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/versions" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult resultGetArtifactById = resultAction.andReturn();
         ObjectMapper mapperGetArtifactById = new ObjectMapper();
         VersionHistoryResponse versionHistoryResponse = mapperGetArtifactById.readValue(resultGetArtifactById.getResponse().getContentAsString(),
               VersionHistoryResponse.class);
         List<VersionHistoryResponseItem> versionHistoryItems = versionHistoryResponse.getVersionHistory();
         VersionHistoryResponseItem versionHistoryResponseItem = versionHistoryItems.get(0);
         assertTrue(versionHistoryResponse.getResponse().equals(Response.OK));
         assertTrue(versionHistoryItems.size() > 0);
         assertTrue(versionHistoryResponseItem.getVersion() == 1);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test07_getVersionHistoryException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/versions" + TESTFOLDER + "test").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test08_getArtifactVersion() {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/version/" + ARTIFACTVERSION + TESTFOLDER + TESTARTIFACTNAME).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactPath = libraryArtifactResponse.getArtifactPath();
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         String pathExpectedPrefix = "jcr:system/jcr:versionStorage";
         assertTrue(artifactPath != null);
         assertTrue(artifactPath.startsWith(pathExpectedPrefix));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue(libraryArtifactResponse.getMimeType().equalsIgnoreCase("application/xml"));
         assertTrue(libraryArtifactResponse.getDeleted().equalsIgnoreCase("false"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test08_getArtifactVersionException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc
               .perform(
                     MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/version/" + INVALIDARTIFACTVERSION + TESTFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_getArtifactContentVersion() {

      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent = new String(encoded, "UTF-8");
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/version/" + ARTIFACTVERSION + TESTFOLDER + TESTARTIFACTNAME).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
         MvcResult result = resultAction.andReturn();
         assertTrue(result.getResponse().getContentAsString().contains(fileContent));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test09_getArtifactContentVersionException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/version/" + INVALIDARTIFACTVERSION + TESTFOLDER).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test10_getArtifactAccessPermissions() {
      // The getArtifactAccessPermissions method in the Librarian Controller is not implemented yet, hence we are checking the response as OK alone
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactId = libraryArtifactResponse.getArtifactId();
         mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/permissions/{artifactId}", artifactId).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // TODO: Need to update this, once the method is ready, currently we don't have any exception case to handle
   // @Test
   // public void test10_getArtifactAccessPermissionsException() {
   //
   // try {
   //
   // mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/permissions/{artifactId}", "123").headers(headers).accept(MediaType.APPLICATION_JSON))
   // .andDo(print()).andExpect(MockMvcResultMatchers.status().isInternalServerError())
   // .andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
   // .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }

   @Test
   public void test11_addHiddenArtifact() {

      try {
         Resource resource = resourceLoader.getResource(HIDDENARTIFACTPATH);
         MockMultipartFile file = new MockMultipartFile("fileContent", HIDDENARTIFACTNAME, "text/plain", resource.getInputStream());
         ResultActions resultAction = mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact/hidden/").file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue((libraryArtifactResponse.getArtifactPath()).contains(MODESHAPEHIDDENARTIFACTPATH + HIDDENARTIFACTNAME));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test11_addHiddenArtifactException() {

      try {
         Resource resource = resourceLoader.getResource(HIDDENARTIFACTPATH);

         MockMultipartFile file = new MockMultipartFile("fileContent1", HIDDENARTIFACTNAME, "text/plain", resource.getInputStream());
         mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact/hidden/").file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test12_updateArtifactwithoutMultipartFile() {

      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart((LIBRARYBASE + "/artifact/update" + TESTFOLDER + TESTARTIFACTNAME))
                     .param("description", "Artifact Description Added").headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue(libraryArtifactResponse.getArtifactPath().contains(TESTFOLDER + TESTARTIFACTNAME));

         // Check the artifact has the updated content
         ResultActions resultActionGetArtifactById = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current/id/{id}", libraryArtifactResponse.getArtifactId()).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult resultGetArtifactById = resultActionGetArtifactById.andReturn();
         ObjectMapper mapperGetArtifactById = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponseGetArtifactById = mapperGetArtifactById.readValue(resultGetArtifactById.getResponse().getContentAsString(),
               LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponseGetArtifactById.getArtifactId() != null);
         Map<String, Object> artifactProperties = libraryArtifactResponseGetArtifactById.getProperties();
         assertTrue(artifactProperties.get("description").equals("Artifact Description Added"));
         assertTrue(artifactProperties.get("comments").equals("Test Artifact used for Unit Testing"));
         assertTrue(artifactProperties.get("customTags").toString().equalsIgnoreCase("[My Custom Tag Info]"));
         assertTrue(artifactProperties.get("equipName").toString().equalsIgnoreCase("File Validation Rules for LCD v1.1"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test13_updateArtifactwithMultipartFile() {
      try {
         Resource resource = resourceLoader.getResource(TESTUPDATEARTIFACTPATH);
         byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
         String fileContent = new String(encoded, "UTF-8");
         MockMultipartFile file = new MockMultipartFile("fileContent", TESTARTIFACTNAME, "text/plain", resource.getInputStream());
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact/update" + TESTFOLDER + TESTARTIFACTNAME).file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         assertTrue(libraryArtifactResponse.getArtifactId() != null);
         assertTrue(libraryArtifactResponse.getArtifactPath().contains(TESTFOLDER + TESTARTIFACTNAME));

         // Check the artifact has the updated content
         ResultActions resultActionGetContent = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/content/current/id/{id}", libraryArtifactResponse.getArtifactId()).headers(headers)
                     .accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
         MvcResult resultGetContent = resultActionGetContent.andReturn();
         assertTrue(resultGetContent.getResponse().getContentAsString().contains(fileContent));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test13_updateArtifactwithoutMultipartFileException() {
      // In this case, MockMvcResultMatchers will be returning Internal Server Error (500),
      // But the current implementation in Librarian Service will be returning Not Found (404), which we are checking in the content here
      try {
         mockMvc.perform(MockMvcRequestBuilders.multipart((LIBRARYBASE + "/artifact/update" + TESTFOLDER + "test")).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(MockMvcResultMatchers.content().string(new StringContains("404 Not Found")))
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // Move the artifact from MOVESOURCEFOLDERPATH to TESTFOLDER
   @Test
   public void test14_moveArtifact() {

      try {
         // 1. Add the testMove folder
         mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + TESTFOLDER + MOVESOURCEFOLDERPATH).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         // 2. Add Artifact to the testMove folder
         Resource resource = resourceLoader.getResource(MOVEARTIFACTPATH);
         MockMultipartFile file = new MockMultipartFile("fileContent", MOVEARTIFACTNAME, "text/plain", resource.getInputStream());
         mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + TESTFOLDER + MOVESOURCEFOLDERPATH).file(file).headers(headers))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         // 3. Move Artifact to TESTFOLDER
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/artifact/move" + TESTFOLDER).param("targetArtifact", TESTFOLDER + MOVESOURCEFOLDERPATH).headers(headers)
                     .contentType(MediaType.APPLICATION_JSON_UTF8))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test14_moveArtifactException() {

      try {

         mockMvc
               .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/artifact/move" + "test").param("targetArtifact", TESTFOLDER + MOVESOURCEFOLDERPATH).headers(headers)
                     .contentType(MediaType.APPLICATION_JSON_UTF8))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test15_promoteArtifactRequest() {

      try {
         removeSubscriptions(NotificationType.REALTIME.getValue(), null, null, "library/global/test/" + TESTARTIFACTNAME + "/", "global_library_promotion_request_result",
               USERID, null, USERID, SYSTEMID);
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/artifact/promote" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test16_promoteArtifactApproveReject() {
      try {
         String userFolderExitsCreated = "ERROR";
         String userIdFolderExitsCreated = "ERROR";
         String artifactExitsCreated = "ERROR";
         String globalFolderExists = "ERROR";
         String result_FoldersCreated = createFoldersForPromoteArtifactApproveReject();
         String[] resultOfFolderCreation = result_FoldersCreated.split("_");
         userFolderExitsCreated = resultOfFolderCreation[0];
         userIdFolderExitsCreated = resultOfFolderCreation[1];
         artifactExitsCreated = resultOfFolderCreation[2];
         globalFolderExists = resultOfFolderCreation[3];
         if ((!userFolderExitsCreated.equalsIgnoreCase("ERROR")) || (!userIdFolderExitsCreated.equalsIgnoreCase("ERROR"))
               || (!artifactExitsCreated.equalsIgnoreCase("ERROR")) || (!globalFolderExists.equalsIgnoreCase("ERROR"))) {
            // Proceed with the approve artifact
            ResultActions resultActionPromoteArtifact = mockMvc
                  .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/artifact/promote/approve" + USERFOLDER + USERID + "/" + TESTARTIFACTNAME).headers(headers)
                        .accept(MediaType.APPLICATION_JSON))
                  .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
            MvcResult result = resultActionPromoteArtifact.andReturn();
            ObjectMapper mapper = new ObjectMapper();
            LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
            assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
            if (resultActionPromoteArtifact.andReturn().getResponse().getStatus() == 200) {
               // Promote artifact completed successfully, proceed with reject artifact
               ResultActions resultActionRejectArtifact = mockMvc
                     .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/artifact/promote/reject" + USERFOLDER + USERID + "/" + TESTARTIFACTNAME).headers(headers)
                           .accept(MediaType.APPLICATION_JSON))
                     .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
               MvcResult resultRejectArtifact = resultActionRejectArtifact.andReturn();
               ObjectMapper mapperRejectArtifact = new ObjectMapper();
               LibraryArtifactResponse libraryArtifactResponseRejectArtifact = mapperRejectArtifact.readValue(resultRejectArtifact.getResponse().getContentAsString(),
                     LibraryArtifactResponse.class);
               assertTrue(libraryArtifactResponseRejectArtifact.getResponse().equals(Response.OK));

            }

         }

         deleteFoldersCreatedForPromoteArtifactApproveReject(userFolderExitsCreated, userIdFolderExitsCreated, artifactExitsCreated, globalFolderExists);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public String createFoldersForPromoteArtifactApproveReject() {
   
      String userFolderExitsCreated = "ERROR";
      String userIdFolderExitsCreated = "ERROR";
      String artifactExitsCreated = "ERROR";
      String globalFolderExists = "ERROR";
      try {
         // Check for the /library/users/<userId> folder exists in mode shape, if it does not exists create a folder and delete it.
         // If it exists use it and don't delete it.
         ResultActions resultActionUserFolderCheck = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + USERFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));
         if (resultActionUserFolderCheck.andReturn().getResponse().getStatus() == 500) {
            // User Folder does not exists, hence create a folder
            // Librarian Service - Add Folder cannot be used to create /user folder because of the access restriction. So Using Repository service to create /user
            BaseLibraryArtifactNode newNode = new BaseLibraryArtifactNode();
            newNode.setPrimaryType("equipLibrary:baseFolder");
            newNode.setDeleted("false");

            repositoryService.addNode(newNode, USERFOLDER);
            userFolderExitsCreated = "CREATED";
            // Create /<userid> folder
            ResultActions resultActionAddUserIdFolder = mockMvc
                  .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + USERFOLDER + USERID).headers(headers).accept(MediaType.APPLICATION_JSON));
            if (resultActionAddUserIdFolder.andReturn().getResponse().getStatus() == 200) {
               userIdFolderExitsCreated = "CREATED";

            }

         } else if (resultActionUserFolderCheck.andReturn().getResponse().getStatus() == 200) {
            // User folder exists, hence proceed with <userid> folder check
            userFolderExitsCreated = "EXISTS";
            ResultActions resultActionUserIdFolderCheck = mockMvc
                  .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + USERFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));
            if (resultActionUserIdFolderCheck.andReturn().getResponse().getStatus() == 500) {
               // <userid> folder doesnot exists , hence create it
               ResultActions resultActionAddUserIdFolder = mockMvc
                     .perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + USERFOLDER + USERID).headers(headers).accept(MediaType.APPLICATION_JSON));
               if (resultActionAddUserIdFolder.andReturn().getResponse().getStatus() == 200) {
                  userIdFolderExitsCreated = "CREATED";
               }

            } else if (resultActionUserIdFolderCheck.andReturn().getResponse().getStatus() == 200) {
               // <userid> folder exists
               userIdFolderExitsCreated = "EXISTS";
            }

         }
         if (userIdFolderExitsCreated.equalsIgnoreCase("EXISTS") || userIdFolderExitsCreated.equalsIgnoreCase("CREATED")) {
            ResultActions resultActionArtifactCheck = mockMvc.perform(MockMvcRequestBuilders
                  .get(LIBRARYBASE + "/artifact/current" + USERFOLDER + USERID + "/" + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON));
            if (resultActionArtifactCheck.andReturn().getResponse().getStatus() == 500) {
               // Add the artifact in the user folder
               Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
               MockMultipartFile file = new MockMultipartFile("fileContent", TESTARTIFACTNAME, "text/plain", resource.getInputStream());
               mockMvc.perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + USERFOLDER + USERID).file(file).headers(headers));
               artifactExitsCreated = "CREATED";
            } else if (resultActionArtifactCheck.andReturn().getResponse().getStatus() == 200) {
               artifactExitsCreated = "EXISTS";
            }
         }

         if (artifactExitsCreated.equalsIgnoreCase("EXISTS") || artifactExitsCreated.equalsIgnoreCase("CREATED")) {
            // Check for /global folder
            ResultActions resultActionGlobalFolderCheck = mockMvc
                  .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + GLOBALFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));

            if (resultActionGlobalFolderCheck.andReturn().getResponse().getStatus() == 500) {
               // Global Folder does not exists, hence create a folder,
               // Librarian Service - Add Folder cannot be used to create /global folder because of the access restriction. So Using Repository service to create /global

               BaseLibraryArtifactNode newNode = new BaseLibraryArtifactNode();
               newNode.setPrimaryType("equipLibrary:baseFolder");
               newNode.setDeleted("false");

               repositoryService.addNode(newNode, GLOBALFOLDER);
               globalFolderExists = "CREATED";
            } else if (resultActionGlobalFolderCheck.andReturn().getResponse().getStatus() == 200) {
               globalFolderExists = "EXISTS";
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return userFolderExitsCreated + "_" + userIdFolderExitsCreated + "_" + artifactExitsCreated + "_" + globalFolderExists;
   }

   public void deleteFoldersCreatedForPromoteArtifactApproveReject(String userFolderExitsCreated, String userIdFolderExitsCreated, String artifactExitsCreated,
         String globalFolderExists) {
      if (globalFolderExists.equalsIgnoreCase("CREATED")) {
         // delete the /global folder
         repositoryService.deleteNode(GLOBALFOLDER);
      } else if (globalFolderExists.equalsIgnoreCase("EXISTS")) {
         // Remove the approved artifact
         repositoryService.deleteNode("/library" + GLOBALFOLDER + TESTARTIFACTNAME);
      }
      if (userFolderExitsCreated.equalsIgnoreCase("CREATED")) {
         // delete the user folder
         repositoryService.deleteNode(USERFOLDER);
      } else if (userIdFolderExitsCreated.equalsIgnoreCase("CREATED")) {
         // delete the user/<userid> folder
         repositoryService.deleteNode(USERFOLDER + USERID);
      } else if (userIdFolderExitsCreated.equalsIgnoreCase("EXISTS") && artifactExitsCreated.equalsIgnoreCase("CREATED")) {
         // delete the Artifact
         repositoryService.deleteNode(USERFOLDER + USERID + "/" + TESTARTIFACTNAME);
      }

   }

   @Test
   public void test17_softDeleteArtifact() {
      try {
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.delete(LIBRARYBASE + "/artifact" + TESTFOLDER + TESTARTIFACTNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
         String deletedFlag = libraryArtifactResponse.getDeleted();
         assertTrue(deletedFlag.equalsIgnoreCase("true"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test17_softDeleteArtifactException() {
      try {
         mockMvc.perform(MockMvcRequestBuilders.delete(LIBRARYBASE + "/artifact" + TESTFOLDER + "test").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError())
               .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // To clean up the subscriptions created as a part of testing.
   private void removeSubscriptions(String notificationTypeName, String studyId, String programNumber, String artifactId, String eventTypeName, String userName,
         String email, String requestUser, String systemId) throws ExecutionException {

      List<Subscription> existingSubscriptions = subscriptionRepository.findByAll(eventTypeRepository.findByEventTypeName(eventTypeName),
            notificationTypeRepo.findByNotificationTypeName(notificationTypeName), StringUtils.defaultIfEmpty(studyId, null),
            StringUtils.defaultIfEmpty(programNumber, null), StringUtils.defaultIfEmpty(artifactId, null), userName, StringUtils.defaultIfEmpty(email, null));
      if (existingSubscriptions.size() > 0) {
         for (Subscription s : existingSubscriptions) {
            subscriptionService.deleteSubscription(s.getSubscriptionId(), USERID, systemId);
         }
      }
   }

   @Test
   public void test18_getAllMctFiles() {
      try {
         // before test - delete the mct artifact if it already exists
         repositoryService.deleteNode(MCTTESTFOLDER + TESTARTIFACTNAME);
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {

         // SetUp
         addMctFile();

         // get the MCT file
         ResultActions resultActionMct = mockMvc.perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/loadMct/").headers(headers).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
         MvcResult resultMct = resultActionMct.andReturn();
         ObjectMapper mapperMct = new ObjectMapper();
         @SuppressWarnings("unchecked")
         List<MCTArtifactResponse> mctFileslist = mapperMct.readValue(resultMct.getResponse().getContentAsString(), new TypeReference<List<MCTArtifactResponse>>() {});
         assertTrue(mctFileslist.size() > 0);
         for (MCTArtifactResponse mctFile : mctFileslist) {
            if (mctFile.getName().equals(TESTARTIFACTNAME)) {
               assertTrue(mctFile.getName().equals(TESTARTIFACTNAME));
            }
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void addMctFile() {
      try {
         Resource resource = resourceLoader.getResource(TESTARTIFACTPATH);
         MockMultipartFile file = new MockMultipartFile("fileContent", TESTARTIFACTNAME, "text/plain", resource.getInputStream());
         // RequestParams
         MultiValueMap<String, String> inputs = new LinkedMultiValueMap<String, String>();
         inputs.add("comments", "Test Artifact used for Unit Testing");
         inputs.add("customTags", "My Custom Tag Info");
         inputs.add("equipName", "File Validation Rules for LCD v1.1");
         inputs.add("primaryType", "equipLibrary:mct");

         // Add artifact
         ResultActions resultAction = mockMvc
               .perform(MockMvcRequestBuilders.multipart(LIBRARYBASE + "/artifact" + MCTTESTFOLDER).file(file).params(inputs).headers(headers)
                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));

         MvcResult result = resultAction.andReturn();
         ObjectMapper mapper1 = new ObjectMapper();
         LibraryArtifactResponse libraryArtifactResponse = mapper1.readValue(result.getResponse().getContentAsString(), LibraryArtifactResponse.class);
         String artifactId = libraryArtifactResponse.getArtifactId();
         assertTrue(artifactId != null);
         assertTrue(libraryArtifactResponse.getArtifactPath().contains(MCTTESTFOLDER + TESTARTIFACTNAME));
         assertTrue(libraryArtifactResponse.getResponse().equals(Response.OK));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
