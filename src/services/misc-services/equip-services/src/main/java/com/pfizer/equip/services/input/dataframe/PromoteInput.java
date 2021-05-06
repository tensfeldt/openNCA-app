package com.pfizer.equip.services.input.dataframe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoteInput {
   private String id;
   private String dataStatus;
   private String dataBlindingStatus;
   private String comment;
   private PromoteMode mode;
   private ScriptDto[] scripts;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getDataStatus() {
      return dataStatus;
   }

   public void setDataStatus(String dataStatus) {
      this.dataStatus = dataStatus;
   }

   public String getDataBlindingStatus() {
      return dataBlindingStatus;
   }

   public void setDataBlindingStatus(String dataBlindingStatus) {
      this.dataBlindingStatus = dataBlindingStatus;
   }

   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public PromoteMode getMode() {
      return mode;
   }

   public void setMode(String mode) {
      this.mode = PromoteMode.fromString(mode);
   }

   public ScriptDto[] getScripts() {
      return scripts;
   }

   public void setScripts(ScriptDto[] scripts) {
      this.scripts = scripts;
   }
}
