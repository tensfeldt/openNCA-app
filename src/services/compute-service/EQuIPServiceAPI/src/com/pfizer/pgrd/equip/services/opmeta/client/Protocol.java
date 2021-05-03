package com.pfizer.pgrd.equip.services.opmeta.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Protocol {
	private String source;
	private Date sourceCreationTimestamp;
	private String modifiedBy;
	private String programCode;
	private String studyBlindingStatus;
	private String studyId;
	private String studyBlindingDescription;
	private String title;
	private long totalSubjectsCompletedStudy;
	private long totalSubjectsDiscontinuedStudy;
	private long totalSubjectsEnrolledStudy;
	private long totalSubjectsEnteredStudy;
	private long totalSubjectsPlannedStudy;
	private int subjectMaxAge;
	private int subjectMinAge;
	private String developmentPhase;
	private String patientDatabase;
	private String studyTherapeuticArea;
	private String studyType;
	private String studyStatusCurrent;
	private String candidateCode;
	private String subjectGender;
	private String workingProtocolDescription;
	private Date setupDate;
	private String setupBy;
	
	private List<String> assignedPkaUsers = new ArrayList<>();
	private List<String> assignedCagUsers = new ArrayList<>();
	private List<ProtocolAlias> protocolAliases;

	public String getStudyBlindingStatus() {
		return studyBlindingStatus;
	}

	public void setStudyBlindingStatus(String studyBlindingStatus) {
		this.studyBlindingStatus = studyBlindingStatus;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public List<ProtocolAlias> getProtocolAliases() {
		return protocolAliases;
	}

	public void setProtocolAliases(List<ProtocolAlias> protocolAliases) {
		this.protocolAliases = protocolAliases;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getSourceCreationTimestamp() {
		return sourceCreationTimestamp;
	}

	public void setSourceCreationTimestamp(Date sourceCreationTimestamp) {
		this.sourceCreationTimestamp = sourceCreationTimestamp;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getProgramCode() {
		return programCode;
	}

	public void setProgramCode(String programCode) {
		this.programCode = programCode;
	}

	public String getStudyBlindingDescription() {
		return studyBlindingDescription;
	}

	public void setStudyBlindingDescription(String studyBlindingDescription) {
		this.studyBlindingDescription = studyBlindingDescription;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getTotalSubjectsCompletedStudy() {
		return totalSubjectsCompletedStudy;
	}

	public void setTotalSubjectsCompletedStudy(long totalSubjectsCompletedStudy) {
		this.totalSubjectsCompletedStudy = totalSubjectsCompletedStudy;
	}

	public long getTotalSubjectsDiscontinuedStudy() {
		return totalSubjectsDiscontinuedStudy;
	}

	public void setTotalSubjectsDiscontinuedStudy(long totalSubjectsDiscontinuedStudy) {
		this.totalSubjectsDiscontinuedStudy = totalSubjectsDiscontinuedStudy;
	}

	public long getTotalSubjectsEnrolledStudy() {
		return totalSubjectsEnrolledStudy;
	}

	public void setTotalSubjectsEnrolledStudy(long totalSubjectsEnrolledStudy) {
		this.totalSubjectsEnrolledStudy = totalSubjectsEnrolledStudy;
	}

	public long getTotalSubjectsEnteredStudy() {
		return totalSubjectsEnteredStudy;
	}

	public void setTotalSubjectsEnteredStudy(long totalSubjectsEnteredStudy) {
		this.totalSubjectsEnteredStudy = totalSubjectsEnteredStudy;
	}

	public long getTotalSubjectsPlannedStudy() {
		return totalSubjectsPlannedStudy;
	}

	public void setTotalSubjectsPlannedStudy(long totalSubjectsPlannedStudy) {
		this.totalSubjectsPlannedStudy = totalSubjectsPlannedStudy;
	}

	public int getSubjectMaxAge() {
		return subjectMaxAge;
	}

	public void setSubjectMaxAge(int subjectMaxAge) {
		this.subjectMaxAge = subjectMaxAge;
	}

	public int getSubjectMinAge() {
		return subjectMinAge;
	}

	public void setSubjectMinAge(int subjectMinAge) {
		this.subjectMinAge = subjectMinAge;
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

	public String getStudyStatusCurrent() {
		return studyStatusCurrent;
	}

	public void setStudyStatusCurrent(String studyStatusCurrent) {
		this.studyStatusCurrent = studyStatusCurrent;
	}

	public String getCandidateCode() {
		return candidateCode;
	}

	public void setCandidateCode(String candidateCode) {
		this.candidateCode = candidateCode;
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

	public Date getSetupDate() {
		return setupDate;
	}

	public void setSetupDate(Date setupDate) {
		this.setupDate = setupDate;
	}

	public String getSetupBy() {
		return setupBy;
	}

	public void setSetupBy(String setupBy) {
		this.setupBy = setupBy;
	}

	public List<String> getAssignedPkaUsers() {
		return assignedPkaUsers;
	}

	public void setAssignedPkaUsers(List<String> assignedPkaUsers) {
		this.assignedPkaUsers = assignedPkaUsers;
	}

	public List<String> getAssignedCagUsers() {
		return assignedCagUsers;
	}

	public void setAssignedCagUsers(List<String> assignedCagUsers) {
		this.assignedCagUsers = assignedCagUsers;
	}
	
}
