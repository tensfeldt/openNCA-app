package com.pfizer.equip.services.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application and environment configuration values.
 */
@ConfigurationProperties(prefix = "equip-services")
public class ApplicationProperties {
   private String name;
   private String eventQueue;
   private String userIdHeader;
   private String dataframeBaseUrl;
   private String computeBaseUrl;
   private boolean isStandaloneMode;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getEventQueue() {
      return eventQueue;
   }

   public void setEventQueue(String eventQueue) {
      this.eventQueue = eventQueue;
   }

   public String getUserIdHeader() {
      return userIdHeader;
   }

   public void setUserIdHeader(String userIdHeader) {
      this.userIdHeader = userIdHeader;
   }

   public String getDataframeBaseUrl() {
      return dataframeBaseUrl;
   }

   public void setDataframeBaseUrl(String dataframeBaseUrl) {
      this.dataframeBaseUrl = dataframeBaseUrl;
   }

   public String getComputeBaseUrl() {
      return computeBaseUrl;
   }

   public void setComputeBaseUrl(String computeBaseUrl) {
      this.computeBaseUrl = computeBaseUrl;
   }

   public boolean isStandaloneMode() {
      return isStandaloneMode;
   }

   public void setStandaloneMode(boolean isStandaloneMode) {
      this.isStandaloneMode = isStandaloneMode;
   }
}
