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
import com.pfizer.equip.shared.opmeta.folder.IndicationFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_study_indication_v")
@JsonInclude(Include.ALWAYS)
public class Indication extends BaseProtocolChildNode {
   
   public Indication() {
      primaryType = "opmeta:indication";
      nodeFolder = new IndicationFolder();
   }

   @ManyToOne
   @JoinColumn(name="study_id", insertable=false, updatable=false)
   @JsonIgnore
   Protocol protocol;

   @Id 
   @Column(name = "study_id")
   @JsonIgnore
   String studyId;
   
   @Id 
   @Column(name = "indication_verbatim_term")
   @JsonProperty("opmeta:indicationVerbatimTerm")
   String indicationVerbatimTerm;

   @Column(name = "ind_preferred_term")
   @JsonProperty("opmeta:indicationPreferredTerm")
   String indicationPreferredTerm;

   @Column(name = "indication_meddra_code")
   @JsonProperty("opmeta:indicationMedraCode")
   String indicationMeddraCode;

   @Override
   public String getPath() {
      return String.format("%s/%s", IndicationFolder.NAME, IndicationFolder.CHILD_NODE_NAME);
   }
}
