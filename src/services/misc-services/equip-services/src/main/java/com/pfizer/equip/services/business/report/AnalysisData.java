package com.pfizer.equip.services.business.report;

import java.util.List;

public class AnalysisData {
      String analysisEquipId;
      String parametersEquipId;
      String parametersId;
      List<String> firstPromotedDataframeIds;
      String concentrationDataId;
      String kelFlagsId;

      public String getAnalysisEquipId() {
         return analysisEquipId;
      }
      public void setAnalysisEquipId(String analysisEquipId) {
         this.analysisEquipId = analysisEquipId;
      }
      public String getParametersEquipId() {
         return parametersEquipId;
      }
      public void setParametersEquipId(String parametersEquipId) {
         this.parametersEquipId = parametersEquipId;
      }
      public String getParametersId() {
         return parametersId;
      }
      public void setParametersId(String parametersId) {
         this.parametersId = parametersId;
      }
      public List<String> getFirstPromotedDataframeIds() {
         return firstPromotedDataframeIds;
      }
      public void setFirstPromotedDataframeIds(List<String> firstPromotedDataframeIds) {
         this.firstPromotedDataframeIds = firstPromotedDataframeIds;
      }
      public String getConcentrationDataId() {
         return concentrationDataId;
      }
      public void setConcentrationDataId(String concentrationDataId) {
         this.concentrationDataId = concentrationDataId;
      }
      public String getKelFlagsId() {
         return kelFlagsId;
      }
      public void setKelFlagsId(String kelFlagsId) {
         this.kelFlagsId = kelFlagsId;
      }
}
