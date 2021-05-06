package com.pfizer.equip.services.responses.opmeta;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pfizer.equip.services.controllers.shared.JsonPropertyNameEditor;
import com.pfizer.equip.shared.opmeta.entity.Attachment;
import com.pfizer.equip.shared.opmeta.entity.mixins.ApplicationMixin;
import com.pfizer.equip.shared.responses.AbstractResponse;

public class AttachmentVersionHistoryResponse extends AbstractResponse {
   
   public AttachmentVersionHistoryResponse() {}
   
   public AttachmentVersionHistoryResponse(List<AttachmentVersionHistoryResponseItem> versionHistory) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setAnnotationIntrospector(new JsonPropertyNameEditor("opmeta:", "", "equip:", "", "jcr:", ""));
      mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
      mapper.addMixIn(Attachment.class, ApplicationMixin.class);
      JsonNode[] attachmentsOut = mapper.convertValue(versionHistory, JsonNode[].class);
      this.versionHistory = attachmentsOut;
   }

   @JsonInclude(Include.ALWAYS)
   @JsonProperty("versionHistory")
   private JsonNode[] versionHistory;

   @JsonIgnore
   public JsonNode[] getVersionHistory() {
      return versionHistory;
   }
}