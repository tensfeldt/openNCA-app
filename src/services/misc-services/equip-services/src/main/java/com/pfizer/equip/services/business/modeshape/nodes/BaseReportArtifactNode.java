package com.pfizer.equip.services.business.modeshape.nodes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * This class allows for a level of simple abstraction over BaseLibraryArtifact and provides some modularization for the two services.
 * This class may override BaseLibraryArtifaceNode methods. If needed, we could decouple the two services via this class.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_ABSENT)
public class BaseReportArtifactNode extends BaseLibraryArtifactNode {

   @JsonProperty("id")
   private String id;

   @JsonProperty("children")
   private Map<String, KeyValuePairNode> children;
   
   @JsonIgnore
   private HttpMethod serializationMode = HttpMethod.PUT;

   public List<Map.Entry<String, KeyValuePairNode>> getChildren() {
      // ModeShape REST API expects "children" attribute to look like this:
      // "children": [
      //              {
      //                  "equipLibrary:parameters": {
      //                      "jcr:primaryType": "equipLibrary:kvp",
      //                      "equipLibrary:key": "key",
      //                      "equipLibrary:value": "value"
      //                  }
      //              },
      //              {
      //                  "equipLibrary:parameters": {
      //                      "jcr:primaryType": "equipLibrary:kvp",
      //                      "equipLibrary:key": "key",
      //                      "equipLibrary:value": "value"
      //                  }
      //              }
      //          ]
      // This getter translates children into the appropriate format
      // Need a list of key-value pairs instead of a standard dictionary:
      List<Map.Entry<String, KeyValuePairNode>> keyValuePairNodes = new ArrayList<Map.Entry<String, KeyValuePairNode>>();
      for (String childId : children.keySet()) {
         // ModeShape returns a map of children in the form of "childName:childValue, childName[2]:childValue, ...", need to strip out the brackets
         Map.Entry<String, KeyValuePairNode> kvp = null;
         if (serializationMode.equals(HttpMethod.POST)) {
            kvp = new AbstractMap.SimpleEntry<String, KeyValuePairNode>(childId.replaceAll("\\[.*\\]", ""), children.get(childId));
         } else if (serializationMode.equals(HttpMethod.PUT)) {
            kvp = new AbstractMap.SimpleEntry<String, KeyValuePairNode>(childId, children.get(childId));
         }
         keyValuePairNodes.add(kvp);
      }
      return keyValuePairNodes;
   }

   public void setChildren(Map<String, KeyValuePairNode> children) {
      this.children = children;
   }

   public HttpMethod getSerializationMode() {
      return serializationMode;
   }

   public void setSerializationMode(HttpMethod serializationMode) {
      this.serializationMode = serializationMode;
   }

   @JsonIgnore
   @Override
   public Map<String, KeyValuePairNode> getParameters() {
      // the parameters attribute is deserialized as children
      return children;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }
}