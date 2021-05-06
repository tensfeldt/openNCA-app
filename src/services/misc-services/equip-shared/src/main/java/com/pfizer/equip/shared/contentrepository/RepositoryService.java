package com.pfizer.equip.shared.contentrepository;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeAlreadyExistsException;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeNotFoundException;
import com.pfizer.equip.shared.properties.OpmetaModeShapeServiceProperties;

/**
 * Service for interacting with content/metadata repository. 
 * <p>
 * Provides generic methods such as getNode, addNode, etc. for interacting with content/metadata repository (ModeShape).
 * </p>
 */
@Service
public class RepositoryService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   
   @Autowired
   private OpmetaModeShapeServiceProperties modeShapeProperties;

   @Autowired
   ObjectMapper mapper;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   private RestTemplate restTemplate; // not a bean
   
   private String itemsUrl;
   private String queryUrl;
   private String nodesUrl;

   public final static String DATA_PATH = "equip:complexData/jcr:content/jcr:data";

   @PostConstruct
   private void initialize() {
      String serviceUser = modeShapeProperties.getUser();
      String servicePassword = modeShapeProperties.getPassword();
      String baseUrl = modeShapeProperties.getUrl() + modeShapeProperties.getRepository() + "/" + modeShapeProperties.getWorkspace();
      itemsUrl = baseUrl + "/items";
      queryUrl = baseUrl + "/query";
      nodesUrl = baseUrl + "/nodes";
      //baseUrl = "http://localhost:8080/modeshape-rest/sample/default/";
      restTemplate = restTemplateBuilder.basicAuthorization(serviceUser, servicePassword).build();
      // Allow for empty objects {} for attachment child update quirks. 
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
   }

   public String addNode(Object node, String nodePath) {
      String nodeId = setNode(node, nodePath, HttpMethod.POST);
      log.debug("Successfully added new node with ID {}.", nodeId);
      return nodeId;
   }

   public Map<String, String> addNodes(Map<String, Object> nodes) {
      Map<String, String> nodeMap = setNodes(nodes, HttpMethod.POST);
      log.debug("Successfully added new nodes: {}", nodeMap.toString());
      return nodeMap;
   }

   public String deleteNode(String nodePath) {
      String nodeId = setNode(null, nodePath, HttpMethod.DELETE);
      log.debug("Successfully deleted node with ID {}.", nodeId);
      return nodeId;
   }

   public Map<String, String> deleteNodes(List<String> nodePaths) {
      Map<String, String> nodeMap = setNodes(nodePaths, HttpMethod.DELETE);
      log.debug("Successfully deleted nodes: {}", nodePaths);
      return nodeMap;
   }

   public <T> T getNode(Class<T> nodeClass, String nodePath, DepthType depth) {
      try {
         String url = itemsUrl + nodePath + "?depth=" + depth;
         T node = restTemplate.getForObject(url, nodeClass);
         return node;
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Node with path %s not found into content repository", nodePath));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public <T> T getNode(Class<T> nodeClass, String nodePath) {
      return getNode(nodeClass, nodePath, DepthType.WITH_TOP_LEVEL);
   }

   public ContentInfo getBinaryNodeById(String nodeId) {
      try {
         // TODO: see if there's a simpler/more performant way of doing this.
         final String NODE_ID = "nodeId";
         String url = String.format("%s/%s", nodesUrl, nodeId); 
         log.debug("Retrieving content from {}", url);
         String nodeUrl = restTemplate.getForObject(url, JsonNode.class).get("self").asText();
         String contentUrl = String.format("%s/%s", nodeUrl.replace("/items/", "/binary/"), DATA_PATH);
         contentUrl = URLDecoder.decode(contentUrl, "UTF-8");
         ResponseEntity<byte[]> response = restTemplate.exchange(contentUrl, HttpMethod.GET, null, byte[].class);
         String mimeType = response.getHeaders().get("Content-Type").get(0).toString();
         byte[] content = response.getBody();
         
         String sql = String.format("select [jcr:name] from [nt:base] where [jcr:uuid] = $%s", NODE_ID);
         Map<String, String> bindVariables = new HashMap<String, String>();
         bindVariables.put(NODE_ID, nodeId);

         // get the "rows" attribute, only one result since by id, then get the name.
         String nodeName;
         JsonNode queryResults = executeQuery(JsonNode.class, sql, bindVariables);
         if (queryResults.has("rows")) {
            nodeName = queryResults.get("rows").get(0).get("jcr:name").asText();
         } else {
            log.warn("Could not find jcr:name for uuid {}. This usually means the user is retrieving a content version.", nodeId);
            nodeName = nodeId; // TODO: Use MimeTypeExtensions to resolve file extension? Worthwhile?
         }
         ContentInfo binaryInfo = new ContentInfo(content, mimeType, nodeName);
         return binaryInfo;
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Node with id %s not found into content repository", nodeId));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public String getIdByPath(String nodePath) {
      try {
         log.info("Retrieving ID for path {}", nodePath);
         String url = itemsUrl + nodePath;
         JsonNode jsonNode = restTemplate.getForObject(url, JsonNode.class);
         log.info("Successfully retrieved ID.");
         return jsonNode.get("id").asText();
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Could not retrieve ID for path %s", nodePath));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public String getIdByUrl(String nodeUrl) {
      try {
         log.info("Retrieving ID for URL {}", nodeUrl);
         JsonNode jsonNode = restTemplate.getForObject(URLDecoder.decode(nodeUrl, "UTF-8"), JsonNode.class);
         return jsonNode.get("id").asText();
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Could not retrieve ID for url %s", nodeUrl));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public String getPathById(String nodeId) {
      try {
         log.info("Retrieving Path for ID {}", nodeId);
         String nodeUrl = String.format("%s/%s", nodesUrl, nodeId);
         JsonNode node = restTemplate.getForObject(nodeUrl, JsonNode.class); // verify existent
         return node.get("self").asText().replace(itemsUrl, "");
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Could not retrieve path for ID %s", nodeId));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public <T> T getNodeByUrl(Class<T> nodeClass, String nodeUrl, DepthType depth) {
      try {
         log.info("Retrieving node for URL {}", nodeUrl);
         String url = nodeUrl + "?depth=" + depth;
         T node = restTemplate.getForObject(URLDecoder.decode(url, "UTF-8"), nodeClass);
         return node;
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Could not retrieve node for url %s", nodeUrl));
         } else {
            log.error("HTTP exception occured while getting node.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node.", e);
         throw new RuntimeException(e);
      }
   }

   public <T> T getNodeByUrl(Class<T> nodeClass, String nodeUrl) {
      return getNodeByUrl(nodeClass, nodeUrl, DepthType.WITH_TOP_LEVEL);
   }

   public <T> T getNodeById(Class<T> nodeClass, String nodeId, DepthType depth) {
      try {
         String url = nodesUrl +"/"+ nodeId + "?depth=" + depth;
         T node = restTemplate.getForObject(URLDecoder.decode(url, "UTF-8"), nodeClass);
         return node;
      } catch (HttpStatusCodeException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NodeNotFoundException(String.format("Node with uuid %s not found into content repository", nodeId));
         } else {
            log.error("HTTP exception occured while getting node by id.", e);
            log.error("Additional info: '{}'", e.getResponseBodyAsString());
            throw e;
         }
      } catch (Exception e) {
         log.error("Exception occured while getting node by id.", e);
         throw new RuntimeException(e);
      }
   }

   public JsonNode getNodes(List<String> nodePaths) {
      log.debug("Retrieving multiple nodes");
      String sql = "SELECT * FROM [nt:base] WHERE [jcr:path] IN ( '%s' )";
      
      sql = String.format(sql, String.join("', '", nodePaths));
      
      HttpHeaders headers = new HttpHeaders();
      MediaType mediaType = new MediaType("application", "jcr+sql2");
      headers.setContentType(mediaType);
      HttpEntity<String> entity = new HttpEntity<String>(sql, headers);

      JsonNode nodes = restTemplate.postForObject(queryUrl, entity, JsonNode.class);

      return nodes;
   }

   public <T> T executeQuery(Class<T> nodeClass, String sql) {
      return executeQuery(nodeClass, sql, null);
   }

   public <T> T executeQuery(Class<T> nodeClass, String sql, Map<String, String> bindVariables) {
      log.debug("Executing query with the following sql and binds: {}, {}", sql, bindVariables);
      
      // Convert queryUrl + params into query?param1=param2&param1=param2...
      String url;
      if (bindVariables != null) {
         String bindParams = bindVariables.entrySet()
               .stream()
               .map(e -> e.getKey()+"="+e.getValue())
               .collect(Collectors.joining("&"));
         url = String.format("%s?%s", queryUrl, bindParams);
      } else {
         url = String.format("%s", queryUrl);
      }
      
      HttpHeaders headers = new HttpHeaders();
      MediaType mediaType = new MediaType("application", "jcr+sql2");
      headers.setContentType(mediaType);
      HttpEntity<String> entity = new HttpEntity<String>(sql, headers);

      T results = restTemplate.postForObject(url, entity, nodeClass);

      return results;
   }

   private String setNode(Object node, String nodePath, HttpMethod httpMethod) {
      try {
         String url = itemsUrl + nodePath;
         // RestTemplate will encode, so needs to be decoded:
         url = URLDecoder.decode(url, "UTF-8");
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
   
         String jsonValue = mapper.writeValueAsString(node);
         HttpEntity<String> entity = new HttpEntity<String>(jsonValue, headers);
         JsonNode jsonNode = restTemplate.exchange(url, httpMethod, entity, JsonNode.class).getBody();
         String nodeId = null;
         if (jsonNode != null) { // if it's not a delete operation. Consider refactoring.
            nodeId = jsonNode.get("id").asText();
         }
         return nodeId;
      } catch (HttpStatusCodeException e) {
         HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) e;
         log.error("HTTP exception occured while setting node: '{}'", node.toString());
         log.error("Additional info: '{}'", httpStatusCodeException.getResponseBodyAsString());
         throw httpStatusCodeException;
      } catch (Exception e) {
         log.error("Exception occured while setting node: '{}'", node.toString(), e);
         throw new RuntimeException(e);
      }
   }

   public Map<String, String> setNodes(Object nodes, HttpMethod httpMethod) {
      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
   
         ObjectMapper mapper = new ObjectMapper();
         String jsonValue = mapper.writeValueAsString(nodes);
         HttpEntity<String> entity = new HttpEntity<String>(jsonValue, headers);
         JsonNode jsonNode = restTemplate.exchange(itemsUrl, httpMethod, entity, JsonNode.class).getBody();
         Map<String, String> nodeMap = new HashMap<String, String>();
         if (jsonNode != null) { // if it's not a delete operation. Consider refactoring.
            for (JsonNode childNode : jsonNode) {
               nodeMap.put(childNode.get("self").asText().replace(itemsUrl, ""), childNode.get("id").asText());
            }
         }
         return nodeMap;
      } catch (HttpStatusCodeException e) {
         HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) e;
         log.error("HTTP exception occured while setting nodes: '{}'", nodes.toString());
         log.error("Additional info: '{}'", httpStatusCodeException.getResponseBodyAsString());
         throw httpStatusCodeException;
      } catch (Exception e) {
         log.error("Exception occured while setting nodes: '{}'", nodes.toString(), e);
         throw new RuntimeException(e);
      }
   }

   public String updateNode(Object node, String nodePath) {
      String nodeId = setNode(node, nodePath, HttpMethod.PUT);
      log.debug("Successfully updated existing node with ID {}.", nodeId);
      return nodeId;
   }

   public Map<String, Object> getStructure(String nodePath, DepthType maxDepth) {
      JsonNode jsonNode = getNode(JsonNode.class, nodePath, maxDepth);
   
      int currentDepth = 0;
      @SuppressWarnings("unchecked") // Top-level return value is always a Map, so this is safe. Still, maybe find a better way of doing this.
      Map<String, Object> nodeChildTree = (HashMap<String, Object>) addChildStructure(jsonNode, currentDepth, maxDepth);
      
      return nodeChildTree;
   }
   
   public boolean nodeExists(String nodePath) {
      Boolean nodeExists = null;
      try {
         getNode(String.class, nodePath, DepthType.WITH_TOP_LEVEL);
         nodeExists = true;
      } catch (NodeNotFoundException e) {
         nodeExists = false;
      }
      return nodeExists;
   }

   public void verifyNodeNonExistent(String nodePath) {
      if (nodeExists(nodePath)) {
         throw new NodeAlreadyExistsException(String.format("Node at path %s already exists.", nodePath));
      }
   }

   private Object addChildStructure(JsonNode jsonNode, int currentDepth, DepthType maxDepth) {
      // recursive function
      currentDepth++;
      // exit condition, return back up the chain (haven't reached our max depth but no children here so return):
      if (!jsonNode.has("children")) { 
         return null;
      } // continue condition, keep iterating down the chain.
      Iterator<Entry<String, JsonNode>> nodeChildren = jsonNode.get("children").fields();
      if (maxDepth.getValue() - currentDepth == 1) {
         // exit condition, we are now at a leaf node (reached our max depth)
         List<String> nodeChildList = new ArrayList<String>();
         while (nodeChildren.hasNext()) {
            Map.Entry<String, JsonNode> childEntry = (Map.Entry<String, JsonNode>) nodeChildren.next();
            nodeChildList.add(childEntry.getKey()); 
         }
         return nodeChildList;
      } else {
         // continue condition, we are at a branch node
         Map<String, Object> nodeTree = new HashMap<String, Object>();
         while (nodeChildren.hasNext()) {
            Map.Entry<String, JsonNode> childEntry = (Map.Entry<String, JsonNode>) nodeChildren.next();
            nodeTree.put(childEntry.getKey(), addChildStructure(childEntry.getValue(), currentDepth, maxDepth));
         }
         return nodeTree;
      }
   }
}
