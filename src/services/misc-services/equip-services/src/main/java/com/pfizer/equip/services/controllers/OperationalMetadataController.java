package com.pfizer.equip.services.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.services.business.authorization.AuthorizationService;
import com.pfizer.equip.services.business.authorization.Facts;
import com.pfizer.equip.services.business.opmeta.OperationalMetadataService;
import com.pfizer.equip.services.business.opmeta.exceptions.MissingRequiredFieldException;
import com.pfizer.equip.services.business.opmeta.exceptions.SourceNotFoundException;
import com.pfizer.equip.services.controllers.shared.SharedUtilties;
import com.pfizer.equip.services.exceptions.InvalidUserException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.opmeta.MasterProtocolInput;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.responses.opmeta.AttachmentVersionHistoryResponse;
import com.pfizer.equip.services.responses.opmeta.OperationalMetadataResponse;
import com.pfizer.equip.services.responses.opmeta.OperationalMetadataStudyIdResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.exceptions.EquipException;
import com.pfizer.equip.shared.opmeta.OperationalMetadataEventService;
import com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.opmeta.entity.Attachment;
import com.pfizer.equip.shared.opmeta.entity.KeyValuePair;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.UserProtocol;
import com.pfizer.equip.shared.opmeta.folder.AttachmentFolder;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.user.Permissions;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.ProtocolPropertyPrivileges;
import com.pfizer.equip.shared.service.user.UserInfo;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;
import com.pfizer.equip.shared.utils.BeanUtils;

