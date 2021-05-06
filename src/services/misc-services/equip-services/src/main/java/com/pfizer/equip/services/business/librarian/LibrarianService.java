package com.pfizer.equip.services.business.librarian;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pfizer.equip.services.business.modeshape.exceptions.ArtifactAlreadyExistsException;
import com.pfizer.equip.services.business.modeshape.exceptions.ExtensionMismatchException;
import com.pfizer.equip.services.business.modeshape.exceptions.PrimaryTypeNotSpecifiedException;
import com.pfizer.equip.services.business.modeshape.exceptions.TransactionException;
import com.pfizer.equip.services.business.modeshape.nodes.ArtifactNode;
import com.pfizer.equip.services.business.modeshape.nodes.BaseExistingNode;
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactContentNode;
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactNode;
import com.pfizer.equip.services.business.modeshape.nodes.FolderNode;
import com.pfizer.equip.services.business.modeshape.nodes.NewFolderNode;
import com.pfizer.equip.services.business.modeshape.nodes.VersionHistoryNode;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.library.LibraryInput;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ModeShapeServiceProperties;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.library.VersionHistoryResponse;
import com.pfizer.equip.services.responses.library.VersionHistoryResponseItem;
import com.pfizer.equip.services.responses.search.FolderResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;

@Service
public class LibrarianService {
   // TODO: Add performance monitoring statements.
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   public final static String GLOBAL_LIBRARY_START = "library/global";
   public final static String HIDDEN_LIBRARY_START = "library/hidden";
   public final static String SYSTEM_ID_NCA = "nca";
   // need to append DATA_PATH when replacing /items/ with /binary/
   public final static String DATA_PATH = "equip:complexData/jcr:content/jcr:data";
   // since library artifacts have child nodes, need to add the depth parameter to the query string of the URL:
   public final static String DEPTH_STRING = String.format("?depth=%d", 2);
   public final static String FOLDER_DEPTH_STRING = String.format("?depth=%d", 3);
   // search results need a displayType (subType):
   private final static String DISPLAY_TYPE = "LibraryArtifact";
   private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   // apparently RestTemplate is not a bean, probably because each instance is tied to a specific service
   private RestTemplate restTemplate;

   @Autowired
   private ModeShapeServiceProperties modeShapeProperties;

   @Autowired
   private UserLookupService userLookupService;

   private String baseUrl;
   private List<String> supportedTypes;

   @PostConstruct
   private void initialize() {
      // for now this service uses basic authentication
      // this may change in the future
      String serviceUser = modeShapeProperties.getUser();
      String servicePassword = modeShapeProperties.getPassword();
      baseUrl = modeShapeProperties.getUrl() + modeShapeProperties.getRepository() + "/" + modeShapeProperties.getWorkspace() + "/";
      restTemplate = restTemplateBuilder.basicAuthorization(serviceUser, servicePassword).build();

      // get supported types and default primary type for library artifact creation
      supportedTypes = modeShapeProperties.getSupportedTypes();
   }

   private static String toISO8601UTC(Date date) {
      TimeZone tz = TimeZone.getTimeZone("UTC");
      DateFormat df = new SimpleDateFormat(DATE_FORMAT);
      df.setTimeZone(tz);
      return df.format(date);
   }

   private static Date fromISO8601UTC(String dateString) {
      DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
      try {
         return dateFormat.parse(dateString);
      } catch (ParseException e) {
         throw new RuntimeException("Caught exception while parsing date from string.", e);
      }
   }

   public void checkUserAccess(String artifactPath, String userId) throws Exception {
      // two use cases: user trying to modify the global library, and user trying to modify a regular library
      if (artifactPath.indexOf("/") == 0) {
         artifactPath = artifactPath.substring(1);
      }
      if (artifactPath.startsWith(GLOBAL_LIBRARY_START)) {
         // check if the current user has access to the WRITE_GLOBAL_LIBRARY privilege, if not throw exception
         Set<PrivilegeType> userFunctions = userLookupService.getUserFunctions(SYSTEM_ID_NCA, userId);
         if (!userFunctions.contains(PrivilegeType.WRITE_GLOBAL_LIBRARY)) {
            throw new NotAuthorizedException(String.format("User '%s' does not have permissions to modify the global library.", userId));
         }
      } else {
         // if the user ID passed in is not the same as the user ID in the path, throw an exception
         // the 14 comes from the length of 'library/users/'
         String libraryPathUser = artifactPath.substring(14, artifactPath.indexOf("/", 14));
         boolean notMatch = !libraryPathUser.contains("-" + userId + "-") && !libraryPathUser.equalsIgnoreCase(userId);
         if (notMatch) {
            throw new NotAuthorizedException(String.format("User '%s' is not authorized to modify library of user '%s'.", userId, libraryPathUser));
         }
      }
   }

