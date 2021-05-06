package com.pfizer.equip.services.input.dataframe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScriptDto {
    private String path;
    private String name;
    private Boolean applyPromotionDataBlindingOnOutput;
    private KVP[] parameters;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isApplyPromotionDataBlindingOnOutput() {
        return applyPromotionDataBlindingOnOutput;
    }

    public void setApplyPromotionDataBlindingOnOutput(Boolean applyPromotionDataBlindingOnOutput) {
        this.applyPromotionDataBlindingOnOutput = applyPromotionDataBlindingOnOutput;
    }

    public KVP[] getParameters() {
        return parameters;
    }

    public void setParameters(KVP[] parameters) {
        this.parameters =parameters;
    }
}
