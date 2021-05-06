package com.pfizer.equip.services.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User lookup service configuration values.
 */
@ConfigurationProperties(prefix = "modeshape-service")
public class ModeShapeServiceProperties {
   private String url;
   private String user;
   private String password;
   private String repository;
   private String workspace;
   private List<String> supportedTypes;
   private String querySelectColumns;

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

   public List<String> getSupportedTypes() {
      return supportedTypes;
   }

   public void setSupportedTypes(List<String> supportedTypes) {
      this.supportedTypes = supportedTypes;
   }

   public String getQuerySelectColumns() {
      return querySelectColumns;
   }

   public void setQuerySelectColumns(String querySelectColumns) {
      this.querySelectColumns = querySelectColumns;
   }
}
