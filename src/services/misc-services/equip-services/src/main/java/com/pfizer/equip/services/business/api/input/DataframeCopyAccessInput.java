package com.pfizer.equip.services.business.api.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
public class DataframeCopyAccessInput {
   String parent;
   String target;
   String transactionId;

   public String getParent() {
      return parent;
   }

   public void setParent(String parent) {
      this.parent = parent;
   }

   public String getTarget() {
      return target;
   }

   public void setTarget(String target) {
      this.target = target;
   }

   public String getTransactionId() {
      return transactionId;
   }

   public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
   }
}
