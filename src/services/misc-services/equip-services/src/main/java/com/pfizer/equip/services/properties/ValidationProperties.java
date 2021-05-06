package com.pfizer.equip.services.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "validation")
public class ValidationProperties {
   private String path;

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

}
