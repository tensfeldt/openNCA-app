package com.pfizer.equip.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modeshape-service")
public class OpmetaModeShapeServiceProperties {
   private String url;
   private String user;
   private String password;
   private String repository;
   private String workspace;

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
   public String getRepository() {
      return repository;
   }
   public void setRepository(String repository) {
      this.repository = repository;
   }
   public String getWorkspace() {
      return workspace;
   }
   public void setWorkspace(String workspace) {
      this.workspace = workspace;
   }

}
