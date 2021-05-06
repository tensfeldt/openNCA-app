package com.pfizer.equip.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User lookup service configuration values.
 */
@ConfigurationProperties(prefix = "user-service")
public class DirectoryServiceProperties {
   private String url;
   private String user;
   private String password;
   
   public String getUrl() {
      return url;
   }
   public void setUrl(String url) {
      this.url = url;
   }
   public String getUser() {
      return user;
   }
   public void setUser(String user) {
      this.user = user;
   }
   public String getPassword() {
      return password;
   }
   public void setPassword(String password) {
      this.password = password;
   }
}
