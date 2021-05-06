package com.pfizer.equip.services.responses.library;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
public class MCTArtifactResponse {
   private String id;
   private String name;

   public MCTArtifactResponse(String id, String name) {
      this.id = id;
      this.name = name;
   }

   public MCTArtifactResponse() {}

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
