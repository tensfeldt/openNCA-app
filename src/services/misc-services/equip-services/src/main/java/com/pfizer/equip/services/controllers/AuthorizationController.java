package com.pfizer.equip.services.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.api.input.DataframeCopyAccessInput;
import com.pfizer.equip.services.business.authorization.AuthorizationService;
import com.pfizer.equip.services.business.authorization.Facts;
import com.pfizer.equip.services.business.authorization.SecurityAdminService;
import com.pfizer.equip.services.business.authorization.exceptions.EntityTypeNotSupportedException;
import com.pfizer.equip.services.exceptions.FailedGroupCopyAccessException;
import com.pfizer.equip.services.exceptions.InvalidRequestException;
import com.pfizer.equip.services.exceptions.NotAuthenticatedException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.AuthorizationResponse;
import com.pfizer.equip.shared.exceptions.EquipException;
import com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.relational.entity.Group;
import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.relational.entity.Role;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.service.user.Permissions;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserInfo;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchUserException;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class AuthorizationController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private SecurityAdminService securityAdminService;

   @Autowired
   private AuthorizationService authorizationService;

   @Autowired
   private EventService eventService;

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;

   @Autowired
   private AuditService auditService;

   @RequestMapping(value = "{systemId}/dataframe/check-access", method = RequestMethod.POST, consumes = "application/json")
   public AuthorizationResponse checkDataframeAccess(@PathVariable("systemId") String systemId, @RequestBody Dataframe dataframe,
         @RequestParam("userId") Optional<String> userIdOverride, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         if (userIdOverride.isPresent()) {
            // If caller has specified a userId in the query string, override it.
            // This supports external user authorization, where userId would not pass the interceptor.
            userId = userIdOverride.get();
         }

         // We will pass the AuthorizationService.canViewDataframe() two objects:
         // - Permissions: Info about what the user is authorized to do.
         // - Facts: Info about the object we're checking access for.
         // From these, the AuthorizationService method determines access.
         // Facts and Permissions are generated inside the AuthorizationInputBuilder using metadata from the dataframe object.
         log.info("Checking authorization access for dataframe for user ID '{}'...", userId);
         boolean canViewDataframe;

         if (applicationProperties.isStandaloneMode()) {
            canViewDataframe = true;
         } else {
            Permissions permissions = userLookupService.getUserPermissions(systemId, userId);

            // assume the dataframe belongs to a single protocol
            String[] programStudyId = dataframe.getProgramStudyIds().iterator().next();
            String programId = programStudyId[0];
            String protocolId = programStudyId[1];
            Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
            Facts facts = authorizationService.getFacts(dataframe, protocol);
            canViewDataframe = permissions.isExternalUser() ? authorizationService.canViewDataframeExternal(permissions, facts)
                  : authorizationService.canViewDataframe(permissions, facts);
         }

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.putPermissionInfo("canViewDataframe", canViewDataframe);
         return response;
      } catch (NoSuchUserException nsue) {
         log.error("Exception occured during authorization check.", nsue);
         if (userIdOverride.isPresent()) {
            // This means the userId passed in the query param was invalid, DF team has asked that this be "false" instead of an error response.
            AuthorizationResponse response = new AuthorizationResponse();
            response.setResponse(Response.OK);
            response.putPermissionInfo("canViewDataframe", false);
            return response;
         } else {
            // Since SSO and Interceptor would already have hit this error, this would only occur in the rarest of circumstances.
            throw new NotAuthenticatedException("Not authorized, user not found", nsue);
         }
      } catch (EquipException ee) {
         log.error("Exception occured during authorization check.", ee);
         throw ee;
      } catch (Exception e) {
         log.error("Exception occured during authorization check.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/dataframe/check-accesses", method = RequestMethod.POST, consumes = "application/json")
   public AuthorizationResponse checkDataframeAccesses(@PathVariable("systemId") String systemId, @RequestBody Set<Dataframe> dataframes, HttpServletRequest request) {
      Monitor monitor = null;
      // Similar to checkDataframeAccess but performs authorization for multiple dataframes.
      try {
         String userId = getUserId(request);
         Permissions permissions = null;
         AuthorizationResponse response = new AuthorizationResponse();

         for (Dataframe dataframe : dataframes) {
            log.info("Checking authorization access for dataframe for user ID '{}'...", userId);

            String[] programStudyId = dataframe.getProgramStudyIds().iterator().next();
            String programId = programStudyId[0];
            String protocolId = programStudyId[1];
            Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);

            boolean canViewDataframe;
            if (applicationProperties.isStandaloneMode()) {
               canViewDataframe = true;
            } else {
               if (permissions == null) {
                  permissions = userLookupService.getUserPermissions(systemId, userId);
               }
               Facts facts = authorizationService.getFacts(dataframe, protocol);
               canViewDataframe = permissions.isExternalUser() ? authorizationService.canViewDataframeExternal(permissions, facts)
                       : authorizationService.canViewDataframe(permissions, facts);
            }
            response.putPermissionInfo(dataframe.getId(), canViewDataframe);
         }

         response.setResponse(Response.OK);
         return response;
      } catch (NoSuchUserException nsue) {
         log.error("Exception occured during authorization check.", nsue);
         // Since SSO and Interceptor would already have hit this error, this would only occur in the rarest of circumstances.
         throw new NotAuthenticatedException("Not authorized, user not found", nsue);
      } catch (EquipException ee) {
         log.error("Exception occured during authorization check.", ee);
         throw ee;
      } catch (Exception e) {
         log.error("Exception occured during authorization check.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/users", method = RequestMethod.GET)
   public AuthorizationResponse getUsers(@PathVariable("systemId") String systemId) {
      Monitor monitor = null;
      try {
         log.info("Getting all users.");

         Set<UserInfo> users = userLookupService.getUsers(systemId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setUsers(users);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of all users.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups", method = RequestMethod.GET)
   public AuthorizationResponse getGroups(@PathVariable("systemId") String systemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Getting all groups for {}", getUserId(request));

         Set<Group> groups = userLookupService.getGroups();

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setGroupDetails(groups);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of all users.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles", method = RequestMethod.GET)
   public AuthorizationResponse getRoles(@PathVariable("systemId") String systemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Getting all roles for {}", getUserId(request));

         Set<Role> roles = userLookupService.getRoles(systemId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setRoleDetails(roles);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of all users.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/users/{userId}/valid", method = RequestMethod.GET)
   public AuthorizationResponse isValidUser(@PathVariable("systemId") String systemId, @PathVariable("userId") String userId) {
      Monitor monitor = null;
      try {
         log.info("Checking validity of user {}.", userId);

         boolean isValidUser = userLookupService.isValidUser(systemId, userId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.putPermissionInfo("isValidUser", isValidUser);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during user validity check.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/users/{userId}/roles", method = RequestMethod.GET)
   public AuthorizationResponse getUserRoles(@PathVariable("systemId") String systemId, @PathVariable("userId") String userId) {
      Monitor monitor = null;
      try {
         log.info("Getting roles for user '{}'", userId);

         Set<String> userRoles = userLookupService.getUserRoleNames(systemId, userId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setRoles(userRoles);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of roles by user.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/users/{userId}/groups", method = RequestMethod.GET)
   public AuthorizationResponse getUserGroups(@PathVariable("systemId") String systemId, @PathVariable("userId") String userId) {
      Monitor monitor = null;
      try {
         log.info("Getting groups for user '{}'", userId);

         Set<String> userGroups = userLookupService.getUserGroupNames(userId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setGroups(userGroups);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during group retrieval by user.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/users/{userId}/privileges", method = RequestMethod.GET)
   public AuthorizationResponse getUserPrivileges(@PathVariable("systemId") String systemId, @PathVariable("userId") String userId) {
      Monitor monitor = null;
      try {
         log.info("Getting functions for user '{}'", userId);

         Set<PrivilegeType> userFunctions = userLookupService.getUserFunctions(systemId, userId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setPrivileges(userFunctions);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of functions by user.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups/{groupName}/users", method = RequestMethod.GET)
   public AuthorizationResponse getUsersbyGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName) {
      Monitor monitor = null;
      try {
         log.info("Getting users for group '{}'", groupName);

         Set<UserInfo> users = userLookupService.getUsersByGroup(systemId, groupName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setUsers(users);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of users by group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups/cache/clear", method = RequestMethod.POST)
   public AuthorizationResponse clearUserGroupsCache(@PathVariable("systemId") String systemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Clearing external user groups cache");

         String requestUserId = getUserId(request);
         if (!userLookupService.hasPrivilege(requestUserId, systemId, PrivilegeType.ALTER_GROUP)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify groups.", requestUserId));
         }

         userLookupService.clearCache();

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during clearing of external user groups cache.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles/{roleName}/users", method = RequestMethod.GET)
   public AuthorizationResponse getUsersbyRole(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName) {
      Monitor monitor = null;
      try {
         log.info("Getting users for role '{}'", roleName);

         Set<UserInfo> users = userLookupService.getUsersByRole(systemId, roleName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setUsers(users);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of users by role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles/{roleName}/privileges", method = RequestMethod.GET)
   public AuthorizationResponse getRolePrivileges(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName) {
      Monitor monitor = null;
      try {
         log.info("Getting functions for role '{}'", roleName);

         Set<PrivilegeType> functions = userLookupService.getRoleFunctions(systemId, roleName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setPrivileges(functions);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of functions by role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups/{groupName}/access", method = RequestMethod.GET)
   public AuthorizationResponse getAccessByGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving group access for {}", userId);

         Set<GroupAccess> groupAccesses = userLookupService.getGroupAccesses(groupName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setGroupAccesses(groupAccesses);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during addition of access to group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/entities/{entityType}/{entityId}/access", method = RequestMethod.GET)
   public AuthorizationResponse getGroupsByEntity(@PathVariable("systemId") String systemId, @PathVariable("entityType") EntityType entityType,
         @PathVariable("entityId") String entityId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving groups for entity {}/{} for user {}", entityType, entityId, userId);

         Set<Group> groups = userLookupService.getGroupsByAccess(entityType, entityId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         response.setGroupDetails(groups);
         return response;
      } catch (Exception e) {
         log.error("Exception occured retrieval of groups for entity.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/entities/{entityType}/{fullStudyId}/hasValidBlindingGroup", method = RequestMethod.GET)
   public AuthorizationResponse hasValidBlindingGroup(@PathVariable("systemId") String systemId, @PathVariable("entityType") EntityType entityType,
         @PathVariable("fullStudyId") String fullStudyId, HttpServletRequest request, HttpServletResponse response) {
      Monitor monitor = null;
      try {
         response.setHeader("Cache-Control", "public, max-age=300");
         AuthorizationResponse authResponse;
         if (applicationProperties.isStandaloneMode()) {
            authResponse = new AuthorizationResponse();
            authResponse.setResponse(Response.OK);
            authResponse.putPermissionInfo("hasValidBlindingGroup", true);
            return authResponse;
         }

         if (entityType != EntityType.PROTOCOL) {
            throw new EntityTypeNotSupportedException("This entity type is not supported for this operation.");
         }

         String programId = fullStudyId.split(":")[0];
         String protocolId = fullStudyId.split(":")[1];

         log.info("Checking if entity {}/{} has a valid blinding group.", programId, protocolId);

         Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);

         boolean hasValidBlindingGroup = false;
         if (!protocol.getIsStudyBlinded()) {
            hasValidBlindingGroup = true;
         } else {
            Set<Group> groups = userLookupService.getGroupsByAccess(EntityType.PROTOCOL, fullStudyId);
            if (groups.size() > 0) {
               for (Group group : groups) {
                  String groupName = group.getGroupName();
                  Set<UserInfo> users = userLookupService.getUsersByGroup(systemId, groupName);
                  if (users.size() > 0) {
                     hasValidBlindingGroup = true;
                     break;
                  }
               }
            }
         }

         authResponse = new AuthorizationResponse();
         authResponse.setResponse(Response.OK);
         authResponse.putPermissionInfo("hasValidBlindingGroup", hasValidBlindingGroup);
         return authResponse;
      } catch (Exception e) {
         log.error("Exception occured during user validity check.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles", method = RequestMethod.POST)
   public AuthorizationResponse addRole(@PathVariable("systemId") String systemId, @RequestParam("roleName") String roleName,
         @RequestParam("externalGroupName") String externalGroupName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Creating role '{}' in system '{}'.", roleName, systemId);
         String userId = getUserId(request);

         securityAdminService.addRole(userId, systemId, roleName, externalGroupName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("New role added", roleName, EntityType.ROLE.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles/{roleName}", method = RequestMethod.DELETE)
   public AuthorizationResponse dropRole(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Dropping role '{}' in system '{}'.", roleName, systemId);
         String userId = getUserId(request);

         securityAdminService.dropRole(userId, systemId, roleName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Role Deleted", roleName, EntityType.ROLE.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during removal of role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   // TODO: Make idempotent
   @RequestMapping(value = "{systemId}/roles/{roleName}/privileges/{privKey}", method = RequestMethod.PUT)
   public AuthorizationResponse addPrivilegeToRole(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName,
         @PathVariable("privKey") String privKey, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Adding priv '{}' to role '{}' in system '{}'.", privKey, roleName, systemId);
         String userId = getUserId(request);

         securityAdminService.addPrivToRole(userId, systemId, roleName, PrivilegeType.valueOf(privKey));

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService
               .insertAuditEntry(new AuditEntryInput("Added " + privKey + " privilege", roleName, EntityType.ROLE.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during addition of function to role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/roles/{roleName}/privileges/{privKey}", method = RequestMethod.DELETE)
   public AuthorizationResponse dropPrivilegeFromRole(@PathVariable("systemId") String systemId, @PathVariable("roleName") String roleName,
         @PathVariable("privKey") String privKey, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Dropping function '{}' from role '{}' in system '{}'.", privKey, roleName, systemId);
         String userId = getUserId(request);

         securityAdminService.dropPrivFromRole(userId, systemId, roleName, PrivilegeType.valueOf(privKey));

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService
               .insertAuditEntry(new AuditEntryInput("Dropped " + privKey + " privilege", roleName, EntityType.ROLE.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during removal of function from role.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups", method = RequestMethod.POST)
   public AuthorizationResponse addGroup(@PathVariable("systemId") String systemId, @RequestParam("groupName") String groupName,
         @RequestParam("externalGroupName") String externalGroupName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Creating group '{}' in system '{}'.", groupName);
         String userId = getUserId(request);

         securityAdminService.addGroup(userId, systemId, groupName, externalGroupName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);

         // Raise event for data_authorization
         Map<String, Object> description = new HashMap<>();
         description.put("comments", "New Group has been added");
         description.put("user_name", userId);
         description.put("system_initiated", "false");
         description.put("group_Name", groupName);

         eventService.createEvent(this.getClass().toString(), new Date(), groupName, "authorization", "add_remove_data_authorization", null, null, description,
               properties.getEventQueue());

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Group Added", groupName, EntityType.GROUP.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups/{groupName}", method = RequestMethod.DELETE)
   public AuthorizationResponse dropGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Dropping group '{}'.", groupName);
         String userId = getUserId(request);

         securityAdminService.dropGroup(userId, systemId, groupName);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);

         // Raise event for data_authorization
         Map<String, Object> description = new HashMap<>();
         description.put("comments", "Group has been deleted");
         description.put("user_name", userId);
         description.put("system_initiated", "false");
         description.put("group_Name", groupName);

         eventService.createEvent(this.getClass().toString(), new Date(), groupName, "authorization", "add_remove_data_authorization", null, null, description,
               properties.getEventQueue());
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Group deleted", groupName, EntityType.GROUP.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during removal of group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   // TODO: Make idempotent
   // Added @Transactional as multiple audit records are inserted here and we want to operation to be atomic
   @Transactional(rollbackFor = Exception.class)
   @RequestMapping(value = "{systemId}/groups/{groupName}/access/{entityType}/{entityId}", method = RequestMethod.PUT)
   public AuthorizationResponse addAccessToGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName,
         @PathVariable("entityId") String entityId, @PathVariable("entityType") EntityType entityType, @RequestParam("viewBlindedFlag") boolean viewBlindedFlag,
         @RequestParam("viewRestrictedFlag") boolean viewRestrictedFlag, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Adding access viewRestrictedFlag={}, viewBlindedFlag={}, to entity '{}/{}' to group '{}'.", viewBlindedFlag, viewRestrictedFlag, entityType, entityId,
               groupName);
         String userId = getUserId(request);

         securityAdminService.addAccessToGroup(userId, systemId, groupName, entityId, entityType, viewBlindedFlag, viewRestrictedFlag);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         // Raise event for data_authorization
         Map<String, Object> description = eventService.createEventDescription("New Group Access has been added", userId, "false");
         description.put("group_Name", groupName);
         description.put("view_blinded_flag", viewBlindedFlag);
         description.put("view_restricted_flag", viewRestrictedFlag);
         description.put("entity_id", entityId);
         description.put("entity_type", entityType);

         eventService.createEvent(this.getClass().toString(), new Date(), groupName, "authorization", "add_remove_data_authorization", null, null, description,
               properties.getEventQueue());
         // Create audit entry
         // For Group
         auditService.insertAuditEntry(
               new AuditEntryInput("Add Access viewRestrictedFlag=" + viewRestrictedFlag + ", viewBlindedFlag=" + viewBlindedFlag + " to entity STUDY/" + entityId,
                     entityId + "_GROUP", EntityType.GROUP.getValue(), userId, ActionStatusType.SUCCESS, null));
         // For Access
         auditService.insertAuditEntry(
               new AuditEntryInput("Add Access viewRestrictedFlag=" + viewRestrictedFlag + ", viewBlindedFlag=" + viewBlindedFlag + " to entity STUDY/" + entityId,
                     entityId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during addition of access to group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   // Added @Transactional as multiple audit records are inserted here and we want to operation to be atomic
   @Transactional(rollbackFor = Exception.class)
   @RequestMapping(value = "{systemId}/groups/{groupName}/access/{entityType}/{entityId}", method = RequestMethod.DELETE)
   public AuthorizationResponse dropAccessFromGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName,
         @PathVariable("entityId") String entityId, @PathVariable("entityType") EntityType entityType, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         log.info("Dropping access to entity '{}/{}' from group '{}' in system '{}'.", entityType, entityId, groupName);
         String userId = getUserId(request);

         securityAdminService.dropAccessFromGroup(userId, systemId, groupName, entityId, entityType);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);

         // Raise event for data_authorization
         Map<String, Object> description = eventService.createEventDescription("Group Access has been dropped", userId, "false");
         description.put("group_Name", groupName);
         description.put("entity_id", entityId);
         description.put("entity_type", entityType);

         eventService.createEvent(this.getClass().toString(), new Date(), groupName, "authorization", "add_remove_data_authorization", null, null, description,
               properties.getEventQueue());

         // Create audit entry
         // For Group
         auditService.insertAuditEntry(new AuditEntryInput("Remove Access from entity STUDY/" + entityId, entityId + "_GROUP", EntityType.GROUP.getValue(), userId,
               ActionStatusType.SUCCESS, null));
         // For Access
         auditService.insertAuditEntry(
               new AuditEntryInput("Remove Access from entity STUDY/" + entityId, entityId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during removal of access from group.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/groups/{groupName}/access/{entityType}/{entityId}", method = RequestMethod.POST, consumes = "application/json")
   public AuthorizationResponse copyAccessToGroup(@PathVariable("systemId") String systemId, @PathVariable("groupName") String groupName,
         @PathVariable("entityId") String sourceEntityId, @PathVariable("entityType") EntityType entityType, @RequestParam("targetEntityId") String targetEntityId,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Copying DF security characteristics from DF '{}' to DF '{}'...", userId);

         securityAdminService.copyAccessToGroup(userId, systemId, entityType, groupName, sourceEntityId, targetEntityId);

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during authorization check.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/dataframe/copy-access", method = RequestMethod.POST, consumes = "application/json")
   public AuthorizationResponse copyAccessForDataframe(@PathVariable("systemId") String systemId, @RequestParam("targetEntityId") String targetEntityId,
         @RequestBody Dataframe dataframe, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Copying DF security characteristics from DF '{}' to DF '{}'...", dataframe.getId(), targetEntityId);

         Set<Group> groups = userLookupService.getGroupsByAccess(EntityType.DATAFRAME, dataframe.getId());
         for (Group group : groups) {
            securityAdminService.copyAccessToGroup(userId, systemId, EntityType.DATAFRAME, group.getGroupName(), dataframe.getId(), targetEntityId);
         }

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during copy access request.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/dataframe/copy-access-multiple", method = RequestMethod.POST, consumes = "application/json")
   public AuthorizationResponse copyAccessForDataframes(@PathVariable("systemId") String systemId, @RequestBody DataframeCopyAccessInput[] copyAccessInputList,
         HttpServletRequest request) {
      Monitor monitor = null;
      String transactionId = "";
      try {
         String userId = getUserId(request);
         for (DataframeCopyAccessInput copyAccessInput : copyAccessInputList) {
            String sourceId = copyAccessInput.getParent();
            String targetId = copyAccessInput.getTarget();
            transactionId = copyAccessInput.getTransactionId();
            if (sourceId == null || targetId == null || transactionId == null) {
               throw new InvalidRequestException("Copy group access request contains null values.");
            }

            log.info("Copying DF security characteristics from DF '{}' to DF '{}'...", sourceId, targetId);

            Set<Group> groups = userLookupService.getGroupsByAccess(EntityType.DATAFRAME, sourceId);
            for (Group group : groups) {
               securityAdminService.copyAccessToGroup(userId, systemId, EntityType.DATAFRAME, group.getGroupName(), sourceId, targetId);
            }
         }

         AuthorizationResponse response = new AuthorizationResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         String message = String.format("Exception occured during group copy access request for transaction ID %s.", transactionId);
         log.error(message, e);
         throw new FailedGroupCopyAccessException(message, e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
}
