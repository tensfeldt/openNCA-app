package com.pfizer.equip.shared.opmeta.folder;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class ClinicalStudyReportFolder extends BaseFolder {
   @JsonProperty("jcr:primaryType")
   public static final String PRIMARY_TYPE = "opmeta:clinicalStudyReportFolder";
   public static final String NAME = "ClinicalStudyReports";
   public static final String CHILD_NODE_NAME = "report";
}
