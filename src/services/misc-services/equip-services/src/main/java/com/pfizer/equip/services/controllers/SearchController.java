package com.pfizer.equip.services.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.business.search.DataSearchService;
import com.pfizer.equip.services.business.search.SearchService;
import com.pfizer.equip.services.controllers.shared.SharedUtilties;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.library.LibraryInput;
import com.pfizer.equip.services.input.library.LibraryInputBuilder;
import com.pfizer.equip.services.input.search.SavedSearchInput;
import com.pfizer.equip.services.input.search.SearchInput;
import com.pfizer.equip.services.input.search.SearchOrdering;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.search.FolderResponse;
import com.pfizer.equip.services.responses.search.SearchResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class SearchController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private SearchService searchService;

   @Autowired
   private DataSearchService dataSearchService;

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   UserLookupService userLookupService;

   @Autowired
   private AuditService auditService;

   private LibraryInput createLibraryInput(SavedSearchInput input, String userId) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      String jsonSearchConfig = mapper.writeValueAsString(input);

      // create a mock multipart file so that we can re-use the library method, which expects the content to be a file
      MockMultipartFile jsonFile = new MockMultipartFile(input.getName(), input.getName(), "application/json", jsonSearchConfig.getBytes());

      // build inputs map to simulate normal add artifact call
      String primaryType = "equipLibrary:savedSearch";
      Map<String, Object> inputs = new HashMap<String, Object>();
      inputs.put(LibraryInput.PROPERTY_PRIMARY_TYPE, primaryType);
      inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, jsonFile);

      LibraryInputBuilder builder = new LibraryInputBuilder();
      return builder.build(userId, inputs);
   }

   @RequestMapping(value = "{systemId}/search", method = RequestMethod.POST)
   public SearchResponse search(@RequestBody SearchInput input, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.RUN_SEARCH)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform search.", userId));
         }

         log.info("Searching system for user ID '{}'...", userId);
         SearchResponse response = searchService.executeQuery(input);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/search/data", method = RequestMethod.POST)
   public SearchResponse searchData(@RequestBody SearchInput input, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Data Searching system for user ID '{}'...", userId);
         SearchResponse response = dataSearchService.executeQuery(input);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/search/save", method = RequestMethod.POST)
   public LibraryArtifactResponse saveSearch(@RequestBody SavedSearchInput input, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         validateSaveSearchPermissions(input, userId);

         String artifactPath = LibrarianService.HIDDEN_LIBRARY_START + "/saved-searches/" + userId + "/";

         log.info("Saving search for user ID '{}'...", userId);
         LibraryInput libInput = createLibraryInput(input, userId);

         LibraryArtifactResponse response = null;
         if (!input.isOverride()) {
            response = librarianService.addArtifact(artifactPath, libInput, userId, false);
         } else {
            artifactPath += input.getName();
            response = librarianService.updateArtifact(artifactPath, libInput, userId);
         }
         response.setResponse(Response.OK);

         // Create audit entry
         if (!input.isOverride()) {
            auditService.insertAuditEntry(new AuditEntryInput(" Saved Search added ", response.getArtifactId(), EntityType.SEARCH.getValue(), userId, ActionStatusType.SUCCESS, null));
         } else
            auditService.insertAuditEntry(new AuditEntryInput(" Saved Search updated ", response.getArtifactId(), EntityType.SEARCH.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/search/load/{name}", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getSavedSearch(@PathVariable("name") String name, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.VIEW_USER_LIBRARY)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to get saved search.", userId));
         }

         String artifactPath = LibrarianService.HIDDEN_LIBRARY_START + "/saved-searches/" + userId + "/" + name + "/";
         log.info("Loading saved search for user ID '{}'...", userId);
         ContentInfo contentInfo = librarianService.getArtifactContent(artifactPath, false);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/search/loadAll", method = RequestMethod.GET)
   public List<String> getAllSavedSearch(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.VIEW_USER_LIBRARY)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to get saved search.", userId));
         }

         String artifactPath = "/" + LibrarianService.HIDDEN_LIBRARY_START + "/saved-searches/" + userId + "/";
         log.info("Loading all saved searches for user ID '{}'...", userId);

         FolderResponse response = librarianService.getFolderContents(artifactPath, false, "jcr:name", false);

         List<String> searchNames = new ArrayList<>();
         if (response.getRows() != null) {
            for (Map<String, Object> row : response.getRows()) {
               String name = (String) row.get("jcr:name");
               searchNames.add(name);
            }
         }

         return searchNames;
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/search/{name}", method = RequestMethod.DELETE)
   public LibraryArtifactResponse deleteSearch(@PathVariable("name") String name, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         // TODO: Consider refactoring to one privilege instead of DROP_SEARCH_CRITERIA and DROP_SEARCH_RESULTS
         if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.DROP_SEARCH_CRITERIA)
               && !userLookupService.hasPrivilege(userId, "nca", PrivilegeType.DROP_SEARCH_RESULTS)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform delete search criteria and serach results.", userId));
         }

         String artifactPath = LibrarianService.HIDDEN_LIBRARY_START + "/saved-searches/" + userId + "/" + name;
         log.info("Deleting saved search for user ID '{}'...", userId);
         // Fetching the artifact as artifactId is required for audit trails.
         LibraryArtifactResponse artifact = librarianService.getArtifact(artifactPath, true);
         LibraryArtifactResponse response = librarianService.deleteArtifact(artifactPath);
         response.setResponse(Response.OK);

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput(" Search Deleted" + name, artifact.getArtifactId(), EntityType.SEARCH.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during search.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private void validateSaveSearchPermissions(SavedSearchInput input, String userId) {
      // check for the user privilege to perform this action
      try {
         if (!input.getSearchCriteria().isEmpty() && !input.getSearchResults().isEmpty()) {
            if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.WRITE_SEARCH_CRITERIA)
                  && !userLookupService.hasPrivilege(userId, "nca", PrivilegeType.WRITE_SEARCH_RESULTS)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform save search criteria and serach results.", userId));
            }

         } else if (!input.getSearchCriteria().isEmpty()) {
            if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.WRITE_SEARCH_CRITERIA)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform save search criteria.", userId));
            }
         } else if (!input.getSearchResults().isEmpty()) {
            if (!userLookupService.hasPrivilege(userId, LibrarianService.SYSTEM_ID_NCA, PrivilegeType.WRITE_SEARCH_RESULTS)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform save serach results.", userId));
            }
         }

      } catch (Exception e) {
         log.error("Exception Exception occured during validate save search permissions.", e);
         throw new RuntimeException(e);
      }

   }

}