   public LibraryArtifactResponse getArtifactById(String id) throws Exception {
      // first get the artifact metadata to get the path to the artifact using the ID endpoint
      String url = baseUrl + "nodes/" + id + DEPTH_STRING;
      ArtifactNode artifactNode = restTemplate.getForObject(url, ArtifactNode.class);
      LibraryArtifactResponse response = new LibraryArtifactResponse(artifactNode, supportedTypes, baseUrl + "items/");
      response.setResponse(Response.OK);
      return response;
   }

   public LibraryArtifactResponse getArtifact(String artifactPath, boolean constructFullPath) throws Exception {
      String url;
      if (artifactPath.contains("hidden/")) {
         url = baseUrl + "items/library/" + artifactPath + DEPTH_STRING;
      } else {
         url = (constructFullPath ? (baseUrl + "items/" + artifactPath) : artifactPath) + DEPTH_STRING;
      }

      ArtifactNode artifactNode = restTemplate.getForObject(url, ArtifactNode.class);
      LibraryArtifactResponse response = new LibraryArtifactResponse(artifactNode, supportedTypes, baseUrl + "items/");
      response.setResponse(Response.OK);
      return response;
   }

   public LibraryArtifactResponse getArtifactVersion(String artifactPath, long versionNumber) throws Exception {
      // get version history node first
      VersionHistoryNode historyNode = getVersionHistoryNode(artifactPath);

      // now get the requested version specific path in ModeShape
      String versionPath = historyNode.getVersionPath(versionNumber);

      // now retrieve that version's content
      return getArtifact(versionPath, false);
   }

   private ContentInfo retrieveContent(String url, String fileName) {
      ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
      // Retrieve MimeType from response, rather than making an extra call:
      String mimeType = response.getHeaders().get("Content-Type").get(0).toString();
      byte[] content = response.getBody();
      ContentInfo contentInfo = new ContentInfo(content, mimeType, fileName);
      return contentInfo;
   }

   public ContentInfo getArtifactContentById(String id) throws UnsupportedEncodingException {
      // first get the artifact metadata to get the path to the artifact using the ID endpoint
      String url = baseUrl + "nodes/" + id;
      ArtifactNode artifactNode = restTemplate.getForObject(url, ArtifactNode.class);

      // now retrieve the content by path
      url = artifactNode.getSelf().replace("/items/", "/binary/") + "/" + DATA_PATH;
      return retrieveContent(url, "");
   }

   public ContentInfo getArtifactContent(String artifactPath, boolean ignoreMimeType) {
      return getArtifactContent(artifactPath, Optional.empty());
   }

   public ContentInfo getArtifactContent(String artifactPath, Optional<String> versionPath) {
      // we have to parse out the last piece of the path to get the file name value
      String fileName = artifactPath.substring(artifactPath.lastIndexOf("/", artifactPath.length() - 2) + 1, artifactPath.length() - 1);

      // now retrieve the binary content
      String url;
      if (artifactPath.contains("hidden/")) {
         url = baseUrl + "items/library/" + artifactPath + DATA_PATH;
      } else {
         url = !versionPath.isPresent() ? (baseUrl + "binary/" + artifactPath + DATA_PATH) : (versionPath.get().replace("/items/", "/binary/") + "/" + DATA_PATH);
      }

      return retrieveContent(url, fileName);
   }

   public ContentInfo getArtifactContentVersion(String artifactPath, long versionNumber) throws UnsupportedEncodingException {
      // get version history node first
      VersionHistoryNode historyNode = getVersionHistoryNode(artifactPath);

      // now get the requested version specific path in ModeShape
      String versionPath = historyNode.getVersionPath(versionNumber);

      // now retrieve that version's content
      return getArtifactContent(artifactPath, Optional.of(versionPath));
   }

