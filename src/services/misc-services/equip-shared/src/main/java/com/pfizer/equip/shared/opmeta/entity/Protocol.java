package com.pfizer.equip.shared.opmeta.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.folder.ClinicalStudyReportFolder;
import com.pfizer.equip.shared.opmeta.folder.CustomAttributesFolder;
import com.pfizer.equip.shared.opmeta.folder.DrugRegimenFolder;
import com.pfizer.equip.shared.opmeta.folder.IndicationFolder;
import com.pfizer.equip.shared.opmeta.folder.MilestoneFolder;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolAliasFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;

@SuppressWarnings("serial")
@Entity
@Table(schema = "podsdal", name = "pods_ods_study_v")
public class Protocol extends UserProtocol {
   
   @Transient 
   private final String CLINICAL_STUDY_REPORTS = "clinicalStudyReports";
   @Transient 
   private final String INDICATIONS = "indications";
   @Transient 
   private final String DRUG_REGIMENS = "drugRegimens";
   @Transient 
   private final String MILESTONES = "milestones";
   @Transient 
   private final String PROTOCOL_ALIASES = "protocolAliases";
   
   @Transient
   @JsonIgnore
   private final String CHILDREN = "children";

   // Used by JPA / JSON when creating the protocol:
   public Protocol() {
      primaryType = PRIMARY_TYPE;        // Used because cannot @Override a field, so set it here:
      nodeFolder = new ProtocolFolder(); // Used when protocol is first being created:
   }

   // Used when creating a new protocol within the code,
   // often just to call getPath() and get the actual protocol from the repo.
   public Protocol(String programCode, String studyId) {
      primaryType = PRIMARY_TYPE;
      nodeFolder = new ProtocolFolder();
      this.programCode = programCode;
      this.studyId = studyId;
   }

   @Override
   @JsonIgnore
   // Used to workaround some serialization issues with ModeShape ("children" path component).
   // When updating repo, a map structure is built from the output of this method and then clearChildEntities is called.
   public Map<String, Set<? extends BaseProtocolChildNode>> getChildren() {
      if (children.size() == 0) {
         if (clinicalStudyReports != null) {
            children.put(ClinicalStudyReportFolder.NAME, clinicalStudyReports);
         }
         if (indications != null) {
            children.put(IndicationFolder.NAME, indications);
         }
         if (drugRegimens != null) {
            children.put(DrugRegimenFolder.NAME, drugRegimens);
         }
         if (milestones != null) {
            children.put(MilestoneFolder.NAME, milestones);
         }
         if (protocolAliases != null) {
            children.put(ProtocolAliasFolder.NAME, protocolAliases);
         }
         if (customAttributes != null) {
            children.put(CustomAttributesFolder.NAME, customAttributes);
         }
      }
      return children;
   }
   
