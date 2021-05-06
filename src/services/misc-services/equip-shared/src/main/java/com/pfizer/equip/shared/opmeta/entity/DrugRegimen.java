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
import com.pfizer.equip.shared.opmeta.folder.DrugRegimenFolder;

@SuppressWarnings("serial")
@Entity
//@Embeddable
@Table(schema = "podsdal", name = "pods_ods_study_drug_regimen_v")
@JsonInclude(Include.ALWAYS)
public class DrugRegimen extends BaseProtocolChildNode {
   
   public DrugRegimen() {
      primaryType = "opmeta:drugRegimen";
      nodeFolder = new DrugRegimenFolder();
      this.sourceCreationTimestamp = super.sourceCreationTimestamp; // Needed because we're shadowing for now.
   }

   @ManyToOne
   @JoinColumn(name="study_id", insertable=false, updatable=false)
   @JsonIgnore
   Protocol protocol;

   @Id
   @Column(name = "rowid")
   @JsonIgnore
   String rowId;
   
   @Column(name = "study_id")
   @JsonIgnore
   String studyId;
   
   @Column(name = "drug_form")
   @JsonProperty("opmeta:drugForm")
   String drugForm; 
   
   @Column(name = "drug_max_daily_dose")
   @JsonProperty("opmeta:drugMaxDailyDose")
   Double drugMaxDailyDose;
   
   @Column(name = "drug_max_dose_unit")
   @JsonProperty("opmeta:drugMaxDailyDoseUnit")
   String drugMaxDoseUnit;
   
   @Column(name = "drug_route")
   @JsonProperty("opmeta:drugRoute")
   String drugRoute;
   
   @Column(name = "drug_trtmnt_verb_term")
   @JsonProperty("opmeta:drugTreatmentVerbatimTerm")
   String drugTreatmentVerbatimTerm;
   
   @Column(name = "drug_preferred_term")
   @JsonProperty("opmeta:drugPreferredTerm")
   String drugPreferredTerm;
   
   @Column(name = "drug_type")
   @JsonProperty("opmeta:drugType")
   String drugType;

   // TODO: attribs
   //@Column(name = "delete_flag")
   //@JsonProperty("opmeta:deleteFlag")
   //String deleteFlag;
   
   //@Column(name = "created_by")
   //@JsonProperty("opmeta:createdBy")
   //String createdBy;
   
   //@Column(name = "end_ts")
   //@JsonProperty("opmeta:endTimestamp")
   //String endTimestamp;
   
   //@Column(name = "updated_by")
   //@JsonProperty("opmeta:updatedBy")
   //String updatedBy;
   
   //@Column(name = "src_sys_name")
   //@JsonProperty("opmeta:sourceSystemName")
   //String sourceSystemName;
   
   @Column(name = "drug_preferred_code")
   @JsonProperty("opmeta:drugPreferredCode")
   String drugPreferredCode;

   @Override
   public String getPath() {
      return String.format("%s/%s", DrugRegimenFolder.NAME, DrugRegimenFolder.CHILD_NODE_NAME);
   }

}
