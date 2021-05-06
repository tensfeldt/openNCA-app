package com.pfizer.equip.shared.opmeta.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.folder.ProtocolAliasFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_study_alias_v")
@JsonInclude(Include.ALWAYS)
public class ProtocolAlias extends BaseProtocolChildNode {

   public ProtocolAlias() {
      primaryType = "opmeta:studyAlias";
      nodeFolder = new ProtocolAliasFolder();
   }

   @ManyToOne
   @JoinColumn(name="study_id", insertable=false, updatable=false)
   @JsonIgnore
   Protocol protocol;

   @Column(name = "study_id")
   @JsonIgnore
   String studyId;
   
   @Id 
   @JsonProperty("opmeta:aliasType")
   @Column(name = "alias_type")
   String aliasType;

   @Id 
   @JsonProperty("opmeta:studyAlias")
   @Column(name = "study_alias")
   String studyAlias;

   @Override
   public String getPath() {
      return String.format("%s/%s", ProtocolAliasFolder.NAME, ProtocolAliasFolder.CHILD_NODE_NAME);
   }

   public String getAliasType() {
      return aliasType;
   }

   public void setAliasType(String aliasType) {
      this.aliasType = aliasType;
   }

   public String getStudyAlias() {
      return studyAlias;
   }

   public void setStudyAlias(String studyAlias) {
      this.studyAlias = studyAlias;
   }
}
