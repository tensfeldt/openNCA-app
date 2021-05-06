package com.pfizer.equip.services.business.report.dto;

import java.util.Optional;

public class ReportDTO {

    private String userId;
    private String reportingEventId;
    private String analysisId;
    private String reportTemplateId;
    private Optional<String> existingReportId;
    private String clientInfo;

    public ReportDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReportingEventId() {
        return reportingEventId;
    }

    public void setReportingEventId(String reportingEventId) {
        this.reportingEventId = reportingEventId;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getReportTemplateId() {
        return reportTemplateId;
    }

    public void setReportTemplateId(String reportTemplateId) {
        this.reportTemplateId = reportTemplateId;
    }

    public Optional<String> getExistingReportId() {
        return existingReportId;
    }

    public void setExistingReportId(Optional<String> existingReportId) {
        this.existingReportId = existingReportId;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public static class ReportDTOBuilder{

        private String userId;
        private String reportingEventId;
        private String analysisId;
        private String reportTemplateId;
        private Optional<String> existingReportId;
        private String clientInfo;

        public ReportDTOBuilder() {
            this.existingReportId = Optional.empty();
        }

        public ReportDTOBuilder setUserId(String userId){
            this.userId = userId;
            return this;
        }

        public ReportDTOBuilder setReportingEventId(String reportingEventId){
            this.reportingEventId = reportingEventId;
            return this;
        }

        public ReportDTOBuilder setAnalysisId(String analysisId){
            this.analysisId = analysisId;
            return this;
        }

        public ReportDTOBuilder setReportTemplateId(String reportTemplateId){
            this.reportTemplateId = reportTemplateId;
            return this;
        }

        public ReportDTOBuilder setExistingReportId(Optional<String> existingReportId){
            this.existingReportId = existingReportId;
            return this;
        }

        public ReportDTOBuilder setClientInfo(String clientInfo){
            this.clientInfo = clientInfo;
            return this;
        }

        public ReportDTO build(){
            ReportDTO reportDTO = new ReportDTO();

            reportDTO.setUserId(this.userId);
            reportDTO.setReportingEventId(this.reportingEventId);
            reportDTO.setAnalysisId(this.analysisId);
            reportDTO.setReportTemplateId(this.reportTemplateId);
            reportDTO.setExistingReportId(this.existingReportId);
            reportDTO.setClientInfo(this.clientInfo);

            return reportDTO;
        }
    }

}
