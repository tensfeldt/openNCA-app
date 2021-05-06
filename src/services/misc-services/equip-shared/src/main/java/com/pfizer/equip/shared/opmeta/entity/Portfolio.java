package com.pfizer.equip.shared.opmeta.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_study_portfolio_v")
@JsonInclude(Include.ALWAYS)
public class Portfolio extends BaseNode {

   public Portfolio() {
      primaryType = "opmeta:portfolio";
   }
   
   @JsonIgnore
   @Transient
   public static final String NODE_NAME = "Portfolio";

   @OneToOne
   @JoinColumn(name="study_id", insertable=false, updatable=false)
   @JsonIgnore
   Protocol protocol;

   @Id 
   @Column(name = "study_id")
   @JsonIgnore
   String studyId;

   @Column(name = "budget_source")
   @JsonProperty("opmeta:budgetSource")
   String budgetSource;
   
   @Column(name = "business_group")
   @JsonProperty("opmeta:businessGroup")
   String businessGroup;
   
   @Column(name = "design")
   @JsonProperty("opmeta:design")
   String design;
   
   @Column(name = "study_funding")
   @JsonProperty("opmeta:studyFunding")
   String studyFunding;

   @Column(name = "study_project_short_name")
   @JsonProperty("opmeta:studyProjectShortName")
   String studyProjectShortName;

   @Override
   public String getPath() {
      return String.format("%s", NODE_NAME);
   }

}
