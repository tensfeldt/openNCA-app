package com.pfizer.equip.services.input.search;

import java.util.List;

public class SearchFullText {
   private SearchMode mode;
   private List<String> textValues;

   public SearchMode getMode() {
      return mode;
   }

   public void setMode(SearchMode mode) {
      this.mode = mode;
   }

   public List<String> getTextValues() {
      return textValues;
   }

   public void setTextValues(List<String> textValues) {
      this.textValues = textValues;
   }
}
