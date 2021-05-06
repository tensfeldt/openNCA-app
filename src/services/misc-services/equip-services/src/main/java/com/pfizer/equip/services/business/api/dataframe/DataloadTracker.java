package com.pfizer.equip.services.business.api.dataframe;

import com.pfizer.equip.services.input.dataframe.DataloadInput;

public class DataloadTracker {
   private DataloadInput input;
   private String dataframeId;
   private boolean performedTransform;
   
   public DataloadTracker(DataloadInput input, String dataframeId) {
      this.input = input;
      this.dataframeId = dataframeId;
   }

   public DataloadInput getInput() {
      return input;
   }
   public void setInput(DataloadInput dataloadInput) {
      this.input = dataloadInput;
   }
   public String getDataframeId() {
      return dataframeId;
   }
   public void setDataframeId(String dataframeId) {
      this.dataframeId = dataframeId;
   }
   public boolean isPerformedTransform() {
      return performedTransform;
   }

   public void setPerformedTransform(boolean performedTransform) {
      this.performedTransform = performedTransform;
   }
}
