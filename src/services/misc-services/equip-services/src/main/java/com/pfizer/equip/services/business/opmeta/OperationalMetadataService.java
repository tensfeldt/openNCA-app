package com.pfizer.equip.services.business.opmeta;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.business.modeshape.exceptions.TransactionException;
import com.pfizer.equip.services.business.modeshape.nodes.VersionHistoryNode;
import com.pfizer.equip.services.business.opmeta.exceptions.CacheException;
import com.pfizer.equip.services.business.opmeta.exceptions.DuplicateProtocolAliasException;
import com.pfizer.equip.services.business.opmeta.exceptions.InvalidAttachmentException;
import com.pfizer.equip.services.business.opmeta.exceptions.NoSuchProtocolAliasException;
import com.pfizer.equip.services.business.opmeta.exceptions.SourceEntityNotFoundException;
import com.pfizer.equip.services.business.opmeta.exceptions.SourceNotFoundException;
import com.pfizer.equip.services.business.opmeta.exceptions.SourceTypeMismatchException;
import com.pfizer.equip.services.responses.opmeta.AttachmentVersionHistoryResponse;
import com.pfizer.equip.services.responses.opmeta.AttachmentVersionHistoryResponseItem;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeAlreadyExistsException;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeNotFoundException;
import com.pfizer.equip.shared.opmeta.OperationalMetadataProcessorService;
import com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatus;
import com.pfizer.equip.shared.opmeta.StudyRestrictionStatus;
import com.pfizer.equip.shared.opmeta.entity.Attachment;
import com.pfizer.equip.shared.opmeta.entity.BaseProtocolChildNode;
import com.pfizer.equip.shared.opmeta.entity.ClinicalStudyReport;
import com.pfizer.equip.shared.opmeta.entity.Comment;
import com.pfizer.equip.shared.opmeta.entity.DrugRegimen;
import com.pfizer.equip.shared.opmeta.entity.Indication;
import com.pfizer.equip.shared.opmeta.entity.KeyValuePair;
import com.pfizer.equip.shared.opmeta.entity.Milestone;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.ProtocolAlias;
import com.pfizer.equip.shared.opmeta.entity.UserProtocol;
import com.pfizer.equip.shared.opmeta.folder.AttachmentFolder;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.ProtocolPropertyPrivileges;
import com.pfizer.equip.shared.service.user.UserInfo;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;
import com.pfizer.equip.shared.utils.BeanUtils;

@Service
// Provides data manipulation services for controller
public class OperationalMetadataService {
   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;

   @Autowired
   private OperationalMetadataProcessorService operationalMetadataProcessorService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private OperationalMetadataCache operationalMetadataCache;

   @Autowired
   private UserLookupService userLookupService;

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   static final String PROGRAM_ROOT = "/" + ProgramFolder.NAME;

   @Autowired
   ObjectMapper mapper;

   @PostConstruct
   public void initialize() {
      operationalMetadataCache.load();
   }

   // Retrieve programs, include master node user-editable attributes
   public Set<Program> getPrograms() {
      Set<Program> programs = new LinkedHashSet<>();
      try {
         // getPrograms going to return the same record irrespective of the userId, so cache is being maintained in a common key "programs".
         programs.addAll(operationalMetadataCache.query("programs").values());
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while caching the OperationalMetadataService getPrograms. ", e);
      }
      return programs;
   }

   public String addProgram(Program program) {
      String nodeId = null;
      repositoryService.verifyNodeNonExistent(program.getPath());
      if (program.getSource().equals(SourceType.EQUIP)) {
         program.setSourceCreationTimestamp(new Date());
         nodeId = operationalMetadataRepositoryService.addEntity(program);
      } else if (program.getSource().equals(SourceType.PODS)) {
         operationalMetadataProcessorService.loadPrograms(Arrays.asList(program.getProgramCode()));
         try {
            nodeId = repositoryService.getIdByPath(program.getPath());
         } catch (NodeNotFoundException e) {
            throw new SourceEntityNotFoundException(String.format("Program '%s' not found in source repository.", program.getProgramCode()));
         }
         // TODO: throw exception if not in PODS
      }
      // Updating the cache
      try {
         operationalMetadataRepositoryService.getProgram(program.getProgramCode());
         operationalMetadataCache.updateCacheForProgram(program, "programs");
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while caching the OperationalMetadataService addPrograms. ", e);
      }
      return nodeId;
   }

