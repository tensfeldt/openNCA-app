package com.pfizer.equip.services.input.report;

import com.pfizer.equip.services.business.modeshape.nodes.BaseReportArtifactNode;

public class ReportInputBuilder {

   public ReportInput build(String userId, BaseReportArtifactNode baseReportingArtifactNode) {
      ReportInput input = new ReportInput();
//      input.setScriptId(baseReportingArtifactNode.getScriptId());
//      input.setStudyDesign(baseReportingArtifactNode.getStudyDesign());
      
      return input;
   }
}
