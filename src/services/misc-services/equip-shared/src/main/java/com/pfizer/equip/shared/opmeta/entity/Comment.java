package com.pfizer.equip.shared.opmeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
   
   @JsonIgnore
   public static final String PRIMARY_TYPE = "equip:comment";
   
   @JsonProperty("jcr:primaryType")
   String primaryType = PRIMARY_TYPE;

   @JsonProperty("equip:commentType")
   String commentType = "default";

   @JsonProperty("equip:body")
   String body;

   public Comment(){}

   public Comment(String body) {
      this.body = body;
   }

   public String getPrimaryType() {
      return primaryType;
   }

   public String getCommentType() {
      return commentType;
   }

   public String getBody() {
      return body;
   }

   public void setBody(String body) {
      this.body = body;
   }
}