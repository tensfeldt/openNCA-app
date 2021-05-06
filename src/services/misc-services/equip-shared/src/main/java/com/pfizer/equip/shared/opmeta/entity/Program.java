package com.pfizer.equip.shared.opmeta.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_drug_program_v")
public class Program extends BaseNode {
   //private static final long serialVersionUID = 1L;
   
   @Transient
   private final String PRIMARY_TYPE = "opmeta:program";

   public Program () {
      primaryType = PRIMARY_TYPE;
      nodeFolder = new ProgramFolder();
   }

   public Program (String programCode) {
      primaryType = PRIMARY_TYPE;
      nodeFolder = new ProgramFolder();
      this.programCode = programCode;
      assignedUsers = new HashSet<String>();
   }

   @Transient
   @JsonProperty("opmeta:currentSnapshot")
   String currentSnapshot;

   @Id 
   @Column(name = "drug_program_cd")
   @JsonProperty("opmeta:programCode")
   @JsonInclude(Include.ALWAYS)
   String programCode;

   @Column(name = "compound")
   @JsonProperty("opmeta:compound")
   @JsonInclude(Include.ALWAYS)
   String compoundNumber;

   @Column(name = "cmpd_disc_therapeutic_area")
   @JsonProperty("opmeta:compoundDiscoveryTherapeuticArea")
   @JsonInclude(Include.ALWAYS)
   String compoundDiscoveryTherapeuticArea;

   @Column(name = "cmpd_mechanism_of_action")
   @JsonProperty("opmeta:compoundMechanismOfAction")
   @JsonInclude(Include.ALWAYS)
   String compoundMechanismOfAction;
   
   @Column(name = "cmpd_name")
   @JsonProperty("opmeta:compoundName")
   @JsonInclude(Include.ALWAYS)
   String compoundName;
   
   @Column(name = "cmpd_source")
   @JsonProperty("opmeta:compoundSource")
   @JsonInclude(Include.ALWAYS)
   String compoundSource;
   
   @Column(name = "generic_name")
   @JsonProperty("opmeta:genericName")
   @JsonInclude(Include.ALWAYS)
   String genericName;
   
   @Column(name = "study_product")
   @JsonProperty("opmeta:studyProduct")
   @JsonInclude(Include.ALWAYS)
   String studyProduct;
   
   @Column(name = "trade_name")
   @JsonProperty("opmeta:tradeName")
   @JsonInclude(Include.ALWAYS)
   String tradeName;
   
   // TODO: confirm deleteFlag?
   //@Column(name = "delete_flag")
   //@JsonProperty("opmeta:deleteFlag")
   //String deleteFlag;
   
   @Transient
   @JsonProperty("protocols")
   Set<Protocol> protocols;

   @JsonProperty("opmeta:comments")
   @Transient
   String comments;

   @JsonProperty("opmeta:source")
   @Transient
   protected SourceType source;

   public void setProgramCode(String programCode) {
      this.programCode = programCode;
   }

   public String getProgramCode() {
      return programCode;
   }

   public Set<Protocol> getProtocols() {
      return protocols;
   }

   public void setProtocols(Set<Protocol> protocols) {
      this.protocols = protocols;
   }

   public void addProtocol(Protocol protocol) {
      if (protocols == null) {
         protocols = new HashSet<Protocol>();
      }
      this.protocols.add(protocol);
   }

   public String getCurrentSnapshot() {
      return currentSnapshot;
   }

   public void setCurrentSnapshot(String currentSnapshot) {
      this.currentSnapshot = currentSnapshot;
   }

   @Override
   public String getPath() {
      return String.format("/%s/%s", ProgramFolder.NAME, programCode);
   }

   // START auto-generated getters and setteres for BeanUtils
   public String getCompoundNumber() {
      return compoundNumber;
   }

   public void setCompoundNumber(String compoundNumber) {
      this.compoundNumber = compoundNumber;
   }

   public String getCompoundDiscoveryTherapeuticArea() {
      return compoundDiscoveryTherapeuticArea;
   }

   public void setCompoundDiscoveryTherapeuticArea(String compoundDiscoveryTherapeuticArea) {
      this.compoundDiscoveryTherapeuticArea = compoundDiscoveryTherapeuticArea;
   }

   public String getCompoundMechanismOfAction() {
      return compoundMechanismOfAction;
   }

   public void setCompoundMechanismOfAction(String compoundMechanismOfAction) {
      this.compoundMechanismOfAction = compoundMechanismOfAction;
   }

   public String getCompoundName() {
      return compoundName;
   }

   public void setCompoundName(String compoundName) {
      this.compoundName = compoundName;
   }

   public String getCompoundSource() {
      return compoundSource;
   }

   public void setCompoundSource(String compoundSource) {
      this.compoundSource = compoundSource;
   }

   public String getGenericName() {
      return genericName;
   }

   public void setGenericName(String genericName) {
      this.genericName = genericName;
   }

   public String getStudyProduct() {
      return studyProduct;
   }

   public void setStudyProduct(String studyProduct) {
      this.studyProduct = studyProduct;
   }

   public String getTradeName() {
      return tradeName;
   }

   public void setTradeName(String tradeName) {
      this.tradeName = tradeName;
   }
   // END auto-generated getters and setters for BeanUtils

   public String getComments() {
      return comments;
   }

   public void setComments(String comments) {
      this.comments = comments;
   }

   public SourceType getSource() {
      return source;
   }

   public void setSource(SourceType source) {
      this.source = source;
   }
}
