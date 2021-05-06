package com.pfizer.equip.messages.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps properties from the application.properties file. Keys with the
 * "messages" prefix will be stored to their corresponding properties in this
 * class.
 * 
 * <pre>
 * messages.host=localhost
 * messages.port=123
 * messages.user=test
 * messages.password=password
 * </pre>
 * 
 * @author philip.lee
 *
 */
@ConfigurationProperties("messages")
public class MessagesProperties {

   private String host;
   private int port;
   private String user;
   private String password;

   public String getHost() {
      return host;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public int getPort() {
      return port;
   }

   public void setPort(int port) {
      this.port = port;
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
