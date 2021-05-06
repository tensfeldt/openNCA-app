package com.pfizer.equip.services.responses;

import java.util.ArrayList;

import com.pfizer.equip.shared.responses.AbstractResponse;
import com.pfizer.equip.shared.responses.Response;

public class ExceptionResponse extends AbstractResponse {
   private ArrayList<String> stackTrace = new ArrayList<String>();

   public ExceptionResponse() {
      this.setResponse(Response.FAILED);
   }

   public ArrayList<String> getStackTrace() {
      return stackTrace;
   }

   public void setStackTrace(ArrayList<String> stackTrace) {
      this.stackTrace = stackTrace;
   }
}
