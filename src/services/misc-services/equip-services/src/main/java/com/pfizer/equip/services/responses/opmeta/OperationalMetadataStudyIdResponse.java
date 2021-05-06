package com.pfizer.equip.services.responses.opmeta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.shared.responses.AbstractResponse;

@JsonInclude(Include.ALWAYS)
public class OperationalMetadataStudyIdResponse extends AbstractResponse {
   private String studyId;

   public OperationalMetadataStudyIdResponse(String studyId) {
      this.studyId = studyId;
   }

   public String getStudyId() {
      return studyId;
   }

   public void setStudyId(String studyId) {
      this.studyId = studyId;
   }
}