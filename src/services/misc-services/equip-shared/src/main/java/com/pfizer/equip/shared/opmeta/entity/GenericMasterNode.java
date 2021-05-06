package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.SourceType;

@SuppressWarnings("serial")
public class GenericMasterNode extends BaseNode {
   
   @JsonIgnore
   String path;

   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:studyId")
   String studyId;

   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:candidateCode")
   String projectCode;

   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:programCode")
   String programCode;
   
   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:candidateProject")
   String candidateProjectUuid;

   @JsonProperty("opmeta:currentSnapshot")
   String currentSnapshot;

   @JsonProperty("opmeta:activeFlag")
   Boolean activeFlag;

   @JsonInclude(Include.NON_NULL)
   @JsonProperty("equip:deleteFlag")
   private Boolean deleteFlag;

   @JsonProperty("opmeta:source")
   @Transient
   protected SourceType source;

   @JsonProperty("opmeta:comments")
   @Transient
   String comments;

   public GenericMasterNode() {
   }

   public GenericMasterNode(BaseNode node) {
      this.primaryType = node.primaryType;
      this.path = node.primaryType;
      
      // TODO: see if we really need this
      if (node instanceof Program) {
         this.programCode = ((Program) node).getProgramCode();
         this.source = ((Program) node).getSource();
      }
      if (node instanceof Project) {
         this.programCode = ((Project) node).getProgramCode();
         this.projectCode = ((Project) node).getProjectCode();
         this.source = ((Project) node).getSource();
      }
      if (node instanceof Protocol) {
         this.programCode = ((Protocol) node).getProgramCode();
         this.projectCode = ((Protocol) node).getProjectCode();
         this.studyId     = ((Protocol) node).getStudyId();
         this.candidateProjectUuid = ((Protocol) node).getCandidateProjectUuid();
         this.activeFlag = ((Protocol) node).getActiveFlag();
         this.deleteFlag = ((Protocol) node).getDeleteFlag();
         this.source = ((Protocol) node).getSource();
      }
      
      this.modified = node.getModified();
      this.modifiedBy = node.getModifiedBy();
      //this.sourceCreationTimestamp = node.getSourceCreationTimestamp(); // TODO: figure out why serialization in PUT is not working.
   }
   

   public void setprimaryType(String primaryType) {
      this.primaryType = primaryType;
   }

   public String getPath() {
      return path;
   }

   public String getCurrentSnapshot() {
      return currentSnapshot;
   }

   public void setCurrentSnapshot(String currentSnapshot) {
      this.currentSnapshot = currentSnapshot;
   }

   public String getStudyId() {
      return studyId;
   }

   public void setStudyId(String studyId) {
      this.studyId = studyId;
   }

   public String getProgramCode() {
      return programCode;
   }

   public void setProgramCode(String programCode) {
      this.programCode = programCode;
   }
   
   public Boolean getDeleteFlag() {
      return deleteFlag;
   }

   public void setDeleteFlag(Boolean deleteFlag) {
      this.deleteFlag = deleteFlag;
   }

   public SourceType getSource() {
      return source;
   }

   public void setSource(SourceType source) {
      this.source = source;
   }

   public String getComments() {
      return comments;
   }

   public void setComments(String comments) {
      this.comments = comments;
   }

   @Override
   @JsonIgnore
   public Date getSourceCreationTimestamp() { return null; } // dummy method to force jackson to ignore this field 
}