   @JsonProperty(CHILDREN)
   // Used when retrieving content from users or from repo, similar to the getter.
   // TODO: Refactor both methods, probably use a custom serializer.
   public void setChildren(Map<String, JsonNode> children) {
      ObjectMapper mapper = new ObjectMapper();
      try {
         if (children.get(IndicationFolder.NAME) != null && children.get(IndicationFolder.NAME).has(CHILDREN)) {
            this.indications = new HashSet<Indication>();
            for (JsonNode indicationJson : children.get(IndicationFolder.NAME).get(CHILDREN)) {
               Indication indication = mapper.treeToValue(indicationJson, Indication.class);
               this.indications.add(indication);
            }
         }
         if (children.get(ProtocolAliasFolder.NAME) != null && children.get(ProtocolAliasFolder.NAME).has(CHILDREN)) {
            this.protocolAliases = new HashSet<ProtocolAlias>();
            for (JsonNode protocolAliasJson : children.get(ProtocolAliasFolder.NAME).get(CHILDREN)) {
               ProtocolAlias protocolAlias = mapper.treeToValue(protocolAliasJson, ProtocolAlias.class);
               this.protocolAliases.add(protocolAlias);
            }
         }
         if (children.get(ClinicalStudyReportFolder.NAME) != null && children.get(ClinicalStudyReportFolder.NAME).has(CHILDREN)) {
            this.clinicalStudyReports = new HashSet<ClinicalStudyReport>();
            for (JsonNode clinicalStudyReportJson : children.get(ClinicalStudyReportFolder.NAME).get(CHILDREN)) {
               ClinicalStudyReport clinicalStudyReport = mapper.treeToValue(clinicalStudyReportJson, ClinicalStudyReport.class);
               this.clinicalStudyReports.add(clinicalStudyReport);
            }
         }
         if (children.get(MilestoneFolder.NAME) != null && children.get(MilestoneFolder.NAME).has(CHILDREN)) {
            this.milestones = new HashSet<Milestone>();
            for (JsonNode milestoneJson : children.get(MilestoneFolder.NAME).get(CHILDREN)) {
               Milestone milestone = mapper.treeToValue(milestoneJson, Milestone.class);
               this.milestones.add(milestone);
            }
         }
         if (children.get(DrugRegimenFolder.NAME) != null && children.get(DrugRegimenFolder.NAME).has(CHILDREN)) {
            this.drugRegimens = new HashSet<DrugRegimen>();
            for (JsonNode drugRegimenJson : children.get(DrugRegimenFolder.NAME).get(CHILDREN)) {
               DrugRegimen drugRegimen = mapper.treeToValue(drugRegimenJson, DrugRegimen.class);
               this.drugRegimens.add(drugRegimen);
            }
         }
         if (children.get(CustomAttributesFolder.NAME) != null && children.get(CustomAttributesFolder.NAME).has(CHILDREN)) {
            this.customAttributes = new HashSet<KeyValuePair>();
            for (JsonNode customAttributeJson : children.get(CustomAttributesFolder.NAME).get(CHILDREN)) {
               KeyValuePair customAttribute = mapper.treeToValue(customAttributeJson, KeyValuePair.class);
               this.customAttributes.add(customAttribute);
            }
         }
         if (children.get(Portfolio.NODE_NAME) != null) {
            this.portfolio = mapper.treeToValue(children.get(Portfolio.NODE_NAME), Portfolio.class);
         }
      } catch (JsonProcessingException e) {
         throw new RuntimeException("Error deserializing children for protocol node.");
      }
   }

   @Override
   // Delete all child records pulled from DB or user so that this object serializes properly during loading.
   // But allow for keeping them when responding to the front-end (instead of JsonIgnore on the getter).
   public void clearChildEntities() {
      this.clinicalStudyReports = null;
      this.indications = null;
      this.drugRegimens = null;
      this.milestones = null;
      this.protocolAliases = null;
      this.portfolio = null;
      this.customAttributes = null;
   }

   @Override
   // Similar to getChildren, used to work with "children" path component. 
   public Set<? extends BaseNode> getDirectChildren() {
      if (this.portfolio != null) {
         directChildren.add(portfolio);
      }
      return directChildren;
   }

   public Set<ClinicalStudyReport> getClinicalStudyReports() {
      return clinicalStudyReports;
   }

   public void setClinicalStudyReports(Set<ClinicalStudyReport> clinicalStudyReports) {
      this.clinicalStudyReports = clinicalStudyReports;
   }

   public Set<Indication> getIndications() {
      return indications;
   }

   public void setIndications(Set<Indication> indications) {
      this.indications = indications;
   }

   public Set<DrugRegimen> getDrugRegimens() {
      return drugRegimens;
   }

   public void setDrugRegimens(Set<DrugRegimen> drugRegimens) {
      this.drugRegimens = drugRegimens;
   }

   public Set<Milestone> getMilestones() {
      return milestones;
   }

   public void setMilestones(Set<Milestone> milestones) {
      this.milestones = milestones;
   }

   public Set<ProtocolAlias> getProtocolAliases() {
      return protocolAliases;
   }

   public void setProtocolAliases(Set<ProtocolAlias> protocolAliases) {
      this.protocolAliases = protocolAliases;
   }

   @Transient
   @JsonProperty("opmeta:activeFlag")
   Boolean activeFlag;

   @Transient
   @JsonProperty("equip:deleteFlag")
   private Boolean deleteFlag;
   
   @Transient
   @JsonProperty("opmeta:currentSnapshot")
   String currentSnapshot;

   @Column(name = "study_status_current")
   @JsonProperty("opmeta:studyStatusCurrent")
   @JsonInclude(Include.ALWAYS)
   String studyStatusCurrent;

   @Column(name = "cand_code")
   @JsonProperty("opmeta:candidateCode")
   @JsonInclude(Include.ALWAYS)
   String projectCode;

   @JsonProperty("opmeta:candidateProject")
   @Transient
   String candidateProjectUuid;
   
   @Column(name = "methodology_study")
   @JsonProperty("opmeta:methodologyStudy")
   @JsonInclude(Include.ALWAYS)
   Boolean methodologyStudy;

