package com.pfizer.equip.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application and environment configuration values.
 */
@ConfigurationProperties(prefix = "equip-services")
public class SharedApplicationProperties {
   private String name;
   private String eventQueue;
   private String userIdHeader;
   private String dataframeBaseUrl;
   private String graabsBaseUrl;
   private String graabsUser;
   private String graabsPassword;
   private String computeBaseUrl;
   private String servicesServiceAccount;
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

   public String getGraabsBaseUrl() {
      return graabsBaseUrl;
   }

   public void setGraabsBaseUrl(String graabsBaseUrl) {
      this.graabsBaseUrl = graabsBaseUrl;
   }

   public String getGraabsUser() {
      return graabsUser;
   }

   public void setGraabsUser(String graabsUser) {
      this.graabsUser = graabsUser;
   }

   public String getGraabsPassword() {
      return graabsPassword;
   }

   public void setGraabsPassword(String graabsPassword) {
      this.graabsPassword = graabsPassword;
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

   public String getServicesServiceAccount() {
      return servicesServiceAccount;
   }

   public void setServicesServiceAccount(String servicesServiceAccount) {
      this.servicesServiceAccount = servicesServiceAccount;
   }
}
