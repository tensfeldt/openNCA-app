package com.pfizer.equip.services.business.authorization;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfizer.equip.services.business.authorization.exceptions.DuplicateAccessRecordException;
import com.pfizer.equip.services.business.authorization.exceptions.DuplicateSecurityPrincipalException;
import com.pfizer.equip.services.business.authorization.exceptions.ExternalGroupAlreadyMappedException;
import com.pfizer.equip.services.business.authorization.exceptions.MissingSecurityPrincipalException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.relational.entity.Group;
import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.relational.entity.Role;
import com.pfizer.equip.shared.relational.entity.RolePrivilege;
import com.pfizer.equip.shared.relational.repository.GroupAccessRepository;
import com.pfizer.equip.shared.relational.repository.GroupRepository;
import com.pfizer.equip.shared.relational.repository.RolePrivilegeRepository;
import com.pfizer.equip.shared.relational.repository.RoleRepository;
import com.pfizer.equip.shared.service.business.api.ReportingEventService;
import com.pfizer.equip.shared.service.user.DirectoryService;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.EntityType;

/**
 * Provides ability to make modifications to security model.
 * <p>
 * Allows for addition/removal of security objects (roles, groups, study access).
 */
@Service
public class SecurityAdminService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private UserLookupService userLookupService;

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
   private RepositoryService repositoryService;

   @Autowired
   private ReportingEventService reportingEventService;

   public void addRole(String requestUserId, String systemId, String roleName, String externalGroupName) throws Exception {
      checkCanModifyRole(requestUserId, systemId);
      // TODO: Proper error handling for DirectoryService 404 if storing membership in AD

      // Check if AD group exists:
      directoryService.getUsersByGroup(externalGroupName);
      
      // Check if we've already mapped this group:
      Role alreadyExistingRole = roleRepository.findBySystemIdAndExternalGroupName(systemId, externalGroupName);
      if (alreadyExistingRole != null) {
         throw new ExternalGroupAlreadyMappedException(String.format("External group '%s' already mapped to role '%s' in system '%s'.", externalGroupName, alreadyExistingRole.getRoleName(), systemId));
      }
      
      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);
      if (role == null) {
         role = new Role();
         role.setExternalGroupName(externalGroupName);
         role.setRoleName(roleName);
         role.setSystemId(systemId);
         roleRepository.save(role);
         log.debug("User '{}' succesfully added role '{}' in system '{}'", requestUserId, roleName, systemId);
      } else {
         throw new DuplicateSecurityPrincipalException(String.format("Role '%s' already exists in system '%s'.", roleName, systemId));
      }
   }

   public void dropRole(String requestUserId, String systemId, String roleName) throws Exception {
      checkCanModifyRole(requestUserId, systemId);
      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);
      if (role != null) {
         roleRepository.delete(role);
         log.debug("User '{}' succesfully dropped role '{}' in system '{}'", requestUserId, roleName, systemId);
      } else {
         throw new MissingSecurityPrincipalException(String.format("Role '%s' does not exist in system '%s'", roleName, systemId));
      }
   }

   // TODO: Should we be able to add multiple privs in one shot?
   public void addPrivToRole(String requestUserId, String systemId, String roleName, PrivilegeType privilegeKey) throws Exception {
      checkCanModifyRole(requestUserId, systemId);
      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);
      if (role != null) {
         RolePrivilege rolePrivilege = new RolePrivilege();
         rolePrivilege.setRoleId(role.getRoleId());
         rolePrivilege.setSystemId(systemId);
         rolePrivilege.setPrivilegeKey(privilegeKey);
         rolePrivilegeRepository.save(rolePrivilege);
         log.debug("User '{}' succesfully added privilege '{}' to role '{}' in system '{}'", requestUserId, privilegeKey, roleName, systemId);
      } else {
         throw new MissingSecurityPrincipalException(String.format("Role '%s' does not exist in system '%s'", roleName, systemId));
      }
   }

   public void dropPrivFromRole(String requestUserId, String systemId, String roleName, PrivilegeType privilegeKey) throws Exception {
      checkCanModifyRole(requestUserId, systemId);
      Role role = roleRepository.findBySystemIdAndRoleName(systemId, roleName);
      if (role != null) {
         Set<RolePrivilege> rolePrivileges = role.getRolePrivileges();
         for (RolePrivilege rolePrivilege : rolePrivileges) {
            if (rolePrivilege.getPrivilegeKey().equals(privilegeKey))
               rolePrivilegeRepository.delete(rolePrivilege);
         }
         log.debug("User '{}' succesfully dropped privilege '{}' from role '{}' in system '{}'", requestUserId, privilegeKey, roleName, systemId);
      } else {
         throw new MissingSecurityPrincipalException(String.format("Role '%s' does not exist in system '%s'", roleName, systemId));
      }
   }

   public void addGroup(String requestUserId, String systemId, String groupName, String externalGroupName) throws Exception {
      checkCanModifyGroup(requestUserId, systemId, Optional.empty());
      // TODO: Proper error handling for DirectoryService 404 if storing membership in AD

      // Check if AD group exists:
      directoryService.getUsersByGroup(externalGroupName);

      // Check if we've already mapped this group:
      Group alreadyExistingGroup = groupRepository.findByExternalGroupName(externalGroupName);
      if (alreadyExistingGroup != null) {
         throw new ExternalGroupAlreadyMappedException(String.format("External group '%s' already mapped to group '%s'.", externalGroupName, alreadyExistingGroup.getGroupName()));
      }

      Group group = groupRepository.findByGroupName(groupName);
      if (group == null) {
         group = new Group();
         group.setExternalGroupName(externalGroupName);
         group.setGroupName(groupName);
         groupRepository.save(group);
         log.debug("User '{}' succesfully added group '{}' in system '{}'", requestUserId, groupName, systemId);
      } else {
         throw new DuplicateSecurityPrincipalException(String.format("Group '%s' already exists.", groupName));
      }
   }

   public void dropGroup(String requestUserId, String systemId, String groupName) throws Exception {
      checkCanModifyGroup(requestUserId, systemId, Optional.empty());
      Group group = groupRepository.findByGroupName(groupName);
      if (group != null) {
         groupRepository.delete(group);
         log.debug("User '{}' succesfully dropped group '{}' in system '{}'", requestUserId, groupName, systemId);
      } else {
         throw new MissingSecurityPrincipalException(String.format("Group '%s' does not exist in system '%s'", groupName, systemId));
      }
   }

   public void addAccessToGroup(String requestUserId, String systemId, String groupName, String entityId, EntityType entityType, boolean viewBlindedFlag, boolean viewRestrictedFlag) throws Exception {
      checkCanModifyGroup(requestUserId, systemId, Optional.of(entityType));
      Group group = groupRepository.findByGroupName(groupName);

      if (group == null) {
         throw new MissingSecurityPrincipalException(String.format("Group '%s' does not exist in system '%s'", groupName, systemId));
      }
      
      Set<GroupAccess> groupAccesses = group.getGroupAccesses();
      for (GroupAccess groupAccess : groupAccesses) {
         // (security_group_id, entity_id, entity_type),
         if (groupAccess.getEntityId().equals(entityId) && groupAccess.getEntityType().equals(entityType)) {
            throw new DuplicateAccessRecordException(String.format(
                  "Group access record for entity '%s/%s' for group '%s' already exists: delete the record and re-create to change.", entityType, entityId, groupName));
         }
      }
      
      GroupAccess groupAccess = new GroupAccess();
      groupAccess.setGroupId(group.getSecurityGroupId());
      groupAccess.setEntityId(entityId);
      groupAccess.setEntityType(entityType);
      groupAccess.setBlindedAccessFlag(viewBlindedFlag);
      groupAccess.setRestrictedAccessFlag(viewRestrictedFlag);
      groupAccessRepository.save(groupAccess);
      log.debug("User '{}' succesfully added access viewRestrictedFlag={}, viewBlindedFlag={}, to entity '{}/{}' to group '{}'.",
            requestUserId, viewBlindedFlag, viewRestrictedFlag, entityType, entityId, groupName);
   }

   public void dropAccessFromGroup(String requestUserId, String systemId, String groupName, String entityId, EntityType entityType) throws Exception {
      checkCanModifyGroup(requestUserId, systemId, Optional.empty());
      Group group = groupRepository.findByGroupName(groupName);
      if (group != null) {
         Set<GroupAccess> securityAccesss = group.getGroupAccesses();
         for (GroupAccess groupAccess : securityAccesss) {
            if (groupAccess.getEntityType().equals(entityType) && groupAccess.getEntityId().equals(entityId))
               groupAccessRepository.delete(groupAccess);
         }
         log.debug("User '{}' succesfully dropped access from entity '{}/{}' to group '{}'.", requestUserId, entityType, entityId, groupName);
      } else {
         throw new MissingSecurityPrincipalException(String.format("Group '%s' does not exist in system '%s'", groupName, systemId));
      }
   }

   public void copyAccessToGroup(String requestUserId, String systemId, EntityType entityType, String groupName, String sourceEntityId, String targetEntityId) throws Exception {
      checkCanModifyGroup(requestUserId, systemId, Optional.empty());
      Group group = groupRepository.findByGroupName(groupName);
      GroupAccess targetGroupAccess = new GroupAccess();
      boolean isMatched = false;

      if (group == null) {
         throw new MissingSecurityPrincipalException(String.format("Group '%s' does not exist in system '%s'", groupName, systemId));
      }

      // Check for duplicates
      for (GroupAccess groupAccess : group.getGroupAccesses()) {
         if (groupAccess.getEntityId().equals(targetEntityId) && groupAccess.getEntityType().equals(entityType)) {
            throw new DuplicateAccessRecordException(String.format(
                  "Group access record for entity '%s/%s' for group '%s' already exists: delete the record and re-create to change.", entityType, targetEntityId, groupName));
         }
      }
      
      // Copy group access record to new entity id
      Set<GroupAccess> groupAccesses = group.getGroupAccesses();
      for (GroupAccess groupAccess : groupAccesses) {
            if (groupAccess.getEntityId().equals(sourceEntityId) && groupAccess.getEntityType().equals(entityType)) {
               targetGroupAccess = new GroupAccess();
               targetGroupAccess.setGroupId(group.getSecurityGroupId());
               targetGroupAccess.setEntityId(targetEntityId);
               targetGroupAccess.setEntityType(groupAccess.getEntityType());
               targetGroupAccess.setBlindedAccessFlag(groupAccess.getBlindedAccessFlag());
               targetGroupAccess.setRestrictedAccessFlag(groupAccess.getRestrictedAccessFlag());
               groupAccessRepository.save(targetGroupAccess);
               isMatched = true;
               break;
            }
      }
      
      // Better throw if we didn't match so the user knows
      if (!isMatched) {
         throw new MissingSecurityPrincipalException(String.format("Source group access record for entity '%s/%s' in group '%s' could not be found for copying.", entityType, sourceEntityId, groupName));
      }
   }
   
   public void checkCanModifyGroup(String requestUserId, String systemId, Optional<EntityType> entityType) throws Exception {
      if (!userLookupService.hasPrivilege(requestUserId, systemId, PrivilegeType.ALTER_GROUP)) {
         if (entityType.isPresent() && entityType.get() == EntityType.REPORTING_EVENT) {
            if (userLookupService.hasPrivilege(requestUserId, systemId, PrivilegeType.ALTER_REPORTING_EVENT) ||
                  userLookupService.hasPrivilege(requestUserId, systemId, PrivilegeType.ALTER_REPORTING_EVENT_SELF)) {
               // specific to reporting event group adds when saving a new reporting event
               // a user can have ALTER_REPORTING_EVENT or ALTER_REPORTING_EVENT_SELF and still be OK
               return;
            }
         }
         throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify groups.", requestUserId));
      }
   }
   public void checkCanModifyRole(String requestUserId, String systemId) throws Exception {
      if (!userLookupService.hasPrivilege(requestUserId, systemId, PrivilegeType.ALTER_ROLE)) {
         throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify roles.", requestUserId));
      }
   }

}
