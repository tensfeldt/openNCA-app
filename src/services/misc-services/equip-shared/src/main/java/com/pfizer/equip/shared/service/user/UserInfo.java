package com.pfizer.equip.shared.service.user;

public class UserInfo {
   private String userId;
   private String firstName;
   private String lastName;
   private String emailAddress;
   private Boolean isActive;

   public UserInfo() {

   }

   public UserInfo(String userId, String firstName, String lastName, String emailAddress, Boolean isActive) {
      this.userId = userId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.emailAddress = emailAddress;
      this.isActive = isActive;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getEmailAddress() {
      return emailAddress;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
   }

   public String getuserId() {
      return userId;
   }

   public void setuserId(String userId) {
      this.userId = userId;
   }

   public Boolean isActive() {
      return isActive;
   }

   public void setActive(Boolean isActive) {
      this.isActive = isActive;
   }
}
