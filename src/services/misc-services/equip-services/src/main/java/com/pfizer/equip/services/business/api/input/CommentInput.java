package com.pfizer.equip.services.business.api.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentInput {
   private String type = "comment";
   private String commentType = "user";
   private String[] metadata = {};
   private String body;
   
   public CommentInput() {}
   
   public CommentInput(String body) {
      this.body = body;
   }

   public String getType() {
      return type;
   }

   public String getCommentType() {
      return commentType;
   }
   
   public void setCommentType(String commentType) {
      this.commentType = commentType;
   }

   public String[] getMetadata() {
      return metadata;
   }

   public String getBody() {
      return body;
   }

   public void setBody(String body) {
      this.body = body;
   }
}