   @Column(name = "pims_flg")
   @JsonProperty("opmeta:pimsFlag")
   @JsonInclude(Include.ALWAYS)
   Boolean pimsFlag;

   @Column(name = "pediatric_study")
   @JsonProperty("opmeta:pediactricStudy")
   @JsonInclude(Include.ALWAYS)
   Boolean pediactricStudy;

   @Column(name = "study_closed")
   @JsonProperty("opmeta:studyClosed")
   @JsonInclude(Include.ALWAYS)
   Boolean studyClosed;

   @Column(name = "terminate_dt")
   @JsonProperty("opmeta:terminatedDate")
   @JsonInclude(Include.ALWAYS)
   Date terminatedDate;

   @Column(name = "tot_subj_cmpltd_study")
   @JsonProperty("opmeta:totalSubjectsCompletedStudy")
   @JsonInclude(Include.ALWAYS)
   Long totalSubjectsCompletedStudy;

   @Column(name = "tot_subj_discntnued_study")
   @JsonProperty("opmeta:totalSubjectsDiscontinuedStudy")
   @JsonInclude(Include.ALWAYS)
   Long totalSubjectsDiscontinuedStudy;

   @Column(name = "tot_subj_enrolled_study")
   @JsonProperty("opmeta:totalSubjectsEnrolledStudy")
   @JsonInclude(Include.ALWAYS)
   Long totalSubjectsEnrolledStudy;

   @Column(name = "tot_subj_entered_study")
   @JsonProperty("opmeta:totalSubjectsEnteredStudy")
   @JsonInclude(Include.ALWAYS)
   Long totalSubjectsEnteredStudy;

   @Column(name = "tot_subj_planned_study")
   @JsonProperty("opmeta:totalSubjectsPlannedStudy")
   @JsonInclude(Include.ALWAYS)
   Long totalSubjectsPlannedStudy;

   @Column(name = "subj_max_age")
   @JsonProperty("opmeta:subjectMaxAge")
   @JsonInclude(Include.ALWAYS)
   Long subjectMaxAge;

   @Column(name = "subj_min_age")
   @JsonProperty("opmeta:subjectMinAge")
   @JsonInclude(Include.ALWAYS)
   Long subjectMinAge;

   @Column(name = "study_title")
   @JsonProperty("opmeta:title")
   @JsonInclude(Include.ALWAYS)
   String title;

   @Column(name = "subj_max_age_unit")
   @JsonProperty("opmeta:subjectMaxAgeUnit")
   @JsonInclude(Include.ALWAYS)
   String subjectMaxAgeUnit;

   @Column(name = "subj_min_age_unit")
   @JsonProperty("opmeta:subjectMinAgeUnit")
   @JsonInclude(Include.ALWAYS)
   String subjectMinAgeUnit;

   @Column(name = "development_phase")
   @JsonProperty("opmeta:developmentPhase")
   @JsonInclude(Include.ALWAYS)
   String developmentPhase;

   @Column(name = "patient_database")
   @JsonProperty("opmeta:patientDatabase")
   @JsonInclude(Include.ALWAYS)
   String patientDatabase;
  
   @Autowired
   @Column(name = "sponsoring_division")
   @JsonProperty("opmeta:sponsoringDivision")
   @JsonInclude(Include.ALWAYS)
   String sponsoringDivision;

   @Column(name = "sponsoring_unit")
   @JsonProperty("opmeta:sponsoringUnit")
   @JsonInclude(Include.ALWAYS)
   String sponsoringUnit;

   @Column(name = "statistical_design")
   @JsonProperty("opmeta:statisticalDesign")
   @JsonInclude(Include.ALWAYS)
   String statisticalDesign;

   @Column(name = "study_design_blinding")
   @JsonProperty("opmeta:studyDesignBlinding")
   @JsonInclude(Include.ALWAYS)
   String studyDesignBlinding;

   @Column(name = "study_therapeutic_area")
   @JsonProperty("opmeta:studyTherapeuticArea")
   @JsonInclude(Include.ALWAYS)
   String studyTherapeuticArea;

   @Column(name = "study_type")
   @JsonProperty("opmeta:studyType")
   @JsonInclude(Include.ALWAYS)
   String studyType;

   @Column(name = "subj_gender")
   @JsonProperty("opmeta:subjectGender")
   @JsonInclude(Include.ALWAYS)
   String subjectGender;

