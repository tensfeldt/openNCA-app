package com.pfizer.equip.services.input;

import org.springframework.util.MultiValueMap;

public interface MultiValueInputBuilder extends InputBuilder {
   public AbstractInput build(String userId, MultiValueMap<String, Object> inputs);
}