   public String addProtocol(Protocol protocol) {
      // TODO: blinding?
      String nodeId;
      repositoryService.verifyNodeNonExistent(protocol.getPath());

      if (protocol.getSource().equals(SourceType.EQUIP)) {
         Date date = new Date();
         protocol.setModified(date);
         protocol.setSetupDate(date);
         protocol.setSourceCreationTimestamp(date);
         protocol.setSetupBy(protocol.getModifiedBy());
         nodeId = operationalMetadataRepositoryService.addEntity(protocol);
      } else if (protocol.getSource().equals(SourceType.PODS)) {
         operationalMetadataProcessorService.loadProtocols(Arrays.asList(protocol.getStudyId()), protocol.getModifiedBy());
         try {
            nodeId = repositoryService.getIdByPath(protocol.getPath());
         } catch (NodeNotFoundException e) {
            throw new SourceEntityNotFoundException(String.format("Protocol '%s' not found in source repository.", protocol.getStudyId()));
         }
      } else {
         throw new SourceNotFoundException(String.format("Could not find source of type '%s'", protocol.getSource()));
      }

      // Updating the cache
      try {
         Protocol updatedProtocol = operationalMetadataRepositoryService.getProtocol(protocol.getProgramCode(), protocol.getStudyId());
         operationalMetadataCache.updateCacheForProtocol(updatedProtocol, protocol.getProgramCode(), "programs");
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while caching the OperationalMetadataService addProtocol. ", e);
      }
      return nodeId;
   }

   // Only for EQuIP-managed entities:
   public String updateProgram(Program program, String[] deletedProperties) {
      program.setSourceCreationTimestamp(new Date());
      Program masterProgram = repositoryService.getNode(Program.class, program.getPath());
      // TODO: Add hook for sensitive fields like study unblinding.
      if (masterProgram.getSource() == null || !masterProgram.getSource().equals(SourceType.EQUIP)) {
         throw new SourceTypeMismatchException(String.format("Cannot manually update this entity (%s), it is not managed in this system.", masterProgram.getPath()));
      }
      Program programSnapshot = repositoryService.getNodeByUrl(Program.class, masterProgram.getCurrentSnapshot());
      BeanUtils.copyNonNullProperties(program, programSnapshot, deletedProperties); // support partial updates
      programSnapshot.setModified(new Date());
      String nodeId = operationalMetadataRepositoryService.updateEntity(programSnapshot);
      // Updating the cache
      try {
         Program updatedProgram = operationalMetadataRepositoryService.getProgram(program.getProgramCode());
         // Using Get Program to fetch the program again in order to fetch all the child and descendant nodes.
         operationalMetadataCache.updateCacheForProgram(updatedProgram, "programs");
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while caching the OperationalMetadataService addPrograms. ", e);
      }
      return nodeId;
   }

   public String updateProtocol(Protocol protocol) throws JsonProcessingException, IOException, ExecutionException {
      return updateProtocol(protocol, new String[0]);
   }

   /**
    * Study blinding status, study blinding status source, and study restriction status are special properties
    * we default them to specific values when they are not set in Modeshape (i.e. they are null)
    * to support proper creation of new protocols.
    * However, we need to check that we don't override these values if they have already been set
    * when updating any other operational metadata.
    */
   String[] getIgnoredProperties(Protocol protocol) {
      List<String> ignoredPropertiesList = Stream.of("children", "customAttributes").collect(Collectors.toList());
      if (!protocol.getIsStudyBlindingStatusSet()) {
         ignoredPropertiesList.add("studyBlindingStatus");
         ignoredPropertiesList.add("isStudyBlinded");
      }
      if (!protocol.getIsStudyBlindingStatusSourceSet()) {
         ignoredPropertiesList.add("studyBlindingStatusSource");
      }
      if (!protocol.getIsStudyRestrictionStatusSet()) {
         ignoredPropertiesList.add("studyRestrictionStatus");
         ignoredPropertiesList.add("isStudyRestricted");
      }

      return ignoredPropertiesList.toArray(new String[0]);
   }

