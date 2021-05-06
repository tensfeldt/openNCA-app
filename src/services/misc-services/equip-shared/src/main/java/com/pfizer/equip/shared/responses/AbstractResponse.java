package com.pfizer.equip.shared.responses;

public abstract class AbstractResponse {
   private Response response;

   public Response getResponse() {
      return response;
   }

   public void setResponse(Response response) {
      this.response = response;
   }
}
