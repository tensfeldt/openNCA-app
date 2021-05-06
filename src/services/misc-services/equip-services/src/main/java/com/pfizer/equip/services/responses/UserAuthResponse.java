package com.pfizer.equip.services.responses;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pfizer.equip.shared.responses.AbstractResponse;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserInfo;

public class UserAuthResponse extends AbstractResponse {
   
   private String userId;
   private UserInfo userInfo;
   private Set<PrivilegeType> privileges;
   private Set<String> roles;
   private Set<String> groups;
   
   public UserAuthResponse() {
      super();
   }
   
   @JsonIgnore
   @Override
   public Response getResponse() {
      return Response.EMPTY;
   }
   
   public UserInfo getUserInfo() {
      return userInfo;
   }
   
   public void setUserInfo(UserInfo userInfo) {
      this.userInfo = userInfo;
   }

   public Set<PrivilegeType> getPrivileges() {
      return privileges;
   }

   public void setPrivileges(Set<PrivilegeType> privileges) {
      this.privileges = privileges;
   }

   public Set<String> getRoles() {
      return roles;
   }

   public void setRoles(Set<String> roles) {
      this.roles = roles;
   }

   public Set<String> getGroups() {
      return groups;
   }

   public void setGroups(Set<String> groups) {
      this.groups = groups;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }
}
