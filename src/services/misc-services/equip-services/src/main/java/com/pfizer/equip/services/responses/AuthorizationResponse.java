package com.pfizer.equip.services.responses;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pfizer.equip.shared.relational.entity.Group;
import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.relational.entity.Role;
import com.pfizer.equip.shared.responses.AbstractResponse;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserInfo;

@JsonInclude(Include.NON_EMPTY)
public class AuthorizationResponse extends AbstractResponse {
   Map<String, Boolean> permissionsInfo = new HashMap<String, Boolean>();

   // TODO: This will end up being included in every response, need to figure out
   // if it should be set.
   private Set<String> roles;
   private Set<String> groups;
   private Set<Group> groupDetails;
   private Set<GroupAccess> groupAccesses;
   private Set<UserInfo> users;
   private Set<Role> roleDetails;
   private Set<PrivilegeType> privileges;

   public void setRoles(Set<String> roles) {
      this.roles = roles;
   }

   public Set<String> getRoles() {
      return roles;
   }

   public void setGroups(Set<String> groups) {
      this.groups = groups;
   }

   public Set<String> getGroups() {
      return groups;
   }

   public void setUsers(Set<UserInfo> users) {
      this.users = users;
   }

   public Set<UserInfo> getUsers() {
      return users;
   }

   public void setPrivileges(Set<PrivilegeType> privileges) {
      this.privileges = privileges;
   }

   public Set<PrivilegeType> getPrivileges() {
      return privileges;
   }

   public Map<String, Boolean> getPermissionsInfo() {
      return permissionsInfo;
   }

   public void setPermissionsInfo(Map<String, Boolean> permissionsInfo) {
      this.permissionsInfo = permissionsInfo;
   }
   
   public void putPermissionInfo(String flagKey, boolean flagValue) {
      this.permissionsInfo.put(flagKey, flagValue);
   }

   public boolean getPermissionInfo(String flagKey) {
      return this.permissionsInfo.get(flagKey);
   }

   public Set<GroupAccess> getGroupAccesses() {
      return groupAccesses;
   }

   public void setGroupAccesses(Set<GroupAccess> groupAccesses) {
      this.groupAccesses = groupAccesses;
   }

   public Set<Group> getGroupDetails() {
      return groupDetails;
   }

   public void setGroupDetails(Set<Group> groupDetails) {
      this.groupDetails = groupDetails;
   }

   public Set<Role> getRoleDetails() {
      return roleDetails;
   }

   public void setRoleDetails(Set<Role> roleDetails) {
      this.roleDetails = roleDetails;
   }
}
