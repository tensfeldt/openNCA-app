package com.pfizer.equip.services.responses.opmeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pfizer.equip.services.controllers.shared.JsonPropertyNameEditor;
import com.pfizer.equip.shared.opmeta.entity.Attachment;
import com.pfizer.equip.shared.opmeta.entity.KeyValuePair;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.mixins.ApplicationMixin;
import com.pfizer.equip.shared.responses.AbstractResponse;
import com.pfizer.equip.shared.service.user.UserInfo;

@JsonInclude(Include.NON_NULL)
public class OperationalMetadataResponse extends AbstractResponse {
   private JsonNode[] nodes;
   @JsonProperty("programs")
   private JsonNode[] programs;
   private Set<UserInfo> users;
   private String nodeId;
   private String nodePath;

   public OperationalMetadataResponse() {}

   public OperationalMetadataResponse(Set<Program> programs) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setAnnotationIntrospector(new JsonPropertyNameEditor("opmeta:", ""));
      mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      Set<Map<String, Object>> programsOut = new HashSet<Map<String, Object>>();

      // To support arbitrary custom attributes, need to use maps instead of POJOs/beans
      // So, convert each node type to a map and insert the KVPs. Then structure things back into the expected front-end format.
      // Pull out the reference to the set:
      for (Program program : programs) {
         Set<Map<String, Object>> protocols = new HashSet<Map<String, Object>>();
         Map<String, Object> programMap = mapper.convertValue(program, new TypeReference<Map<String, Object>>() {});
         if (program.getProtocols() != null) {
            for (Protocol protocol : program.getProtocols()) {
               Set<KeyValuePair> kvps = protocol.getCustomAttributes();
               // And remove it from the bean, otherwise we'll have the extra mis-serialized:
               protocol.setCustomAttributes(null);
               // TODO: Possibly need a way of supporting front-end soft deletion:
               protocol.setDeleteFlag(null);
               Map<String, Object> protocolMap = mapper.convertValue(protocol, new TypeReference<Map<String, Object>>() {});
               if (kvps != null) {
                  for (KeyValuePair kvp : kvps) {
                     // Makes these seem like normal, top-level attributes even though they are custom:
                     protocolMap.put(kvp.getKey(), kvp.getValue());
                  }
               }
               // Front-end expects "protocols" to be a list. Consider asking to refactor.
               protocols.add(protocolMap);
            }
         }
         programMap.put("protocols", protocols);
         programsOut.add(programMap);
      }
      this.programs = mapper.convertValue(programsOut, JsonNode[].class);
   }

   public OperationalMetadataResponse(List<Attachment> attachments) {
      makeAttachmentsResponse(attachments);
   }

   public OperationalMetadataResponse(Attachment attachment) {
      List<Attachment> attachments = new ArrayList<Attachment>(Arrays.asList(attachment));
      makeAttachmentsResponse(attachments);
   }
   
   private void makeAttachmentsResponse(List<Attachment> attachments) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setAnnotationIntrospector(new JsonPropertyNameEditor("opmeta:", "", "equip:", "", "jcr:", ""));
      mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
      mapper.addMixIn(Attachment.class, ApplicationMixin.class);
      JsonNode[] attachmentsOut = mapper.convertValue(attachments, JsonNode[].class);
      this.nodes = attachmentsOut;
   }

   public JsonNode[] getNodes() {
      return nodes;
   }

   public void setNodes(JsonNode[] nodes) {
      this.nodes = nodes;
   }

   public Set<UserInfo> getUsers() {
      return users;
   }

   public void setUsers(Set<UserInfo> users) {
      this.users = users;
   }

   public String getNodeId() {
      return nodeId;
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }

   public String getNodePath() {
      return nodePath;
   }

   public void setNodePath(String nodePath) {
      this.nodePath = nodePath;
   }
}
