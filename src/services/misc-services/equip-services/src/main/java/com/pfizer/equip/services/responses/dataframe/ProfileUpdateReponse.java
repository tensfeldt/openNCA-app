package com.pfizer.equip.services.responses.dataframe;

import com.pfizer.equip.shared.responses.AbstractResponse;

public class ProfileUpdateReponse extends AbstractResponse {
   private String dataframeId;

   public String getDataframeId() {
      return dataframeId;
   }

   public void setDataframeId(String dataframeId) {
      this.dataframeId = dataframeId;
   }
}
