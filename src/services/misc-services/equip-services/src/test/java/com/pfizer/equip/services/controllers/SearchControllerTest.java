package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
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
import org.springframework.core.io.ResourceLoader;
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
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactNode;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.input.search.SavedSearchInput;
import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.services.input.search.SearchFullText;
import com.pfizer.equip.services.input.search.SearchGroup;
import com.pfizer.equip.services.input.search.SearchInput;
import com.pfizer.equip.services.input.search.SearchMode;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.search.SearchResponse;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.responses.Response;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchControllerTest {

   public final static String USERID = "atlamr-ncadev1";
   public final static String SAVED_SEARCH_FOLDER = "/library/hidden/saved-searches";
   public final static String SYSTEMID = "nca";
   public final static String LIBRARYBASE = "/" + SYSTEMID + "/librarian";
   public final static String SEARCHBASE = "/" + SYSTEMID + "/search";

   private MockMvc mockMvc;
   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private WebApplicationContext webApplicationContext;
   HttpHeaders headers = new HttpHeaders();

   @Autowired
   private ResourceLoader resourceLoader;

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("IAMPFIZERUSERCN", USERID);

   }

   @Test
   public void setUp_CreateNecessaryFolders() {
      try {

         ResultActions resultActionHiddenFolderCheck = mockMvc
               .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + "/library/hidden").headers(headers).accept(MediaType.APPLICATION_JSON));
         if (resultActionHiddenFolderCheck.andReturn().getResponse().getStatus() == 500) {
            // Hidden Folder does not exists, hence create a folder
            mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + "/library/hidden").headers(headers).accept(MediaType.APPLICATION_JSON));
            mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + SAVED_SEARCH_FOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));

         } else if (resultActionHiddenFolderCheck.andReturn().getResponse().getStatus() == 200) {

            ResultActions resultActionSavedSearchesFolderCheck = mockMvc
                  .perform(MockMvcRequestBuilders.get(LIBRARYBASE + "/artifact/current" + SAVED_SEARCH_FOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));
            if (resultActionSavedSearchesFolderCheck.andReturn().getResponse().getStatus() == 500) {
               // Saved search Folder does not exists, hence create a folder
               mockMvc.perform(MockMvcRequestBuilders.post(LIBRARYBASE + "/folder" + SAVED_SEARCH_FOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));

            }
         }
         // Create a user folder
         BaseLibraryArtifactNode newNode = new BaseLibraryArtifactNode();
         newNode.setPrimaryType("equipLibrary:baseFolder");
         newNode.setDeleted("false");

         repositoryService.addNode(newNode, SAVED_SEARCH_FOLDER + "/" + USERID);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void test01_Search_ChildFolder() {
      SearchInput input = new SearchInput();

      input.setChildFolder(SAVED_SEARCH_FOLDER + "/" + USERID);
      List<String> typesToSearch = new ArrayList<>();
      typesToSearch.add("equipLibrary:specification");
      input.setTypesToSearch(typesToSearch);

      ObjectMapper mapper = new ObjectMapper();
      try {

         ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

         SearchResponse searchResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), SearchResponse.class);
         assertFalse(searchResponse.getColumns().isEmpty());
         assertNull(searchResponse.getRows());

         // Now, add a file and check if search returns the file

         BaseLibraryArtifactNode file1 = new BaseLibraryArtifactNode();
         file1.setPrimaryType("equipLibrary:specification");
         file1.setSpecificationType("ARD");
         file1.setSpecificationVersion("1.0");
         file1.setDeleted("false");
         file1.setEquipCreatedBy(USERID);
         file1.setEquipName("TestLCDFile");
         file1.setEncodedContent("dGVzdA==");

         repositoryService.addNode(file1, SAVED_SEARCH_FOLDER + "/" + USERID + "/fileSpec1");
         ResultActions resultActions1 = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

         SearchResponse searchResponse1 = mapper.readValue(resultActions1.andReturn().getResponse().getContentAsString(), SearchResponse.class);
         assertFalse(searchResponse1.getColumns().isEmpty());
         assertFalse(searchResponse1.getRows().isEmpty());
         assert (searchResponse1.getRows().size() == 1);
         assert (searchResponse1.getRows().get(0).get("equipLibrary:name").equals("TestLCDFile"));

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test01_Search_DescendentFolder() {
      SearchInput input = new SearchInput();
      List<String> descendantFoldersList = new ArrayList<>();
      descendantFoldersList.add(SAVED_SEARCH_FOLDER);
      input.setDescendantFolders(descendantFoldersList);
      List<String> typesToSearch = new ArrayList<>();
      typesToSearch.add("equipLibrary:specification");
      input.setTypesToSearch(typesToSearch);

      ObjectMapper mapper = new ObjectMapper();
      try {
         // Add another folder
         BaseLibraryArtifactNode newNode = new BaseLibraryArtifactNode();
         newNode.setPrimaryType("equipLibrary:baseFolder");
         newNode.setDeleted("false");
         repositoryService.addNode(newNode, SAVED_SEARCH_FOLDER + "/" + USERID + "/test");
         // Adding another file
         BaseLibraryArtifactNode file1 = new BaseLibraryArtifactNode();
         file1.setPrimaryType("equipLibrary:specification");
         file1.setSpecificationType("LCD");
         file1.setSpecificationVersion("1.0");
         file1.setDeleted("false");
         file1.setEquipCreatedBy(USERID);
         file1.setSpecificationType("LCD");
         file1.setEquipName("TestOneFile");
         file1.setEncodedContent("dGVzdA==");

         repositoryService.addNode(file1, SAVED_SEARCH_FOLDER + "/" + USERID + "/test/fileSpec1");

         ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

         SearchResponse searchResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), SearchResponse.class);
         assertFalse(searchResponse.getColumns().isEmpty());
         assertFalse(searchResponse.getRows().isEmpty());

         // Now, adding criteria

         List<SearchGroup> criteriaGroupsList = new ArrayList<>();
         List<SearchCriteria> criteriaListActive = new ArrayList<>();
         SearchCriteria searchCriteriaActive = new SearchCriteria();
         searchCriteriaActive.setField("equipLibrary:name");
         searchCriteriaActive.setOperator("=");
         searchCriteriaActive.setValue("TestLCDFile");
         criteriaListActive.add(searchCriteriaActive);
         List<String> excludedTypes = new ArrayList<>();
         excludedTypes.add("equipLibrary:script");
         input.setExcludedTypes(excludedTypes);
         SearchFullText fullText = new SearchFullText();
         List<String> searchText = new ArrayList<>();
         searchText.add("LCD");
         searchText.add("LPD");
         fullText.setTextValues(searchText);
         fullText.setMode(SearchMode.MODE_OR);
         input.setFullTextQuery(fullText);

         List<SearchCriteria> subcriteriaList = new ArrayList<>();
         SearchCriteria subsearchCriteria = new SearchCriteria();
         subsearchCriteria.setField("equipLibrary:createdBy");
         subsearchCriteria.setOperator("=");
         subsearchCriteria.setValue(USERID);
         subcriteriaList.add(subsearchCriteria);
         SearchGroup subSearchGroup = new SearchGroup();
         subSearchGroup.setCriteria(subcriteriaList);
         subSearchGroup.setMode(SearchMode.MODE_OR);
         List<SearchGroup> subSearchGroupList = new ArrayList<>();
         subSearchGroupList.add(subSearchGroup);

         SearchGroup searchGroup = new SearchGroup();
         searchGroup.setCriteria(criteriaListActive);
         searchGroup.setSubCriteriaGroups(subSearchGroupList);
         searchGroup.setMode(SearchMode.MODE_AND);
         criteriaGroupsList.add(searchGroup);

         input.setCriteriaGroups(criteriaGroupsList);

         ResultActions resultActions1 = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

         SearchResponse searchResponse1 = mapper.readValue(resultActions1.andReturn().getResponse().getContentAsString(), SearchResponse.class);
         assertFalse(searchResponse1.getColumns().isEmpty());
         assertNull(searchResponse1.getRows());

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test01_SearchException() {
      SearchInput input = new SearchInput();
      List<String> descendantFoldersList = new ArrayList<>();
      descendantFoldersList.add("/library/global");
      input.setDescendantFolders(descendantFoldersList);
      List<String> typesToSearch = new ArrayList<>();
      typesToSearch.add("equipLibrary:specificaon");
      input.setTypesToSearch(typesToSearch);
      ObjectMapper mapper = new ObjectMapper();
      try {
         ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isInternalServerError());

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test01_Search_MissingTypeException() {
      SearchInput input = new SearchInput();
      List<String> descendantFoldersList = new ArrayList<>();
      descendantFoldersList.add("/library/global");
      input.setDescendantFolders(descendantFoldersList);
      ObjectMapper mapper = new ObjectMapper();
      try {
         mockMvc.perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/search").headers(headers).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(input))
               .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test02_SaveSearch() {
      SavedSearchInput input = new SavedSearchInput();
      input.setName("testsearch_001");
      input.setOverride(false);
      Map<String, Object> searchcriteria = new HashMap<>();
      searchcriteria.put("foo", "bar");
      input.setSearchCriteria(searchcriteria);

      List<Map<String, Object>> searchResults = new ArrayList<>();
      searchResults.add(searchcriteria);
      input.setSearchResults(searchResults);

      ObjectMapper mapper = new ObjectMapper();
      try {
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(SEARCHBASE + "/save").headers(headers).content(mapper.writeValueAsString(input))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertNotNull(libraryArtifactResponse.getArtifactId());
         assert (libraryArtifactResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(
               MockMvcRequestBuilders.get(SEARCHBASE + "/load/testsearch_001").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         SavedSearchInput searchInput = mapper.readValue(getAction.andReturn().getResponse().getContentAsString(), SavedSearchInput.class);
         assert (searchInput.getName().equals(input.getName()));
         assert (searchInput.getSearchCriteria().keySet().containsAll(input.getSearchCriteria().keySet()));
         assert (searchInput.getSearchCriteria().values().containsAll(input.getSearchCriteria().values()));
         assert (searchInput.getSearchResults().containsAll(input.getSearchResults()));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test02_SaveSearchException() {
      SavedSearchInput input = new SavedSearchInput();
      input.setName("testsearch_001");
      input.setOverride(false);
      Map<String, Object> searchcriteria = new HashMap<>();
      searchcriteria.put("foo", "bar");
      input.setSearchCriteria(searchcriteria);

      List<Map<String, Object>> searchResults = new ArrayList<>();
      searchResults.add(searchcriteria);
      input.setSearchResults(searchResults);

      ObjectMapper mapper = new ObjectMapper();
      try {
         mockMvc.perform(MockMvcRequestBuilders.post(SEARCHBASE + "/save").headers(headers).content(mapper.writeValueAsString(input)).contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test03_SaveSearch_OverideExistingSavedSearch() {
      SavedSearchInput input = new SavedSearchInput();
      input.setName("testsearch_001");
      input.setOverride(true);
      Map<String, Object> searchcriteria = new HashMap<>();
      searchcriteria.put("foo1", "bar1");
      input.setSearchCriteria(searchcriteria);

      Map<String, Object> searchResult = new HashMap<>();
      searchResult.put("test", "one");
      List<Map<String, Object>> searchResults = new ArrayList<>();
      searchResults.add(searchResult);
      input.setSearchResults(searchResults);

      ObjectMapper mapper = new ObjectMapper();
      try {
         ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(SEARCHBASE + "/save").headers(headers).content(mapper.writeValueAsString(input))
               .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
         LibraryArtifactResponse libraryArtifactResponse = mapper.readValue(result.andReturn().getResponse().getContentAsString(), LibraryArtifactResponse.class);
         assertNotNull(libraryArtifactResponse.getArtifactId());
         assert (libraryArtifactResponse.getResponse().equals(Response.OK));
         ResultActions getAction = mockMvc.perform(
               MockMvcRequestBuilders.get("/search/load/testsearch_001").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         SavedSearchInput searchInput = mapper.readValue(getAction.andReturn().getResponse().getContentAsString(), SavedSearchInput.class);
         assert (searchInput.getName().equals(input.getName()));
         assert (searchInput.getSearchCriteria().keySet().containsAll(input.getSearchCriteria().keySet()));
         assert (searchInput.getSearchCriteria().values().containsAll(input.getSearchCriteria().values()));
         assert (searchInput.getSearchResults().containsAll(input.getSearchResults()));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test04_getAllSavedSearch() {

      try {
         ResultActions result = mockMvc
               .perform(MockMvcRequestBuilders.get(SEARCHBASE + "/loadAll").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         assert (result.andReturn().getResponse().getContentAsString().contains("testsearch_001"));
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test05_getSavedSearch() {

      try {
         ResultActions result = mockMvc.perform(
               MockMvcRequestBuilders.get(SEARCHBASE + "/load/testsearch_001").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         ObjectMapper mapper = new ObjectMapper();
         SavedSearchInput searchInput = mapper.readValue(result.andReturn().getResponse().getContentAsString(), SavedSearchInput.class);
         assert (searchInput.getName().equals("testsearch_001"));
         assert (searchInput.getSearchCriteria().keySet().contains("foo1"));
         assert (searchInput.getSearchCriteria().values().contains("bar1"));
         Map<String, Object> searchResult = new HashMap<>();
         searchResult.put("test", "one");
         assert (searchInput.getSearchResults().contains(searchResult));

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test05_getSavedSearchException_NoSuchSavedSearch() {

      try {
         mockMvc.perform(
               MockMvcRequestBuilders.get(SEARCHBASE + "/load/testsearch_002").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test06_delete() {

      try {
         mockMvc
               .perform(
                     MockMvcRequestBuilders.delete(SEARCHBASE + "/testsearch_001").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());
         mockMvc.perform(
               MockMvcRequestBuilders.get(SEARCHBASE + "/load/testsearch_001").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test07_deleteException() {

      try {
         mockMvc
               .perform(
                     MockMvcRequestBuilders.delete(SEARCHBASE + "/testsearch_002").headers(headers).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isInternalServerError());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   // Data frames- Data sets to be included for searchData .Not able to create dataframes in Scratch repository using the dataFrame Service.
   @Test
   public void test08_SearchData() {
      SearchInput input = new SearchInput();
      List<String> descendantFoldersList = new ArrayList<>();
      descendantFoldersList.add("/library/global");
      input.setDescendantFolders(descendantFoldersList);
      List<String> typesToSearch = new ArrayList<>();
      typesToSearch.add("equipLibrary:specification");
      input.setTypesToSearch(typesToSearch);

      List<SearchGroup> criteriaGroupsList = new ArrayList<>();
      List<SearchCriteria> criteriaListActive = new ArrayList<>();
      SearchCriteria searchCriteriaActive = new SearchCriteria();
      searchCriteriaActive.setField("equip:deleteFlag");
      searchCriteriaActive.setOperator("=");
      searchCriteriaActive.setValue("false");
      criteriaListActive.add(searchCriteriaActive);

      SearchGroup searchGroupActive = new SearchGroup();
      searchGroupActive.setCriteria(criteriaListActive);
      searchGroupActive.setMode(SearchMode.MODE_AND);
      criteriaGroupsList.add(searchGroupActive);

      input.setCriteriaGroups(criteriaGroupsList);
      ObjectMapper mapper = new ObjectMapper();
      try {
         ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(SEARCHBASE + "/data").headers(headers).contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(input)).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

         SearchResponse searchResponse = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), SearchResponse.class);
         assertFalse(searchResponse.getColumns().isEmpty());
         // TODO: Data frame to be created and verified . Exception cases to be added.
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Test
   public void test99_tearDown() {

      try {
         repositoryService.deleteNode(SAVED_SEARCH_FOLDER + "/" + USERID);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

}
