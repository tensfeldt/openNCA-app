package com.pfizer.equip.shared.opmeta;

public enum StudyTaskActivityValueType {
   SEND_TO_WRITER("send_to_medical_study_writer_downstream"),
   PUBLISHING("publishing"),
   FINAL_APPROVED_PROTOCOL("final_approved_protocol_date"),
   FINAL_TABLE_ACTUAL("final_tables_actual"),
   CSR_APPROVAL_DATE("csr_approval_date"),
   ASSAY_LAB("date_last_pk_sample_received_by_assay_lab");

   StudyTaskActivityValueType(String studyTaskActValType) {
      this.setStudyTaskActValType(studyTaskActValType);
   }

   private String studyTaskActValType;

   public String getStudyTaskActValType() {
      return studyTaskActValType;
   }

   public void setStudyTaskActValType(String studyTaskActValType) {
      this.studyTaskActValType = studyTaskActValType;
   }

}