   @Column(name = "working_protocol_desc")
   @JsonProperty("opmeta:workingProtocolDescription")
   @JsonInclude(Include.ALWAYS)
   String workingProtocolDescription;

   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToMany(mappedBy = "protocol")
   @JsonProperty(CLINICAL_STUDY_REPORTS)
   Set<ClinicalStudyReport> clinicalStudyReports;

   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToMany(mappedBy = "protocol")
   @JsonProperty(INDICATIONS)
   Set<Indication> indications;

   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToMany(mappedBy = "protocol")
   @JsonProperty(DRUG_REGIMENS)
   Set<DrugRegimen> drugRegimens;

   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToMany(mappedBy = "protocol")
   @JsonProperty(MILESTONES)
   Set<Milestone> milestones;

   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToMany(mappedBy = "protocol")
   @JsonProperty(PROTOCOL_ALIASES)
   Set<ProtocolAlias> protocolAliases;
   
   @BatchSize(size = 1000)
   @LazyCollection(LazyCollectionOption.FALSE)
   @Fetch(FetchMode.SELECT)
   @OneToOne(mappedBy = "protocol")
   @JsonProperty("portfolio")
   Portfolio portfolio;
   
   @JsonProperty("groupTemplates")
   @Transient
   Map<String, Object> groupTemplates;
   @JsonProperty("opmeta:source")
   @Transient
   protected SourceType source;
   @JsonProperty("opmeta:comments")
   @Transient
   String comments;

   public Map<String, Object> getGroupTemplates() {
      return groupTemplates;
   }

   public void initializeGroupTemplates() {
      groupTemplates = new HashMap<String, Object>();
      groupTemplates.put(INDICATIONS, new Indication());
      groupTemplates.put(PROTOCOL_ALIASES, new ProtocolAlias());
      groupTemplates.put(CLINICAL_STUDY_REPORTS, new ClinicalStudyReport());
      groupTemplates.put(MILESTONES, new Milestone());
      groupTemplates.put(DRUG_REGIMENS, new DrugRegimen());
   }

   public String getProjectCode() {
      return projectCode;
   }

   public void setCandidateProjectUuid(String candidateProjectUuid) {
      this.candidateProjectUuid = candidateProjectUuid;
   }

   public String getCandidateProjectUuid() {
      return candidateProjectUuid;
   }

   public String getCurrentSnapshot() {
      return currentSnapshot;
   }

   public void setCurrentSnapshot(String currentSnapshot) {
      this.currentSnapshot = currentSnapshot;
   }
   
   @Override
   public String getPath() {
      return String.format("/%s/%s/%s/%s", ProgramFolder.NAME, programCode, ProtocolFolder.NAME, studyId);
   }

   // START Auto-generated getters and setters for BeanUtils copies
   public Boolean getMethodologyStudy() {
      return methodologyStudy;
   }

   public void setMethodologyStudy(Boolean methodologyStudy) {
      this.methodologyStudy = methodologyStudy;
   }

   public Boolean getPimsFlag() {
      return pimsFlag;
   }

   public void setPimsFlag(Boolean pimsFlag) {
      this.pimsFlag = pimsFlag;
   }

   public Boolean getPediactricStudy() {
      return pediactricStudy;
   }

   public void setPediactricStudy(Boolean pediactricStudy) {
      this.pediactricStudy = pediactricStudy;
   }

   public Boolean getStudyClosed() {
      return studyClosed;
   }

   public void setStudyClosed(Boolean studyClosed) {
      this.studyClosed = studyClosed;
   }

   public Date getTerminatedDate() {
      return terminatedDate;
   }

   public void setTerminatedDate(Date terminatedDate) {
      this.terminatedDate = terminatedDate;
   }

   public Long getTotalSubjectsCompletedStudy() {
      return totalSubjectsCompletedStudy;
   }

   public void setTotalSubjectsCompletedStudy(Long totalSubjectsCompletedStudy) {
      this.totalSubjectsCompletedStudy = totalSubjectsCompletedStudy;
   }

   public Long getTotalSubjectsDiscontinuedStudy() {
      return totalSubjectsDiscontinuedStudy;
   }

   public void setTotalSubjectsDiscontinuedStudy(Long totalSubjectsDiscontinuedStudy) {
      this.totalSubjectsDiscontinuedStudy = totalSubjectsDiscontinuedStudy;
   }

   public Long getTotalSubjectsEnrolledStudy() {
      return totalSubjectsEnrolledStudy;
   }

