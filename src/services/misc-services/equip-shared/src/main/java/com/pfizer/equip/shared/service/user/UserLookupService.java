package com.pfizer.equip.shared.service.user;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.relational.entity.Group;
import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.relational.entity.Role;
import com.pfizer.equip.shared.relational.entity.RolePrivilege;
import com.pfizer.equip.shared.relational.repository.GroupAccessRepository;
import com.pfizer.equip.shared.relational.repository.GroupRepository;
import com.pfizer.equip.shared.relational.repository.RolePrivilegeRepository;
import com.pfizer.equip.shared.relational.repository.RoleRepository;
import com.pfizer.equip.shared.service.user.exceptions.InvalidUserException;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchGroupException;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchRoleException;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchUserException;
import com.pfizer.equip.shared.types.EntityType;

/**
 * Provides abstractions layer between DirectoryService and application-level groups and roles.
 * <p>
 * DirectoryService can be used as a generic adapter for the underlying directory interface, whereas this class provides a mapping between directory-level objects (AD
 * users and AD groups) and application-level objects (users, groups, roles, functions). UserLookupService class will take care of database interaction to retrieve the
 * mappings.
 */
@Service
public class UserLookupService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private GroupRepository groupRepository;

   @Autowired
   private GroupAccessRepository groupAccessRepository;

   @Autowired
   private RoleRepository roleRepository;

   @Autowired
   private RolePrivilegeRepository rolePrivilegeRepository;

   @Autowired
   private DirectoryService directoryService;

   @Autowired
   private ExternalUserGroupsCache externalUserGroupsCache;

   @Autowired
   private UserInfoCache userInfoCache;

   @Autowired
   private SharedApplicationProperties applicationProperties;

   private final static String INVALID_USER_MESSAGE = "User '%s' exists in directory but does not have access to system '%s'";

   @PostConstruct
   public void initialize() {
      // Get the LoadingCache from CacheService via OperationalMetadataCacheLoader
      if (!applicationProperties.isStandaloneMode()) {
         externalUserGroupsCache.load();
         userInfoCache.load();
      }
   }

   public void clearCache() throws ExecutionException {
      externalUserGroupsCache.clear();
   }

   private Set<Group> mapGroups(Set<String> externalUserGroupId) {
      return groupRepository.findByExternalGroupNameIn(externalUserGroupId);
   }

   private String unmapGroup(String groupName) {
      // TODO: Confirm approach for handling groups that don't exist.
      Group group = groupRepository.findByGroupName(groupName);
      String externalGroupName = "";
      if (group != null) {
         externalGroupName = group.getExternalGroupName();
      } else {
         throw new NoSuchGroupException(String.format("Group '%s' could not be found.", groupName));
      }
      return externalGroupName;
   }

   private String unmapRole(String systemId, String roleName) {
      // TODO: Confirm approach for handling roles that don't exist
      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);
      String externalGroupName = "";
      if (role != null) {
         externalGroupName = role.getExternalGroupName();
      } else {
         throw new NoSuchRoleException(String.format("Role '%s' in system '%s' could not be found.", roleName, systemId));
      }
      return externalGroupName;
   }

   private Set<String> mapRoleNames(String systemId, Set<String> externalUserGroupId) {
      Set<Role> roles = roleRepository.findBySystemIdAndExternalGroupNameIn(systemId, externalUserGroupId);
      Set<String> roleNames = new HashSet<String>();
      // TODO: Confirm approach for handling roles that don't exist.
      for (Role role : roles) {
         roleNames.add(role.getRoleName());
      }
      return roleNames;
   }

   private Set<Role> mapRoles(String systemId, Set<String> externalUserGroupId) {
      return roleRepository.findBySystemIdAndExternalGroupNameIn(systemId, externalUserGroupId);
   }

   // TODO: Fix bug with invalid users when retrieving users by group/role! FIXME
   private Set<String> getLogonRoles(String systemId) {
      // We really only need one logon role but it simplifies the data model to allow for more than one.
      Set<String> logonRoleNames = new HashSet<String>();
      Set<RolePrivilege> rolePrivileges = rolePrivilegeRepository.findBySystemIdAndPrivilegeKey(systemId, PrivilegeType.LOGON);
      for (RolePrivilege rolePrivilege : rolePrivileges) {
         logonRoleNames.add(rolePrivilege.getRole().getRoleName());
      }

      return logonRoleNames;
   }

   public UserInfo lookupUser(String userId) throws JsonProcessingException, IOException, ExecutionException {
      return userInfoCache.query(userId);
   }

   public Set<UserInfo> lookupUsersBySystem(String systemId, Set<String> userIds) throws JsonProcessingException, IOException, ExecutionException {
      Set<UserInfo> userInfos = new HashSet<UserInfo>();
      for (String userId : userIds) {
         // We don't have a bulk way of interacting with the directory at this point.
         UserInfo userInfo;
         try {
            userInfo = userInfoCache.query(userId);
            // If the directory says the user is active, we need to also make sure they have access to this system
            // If the user is inactive in the directory, then they don't have access to any system. So only check if active.
            if (userInfo.isActive()) {
               userInfo.setActive(isValidUser(systemId, userId));
            }
         } catch (NoSuchUserException e) {
            // Some users may have been archived to a separate part of the directory
            // Include blanks for missing info with archived placeholder
            userInfo = new UserInfo(userId, "(archived)", "", "", false);
         }
         userInfos.add(userInfo);
      }
      return userInfos;
   }

   public Set<UserInfo> getUsers(String systemId) throws JsonProcessingException, IOException, ExecutionException {
      Set<String> logonRoles = getLogonRoles(systemId);
      Set<String> users = new HashSet<String>();

      for (String role : logonRoles) {
         users.addAll(directoryService.getUsersByGroup(unmapRole(systemId, role)));
      }
      return lookupUsersBySystem(systemId, users);
   }

   public boolean isValidUser(String systemId, String userId) throws JsonProcessingException, IOException, ExecutionException {
      return getUserFunctions(systemId, userId).contains(PrivilegeType.LOGON);
   }

   public void validateUser(String systemId, String userId) throws JsonProcessingException, IOException, ExecutionException {
      if (!isValidUser(systemId, userId)) {
         throw new InvalidUserException(String.format(INVALID_USER_MESSAGE, userId, systemId));
      }
   }

   public Set<Group> getUserGroups(String userId, Set<String> externalUserGroups) throws JsonProcessingException, IOException, ExecutionException {
      if (externalUserGroups == null) {
         externalUserGroups = externalUserGroupsCache.query(userId);
      }
      Set<Group> userGroups = mapGroups(externalUserGroups);
      return userGroups;
   }

   public Set<String> getUserGroupNames(String userId) throws JsonProcessingException, IOException, ExecutionException {
      Set<String> externalUserGroups = externalUserGroupsCache.query(userId);
      Set<Group> userGroups = mapGroups(externalUserGroups);
      Set<String> userGroupNames = new HashSet<String>();
      for (Group group : userGroups) {
         userGroupNames.add(group.getGroupName());
      }
      return userGroupNames;
   }

   public Set<String> getUserRoleNames(String systemId, String userId) throws JsonProcessingException, IOException, ExecutionException {
      validateUser(systemId, userId);
      Set<String> externalUserGroups = externalUserGroupsCache.query(userId);
      Set<String> userRoles = mapRoleNames(systemId, externalUserGroups);

      return userRoles;
   }

   public Set<Role> getUserRoles(String systemId, String userId) throws JsonProcessingException, IOException, ExecutionException {
      Set<String> externalGroups = externalUserGroupsCache.query(userId); // get the groups only once, avoid repeated calls
      return getUserRoles(systemId, userId, externalGroups);
   }

   public Set<Role> getUserRoles(String systemId, String userId, Set<String> externalUserGroups) throws JsonProcessingException, IOException, ExecutionException {
      if (applicationProperties.isStandaloneMode()) {
         return roleRepository.findBySystemId(systemId); // return all roles configured for this system
      }

      if (externalUserGroups == null) {
         externalUserGroups = externalUserGroupsCache.query(userId);
      }
      Set<Role> userRoles = mapRoles(systemId, externalUserGroups);

      return userRoles;
   }

   public EnumSet<PrivilegeType> getUserFunctions(String systemId, String userId) throws JsonProcessingException, IOException, ExecutionException {
      return getUserFunctions(systemId, userId, null);
   }

   public EnumSet<PrivilegeType> getUserFunctions(String systemId, String userId, Set<String> externalUserGroups)
         throws JsonProcessingException, IOException, ExecutionException {
      Set<Role> userRoles = getUserRoles(systemId, userId, externalUserGroups);
      EnumSet<PrivilegeType> userFunctions = EnumSet.noneOf(PrivilegeType.class);

      for (Role role : userRoles) {
         for (RolePrivilege privilege : role.getRolePrivileges()) {
            userFunctions.add(privilege.getPrivilegeKey());
         }
      }

      return userFunctions;
   }

   public Set<PrivilegeType> getRoleFunctions(String systemId, String roleName) throws IOException {
      Set<PrivilegeType> roleFunctions = new HashSet<PrivilegeType>();

      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);

      if (role == null) {
         throw new NoSuchRoleException(String.format("Role '%s' in system '%s' could not be found", roleName, systemId));
      }

      Set<RolePrivilege> rolePrivileges = role.getRolePrivileges();
      for (RolePrivilege rolePrivilege : rolePrivileges) {
         roleFunctions.add(rolePrivilege.getPrivilegeKey());
      }

      return roleFunctions;
   }

   public Set<GroupAccess> getGroupAccesses(String groupName) throws IOException {
      Group group = groupRepository.findByGroupName(groupName);
      if (group == null) {
         throw new NoSuchGroupException(String.format("Group '%s' could not be found.", groupName));
      } else {
         return group.getGroupAccesses();
      }
   }

   public Permissions getUserPermissions(String systemId, String userId) throws IOException, ExecutionException {
      // Will build an entity map from group access records and put it in a Permissions object that the authorization service can understand.
      // Top level is the type of access (blinded, restricted), next level down is the type of entity (protocol, dataframe, reporting event),
      // and then the bottom level is a set of entity IDs.
      Map<AccessFlag, EntityMap> entityAccess = new HashMap<AccessFlag, EntityMap>();
      Set<GroupAccess> groupAccesses = new HashSet<GroupAccess>();
      Permissions permissions = new Permissions();

      // Get unmapped groups, and only do it once. Querying the directory is expensive:
      Set<String> externalGroups = externalUserGroupsCache.query(userId);

      // Now pass in the unmapped groups to getUserGroups which will call only the database, rather than also querying directory.
      Set<Group> userGroups = getUserGroups(userId, externalGroups);
      // Similar here, pass in the unmapped groups and query the database only, not the directory to determine the user's functions.
      Set<PrivilegeType> userPrivileges = getUserFunctions(systemId, userId, externalGroups);

      permissions.setPrivileges(userPrivileges);

      for (Group group : userGroups) {
         groupAccesses.addAll(group.getGroupAccesses());
      }

      // Translate the group access records into an entity map that the authorization service can understand.
      for (GroupAccess groupAccess : groupAccesses) {
         // Build the blinded access entity map
         if (groupAccess.getBlindedAccessFlag()) {
            EntityMap entry;
            if (entityAccess.get(AccessFlag.STUDY_BLINDED) == null) {
               entry = new EntityMap();
               entityAccess.put(AccessFlag.STUDY_BLINDED, entry);
            } else {
               entry = entityAccess.get(AccessFlag.STUDY_BLINDED);
            }

            // Initialize the Set if needed:
            if (entry.get(groupAccess.getEntityType()) == null) {
               entry.put(groupAccess.getEntityType(), new HashSet<>());
            }
            // Now add the entity ID to the appropriate entity type set
            entry.get(groupAccess.getEntityType()).add(groupAccess.getEntityId());
         }
         // Build the restricted access entity map
         if (groupAccess.getRestrictedAccessFlag()) {
            EntityMap entry;
            if (entityAccess.get(AccessFlag.RESTRICTED) == null) {
               entry = new EntityMap();
               entityAccess.put(AccessFlag.RESTRICTED, entry);
            } else {
               entry = entityAccess.get(AccessFlag.RESTRICTED);
            }
            // Initialize the Set if needed:
            if (entry.get(groupAccess.getEntityType()) == null) {
               entry.put(groupAccess.getEntityType(), new HashSet<>());
            }
            // Now add the entity ID to the appropriate entity type set
            entry.get(groupAccess.getEntityType()).add(groupAccess.getEntityId());
         }
         if (groupAccess.getEntityType().equals(EntityType.REPORTING_EVENT)) {
            permissions.addReportingEventId(groupAccess.getEntityId());
         }
      }

      // Now add each entity Map to the Permissions object
      if (entityAccess.get(AccessFlag.STUDY_BLINDED) != null) {
         for (EntityType entityType : entityAccess.get(AccessFlag.STUDY_BLINDED).keySet()) {
            permissions.setUnblindedEntities(entityType, entityAccess.get(AccessFlag.STUDY_BLINDED).get(entityType));
         }
      }
      if (entityAccess.get(AccessFlag.RESTRICTED) != null) {
         for (EntityType entityType : entityAccess.get(AccessFlag.RESTRICTED).keySet()) {
            permissions.setUnrestrictedEntities(entityType, entityAccess.get(AccessFlag.RESTRICTED).get(entityType));
         }
      }

      if (!userPrivileges.contains(PrivilegeType.LOGON)) {
         log.info("External user %s detected during permissions retrieval.", userId);
         permissions.setIsExternalUser(true);
         if (permissions.hasProtocolAccessRecord() || permissions.hasDataframeAccessRecord()) {
            throw new RuntimeException(
                  String.format("User '%s' is an external user but has records for Protocol-level or Dataframe-level access. Invalid security config.", userId));
         }
      }

      return permissions;
   }

   public Set<UserInfo> getUsersByGroup(String systemId, String groupName) throws JsonProcessingException, IOException, ExecutionException {
      String externalGroupName = unmapGroup(groupName);
      Set<String> users = directoryService.getUsersByGroup(externalGroupName);

      return lookupUsersBySystem(systemId, users);
   }

   public Set<String> getUsernamesByRole(String systemId, String roleName) throws JsonProcessingException, IOException, ExecutionException {
      String externalGroupName = unmapRole(systemId, roleName);
      Set<String> users = directoryService.getUsersByGroup(externalGroupName);

      return users;
   }

   public Set<UserInfo> getUsersByRole(String systemId, String roleName) throws JsonProcessingException, IOException, ExecutionException {
      String externalGroupName = unmapRole(systemId, roleName);
      Set<String> users = directoryService.getUsersByGroup(externalGroupName);

      return lookupUsersBySystem(systemId, users);
   }

   public boolean hasPrivilege(String requestUserId, String systemId, PrivilegeType privilegeKey) throws JsonProcessingException, IOException, ExecutionException {
      log.info("Checking if user '{}' has privilege '{}' in system '{}'", requestUserId, privilegeKey, systemId);
      EnumSet<PrivilegeType> userPrivileges = getUserFunctions(systemId, requestUserId);
      // Normally would use validateUser but that incurs an extra call to research service.
      if (!userPrivileges.contains(PrivilegeType.LOGON)) {
         throw new InvalidUserException(String.format(INVALID_USER_MESSAGE, requestUserId, systemId));
      }
      return (!userPrivileges.isEmpty() && userPrivileges.contains(privilegeKey));
   }

   public Set<Group> getGroupsByAccess(EntityType entityType, String entityId) {
      Set<Group> groups = new HashSet<Group>();
      Set<GroupAccess> groupAccesses = groupAccessRepository.findByEntityTypeAndEntityId(entityType, entityId);
      for (GroupAccess groupAccess : groupAccesses) {
         groups.add(groupAccess.getGroup());
      }
      return groups;
   }

   public Set<Group> getGroups() {
      Set<Group> groups = new HashSet<Group>();
      groupRepository.findAll().forEach(group -> groups.add(group));
      return groups;
   }

   public Set<Role> getRoles(String systemId) {
      Set<Role> roles = roleRepository.findBySystemId(systemId);
      return roles;
   }
}