   private String generateArtifactJson(LibraryInput input, String base64Content, String userId) throws Exception {
      return generateArtifactJson(input, base64Content, null, userId);
   }

   private String generateArtifactJson(LibraryInput input, String base64Content, String primaryType, String userId) throws Exception {
      // create node bean to represent the JSON we will send over
      BaseLibraryArtifactContentNode node = new BaseLibraryArtifactContentNode();
      // used for displaying search results when returned to the front end:
      node.setSubType(DISPLAY_TYPE);
      if (StringUtils.isNotEmpty(primaryType)) {
         node.setPrimaryType(primaryType);

         // set custom fields to track the create user / time
         node.setEquipCreatedBy(userId);
         node.setEquipCreated(toISO8601UTC(new Date()));
      } else {
         // set custom fields to track the modified user / time
         node.setEquipModifiedBy(userId);
         node.setEquipModified(toISO8601UTC(new Date()));
      }
      
      // Comment node must always exist even if null in order to be searachable? Default it to empty string.
      String comment = StringUtils.isNotEmpty(input.getComments()) ? input.getComments() : "";
      if (StringUtils.isNotEmpty(base64Content)) {
         // If this is creation, base64Content will not be null.
         node.setContent(base64Content);
      } else {
         // If this is update, base64Content may be null and should be retrieved, otherwise we wipe away the existing content.
         ContentInfo fileContent = getArtifactContent(input.getArtifactPath(), true);
         node.setContent(Base64.getEncoder().encodeToString(fileContent.getContent()));
         // If no comments and base64content is null, this is an update (or delete/restore) and we should retrieve the previous comment content
         if (input.getComments() == null) {
            comment = getArtifact(input.getArtifactPath(), true).getComments();
         }
      }

      // comments need to use BaseLibraryArtifactContentNode.setComments rather than setting directly via reflection
      node.setComments(comment);

      // set custom metadata using reflection
      // set the field on the node class for the same field on the input class
      Field[] fields = LibraryInput.class.getDeclaredFields();
      for (Field field : fields) {
         field.setAccessible(true);
         String name = field.getName();
         Object value = field.get(input);
         // have to skip null values and file content is handled specially
         if (value != null && !name.equals(LibraryInput.PROPERTY_FILE_CONTENT) && !Modifier.isStatic(field.getModifiers())) {
            try {
               Field nodeField = BaseLibraryArtifactNode.class.getDeclaredField(name);
               nodeField.setAccessible(true);
               nodeField.set(node, value);
            } catch (NoSuchFieldException e) {} // ignore this error
         }
      }

      ObjectMapper mapper = new ObjectMapper();
      String jsonValue = mapper.writeValueAsString(node);
      return jsonValue;
   }