   public void setTotalSubjectsEnrolledStudy(Long totalSubjectsEnrolledStudy) {
      this.totalSubjectsEnrolledStudy = totalSubjectsEnrolledStudy;
   }

   public Long getTotalSubjectsEnteredStudy() {
      return totalSubjectsEnteredStudy;
   }

   public void setTotalSubjectsEnteredStudy(Long totalSubjectsEnteredStudy) {
      this.totalSubjectsEnteredStudy = totalSubjectsEnteredStudy;
   }

   public Long getTotalSubjectsPlannedStudy() {
      return totalSubjectsPlannedStudy;
   }

   public void setTotalSubjectsPlannedStudy(Long totalSubjectsPlannedStudy) {
      this.totalSubjectsPlannedStudy = totalSubjectsPlannedStudy;
   }

   public Long getSubjectMaxAge() {
      return subjectMaxAge;
   }

   public void setSubjectMaxAge(Long subjectMaxAge) {
      this.subjectMaxAge = subjectMaxAge;
   }

   public Long getSubjectMinAge() {
      return subjectMinAge;
   }

   public void setSubjectMinAge(Long subjectMinAge) {
      this.subjectMinAge = subjectMinAge;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getSubjectMaxAgeUnit() {
      return subjectMaxAgeUnit;
   }

   public void setSubjectMaxAgeUnit(String subjectMaxAgeUnit) {
      this.subjectMaxAgeUnit = subjectMaxAgeUnit;
   }

   public String getSubjectMinAgeUnit() {
      return subjectMinAgeUnit;
   }

   public void setSubjectMinAgeUnit(String subjectMinAgeUnit) {
      this.subjectMinAgeUnit = subjectMinAgeUnit;
   }

   public String getDevelopmentPhase() {
      return developmentPhase;
   }

   public void setDevelopmentPhase(String developmentPhase) {
      this.developmentPhase = developmentPhase;
   }

   public String getPatientDatabase() {
      return patientDatabase;
   }

   public void setPatientDatabase(String patientDatabase) {
      this.patientDatabase = patientDatabase;
   }

   public String getSponsoringDivision() {
      return sponsoringDivision;
   }

   public void setSponsoringDivision(String sponsoringDivision) {
      this.sponsoringDivision = sponsoringDivision;
   }

   public String getSponsoringUnit() {
      return sponsoringUnit;
   }

   public void setSponsoringUnit(String sponsoringUnit) {
      this.sponsoringUnit = sponsoringUnit;
   }

   public String getStatisticalDesign() {
      return statisticalDesign;
   }

   public void setStatisticalDesign(String statisticalDesign) {
      this.statisticalDesign = statisticalDesign;
   }

   public String getStudyDesignBlinding() {
      return studyDesignBlinding;
   }

   public void setStudyDesignBlinding(String studyDesignBlinding) {
      this.studyDesignBlinding = studyDesignBlinding;
   }

   public String getStudyTherapeuticArea() {
      return studyTherapeuticArea;
   }

   public void setStudyTherapeuticArea(String studyTherapeuticArea) {
      this.studyTherapeuticArea = studyTherapeuticArea;
   }

   public String getStudyType() {
      return studyType;
   }

   public void setStudyType(String studyType) {
      this.studyType = studyType;
   }

   public String getSubjectGender() {
      return subjectGender;
   }

   public void setSubjectGender(String subjectGender) {
      this.subjectGender = subjectGender;
   }

   public String getWorkingProtocolDescription() {
      return workingProtocolDescription;
   }

   public void setWorkingProtocolDescription(String workingProtocolDescription) {
      this.workingProtocolDescription = workingProtocolDescription;
   }

   public Portfolio getPortfolio() {
      return portfolio;
   }

   public void setPortfolio(Portfolio portfolio) {
      this.portfolio = portfolio;
   }

   public void setProjectCode(String projectCode) {
      this.projectCode = projectCode;
   }

   public String getStudyStatusCurrent() {
      return studyStatusCurrent;
   }

   public void setStudyStatusCurrent(String studyStatusCurrent) {
      this.studyStatusCurrent = studyStatusCurrent;
   }

   public Boolean getActiveFlag() {
      return activeFlag;
   }

   public void setActiveFlag(Boolean activeFlag) {
      this.activeFlag = activeFlag;
   }

   public Boolean getDeleteFlag() {
      return deleteFlag;
   }

   public void setDeleteFlag(Boolean deleteFlag) {
      this.deleteFlag = deleteFlag;
   }

   // END Auto-generated getters and setters for BeanUtils copies

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