   public String updateProtocol(Protocol protocol, String[] deletedProperties) throws JsonProcessingException, IOException, ExecutionException {
      protocol.setSourceCreationTimestamp(new Date());
      Protocol masterProtocol = repositoryService.getNode(Protocol.class, protocol.getPath());
      // TODO: blank out masternode-only attributes?
      String nodeId = repositoryService.getIdByPath(protocol.getPath());
      String[] ignoredProperties = getIgnoredProperties(protocol);
      if (masterProtocol.getSource() == null) {
         throw new SourceTypeMismatchException(String.format("Node does not have source attribute, invalid data, cannot update: %s", masterProtocol.getPath()));
      } else if (masterProtocol.getSource().equals(SourceType.EQUIP)) {
         protocol.setModified(new Date());
         Protocol currentSnapshot = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
         Protocol newSnapshot = new Protocol();
         handleChildUpdates(protocol, protocol.getModified(), protocol.getModifiedBy());
         BeanUtils.copyNonNullProperties(currentSnapshot, newSnapshot, null, "children"); // copy existing data, don't mark deleted properties yet, they're probably not
                                                                                          // null anyway.
         BeanUtils.copyNonNullProperties(protocol, newSnapshot, deletedProperties, ignoredProperties); // support partial updates
         updateKeyValuePairs(protocol, newSnapshot); // partial updates for KVPs, will match on key and update value
         operationalMetadataRepositoryService.updateEntity(newSnapshot);
         auditProtocolBlinding(masterProtocol, currentSnapshot, newSnapshot);
      } else if (masterProtocol.getSource().equals(SourceType.PODS)) {
         Protocol currentSnapshot = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
         UserProtocol userProtocol = new UserProtocol();
         Protocol newSnapshot = new Protocol();
         BeanUtils.copyNonNullProperties(protocol, userProtocol, deletedProperties); // filter out non-user-editable attributes, do include the deletedProperties so as to
                                                                                     // blank them out.
         // TODO: ignorableProperties smells bad
         BeanUtils.copyNonNullProperties(currentSnapshot, newSnapshot, null, "children"); // copy existing data, don't mark deleted properties yet, they're probably not
                                                                                          // null anyway.
         BeanUtils.copyNonNullProperties(userProtocol, newSnapshot, deletedProperties, ignoredProperties); // overlay existing data in new snapshot with the
                                                                                                           // filtered attrs
         updateKeyValuePairs(userProtocol, newSnapshot);
         newSnapshot.setModified(new Date());
         if (currentSnapshot.getCandidateProjectUuid() != null) {
            newSnapshot.setCandidateProjectUuid(repositoryService.getIdByUrl(currentSnapshot.getCandidateProjectUuid()));
         }
         operationalMetadataRepositoryService.updateEntity(newSnapshot);
         auditProtocolBlinding(masterProtocol, currentSnapshot, newSnapshot);
      }
      // Updating the cache
      try {
         Protocol updatedProtocol = operationalMetadataRepositoryService.getProtocol(protocol.getProgramCode(), protocol.getStudyId());
         operationalMetadataCache.updateCacheForProtocol(updatedProtocol, protocol.getProgramCode(), "programs");
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while caching the OperationalMetadataService addProtocol. ", e);
      }
      return nodeId;
   }

   public String updateMasterProtocol(UserProtocol userProtocol) {
      Protocol masterProtocol = repositoryService.getNode(Protocol.class, userProtocol.getPath()); // get current snapshot and validate node exists
      Protocol currentSnapshot = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      Protocol protocol = new Protocol();
      BeanUtils.copyNonNullProperties(userProtocol, protocol, null, "children"); // copy into protocol for modified and active fields
      BeanUtils.copyNonNullProperties(userProtocol, currentSnapshot, null, "children"); // copy into protocol for modified and active fields
      protocol.setModified(new Date());
      protocol.setActiveFlag(operationalMetadataRepositoryService.isProtocolActive(currentSnapshot));
      String nodeId = repositoryService.updateNode(protocol, protocol.getPath());
      try {
         Protocol updatedProtocol = operationalMetadataRepositoryService.getProtocol(protocol.getProgramCode(), protocol.getStudyId());
         operationalMetadataCache.updateCacheForProtocol(updatedProtocol, protocol.getProgramCode(), "programs");
      } catch (ExecutionException e) {
         throw new CacheException("Execution Exception occured while updating protocol cache", e);
      }
      return nodeId;
   }

