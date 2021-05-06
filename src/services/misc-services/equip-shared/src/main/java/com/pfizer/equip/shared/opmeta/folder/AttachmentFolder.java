package com.pfizer.equip.shared.opmeta.folder;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.entity.Attachment;
import com.pfizer.equip.shared.opmeta.entity.Protocol;

@SuppressWarnings("serial")
public class AttachmentFolder extends BaseFolder {
   // Cannot be static for Jackson:
   @JsonProperty("jcr:primaryType")
   public final String PRIMARY_TYPE =  "opmeta:attachmentFolder";
   public static final String NAME = "Attachments";

   @JsonProperty("children")
   Map<String, Attachment> children;

   public Map<String, Attachment> getChildren() {
      return children;
   }

   public void setChildren(Map<String, Attachment> children) {
      this.children = children;
   }
}
