package com.pfizer.equip.services.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.shared.responses.AbstractResponse;

@JsonInclude(Include.NON_ABSENT)
public class AuditEntryResponse extends AbstractResponse {
   Long auditEntryId;

   public Long getAuditEntryId() {
      return auditEntryId;
   }

   public void setAuditEntryId(Long auditEntryId) {
      this.auditEntryId = auditEntryId;
   }

}