   public Set<Program> getProtocolsByProgram(String programId) throws IOException {
      String programsPath = PROGRAM_ROOT + "/" + programId + "/" + ProtocolFolder.NAME;
      String protocolsPath;
      Set<Program> programs = new HashSet<Program>();
      ProgramFolder programsFolder = repositoryService.getNode(ProgramFolder.class, programsPath, DepthType.WITH_DIRECT_CHILDREN);
      for (Program masterProgram : programsFolder.getChildren().values()) {
         protocolsPath = String.format("%s/%s/%s", PROGRAM_ROOT, masterProgram.getProgramCode(), ProtocolFolder.NAME);
         ProtocolFolder protocolFolder = repositoryService.getNode(ProtocolFolder.class, protocolsPath, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

         Program snapshotProgram = repositoryService.getNodeByUrl(Program.class, masterProgram.getCurrentSnapshot());
         snapshotProgram.setAssignedUsers(masterProgram.getAssignedUsers());

         for (Protocol masterProtocol : protocolFolder.getChildren().values()) {
            Protocol snapshotProtocol = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
            snapshotProtocol.setAssignedCagUsers(masterProtocol.getAssignedCagUsers());
            snapshotProtocol.setAssignedPkaUsers(masterProtocol.getAssignedPkaUsers());
            snapshotProtocol.setCandidateProjectUuid(null);
            snapshotProgram.addProtocol(snapshotProtocol);
         }
         programs.add(snapshotProgram);
      }
      return programs;
   }

   public Program getProgramAndProtocol(String programId, String protocolId) throws IOException {
      String programPath = String.format("%s/%s", PROGRAM_ROOT, programId);

      Program masterProgram = repositoryService.getNode(Program.class, programPath, DepthType.WITH_TOP_LEVEL);
      Program snapshotProgram = repositoryService.getNodeByUrl(Program.class, masterProgram.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

      Protocol snapshotProtocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);

      // Include empty lists for front end to display these records
      if (snapshotProtocol.getClinicalStudyReports() == null) {
         snapshotProtocol.setClinicalStudyReports(new LinkedHashSet<ClinicalStudyReport>());
      }
      if (snapshotProtocol.getMilestones() == null) {
         snapshotProtocol.setMilestones(new LinkedHashSet<Milestone>());
      }
      if (snapshotProtocol.getProtocolAliases() == null) {
         snapshotProtocol.setProtocolAliases(new LinkedHashSet<ProtocolAlias>());
      }
      if (snapshotProtocol.getIndications() == null) {
         snapshotProtocol.setIndications(new LinkedHashSet<Indication>());
      }
      if (snapshotProtocol.getDrugRegimens() == null) {
         snapshotProtocol.setDrugRegimens(new LinkedHashSet<DrugRegimen>());
      }

      snapshotProtocol.initializeGroupTemplates();

      snapshotProtocol.setCandidateProjectUuid(null);
      snapshotProgram.addProtocol(snapshotProtocol);
      return snapshotProgram;
   }

   public Set<UserInfo> getCagUsersByProtocol(String systemId, String programId, String protocolId) throws JsonProcessingException, IOException, ExecutionException {
      Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      return userLookupService.lookupUsersBySystem(systemId, protocol.getAssignedCagUsersList());
   }

   public void addCagUsersToProtocol(String programId, String protocolId, Collection<String> users, String requestUserId) throws JsonProcessingException, IOException, ExecutionException {
      Protocol protocol = new Protocol(programId, protocolId);
      Protocol snapshotProtocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      protocol.addAssignedCagUsers(snapshotProtocol.getAssignedCagUsersList());
      protocol.addAssignedCagUsers(users);
      protocol.setModifiedBy(requestUserId);
      updateProtocol(protocol);
   }

   public void removeCagUsersFromProtocol(String programId, String protocolId, Collection<String> users, String requestUserId)
         throws JsonProcessingException, IOException, ExecutionException {
      Protocol snapshotProtocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      Protocol protocol = new Protocol(programId, protocolId);
      // TODO: Need this because JsonInclude.ALWAYS will blank it out, will do better solution using JsonViews:
      protocol.addAssignedCagUsers(snapshotProtocol.getAssignedCagUsersList());
      protocol.removeAssignedCagUsers(users);
      protocol.setModifiedBy(requestUserId);
      updateProtocol(protocol);
   }

   public Set<UserInfo> getPkaUsersByProtocol(String systemId, String programId, String protocolId) throws JsonProcessingException, IOException, ExecutionException {
      Protocol protocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      return userLookupService.lookupUsersBySystem(systemId, protocol.getAssignedPkaUsersList());
   }

   public void addPkaUsersToProtocol(String programId, String protocolId, Collection<String> users, String requestUserId) throws JsonProcessingException, IOException, ExecutionException {
      Protocol protocol = new Protocol(programId, protocolId);
      Protocol snapshotProtocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      protocol.addAssignedPkaUsers(snapshotProtocol.getAssignedPkaUsersList());
      protocol.addAssignedPkaUsers(users);
      protocol.setModifiedBy(requestUserId);
      updateProtocol(protocol);
   }

   public void removePkaUsersFromProtocol(String programId, String protocolId, Collection<String> users, String requestUserId)
         throws JsonProcessingException, IOException, ExecutionException {
      Protocol protocol = new Protocol(programId, protocolId);
      Protocol snapshotProtocol = operationalMetadataRepositoryService.getProtocol(programId, protocolId);
      protocol.addAssignedPkaUsers(snapshotProtocol.getAssignedPkaUsersList());
      protocol.removeAssignedPkaUsers(users);
      protocol.setModifiedBy(requestUserId);
      updateProtocol(protocol);
   }

   public String addAttachment(Attachment attachment, String attachmentPath, String userId) throws Exception {
      String firstNodeId = null;
      String nodeId = null;
      try {
         repositoryService.verifyNodeNonExistent(attachmentPath);
         attachment.setDeleted(false);
         attachment.setEquipCreatedBy(userId);
         attachment.setEquipCreated(toISO8601UTC(new Date()));
         attachment.setEquipModified(attachment.getEquipCreated());
         attachment.setEquipModifiedBy(attachment.getEquipCreatedBy());
         if (attachment.getChild(Comment.PRIMARY_TYPE) == null) {
            attachment.setComment("");
         }
         firstNodeId = repositoryService.addNode(attachment, attachmentPath);
         // Now update the same exact path with the same exact JSON using an HTTP PUT, similar to Librarian.
         // This is due to a quirk in ModeShape where the 1st version of a node does not store metadata or content as part of its history.
         nodeId = repositoryService.updateNode(attachment, attachmentPath);
         return nodeId;
      } catch (Exception e) {
         if (e instanceof NodeAlreadyExistsException) {
            throw e;
         }
         log.error("Exception occurred adding new artifact.", e);
         if (firstNodeId != null) {
            try {
               // Need to delete the dangling node, since this means the content creation failed
               repositoryService.deleteNode(attachmentPath);
            } catch (Exception e2) {
               log.error("Deletion of dangling first node {} failed.", firstNodeId, e2);
            }
         }
         throw new TransactionException("An exception occurred during the operations required to create a new attachment.", e);
      }
   }

   public String updateAttachment(Attachment attachment, String attachmentId, String userId) throws Exception {
      try {
         attachment.setEquipModified(toISO8601UTC(new Date()));
         attachment.setEquipModifiedBy(userId);
         String nodePath = repositoryService.getPathById(attachmentId); // TODO: probably consolidate these calls
         Attachment existingAttachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
         if (existingAttachment.getDeleted() == true) {
            throw new InvalidAttachmentException(String.format("Attachment with id %s is marked as deleted.", attachmentId));
         }

         // Need to set these to an empty object or else they get deleted.
         if (attachment.getChild(Attachment.COMPLEX_DATA) == null) {
            attachment.putChild(Attachment.COMPLEX_DATA, new Object());
         }
         if (attachment.getChild(Comment.PRIMARY_TYPE) == null) {
            attachment.putChild(Comment.PRIMARY_TYPE, new Object());
         }

         repositoryService.updateNode(attachment, nodePath);
         return nodePath;
      } catch (Exception e) {
         log.error("Exception occurred updating existing attachment.", e);
         if (e instanceof NodeNotFoundException || e instanceof InvalidAttachmentException) {
            throw e; // rethrow if not found, otherwise throw more generic TransactionException.
         } else {
            throw new TransactionException("An exception occurred during the operations required to create a new artifact.", e);
         }
      }
   }

   public String softDeleteAttachment(String attachmentId) throws Exception {
      try {
         Attachment attachment = new Attachment();
         attachment.setDeleted(true);
         String nodePath = repositoryService.getPathById(attachmentId); // TODO: do we really need to return the path?
         repositoryService.updateNode(attachment, nodePath);
         return nodePath;
      } catch (Exception e) {
         log.error("Exception occurred soft deleting attachment.", e);
         if (e instanceof NodeNotFoundException) {
            throw e; // rethrow if not found, otherwise throw more generic TransactionException.
         } else {
            throw new TransactionException("An exception occurred during the operations required to create a new artifact.", e);
         }
      }
   }

   public String restoreAttachment(String attachmentId) throws Exception {
      try {
         Attachment attachment = new Attachment();
         attachment.setDeleted(false);
         String nodePath = repositoryService.getPathById(attachmentId); // TODO: do we really need to return the path?
         repositoryService.updateNode(attachment, nodePath);
         return nodePath;
      } catch (Exception e) {
         log.error("Exception occurred soft deleting attachment.", e);
         if (e instanceof NodeNotFoundException) {
            throw e; // rethrow if not found, otherwise throw more generic TransactionException.
         } else {
            throw new TransactionException("An exception occurred during the operations required to create a new artifact.", e);
         }
      }
   }

   public Attachment getAttachment(String attachmentId) throws Exception {
      Attachment attachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      if (attachment.getDeleted() == true) {
         throw new InvalidAttachmentException(String.format("Attachment with id %s is marked as deleted.", attachmentId));
      }
      return attachment;
   }

   public List<Attachment> getAttachments(String path) {
      AttachmentFolder attachmentFolder = repositoryService.getNode(AttachmentFolder.class, path + "/" + AttachmentFolder.NAME,
            DepthType.WITH_SUBFOLDERS_AND_GRANDCHILD_RECORDS);
      List<Attachment> attachments;
      if (attachmentFolder.getChildren() != null) {
         attachments = new ArrayList<Attachment>(attachmentFolder.getChildren().values());
      } else {
         attachments = new ArrayList<Attachment>();
      }
      return attachments;
   }

   public Attachment getAttachmentVersion(String attachmentId, long versionNumber) throws Exception {
      Attachment attachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      if (attachment.getDeleted() == true) {
         throw new InvalidAttachmentException(String.format("Attachment with id %s is marked as deleted.", attachmentId));
      }
      VersionHistoryNode historyNode = repositoryService.getNodeByUrl(VersionHistoryNode.class, attachment.getVersionHistoryUrl()); // now get the requested version
                                                                                                                                    // specific path in ModeShape
      String versionPath = historyNode.getVersionPath(versionNumber);
      return repositoryService.getNodeByUrl(Attachment.class, versionPath, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
   }

   public ContentInfo getAttachmentContent(String attachmentId) throws Exception {
      Attachment attachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      if (attachment.getDeleted() == true) {
         throw new InvalidAttachmentException(String.format("Attachment with id %s is marked as deleted.", attachmentId));
      }
      return repositoryService.getBinaryNodeById(attachmentId);
   }

   public ContentInfo getAttachmentContentVersion(String attachmentId, long versionNumber) throws Exception {
      Attachment attachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      if (attachment.getDeleted() == true) {
         throw new InvalidAttachmentException(String.format("Attachment with id %s is marked as deleted.", attachmentId));
      }
      VersionHistoryNode historyNode = repositoryService.getNodeByUrl(VersionHistoryNode.class, attachment.getVersionHistoryUrl()); // now get the requested version
                                                                                                                                    // specific path in ModeShape
      String versionUrl = historyNode.getVersionPath(versionNumber);
      String versionId = repositoryService.getIdByUrl(versionUrl); // TODO: see if we should consolidate this with the next call as getBinaryNode (by path)
      return repositoryService.getBinaryNodeById(versionId);
   }

   public AttachmentVersionHistoryResponse getAttachmentVersionHistory(String attachmentId) throws Exception {
      Attachment attachment = repositoryService.getNodeById(Attachment.class, attachmentId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      VersionHistoryNode historyNode = repositoryService.getNodeByUrl(VersionHistoryNode.class, attachment.getVersionHistoryUrl());

      // for each version, add to the response
      List<AttachmentVersionHistoryResponseItem> historyList = new ArrayList<AttachmentVersionHistoryResponseItem>();
      int versionCount = historyNode.getVersionCount();
      for (int i = 1; i <= versionCount; i++) {
         String versionPath = historyNode.getVersionPath(i);
         log.debug("Found version node {} for attachment {}.", versionPath, attachmentId);

         // create the artifact node for that specific version
         Attachment versionNode = repositoryService.getNodeByUrl(Attachment.class, versionPath, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
         String versionId = versionNode.getId();

         AttachmentVersionHistoryResponseItem historyItem = new AttachmentVersionHistoryResponseItem();
         historyItem.setVersion(i);
         historyItem.setVersionId(versionId);
         historyItem.setAttachment(versionNode);
         historyList.add(historyItem);
      }

      AttachmentVersionHistoryResponse response = new AttachmentVersionHistoryResponse(historyList);
      return response;
   }

   public void clearCache() throws ExecutionException {
      operationalMetadataCache.clear();
   }

   public String getStudyIdByAlias(String aliasValue) throws ExecutionException {
      Map<String, String> aliasMap = new HashMap<>();
      for (Program program : operationalMetadataCache.query("programs").values()) {
         for (Protocol protocol : program.getProtocols()) {
            for (ProtocolAlias alias : protocol.getProtocolAliases()) {
               if (alias.getStudyAlias().equals(aliasValue)) {
                  aliasMap.put(protocol.getStudyId(), alias.getAliasType());
               }
            }
         }
      }
      if (aliasMap.keySet().size() > 1) {
         throw new DuplicateProtocolAliasException(String.format("Multiple studyIds found for this alias value: %s", aliasMap));
      } else if (aliasMap.keySet().size() == 0) {
         throw new NoSuchProtocolAliasException(String.format("No studies found for alias %s", aliasValue));
      } else {
         return aliasMap.keySet().iterator().next();
      }
   }

   private void auditProtocolBlinding(Protocol oldMasterProtocol, Protocol currentSnapshot, Protocol newSnapshot) throws JsonProcessingException, IOException, ExecutionException {
      boolean studyBlindingModified = !currentSnapshot.getStudyBlindingStatus().equals(newSnapshot.getStudyBlindingStatus());
      boolean studyRestrictionModified = !currentSnapshot.getStudyRestrictionStatus().equals(newSnapshot.getStudyRestrictionStatus());

      if (studyBlindingModified) {
         String oldBlindingStatus = currentSnapshot.getStudyBlindingStatus();
         String newBlindingStatus = newSnapshot.getStudyBlindingStatus();
         String action = String.format("Study blinding fields changed, status changed from %s to %s, old snapshot = %s", oldBlindingStatus, newBlindingStatus,
               repositoryService.getIdByUrl(oldMasterProtocol.getCurrentSnapshot())); // old
         // snapshot
         String newSnapshotUrl = repositoryService.getNode(Protocol.class, oldMasterProtocol.getPath()).getCurrentSnapshot(); // new snapshot
         String newSnapshotId = repositoryService.getIdByUrl(newSnapshotUrl);
         auditService.insertAuditEntry(new AuditEntryInput(action, newSnapshot.getStudyId(), EntityType.PROTOCOL.toString(), newSnapshot.getModifiedBy(),
               ActionStatusType.SUCCESS, newSnapshotId));
      }

      if (studyRestrictionModified) {
         String oldRestrictionStatus = currentSnapshot.getStudyRestrictionStatus();
         String newRestrictionStatus = newSnapshot.getStudyRestrictionStatus();
         String action = String.format("Study restriction fields changed, status changed from %s to %s, old snapshot = %s", oldRestrictionStatus, newRestrictionStatus,
               repositoryService.getIdByUrl(oldMasterProtocol.getCurrentSnapshot())); // old
         // snapshot
         String newSnapshotUrl = repositoryService.getNode(Protocol.class, oldMasterProtocol.getPath()).getCurrentSnapshot(); // new snapshot
         String newSnapshotId = repositoryService.getIdByUrl(newSnapshotUrl);
         auditService.insertAuditEntry(new AuditEntryInput(action, newSnapshot.getStudyId(), EntityType.PROTOCOL.toString(), newSnapshot.getModifiedBy(),
               ActionStatusType.SUCCESS, newSnapshotId));
      }
   }

   private void updateKeyValuePairs(UserProtocol sourceProtocol, UserProtocol targetProtocol) {
      Set<KeyValuePair> sourceKvps = sourceProtocol.getCustomAttributes();
      Set<KeyValuePair> targetKvps = targetProtocol.getCustomAttributes();
      Set<String> sourceKeys = new HashSet<>();
      Set<String> targetKeys = new HashSet<>();

      if (sourceKvps == null) {
         // nothing to update if the sourceKvps are null
         return;
      }

      if (sourceKvps != null && targetKvps == null) {
         // Initialize the list if the target doesn't have any existing KVPs
         targetKvps = new HashSet<KeyValuePair>();
         // Set the reference since it was null
         targetProtocol.setCustomAttributes(targetKvps);
      }

      for (KeyValuePair sourceKvp : sourceKvps) {
         // Keep track of these for when we handle creation
         String sourceKey = sourceKvp.getKey();
         sourceKeys.add(sourceKey);
         for (KeyValuePair targetKvp : targetKvps) {
            String targetKey = targetKvp.getKey();
            targetKeys.add(targetKey);

            if (sourceKey.equals(targetKey)) {
               if (sourceKvp.getValue() == null) {
                  // Once we have matching keys, delete the target KVP if the source was null (deletion)
                  targetKvps.remove(targetKvp);
               } else {
                  // Otherwise, update the value of the existing kvp (update)
                  targetKvp.setValue(sourceKvp.getValue());
               }
            }
         }
      }

      // Create a set of newKeys by subtracting sourceKeys - targetKeys
      // Anything left after that must be new, add it to the targetKvps (creation)
      Set<String> newKeys = new HashSet<String>(sourceKeys);
      newKeys.removeAll(targetKeys);
      for (String newKey : newKeys) {
         for (KeyValuePair sourceKvp : sourceKvps) {
            if (sourceKvp.getKey().equals(newKey)) {
               targetKvps.add(sourceKvp);
            }
         }
      }
   }

   private static String toISO8601UTC(Date date) {
      TimeZone tz = TimeZone.getTimeZone("UTC");
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      df.setTimeZone(tz);
      return df.format(date);
   }

   private void handleChildUpdates(Protocol newSnapshot, Date modifiedAt, String modifiedBy) {
      Map<String, Set<? extends BaseProtocolChildNode>> childrenMap = newSnapshot.getChildren();
      for (String recordType : childrenMap.keySet()) {
         for (BaseProtocolChildNode node : childrenMap.get(recordType)) {
            if (node.isChanged()) {
               node.setSourceCreationTimestamp(modifiedAt);
               node.setModifiedBy(modifiedBy);

               if (node.getCreatedBy() == null) {
                  node.setCreated(modifiedAt);
                  node.setCreatedBy(modifiedBy);
               }
            }
            node.clearChanged();
         }
      }
   }
}
