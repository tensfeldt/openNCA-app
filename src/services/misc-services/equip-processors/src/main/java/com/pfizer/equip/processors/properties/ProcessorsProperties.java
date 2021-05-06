package com.pfizer.equip.processors.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("processors")
public class ProcessorsProperties {
   private String mailServiceAccount;
   private String eventQueue;
   private String notificationQueue;
   private String notificationEmailRealtimeQueue;
   private String mailSubjectPrefix;
   private String mailToSupport;
   private String servicesBaseUrl;
   private String servicesServiceAccount;
   private int mailSizeLimit;

   public String getMailToSupport() {
      return mailToSupport;
   }

   public void setMailToSupport(String mailToSupport) {
      this.mailToSupport = mailToSupport;
   }

   public String getMailServiceAccount() {
      return mailServiceAccount;
   }

   public void setMailServiceAccount(String mailServiceAccount) {
      this.mailServiceAccount = mailServiceAccount;
   }

   public String getEventQueue() {
      return eventQueue;
   }

   public void setEventQueue(String eventQueue) {
      this.eventQueue = eventQueue;
   }

   public String getNotificationQueue() {
      return notificationQueue;
   }

   public void setNotificationQueue(String notificationQueue) {
      this.notificationQueue = notificationQueue;
   }

   public String getNotificationEmailRealtimeQueue() {
      return notificationEmailRealtimeQueue;
   }

   public void setNotificationEmailRealtimeQueue(String notificationEmailRealtimeQueue) {
      this.notificationEmailRealtimeQueue = notificationEmailRealtimeQueue;
   }

   public String getMailSubjectPrefix() {
      return mailSubjectPrefix;
   }

   public void setMailSubjectPrefix(String mailSubjectPrefix) {
      this.mailSubjectPrefix = mailSubjectPrefix;
   }

   public int getMailSizeLimit() {
      return mailSizeLimit;
   }

   public void setMailSizeLimit(int mailSizeLimit) {
      this.mailSizeLimit = mailSizeLimit;
   }

   public String getServicesBaseUrl() {
      return servicesBaseUrl;
   }

   public void setServicesBaseUrl(String servicesBaseUrl) {
      this.servicesBaseUrl = servicesBaseUrl;
   }

   public String getServicesServiceAccount() {
      return servicesServiceAccount;
   }

   public void setServicesServiceAccount(String servicesServiceAccount) {
      this.servicesServiceAccount = servicesServiceAccount;
   }
}
