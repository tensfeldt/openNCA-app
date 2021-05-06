package com.pfizer.equip.shared.opmeta.folder;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class IndicationFolder extends BaseFolder {
   @JsonProperty("jcr:primaryType")
   public static final String PRIMARY_TYPE = "opmeta:indicationFolder";
   public static final String NAME = "Indications";
   public static final String CHILD_NODE_NAME = "indication";
}
