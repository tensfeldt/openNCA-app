package com.pfizer.equip.shared.opmeta.folder;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class DrugRegimenFolder extends BaseFolder {
   @JsonProperty("jcr:primaryType")
   public static final String PRIMARY_TYPE = "opmeta:drugRegimenFolder";
   public static final String NAME = "DrugRegimens";
   public static final String CHILD_NODE_NAME = "regimen";
}