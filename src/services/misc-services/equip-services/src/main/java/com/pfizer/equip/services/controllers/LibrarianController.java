package com.pfizer.equip.services.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.business.notifications.SubscriptionService;
import com.pfizer.equip.services.business.types.PrimaryType;
import com.pfizer.equip.services.controllers.shared.SharedUtilties;
import com.pfizer.equip.services.input.library.LibraryInput;
import com.pfizer.equip.services.input.library.LibraryInputBuilder;
import com.pfizer.equip.services.input.notification.NotificationType;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.library.MCTArtifactResponse;
import com.pfizer.equip.services.responses.library.VersionHistoryResponse;
import com.pfizer.equip.services.responses.search.FolderResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class LibrarianController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   private SubscriptionService subscriptionService;

   @Autowired
   private EventService eventService;

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   private AuditService auditService;

   /**
    * Extract path from a controller mapping. /controllerUrl/** => return matched **
    * 
    * @param request incoming request.
    * @return extracted path
    * 
    * Technique from https://stackoverflow.com/questions/3686808/spring-3-requestmapping-get-path-value
    */
   private String extractPathFromPattern(final HttpServletRequest request) {
      String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
      String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

      AntPathMatcher apm = new AntPathMatcher();
      String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

      // add trailing '/' if needed
      if (!finalPath.endsWith("/")) {
         finalPath = finalPath + "/";
      }

      return finalPath;
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/current/**", method = RequestMethod.GET)
   public LibraryArtifactResponse getArtifact(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);
         log.info("Getting artifact ID '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse response = librarianService.getArtifact(artifactPath, true);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/current/id/{id}", method = RequestMethod.GET)
   public LibraryArtifactResponse getArtifactById(@PathVariable("id") String id, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Getting artifact ID '{}' for user ID '{}'...", id, userId);

         LibraryArtifactResponse response = librarianService.getArtifactById(id);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/**", method = RequestMethod.POST)
   public LibraryArtifactResponse addArtifact(@RequestParam MultiValueMap<String, Object> inputs, @RequestParam("fileContent") MultipartFile fileContent,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPathForCreate = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(artifactPathForCreate, userId);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, Arrays.asList(fileContent));

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Adding new artifact for user ID '{}'...", userId);
         LibraryArtifactResponse response = librarianService.addArtifact(artifactPathForCreate, input, userId, false);

         // Raise event for new artifact addition
         Map<String, Object> description = eventService.createEventDescription("New artifacts added. File name is  " + fileContent.getOriginalFilename(), userId,
               "false");
         addAuditAndEvent(description, input.getPrimaryType(), response, artifactPathForCreate, fileContent.getOriginalFilename(), true, "added", userId);

         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/hidden", method = RequestMethod.POST)
   public LibraryArtifactResponse addHiddenArtifact(@RequestParam MultiValueMap<String, Object> inputs, @RequestParam("fileContent") MultipartFile fileContent,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // hidden artifacts always go to 'library/hidden/'
         String artifactPathForCreate = "library/hidden/";

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, Arrays.asList(fileContent));

         LibraryInputBuilder builder = new LibraryInputBuilder();
         String userId = getUserId(request);
         LibraryInput input = builder.build(userId, inputs);
         log.info("Adding new artifact for user ID '{}'...", userId);
         LibraryArtifactResponse response = librarianService.addArtifact(artifactPathForCreate, input, userId, true);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Added Hidden Artifact. Artifact Name :" + fileContent.getOriginalFilename(), response.getArtifactId(),
               EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/folder/**", method = RequestMethod.POST)
   public LibraryArtifactResponse addFolder(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPathForCreate = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(artifactPathForCreate, userId);
         log.info("Adding new folder for user ID '{}'...", userId);

         LibraryArtifactResponse response = librarianService.addFolder(artifactPathForCreate);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("New Folder added. Folder Path :" + artifactPathForCreate, response.getArtifactId(),
               EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of folder.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/user/{userId}", method = RequestMethod.POST)
   public List<LibraryArtifactResponse> addUser(@PathVariable("userId") String userId, HttpServletRequest request) {
      Monitor monitor = null;

      String searchPath = "library/users/" + userId;
      String hiddenPath = "library/hidden/saved-searches/" + userId;

      List<LibraryArtifactResponse> responses = new ArrayList<>();

      try {
         // If this call fails (I.e. folder doesn't exist at given path) an exception is thrown triggering the adding of the folders
         librarianService.getArtifact(searchPath, true);

         LibraryArtifactResponse response = new LibraryArtifactResponse();
         response.setResponse(Response.FAILED);
         response.setComments("Search folder already exists for the given user.");
         response.setArtifactPath(searchPath);
         responses.add(response);
      } catch (HttpClientErrorException e) {
         try {
            LibraryArtifactResponse searchResponse = librarianService.addFolder(searchPath);

            responses.add(searchResponse);
         } catch (Exception error) {
            log.error("Exception occured during creation of folder.", error);
            throw new RuntimeException(error);
         }
      } catch (Exception e) {
         log.error("Exception occured during creation of folder.", e);
         throw new RuntimeException(e);
      }

      try {
         // If this call fails (I.e. folder doesn't exist at given path) an exception is thrown triggering the adding of the folders
         librarianService.getArtifact(hiddenPath, true);

         LibraryArtifactResponse response = new LibraryArtifactResponse();
         response.setResponse(Response.FAILED);
         response.setComments("Hidden search folder already exists for the given user.");
         response.setArtifactPath(hiddenPath);
         responses.add(response);
      } catch (HttpClientErrorException e) {
         try {
            LibraryArtifactResponse hiddenResponse = librarianService.addFolder(hiddenPath);

            responses.add(hiddenResponse);
         } catch (Exception error) {
            log.error("Exception occured during creation of folder.", error);
            throw new RuntimeException(error);
         }
      } catch (Exception e) {
         log.error("Exception occured during creation of folder.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }

      return responses;
   }

   @RequestMapping(value = "{systemId}/librarian/user/{userId}", method = RequestMethod.DELETE)
   public List<LibraryArtifactResponse> removeUser(@PathVariable("userId") String userId, HttpServletRequest request) {
      Monitor monitor = null;

      String searchPath = "library/users/" + userId;
      String hiddenPath = "library/hidden/saved-searches/" + userId;

      List<LibraryArtifactResponse> responses = new ArrayList<>();

      try {
         LibraryArtifactResponse searchResponse = librarianService.deleteArtifact(searchPath);

         responses.add(searchResponse);
      } catch (HttpClientErrorException e) {
         if (e.getRawStatusCode() == 404) {
            LibraryArtifactResponse searchResponse = new LibraryArtifactResponse();
            searchResponse.setArtifactPath(searchPath);
            searchResponse.setResponse(Response.FAILED);
            searchResponse.setComments("No such folder exists");
            responses.add(searchResponse);
         } else {
            log.error("Exception occured during deletion of folder.", e);
            throw new RuntimeException(e);
         }
      } catch (Exception e) {
         log.error("Exception occured during deletion of folder.", e);
         throw new RuntimeException(e);
      }

      try {
         LibraryArtifactResponse hiddenResponse = librarianService.deleteArtifact(hiddenPath);

         responses.add(hiddenResponse);
      } catch (HttpClientErrorException e) {
         if (e.getRawStatusCode() == 404) {
            LibraryArtifactResponse hiddenResponse = new LibraryArtifactResponse();
            hiddenResponse.setArtifactPath(searchPath);
            hiddenResponse.setResponse(Response.FAILED);
            hiddenResponse.setComments("No such folder exists");
            responses.add(hiddenResponse);
         } else {
            log.error("Exception occured during deletion of folder.", e);
            throw new RuntimeException(e);
         }
      } catch (Exception e) {
         log.error("Exception occured during deletion of folder.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }

      return responses;
   }

   @RequestMapping(value = "{systemId}/librarian/folder/contents/**", method = RequestMethod.GET)
   public FolderResponse getFolderContents(@RequestParam("includeDeleted") Optional<Boolean> includeDeletedOption,
         @RequestParam("orderBy") Optional<String> orderByOption, @RequestParam("orderDirection") Optional<String> orderDirectionOption, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         String folderPath = extractPathFromPattern(request);
         log.info("Getting contents of folder with ID '{}' for user ID '{}'...", folderPath, userId);

         // Set the defaults
         boolean includeDeleted = includeDeletedOption.isPresent() ? includeDeletedOption.get() : false;
         String orderBy = orderByOption.isPresent() ? orderByOption.get() : "equip:created";
         String orderDirection = orderDirectionOption.isPresent() ? orderDirectionOption.get() : "desc";
         boolean inReversedOrder = orderDirection.equals("asc") ? false : true;

         FolderResponse response = librarianService.getFolderContents(folderPath, includeDeleted, orderBy, inReversedOrder);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/content/current/**", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getArtifactContent(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);
         String userId = getUserId(request);
         log.info("Getting content for artifact '{}' for user ID '{}'...", artifactPath, userId);

         ContentInfo contentInfo = librarianService.getArtifactContent(artifactPath, false);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during getting content of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/content/current/id/{id}", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getArtifactContentById(@PathVariable("id") String id, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Getting artifact ID '{}' for user ID '{}'...", id, userId);

         ContentInfo contentInfo = librarianService.getArtifactContentById(id);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during getting content of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/versions/**", method = RequestMethod.GET)
   public VersionHistoryResponse getVersionHistory(@RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);
         String userId = getUserId(request);
         log.info("Getting version history for artifact '{}' for user ID '{}'...", artifactPath, userId);

         VersionHistoryResponse response = librarianService.getVersionHistory(artifactPath);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/version/{versionNumber:[\\d]+}/**", method = RequestMethod.GET)
   public LibraryArtifactResponse getArtifactVersion(@PathVariable long versionNumber, @RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);
         String userId = getUserId(request);
         log.info("Getting version '{}' of artifact ID '{}' for user ID '{}'...", versionNumber, artifactPath, userId);

         LibraryArtifactResponse response = librarianService.getArtifactVersion(artifactPath, versionNumber);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/content/version/{versionNumber:[\\d]+}/**", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getArtifactContentVersion(@PathVariable long versionNumber, @RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);
         String userId = getUserId(request);
         log.info("Getting content for version '{}' of artifact '{}' for user ID '{}'...", versionNumber, artifactPath, userId);

         ContentInfo contentInfo = librarianService.getArtifactContentVersion(artifactPath, versionNumber);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during getting content of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/update/**", method = RequestMethod.POST)
   public LibraryArtifactResponse updateArtifact(@RequestParam MultiValueMap<String, Object> inputs, @RequestParam("fileContent") Optional<MultipartFile> fileContent,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(artifactPath, userId);

         // add to inputs map as needed
         if (fileContent.isPresent()) {
            inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, Arrays.asList(fileContent.get()));
         }
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, Arrays.asList(artifactPath));

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Updating artifact '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse response = librarianService.updateArtifact(artifactPath, input, userId);
         response.setResponse(Response.OK);

         // Create audit entry
         String actionMsg = (StringUtils.isNotEmpty(input.getDeleted()) && input.getDeleted().equalsIgnoreCase("true")) ? "deleted" : "updated";
         addAuditAndEvent(null, input.getPrimaryType(), response, artifactPath, artifactPath, false, actionMsg, userId);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during updating of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/move/**", method = RequestMethod.POST)
   public LibraryArtifactResponse moveArtifact(@RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String newParentPath = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(newParentPath, userId);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, newParentPath);

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Moving artifact '{}' to '{}' for user ID '{}'...", input.getTargetArtifact(), newParentPath, userId);

         LibraryArtifactResponse response = librarianService.moveArtifact(newParentPath, input);
         response.setResponse(Response.OK);

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Artifact moved to " + newParentPath, response.getArtifactId(), EntityType.ARTIFACT.getValue(), userId,
               ActionStatusType.SUCCESS, null));

         return response;
      } catch (Exception e) {
         log.error("Exception occured during moving of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/promote/**", method = RequestMethod.POST)
   public LibraryArtifactResponse promoteArtifactRequest(@PathVariable String systemId, @RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(artifactPath, userId);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);
         log.info("Requesting promotion for artifact '{}' for user ID '{}'...", artifactPath, userId);

         // submit promotion request event
         Map<String, Object> description = new HashMap<>();
         description.put("system_initiated", "false");
         description.put("user_name", userId);
         description.put("comments", "User " + userId + " has requested promotion authorization");
         eventService.createEvent(this.getClass().toString(), new Date(), artifactPath, "artifact", "global_library_promotion_request", null, null, description,
               properties.getEventQueue());

         // subscribe user to receive approve/request status of the promotion request
         subscriptionService.createSubscription(NotificationType.REALTIME.getValue(), null, null, artifactPath, "global_library_promotion_request_result", userId, null,
               getUserId(request), systemId);

         LibraryArtifactResponse response = new LibraryArtifactResponse();
         String artifactId = librarianService.getArtifact(artifactPath, true).getArtifactId();
         response.setArtifactId(artifactId);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Requested promotion authorization", response.getArtifactId(), EntityType.ARTIFACT.getValue(), userId,
               ActionStatusType.SUCCESS, null));
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of promotion request of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/promote/approve/**", method = RequestMethod.POST)
   public LibraryArtifactResponse promoteArtifactApprove(@RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);
         String userId = getUserId(request);
         log.info("Approving promotion of artifact '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse response = librarianService.promoteArtifact(artifactPath);

         // raise approve event
         Map<String, Object> description = new HashMap<>();
         description.put("system_initiated", "false");
         description.put("user_name", userId);
         description.put("comments", "Promotion Approved");
         eventService.createEvent(this.getClass().toString(), new Date(), artifactPath, "artifact", "global_library_promotion_request_result", null, null, description,
               properties.getEventQueue());

         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput("Promotion Approved", response.getArtifactId(), EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));

         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during promotion approval of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/promote/reject/**", method = RequestMethod.POST)
   public LibraryArtifactResponse promoteArtifactReject(@RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);
         String userId = getUserId(request);
         log.info("Rejecting promotion of artifact '{}' for user ID '{}'...", artifactPath, userId);

         // raise rejection event
         Map<String, Object> description = new HashMap<>();
         description.put("system_initiated", "false");
         description.put("user_name", userId);
         description.put("comments", "Promotion Rejected");
         eventService.createEvent(this.getClass().toString(), new Date(), artifactPath, "artifact", "global_library_promotion_request_result", null, null, description,
               properties.getEventQueue());

         LibraryArtifactResponse response = new LibraryArtifactResponse();
         // Create audit entry
         String artifactId = librarianService.getArtifact(artifactPath, true).getArtifactId();
         response.setArtifactId(artifactId);
         auditService.insertAuditEntry(
               new AuditEntryInput("Promotion Rejected", response.getArtifactId(), EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during promotion rejection of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/**", method = RequestMethod.DELETE)
   public LibraryArtifactResponse softDeleteArtifact(@RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = extractPathFromPattern(request);

         // check access
         String userId = getUserId(request);
         librarianService.checkUserAccess(artifactPath, userId);

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);
         // a 'delete' is just a soft delete with an update to a custom metadata property
         inputs.put(LibraryInput.PROPERTY_DELETED, "true");

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Deleting artifact '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse response = librarianService.updateArtifact(artifactPath, input, userId);
         response.setResponse(Response.OK);

         // Create audit entry
         addAuditAndEvent(null, input.getPrimaryType(), response, artifactPath, artifactPath, false, "deleted", userId);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during deletion of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/artifact/permissions/{artifactId}", method = RequestMethod.GET)
   public LibraryArtifactResponse getArtifactAccessPermissions(@PathVariable String artifactId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Getting artifact ID '{}' for user ID '{}'...", artifactId, userId);

         // TODO: do something with the input and get access permissions.
         // TODO: Might not need inputs, just GET /artifact/permissions/{artfiactId}
         // TODO: need to determine what kind of response the front end requires
         LibraryArtifactResponse response = new LibraryArtifactResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting of artifact permissions.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/librarian/loadMct", method = RequestMethod.GET)
   public List<MCTArtifactResponse> getAllMctFiles() {
      Monitor monitor = null;
      try {
         String artifactPath = "/" + LibrarianService.GLOBAL_LIBRARY_START + "/mct/";
         log.info("Loading all MCT files...");

         FolderResponse response = librarianService.getFolderContents(artifactPath, false, "jcr:name", false);

         List<MCTArtifactResponse> mctList = new ArrayList<>();
         if (response.getRows() != null) {
            for (Map<String, Object> row : response.getRows()) {
               String id = (String) row.get("id");
               String name = (String) row.get("jcr:name");
               MCTArtifactResponse mct = new MCTArtifactResponse(id, name);
               mctList.add(mct);
            }
         }

         return mctList;
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

   @RequestMapping(value = "{systemId}/librarian/units", method = RequestMethod.GET)
   public ResponseEntity<byte[]> getUnitsManagementContent(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String artifactPath = "/" + LibrarianService.GLOBAL_LIBRARY_START + "/units-management/units-conversions.json/";
         log.info("Retrieving units management config file...");

         ContentInfo contentInfo = librarianService.getArtifactContent(artifactPath, false);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during getting content of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private void addAuditAndEvent(Map<String, Object> description, String primaryType, LibraryArtifactResponse response, String artifactPathForCreate, String fileName,
         boolean createEvent, String actionMsg, String userId) throws IOException, ExecutionException {
      // Events are not triggered for updates.
      String eventType = null;
      String action = "";
      String entityType = "";
      PrimaryType primaryTypeValue = PrimaryType.getPrimaryTypeFromValue(primaryType);

      if (artifactPathForCreate.contains("global") && createEvent) {
         eventService.createEvent(this.getClass().toString(), new Date(), response.getArtifactId(), "artifact", "global_library_additions", null, null, description,
               properties.getEventQueue());
      }

      // set action string based on actionMsg
      action = "Document " + actionMsg + " Document Name :" + fileName;

      if (createEvent) {
         description.put("artifact_type", "document");
         eventType = "documents_additions";
         entityType = EntityType.ARTIFACT.getValue();
         eventService.createEvent(this.getClass().toString(), new Date(), response.getArtifactId(), "artifact", eventType, null, null, description,
               properties.getEventQueue());
      }

      switch (primaryTypeValue) {
         case SCRIPT:
            eventType = "scripts_additions";
            action = "Script " + actionMsg + " Script Name :" + fileName;
            entityType = EntityType.ARTIFACT.getValue();
            break;
         case REPORTING_ITEM_TEMPLATE:
            eventType = "reporting_templates_additions";
            action = "Reporting Item Template " + actionMsg + " Reporting Item Template Name :" + fileName;
            entityType = EntityType.REPORTING_ITEM_TEMPLATE.getValue();
            break;
         case REPORT_TEMPLATE:
            eventType = "reporting_templates_additions";
            action = "Reporting Template " + actionMsg + " Reporting Template Name :" + fileName;
            entityType = EntityType.REPORT_TEMPLATE.getValue();
            break;
         default:
            entityType = EntityType.ARTIFACT.getValue();
            break;
      }
      if (!StringUtils.equals(eventType, "documents_additions") && createEvent) {
         eventService.createEvent(this.getClass().toString(), new Date(), response.getArtifactId(), "artifact", eventType, null, null, description,
               properties.getEventQueue());
      }
      auditService.insertAuditEntry(new AuditEntryInput(action, response.getArtifactId(), entityType, userId, ActionStatusType.SUCCESS, null));
   }
}
