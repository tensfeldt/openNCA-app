package com.pfizer.equip.services.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.shared.responses.AbstractResponse;

@JsonInclude(Include.NON_EMPTY)
public class DataframeCopyAccessResponse extends AbstractResponse {
   String transactionId;
   String statusCode;
   String failureReason;

   public String getTransactionId() {
      return transactionId;
   }

   public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
   }

   public String getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(String statusCode) {
      this.statusCode = statusCode;
   }

   public String getFailureReason() {
      return failureReason;
   }

   public void setFailureReason(String failureReason) {
      this.failureReason = failureReason;
   }
}