   public LibraryArtifactResponse addArtifact(String artifactPath, LibraryInput input, String userId, boolean addTimeStamp) throws Exception {
      ArtifactNode firstNode = null;
      try {
         MultipartFile fileContent = input.getFileContent();
         String filename = fileContent.getOriginalFilename();

         // for certain hidden files, we need to append the user ID and timestamp to avoid collisions
         if (addTimeStamp) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
            Date today = Calendar.getInstance().getTime();
            String dateString = df.format(today);
            filename += "-" + userId + "-" + dateString;
         }

         // first check to see if an artifact with the same name exists on this path
         // if it does, throw an exception
         try {
            LibraryArtifactResponse response = getArtifact(artifactPath + filename, true);
            if (response.getResponse() == Response.OK) {
               throw new ArtifactAlreadyExistsException(String.format("The artifact with name '%s' at path '%s' already exists.", filename, artifactPath));
            }
         } catch (HttpClientErrorException e) {
            if (!e.getMessage().equals("404 Not Found")) {
               // ignore the 404 not found, otherwise re-throw
               throw e;
            }
         }

         String url = baseUrl + "items/" + artifactPath + filename;
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         // convert content to base64 encoded string so we can pass metadata and content in 1 call
         String base64Content = Base64.getEncoder().encodeToString(fileContent.getBytes());

         // caller can specify an optional primary type, if empty set to default
         String primaryType = input.getPrimaryType();
         if (StringUtils.isEmpty(primaryType)) {
            throw new PrimaryTypeNotSpecifiedException("You must specify a primary type to add artifacts.");
         }

         String requestJson = generateArtifactJson(input, base64Content, primaryType, userId);
         HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
         firstNode = restTemplate.postForObject(url, entity, ArtifactNode.class);

         // now update the same exact path with the same exact JSON using an HTTP PUT
         // this is due to a quirk in ModeShape where the 1st version of a node does not store metadata or content as part of its history
         ArtifactNode node = restTemplate.exchange(url, HttpMethod.PUT, entity, ArtifactNode.class).getBody();
         String nodeId = node.getId();
         log.debug("Successfully added new artifact with ID {}.", nodeId);

         // construct response object
         LibraryArtifactResponse response = new LibraryArtifactResponse();
         response.setArtifactId(nodeId);
         response.setArtifactPath(node.getSelf());
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         if (e instanceof ArtifactAlreadyExistsException) {
            // this was thrown by code above, simply re-throw
            throw e;
         }

         log.error("Exception occurred adding new artifact.", e);
         if (firstNode != null) {
            String firstNodeId = firstNode.getId();
            try {
               // need to delete the dangling node, since this means the content creation failed
               String url = baseUrl + "nodes/" + firstNodeId;
               restTemplate.delete(url);
            } catch (Exception e2) {
               log.error("Deletion of dangling first node {} failed.", firstNodeId, e2);
            }
         }
         throw new TransactionException("An exception occurred during the operations required to create a new artifact.", e);
      }
   }

   public LibraryArtifactResponse addFolder(String artifactPath) throws Exception {
      // trim any trailing '/' character
      if (artifactPath.endsWith("/")) {
         artifactPath = artifactPath.substring(0, artifactPath.length() - 1);
      }

      // for folders to have lower case
      String url = baseUrl + "items/" + artifactPath.toLowerCase();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      NewFolderNode newFolderNode = new NewFolderNode();
      ObjectMapper mapper = new ObjectMapper();
      String jsonValue = mapper.writeValueAsString(newFolderNode);
      HttpEntity<String> entity = new HttpEntity<String>(jsonValue, headers);
      FolderNode folderNode = restTemplate.postForObject(url, entity, FolderNode.class);
      String nodeId = folderNode.getId();
      log.debug("Successfully added new folder with ID {}.", nodeId);

      // construct response object
      LibraryArtifactResponse response = new LibraryArtifactResponse();
      response.setArtifactId(nodeId);
      response.setArtifactPath(folderNode.getSelf());
      response.setResponse(Response.OK);
      return response;
   }

   public FolderResponse getFolderContents(String folderPath, boolean includeDeleted, String orderBy, boolean inReversedOrder)
         throws JsonProcessingException, UnsupportedEncodingException {
      String url = baseUrl + "items/" + folderPath + FOLDER_DEPTH_STRING;
      List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
      // Keep lastRow so we can check data types after:
      Map<String, Object> lastFileRow = new HashMap<String, Object>();
      ObjectMapper mapper = new ObjectMapper();
      // Going to get a lot of extra stuff so use this to filter:
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      JsonNode folderNode = restTemplate.getForObject(url, JsonNode.class);

      // TODO: See if we want to centralize/externalize these strings
      if (folderNode.has("children")) {
         for (JsonNode childNode : folderNode.get("children")) {
            String primaryType = childNode.get("jcr:primaryType").asText();
            ArtifactNode artifactNode = mapper.convertValue(childNode, ArtifactNode.class);

            if (!primaryType.equalsIgnoreCase("equipLibrary:baseFolder")) {
               if (includeDeleted || artifactNode.getDeleted().equals("false")) {
                  // mimeType is all the way at the bottom-level, need to dig it out:
                  String mimeType = artifactNode.getChildren().get("equip:complexData").getChildren().get("jcr:content").getMimeType();
                  artifactNode.setMimeType(mimeType);
               } else {
                  // ignore this artifact in the response
                  continue;
               }
            }

            // Now make it a map for SearchResponse and add some additional fields to match expected format:
            Map<String, Object> row = mapper.convertValue(artifactNode, new TypeReference<LinkedHashMap<String, Object>>() {});
            row.put("jcr:path", artifactNode.getSelf().replace(baseUrl + "items", ""));
            row.put("jcr:name", artifactNode.getSelf().substring(artifactNode.getSelf().lastIndexOf("/") + 1));
            rows.add(row);

            if (!primaryType.equalsIgnoreCase("equipLibrary:baseFolder")) {
               // Will use to check the data type:
               lastFileRow = row;
            }
         }

         if (lastFileRow.get(orderBy) != null) {
            // not all use cases will work with an order by
            // if the order by property doesn't exist on the last node (it's a folder for example), ignore this error
            Comparator<Map<String, Object>> comparator;
            if (isValidDate((String) lastFileRow.get(orderBy))) {
               comparator = (o1, o2) -> fromISO8601UTC((String) o1.get(orderBy)).compareTo((fromISO8601UTC((String) o2.get(orderBy))));
            } else {
               comparator = (o1, o2) -> ((String) o1.get(orderBy)).compareToIgnoreCase((String) o2.get(orderBy));
            }

            if (inReversedOrder) {
               rows.sort(comparator.reversed());
            } else {
               rows.sort(comparator);
            }
         }
      }

      FolderResponse response = new FolderResponse();
      response.setRows(rows);
      return response;
   }

   public LibraryArtifactResponse updateArtifact(String artifactPath, LibraryInput input, String userId) throws Exception {
      // check to see if the content needs to be updated
      MultipartFile fileContent = input.getFileContent();
      String base64Content = null;
      if (fileContent != null) {
         // check to see that the file extension of the new file matches the existing one
         // throw an exception otherwise
         String currentExtension = FilenameUtils.getExtension(artifactPath.substring(0, artifactPath.length() - 1));
         String originalExtension = FilenameUtils.getExtension(fileContent.getOriginalFilename());
         if (!originalExtension.equalsIgnoreCase(currentExtension)) {
            throw new ExtensionMismatchException(String.format("The file extension for this artifact must match its predecessor of '%s'", currentExtension));
         }
         base64Content = Base64.getEncoder().encodeToString(fileContent.getBytes());
      }

      String url = baseUrl + "items/" + (artifactPath.contains("hidden/") ? "library/" : "") + artifactPath;
      String requestJson = generateArtifactJson(input, base64Content, userId);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

      ArtifactNode node = restTemplate.exchange(url, HttpMethod.PUT, entity, ArtifactNode.class).getBody();
      String nodeId = node.getId();
      log.debug("Successfully updated artifact with ID {}.", nodeId);

      // construct response object
      LibraryArtifactResponse response = new LibraryArtifactResponse();
      response.setArtifactId(nodeId);
      response.setArtifactPath(node.getSelf());
      response.setResponse(Response.OK);
      return response;
   }

   private String generateChildrenJson(Map<String, BaseExistingNode> children, String targetArtifactId, boolean ignoreTargetArtifact) throws Exception {
      Map<String, Object> newChildren = new LinkedHashMap<String, Object>();
      for (Map.Entry<String, BaseExistingNode> entry : children.entrySet()) {
         BaseExistingNode node = entry.getValue();
         // add the target artifact to the children map (only the key as GUID has to be present in the request)
         newChildren.put(node.getId(), new Object());
      }

      // add the target artifact to the children map (only the key as GUID has to be present in the request)
      if (!ignoreTargetArtifact) {
         newChildren.put(targetArtifactId, new Object());
      }
      Map<String, Object> finalChildren = new LinkedHashMap<String, Object>();
      finalChildren.put("children", newChildren);

      ObjectMapper mapper = new ObjectMapper();
      // the ModeShape REST API requires us to pass an empty object for the GUID property value
      // we have to explicitly configure Jackson to allow us to serialize this empty JSON object
      // as it would throw an exception otherwise
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      String requestJson = mapper.writeValueAsString(finalChildren);
      return requestJson;
   }

   public LibraryArtifactResponse moveArtifact(String newParentPath, LibraryInput input) throws Exception {
      try {
         // trim any trailing '/' character
         if (newParentPath.endsWith("/")) {
            newParentPath = newParentPath.substring(0, newParentPath.length() - 1);
         }

         // get the new folder information so we can keep its children nodes
         String url = baseUrl + "items/" + newParentPath;
         FolderNode folderNode = null;
         try {
            folderNode = restTemplate.getForObject(url, FolderNode.class);
         } catch (HttpClientErrorException e) {
            // it's possible that the new parent folder will not exist in the case of a folder re-name
            // if so, we need to create the new parent folder
            if (e.getMessage().equals("404 Not Found")) {
               log.debug("New parent folder does not exist, creating it...");
               addFolder(newParentPath);
               folderNode = restTemplate.getForObject(url, FolderNode.class);
            } else {
               throw e;
            }
         }
         Map<String, BaseExistingNode> children = folderNode.getChildren();

         // check if new parent folder is empty
         if (children == null) {
            children = new LinkedHashMap<String, BaseExistingNode>();
         }

         // get the target artifact to move
         String targetArtifactOldPath = input.getTargetArtifact();
         url = baseUrl + "items/" + targetArtifactOldPath;
         ArtifactNode artifactNode = restTemplate.getForObject(url, ArtifactNode.class);
         String targetArtifactId = artifactNode.getId();
         String artifactType = artifactNode.getPrimaryType();
         String requestJson = null;
         boolean isFolderMove = false;
         if (!artifactType.equals(NewFolderNode.EQUIP_FOLDER_TYPE)) {
            requestJson = generateChildrenJson(children, targetArtifactId, false);
         } else {
            // for folders, we are not moving the folder node itself, we are moving its children
            Map<String, ArtifactNode> folderChildren = artifactNode.getChildren();
            if (folderChildren != null) {
               children.putAll(folderChildren);
            }
            requestJson = generateChildrenJson(children, targetArtifactId, true);
            isFolderMove = true;
         }

         // update the folder with the new children value
         url = baseUrl + "items/" + newParentPath;
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
         folderNode = restTemplate.exchange(url, HttpMethod.PUT, entity, FolderNode.class).getBody();

         // finally, if this is a folder move, delete the original folder
         if (isFolderMove) {
            url = baseUrl + "nodes/" + targetArtifactId;
            restTemplate.delete(url);
         }

         LibraryArtifactResponse response = new LibraryArtifactResponse();
         response.setResponse(Response.OK);
         // TODO: If this is a folder, then we are auditing the deleted ID. Check whether this is a problem.
         response.setArtifactId(targetArtifactId);
         return response;
      } catch (Exception e) {
         log.error("Exception occurred moving artifact.", e);
         throw new TransactionException("An exception occurred during the operations required to create a move an artifact. Please ask an admin to check logs.", e);
      }
   }

   public LibraryArtifactResponse promoteArtifact(String artifactPath) throws Exception {
      ArtifactNode firstNode = null;
      try {
         String url = baseUrl + "items/" + artifactPath + DEPTH_STRING;
         // serialize to the BaseArtifactNode since we are only going to copy the custom properties for creating the copy of this artifact in the global library
         BaseLibraryArtifactContentNode currentArtifactNode = restTemplate.getForObject(url, BaseLibraryArtifactContentNode.class);

         // get its content object and convert to base 64
         ContentInfo info = getArtifactContent(artifactPath, true);
         String base64Content = Base64.getEncoder().encodeToString(info.getContent());
         currentArtifactNode.setContent(base64Content);

         // create new artifact in global library, in the root (trim trailing '/' at end)
         String filename = artifactPath.substring(0, artifactPath.length() - 1);
         filename = filename.substring(filename.lastIndexOf("/") + 1);
         String globalArtifactPath = GLOBAL_LIBRARY_START + "/" + filename;;
         
         // verify non-existent
         try {
            LibraryArtifactResponse response = getArtifact(globalArtifactPath, true);
            if (response.getResponse() == Response.OK) {
               throw new ArtifactAlreadyExistsException(String.format("The artifact with name '%s' at path '%s' already exists.", filename, artifactPath));
            }
         } catch (HttpClientErrorException e) {
            if (!e.getMessage().equals("404 Not Found")) {
               // ignore the 404 not found, otherwise re-throw
               throw e;
            }
         }
         
         url = baseUrl + "items/" + globalArtifactPath;
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         ObjectMapper mapper = new ObjectMapper();
         String jsonValue = mapper.writeValueAsString(currentArtifactNode);
         HttpEntity<String> entity = new HttpEntity<String>(jsonValue, headers);
         // First call to create the base artifact:
         firstNode = restTemplate.postForObject(url, entity, ArtifactNode.class);
         // Do a PUT similar to addArtifact, so the first version gets created:
         restTemplate.exchange(url, HttpMethod.PUT, entity, ArtifactNode.class).getBody();
         // Retrieve it for the response. Must be separate because of the 'depth' parameter in order to include comments in response.
         // Alternatively, could clear out the children from globalArtifactNode to avoid the extra call.
         ArtifactNode globalArtifactNode = restTemplate.getForObject(url + DEPTH_STRING, ArtifactNode.class);

         LibraryArtifactResponse response = new LibraryArtifactResponse(globalArtifactNode, supportedTypes, baseUrl + "items/");
         response.setResponse(Response.OK);
         response.setArtifactId(globalArtifactNode.getId());
         return response;
      } catch (Exception e) {
         if (e instanceof ArtifactAlreadyExistsException) {
            // this was thrown by code above, simply re-throw
            throw e;
         }

         log.error("Exception occurred adding new artifact.", e);
         if (firstNode != null) {
            String firstNodeId = firstNode.getId();
            try {
               // need to delete the dangling node, since this means the content creation failed
               String url = baseUrl + "nodes/" + firstNodeId;
               restTemplate.delete(url);
            } catch (Exception e2) {
               log.error("Deletion of dangling first node {} failed.", firstNodeId, e2);
            }
         }
         throw new TransactionException("An exception occurred during the operations required to create a new artifact.", e);
      }
   }

   private VersionHistoryNode getVersionHistoryNode(String artifactPath) throws UnsupportedEncodingException {
      // first retrieve the node to get its version history path
      String url = baseUrl + "items/" + artifactPath;
      ArtifactNode artifactNode = restTemplate.getForObject(url, ArtifactNode.class);
      String versionHistoryPath = artifactNode.getVersionHistoryPath();

      // now retrieve the version history node
      VersionHistoryNode historyNode = restTemplate.getForObject(versionHistoryPath, VersionHistoryNode.class);
      return historyNode;
   }

   public VersionHistoryResponse getVersionHistory(String artifactPath) throws Exception {
      VersionHistoryNode historyNode = getVersionHistoryNode(artifactPath);

      // for each version, add to the response
      List<VersionHistoryResponseItem> historyList = new ArrayList<VersionHistoryResponseItem>();
      int versionCount = historyNode.getVersionCount();
      for (int i = 1; i <= versionCount; i++) {
         String versionPath = historyNode.getVersionPath(i);
         log.debug("Found version node {} for artifact {}.", versionPath, artifactPath);

         // create the artifact node for that specific version
         ArtifactNode versionNode = restTemplate.getForObject(versionPath + DEPTH_STRING, ArtifactNode.class);
         String versionId = versionNode.getId();

         VersionHistoryResponseItem historyItem = new VersionHistoryResponseItem();
         historyItem.setVersion(i);
         historyItem.setVersionId(versionId);
         LibraryArtifactResponse artifactInfo = new LibraryArtifactResponse(versionNode, supportedTypes, baseUrl + "items/");
         historyItem.setArtifactInfo(artifactInfo);
         historyList.add(historyItem);
      }

      VersionHistoryResponse response = new VersionHistoryResponse();
      response.setVersionHistory(historyList);
      return response;
   }

   public LibraryArtifactResponse deleteArtifact(String artifactPath) {
      String url = baseUrl + "items/" + artifactPath;
      restTemplate.delete(url);

      LibraryArtifactResponse response = new LibraryArtifactResponse();
      response.setResponse(Response.OK);
      response.setArtifactPath(artifactPath);
      return response;
   }

   private static boolean isValidDate(String dateString) {
      try {
         new SimpleDateFormat(DATE_FORMAT).parse(dateString);
         return true;
      } catch (ParseException pe) {
         return false;
      }
   }
}