@RestController
public class OperationalMetadataController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private OperationalMetadataService operationalMetadataService;

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private OperationalMetadataEventService operationalMetadataEventService;

   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;

   @Autowired
   private AuthorizationService authorizationService;

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs", method = RequestMethod.GET)
   public OperationalMetadataResponse getPrograms(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving programs for user {}", userId);

         // make call to service
         Set<Program> programs = operationalMetadataService.getPrograms();

         OperationalMetadataResponse response = new OperationalMetadataResponse(programs);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs", method = RequestMethod.POST)
   public OperationalMetadataResponse addProgram(@PathVariable String systemId, @RequestBody Program program, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Adding program for user {}", userId);

         // Check for user privilege
         if (program.getSource() != null) {
            if (program.getSource().equals(SourceType.PODS) && !userLookupService.hasPrivilege(userId, systemId, PrivilegeType.QUERY_PODS)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to add program in PODS source.", userId));
            } else if (program.getSource().equals(SourceType.EQUIP) && !userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROGRAM)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to add program in EQUIP source.", userId));
            }
         } else {
            throw new SourceNotFoundException("Field 'source' cannot be null; must specify data source of this node.");
         }
         
         if (StringUtils.isEmpty(program.getProgramCode())) {
            throw new MissingRequiredFieldException("Field 'programCode' cannot be null/blank.");
         }
         // make call to service
         String nodeId = operationalMetadataService.addProgram(program);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(nodeId);
         response.setNodePath(program.getPath());
         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput("Program added", program.getProgramCode(), EntityType.PROGRAM.getValue(), userId, ActionStatusType.SUCCESS, program.getCurrentSnapshot()));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creating of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols", method = RequestMethod.POST)
   public OperationalMetadataResponse addProtocolByProgram(@PathVariable String systemId, @PathVariable String programId, @RequestBody Protocol protocol,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Adding program for user {}", userId);

         protocol.setProgramCode(programId);

         // Check for user privilege
         if (protocol.getSource() != null) {
            if (protocol.getSource().equals(SourceType.PODS) && !userLookupService.hasPrivilege(userId, systemId, PrivilegeType.QUERY_PODS)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to add protocol in PODS source.", userId));
            } else if (protocol.getSource().equals(SourceType.EQUIP) && !userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL)) {
               throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to add protocol in EQUIP source.", userId));
            }
         } else {
            throw new SourceNotFoundException("Field 'source' cannot be null; must specify data source of this node.");
         }

         if (StringUtils.isEmpty(protocol.getStudyId())) {
            throw new MissingRequiredFieldException("Field 'studyId' cannot be null/blank.");
         }

         if (protocol.getModifiedBy() == null) {
            protocol.setModifiedBy(userId);
         }

         String nodeId = operationalMetadataService.addProtocol(protocol);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(nodeId);
         response.setNodePath(protocol.getPath());
         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput("Protocol added", protocol.getStudyId(), EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, protocol.getCurrentSnapshot()));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creating of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}", method = RequestMethod.PUT)
   public OperationalMetadataResponse updateProgram(@PathVariable String systemId, @PathVariable String programId, @RequestBody Map<String, Object> inputs,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Adding program for user {}", userId);

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROGRAM)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform update program '%s'.", userId, programId));
         }
         // Because we support partial updates, missing keys are considered to be "no change".
         // Need a list of properties which were sent as `"key": null` in the JSON. These would normally be equated to "missing key" with the Protocol class.
         // But sine we're using a map as our input, the keys will in fact be missing and so we have a way of distinguish the two scenarios.
         String[] deletedProperties = BeanUtils.getNullMapKeys(inputs, "opmeta:", "");
         Program program = objectMapper.convertValue(inputs, Program.class);
         program.setProgramCode(programId);
         String nodeId = operationalMetadataService.updateProgram(program, deletedProperties);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(nodeId);
         response.setNodePath(program.getPath());
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Updated Program", programId, EntityType.PROGRAM.getValue(), userId, ActionStatusType.SUCCESS, program.getCurrentSnapshot()));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creating of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}", method = RequestMethod.PUT)
   public OperationalMetadataResponse updateProtocolByProgram(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Updating protocol {} for user {}", protocolId, userId);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL)) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to perform update protocol '%s' under the program '%s'.", userId, protocolId, programId));
         }

         validateProtocolPermissions(systemId, userId, inputs.keySet());
         Protocol protocol = objectMapper.convertValue(inputs, Protocol.class);
         BeanUtils.replaceMapKeys(inputs, "opmeta:", "");
         BeanUtils.replaceMapKeys(inputs, "equip:", "");

         // Because we support partial updates, missing keys are considered to be "no change".
         // Need a list of properties which were sent as `"key": null` in the JSON. These would normally be equated to "missing key" with the Protocol class.
         // But sine we're using a map as our input, the keys will in fact be missing and so we have a way of distinguish the two scenarios.
         String[] deletedProperties = BeanUtils.getNullMapKeys(inputs);
         // We also need to get the named properties to find custom attributes
         String[] namedProperties = BeanUtils.getPropertyNames(new Protocol());
         // We will compare the properties we know about (named) to the input properties
         String[] inputProperties = BeanUtils.getMapKeys(inputs);
         // Remove any elements that are not named, leaving just the custom ones.
         String[] customAttributeKeys = ArrayUtils.removeElements(inputProperties, namedProperties);

         // Now iterate through the keys and grab the attributes, make KVPs
         Set<KeyValuePair> customAttributes = new HashSet<KeyValuePair>();
         for (String attribute : customAttributeKeys) {
            KeyValuePair keyValuePair = new KeyValuePair(attribute, inputs.get(attribute));
            customAttributes.add(keyValuePair);
         }

         if (!customAttributes.isEmpty()) {
            protocol.setCustomAttributes(customAttributes);
         }
         protocol.setProgramCode(programId);
         protocol.setStudyId(protocolId);
         if (protocol.getModifiedBy() == null) {
            protocol.setModifiedBy(userId);
         }

         String nodeId = operationalMetadataService.updateProtocol(protocol, deletedProperties);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(nodeId);
         response.setNodePath(protocol.getPath());
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Updated Protocol", protocolId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, protocol.getCurrentSnapshot()));
         // Milestone notifications:
         operationalMetadataEventService.processMilestones(protocol.getPath());

         return response;
      } catch (Exception e) {
         log.error("Exception occured during creating of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/master", method = RequestMethod.PUT)
   public OperationalMetadataResponse updateMasterProtocolByProgram(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody MasterProtocolInput protocolInput, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Updating master protocol {} for user {}, setting modified: {}", protocolId, userId, protocolInput.getModified());

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL)) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to perform update protocol '%s' under the program '%s'.", userId, protocolId, programId));
         }

         UserProtocol protocol = new UserProtocol(programId, protocolId);
         if (protocolInput.getModifiedBy() == null) {
            protocol.setModifiedBy(userId);
         } else {
            protocol.setModifiedBy(protocolInput.getModifiedBy());
         }

         String nodeId = operationalMetadataService.updateMasterProtocol(protocol);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(nodeId);
         response.setNodePath(protocol.getPath());
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput(String.format("Master protocol updated, modified by %s", protocol.getModifiedBy()), protocol.getStudyId(),
               EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         // Milestone notifications:
         operationalMetadataEventService.processMilestones(protocol.getPath());
         return response;
      } catch (Exception e) {
         log.error("Exception occured during update of operational metadata master node.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols", method = RequestMethod.GET)
   public OperationalMetadataResponse getProtocolsByProgram(@PathVariable String programId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving protocols for program {} for user {}", programId, userId);

         // make call to service
         Set<Program> programsAndProtocols = operationalMetadataService.getProtocolsByProgram(programId);

         OperationalMetadataResponse response = new OperationalMetadataResponse(programsAndProtocols);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}", method = RequestMethod.GET)
   public OperationalMetadataResponse getProtocol(@PathVariable String programId, @PathVariable String protocolId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving protocol {} for user {}", protocolId, userId);

         // make call to service
         Program program = operationalMetadataService.getProgramAndProtocol(programId, protocolId);

         Set<Program> programs = new HashSet<Program>();
         programs.add(program);
         OperationalMetadataResponse response = new OperationalMetadataResponse(programs);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-cag-users", method = RequestMethod.GET)
   public OperationalMetadataResponse getCagUsersByProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId) {
      Monitor monitor = null;
      try {
         log.info("Getting CAG users from protocol {} in program {}", protocolId, programId);

         Set<UserInfo> users = operationalMetadataService.getCagUsersByProtocol(systemId, programId, protocolId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setUsers(users);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-cag-users", method = RequestMethod.PUT)
   public OperationalMetadataResponse addCagUsersToProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody Set<String> users, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Adding users {} to protocol {} in program {}", users, protocolId, programId);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ASSIGNMENT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol assignment.", userId));
         }

         validateUsers(systemId, users);
         operationalMetadataService.addCagUsersToProtocol(programId, protocolId, users, userId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Cag Users added to Protocol", protocolId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-cag-users", method = RequestMethod.DELETE)
   public OperationalMetadataResponse removeCagUsersFromProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody Set<String> users, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Removing users {} from protocol {} in program {}", users, protocolId, programId);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ASSIGNMENT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol assignment.", userId));
         }

         operationalMetadataService.removeCagUsersFromProtocol(programId, protocolId, users, userId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Cag Users removed from Protocol", protocolId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-pka-users", method = RequestMethod.GET)
   public OperationalMetadataResponse getPkaUsersByProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId) {
      Monitor monitor = null;
      try {
         log.info("Getting pka users from protocol {} in program {}", protocolId, programId);

         Set<UserInfo> users = operationalMetadataService.getPkaUsersByProtocol(systemId, programId, protocolId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setUsers(users);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-pka-users", method = RequestMethod.PUT)
   public OperationalMetadataResponse addPkaUsersToProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody Set<String> users, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Adding users {} to protocol {} in program {}", users, protocolId, programId);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ASSIGNMENT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol assignment.", userId));
         }

         validateUsers(systemId, users);
         operationalMetadataService.addPkaUsersToProtocol(programId, protocolId, users, userId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("PKA Users added to Protocol", protocolId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/assigned-pka-users", method = RequestMethod.DELETE)
   public OperationalMetadataResponse removePkaUsersFromProtocol(@PathVariable String systemId, @PathVariable String programId, @PathVariable String protocolId,
         @RequestBody Set<String> users, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Removing users {} from protocol {} in program {}", users, protocolId, programId);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ASSIGNMENT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol assignment.", userId));
         }

         operationalMetadataService.removePkaUsersFromProtocol(programId, protocolId, users, userId);

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("PKA Users removed from Protocol", protocolId, EntityType.PROTOCOL.getValue(), userId, ActionStatusType.SUCCESS, null));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
   
   private boolean checkAttachmentAccess(String systemId, String userId, String programId, String protocolId) throws Exception  {
      Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      Permissions permissions = userLookupService.getUserPermissions(systemId, userId);
      Dataframe dataframe = new Dataframe();
      dataframe.setPromotionStatus("Promoted");
      dataframe.setDataBlindingStatus(StudyBlindingStatus.UNBLINDED.getValue());
      dataframe.setRestrictionStatus(protocol.getStudyRestrictionStatus());
      Facts facts = authorizationService.getFacts(dataframe, protocol);
      boolean hasAccess = permissions.isExternalUser() ? authorizationService.canViewDataframeExternal(permissions, facts)
            : authorizationService.canViewDataframe(permissions, facts);
      
      return hasAccess;
   }

   @RequestMapping(method = RequestMethod.POST, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments" })
   public OperationalMetadataResponse addAttachment(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @RequestParam MultiValueMap<String, Object> inputs,
         @RequestParam("fileContent") MultipartFile fileContent, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String attachmentPath = null;
         String parentPath = null;
         String userId = getUserId(request);
         
         // first perform group based authorization check
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to modify protocol attachments, does not have unblinded promoted access to this protocol.", userId));
         }

         // now check ALTER_PROTOCOL_ATTACHMENTS permission
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ATTACHMENTS)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol attachments, failed privilege check.", userId));
         }

         Attachment attachment = new Attachment(inputs, fileContent.getBytes());
         if (protocolId.isPresent()) {
            parentPath = new Protocol(programId, protocolId.get()).getPath();
            attachment.addStudyId(protocolId.get()); // required for equip:searchable
         } else {
            parentPath = new Program(programId).getPath();
         }
         attachmentPath = String.format("%s/%s/%s", parentPath, AttachmentFolder.NAME, fileContent.getOriginalFilename());

         String attachmentId = operationalMetadataService.addAttachment(attachment, attachmentPath, userId);
         // Update modified date of protocol
         if (protocolId.isPresent()) {
            UserProtocol protocol = new UserProtocol(programId, protocolId.get());
            protocol.setModifiedBy(userId);
            operationalMetadataService.updateMasterProtocol(protocol);
         }

         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput(String.format("Attachment added at %s", attachmentPath), attachmentId, EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(attachmentId);
         response.setNodePath(attachmentPath);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.POST, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}" })
   public OperationalMetadataResponse updateAttachment(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, @RequestParam MultiValueMap<String, Object> inputs,
         @RequestParam("fileContent") Optional<MultipartFile> fileContent, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // first perform group based authorization check
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to modify protocol attachments, does not have unblinded promoted access to this protocol.", userId));
         }

         // now check ALTER_PROTOCOL_ATTACHMENTS permission
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ATTACHMENTS)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol attachments, failed privilege check.", userId));
         }

         byte[] bytes = fileContent.isPresent() ? fileContent.get().getBytes() : null;
         Attachment attachment = new Attachment(inputs, bytes);

         String attachmentPath = operationalMetadataService.updateAttachment(attachment, attachmentId, userId);
         // Update modified date of protocol
         if (protocolId.isPresent()) {
            UserProtocol protocol = new UserProtocol(programId, protocolId.get());
            protocol.setModifiedBy(userId);
            operationalMetadataService.updateMasterProtocol(protocol);
         }

         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput(String.format("Attachment at %s updated", attachmentPath), attachmentId, EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));

         // TODO: notification

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(attachmentId);
         response.setNodePath(attachmentPath);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.DELETE, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}" })
   public OperationalMetadataResponse deleteAttachment(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, @RequestParam MultiValueMap<String, Object> inputs,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         // first perform group based authorization check
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to modify protocol attachments, does not have unblinded promoted access to this protocol.", userId));
         }

         // now check ALTER_PROTOCOL_ATTACHMENTS permission
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ATTACHMENTS)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol attachments, failed privilege check.", userId));
         }

         String attachmentPath = operationalMetadataService.softDeleteAttachment(attachmentId);
         // Update modified date of protocol
         if (protocolId.isPresent()) {
            UserProtocol protocol = new UserProtocol(programId, protocolId.get());
            protocol.setModifiedBy(userId);
            operationalMetadataService.updateMasterProtocol(protocol);
         }

         auditService.insertAuditEntry(
               new AuditEntryInput(String.format("Attachment at %s deleted", attachmentPath), attachmentId, EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));

         // TODO: notification

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(attachmentId);
         response.setNodePath(attachmentPath);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.PUT, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}" })
   public OperationalMetadataResponse restoreAttachmet(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.RESTORE_ANY_ENTITY)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to restore entities.", userId));
         }

         // first perform group based authorization check
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to modify protocol attachments, does not have unblinded promoted access to this protocol.", userId));
         }

         // now check ALTER_PROTOCOL_ATTACHMENTS permission
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL_ATTACHMENTS)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to modify protocol attachments, failed privilege check.", userId));
         }

         String attachmentPath = operationalMetadataService.restoreAttachment(attachmentId);

         // Create audit entry
         auditService.insertAuditEntry(
               new AuditEntryInput(String.format("Attachment at %s restored", attachmentPath), attachmentId, EntityType.ARTIFACT.getValue(), userId, ActionStatusType.SUCCESS, null));

         // Update modified date of protocol
         if (protocolId.isPresent()) {
            UserProtocol protocol = new UserProtocol(programId, protocolId.get());
            protocol.setModifiedBy(userId);
            operationalMetadataService.updateMasterProtocol(protocol);
         }

         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         response.setNodeId(attachmentId);
         response.setNodePath(attachmentPath);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during restoration of attachment.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}" })
   public OperationalMetadataResponse getAttachment(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         Attachment attachment = operationalMetadataService.getAttachment(attachmentId);

         OperationalMetadataResponse response = new OperationalMetadataResponse(attachment);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments" })
   public OperationalMetadataResponse getAttachments(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String path;
         if (protocolId.isPresent()) {
            path = new Protocol(programId, protocolId.get()).getPath();
         } else {
            path = new Program(programId).getPath();
         }

         List<Attachment> attachments = operationalMetadataService.getAttachments(path);

         OperationalMetadataResponse response = new OperationalMetadataResponse(attachments);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}/versions/{versionNumber:[\\d]+}",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}/versions/{versionNumber:[\\d]+}" })
   public OperationalMetadataResponse getAttachmentVersion(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, @PathVariable long versionNumber,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         Attachment attachment = operationalMetadataService.getAttachmentVersion(attachmentId, versionNumber);

         OperationalMetadataResponse response = new OperationalMetadataResponse(attachment);
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}/content",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}/content" })
   public ResponseEntity<byte[]> getAttachmentContent(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to view protocol attachment content, does not have unblinded promoted access to this protocol.", userId));
         }

         ContentInfo contentInfo = operationalMetadataService.getAttachmentContent(attachmentId);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = {
         "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}/versions/{versionNumber:[\\d]+}/content",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}/versions/{versionNumber:[\\d]+}/content" })
   public ResponseEntity<byte[]> getAttachmentContentVersion(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, @PathVariable long versionNumber,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to view protocol attachment content, does not have unblinded promoted access to this protocol.", userId));
         }

         ContentInfo contentInfo = operationalMetadataService.getAttachmentContentVersion(attachmentId, versionNumber);
         return SharedUtilties.createBinaryResponse(contentInfo);
      } catch (Exception e) {
         log.error("Exception occured during creation of artifact.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(method = RequestMethod.GET, value = { "{systemId}/opmeta/nodes/programs/{programId}/attachments/{attachmentId}/versions",
         "{systemId}/opmeta/nodes/programs/{programId}/protocols/{protocolId}/attachments/{attachmentId}/versions" })
   public AttachmentVersionHistoryResponse getAttachmentVersionHistory(@PathVariable("systemId") String systemId, @PathVariable("programId") String programId,
         @PathVariable("protocolId") Optional<String> protocolId, @PathVariable("attachmentId") String attachmentId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         if (!checkAttachmentAccess(systemId, userId, programId, protocolId.get())) {
            throw new NotAuthorizedException(
                  String.format("Request user '%s' is not authorized to view protocol attachment content history, does not have unblinded promoted access to this protocol.", userId));
         }

         AttachmentVersionHistoryResponse response = operationalMetadataService.getAttachmentVersionHistory(attachmentId);

         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of version history.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private void validateUsers(String systemId, Set<String> userIds) throws JsonProcessingException, IOException, ExecutionException {
      for (String userId : userIds) {
         if (!userLookupService.isValidUser(systemId, userId)) {
            throw new InvalidUserException(String.format("Invalid user %s detected during operational metadata operation.", userId));
         }
      }
   }

   private void validateProtocolPermissions(String systemId, String requestUserId, Set<String> modifiedProperties)
         throws JsonProcessingException, IOException, ExecutionException {
      // Move to userLookupService?
      Map<String, PrivilegeType> attributePrivileges = new ProtocolPropertyPrivileges();
      Set<PrivilegeType> requiredPrivileges = new HashSet<PrivilegeType>();
      Set<PrivilegeType> userPrivileges = userLookupService.getUserFunctions(systemId, requestUserId);
      for (String property : modifiedProperties) {
         property = property.replace("opmeta:", ""); // TODO: integrate this better
         if (attributePrivileges.get(property) != null) {
            requiredPrivileges.add(attributePrivileges.get(property));
         }
      }
      for (PrivilegeType privilege : requiredPrivileges) {
         if (!userPrivileges.contains(privilege)) {
            throw new NotAuthorizedException(
                  String.format("User '%s' is missing privilege '%s' required to perform this operation. All required privileges for this operation are: %s",
                        requestUserId, privilege, requiredPrivileges));
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/cache/clear", method = RequestMethod.POST)
   public OperationalMetadataResponse clearCache(@PathVariable String systemId, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         if (!userLookupService.hasPrivilege(userId, systemId, PrivilegeType.ALTER_PROTOCOL)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to clear opmeta cache.", userId));
         }
         operationalMetadataService.clearCache();
         OperationalMetadataResponse response = new OperationalMetadataResponse();
         response.setResponse(Response.OK);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creating of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/opmeta/aliases/{aliasValue}", method = RequestMethod.GET)
   public OperationalMetadataStudyIdResponse getStudyIdByAlias(@PathVariable String aliasValue, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         log.info("Retrieving studyId for alias value {} for user {}", aliasValue, userId);
         

         // make call to service
         String studyId = operationalMetadataService.getStudyIdByAlias(aliasValue);

         OperationalMetadataStudyIdResponse response = new OperationalMetadataStudyIdResponse(studyId);
         response.setResponse(Response.OK);
         return response;
      } catch (EquipException ee) {
         log.error("Exception occured during retrieval of study ID by alias.", ee);
         throw ee;
      } catch (Exception e) {
         log.error("Exception occured during retrieval of operational metadata nodes.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
}
