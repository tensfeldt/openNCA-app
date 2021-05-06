package com.pfizer.equip.shared.opmeta;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.entity.BaseNode;
import com.pfizer.equip.shared.opmeta.entity.GenericMasterNode;
import com.pfizer.equip.shared.opmeta.entity.Milestone;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.ProtocolAlias;
import com.pfizer.equip.shared.opmeta.exceptions.MissingSnapshotException;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolAliasFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;
import com.pfizer.equip.shared.service.business.api.ReportingEventService;
import com.pfizer.equip.shared.service.business.audit.AuditService;

@Service
// Encapsulates logic for creating and updating opmeta entities with snapshot logic
// Does not include ETL/source database interaction.
public class OperationalMetadataRepositoryService {
   private static final Logger logger = LoggerFactory.getLogger(OperationalMetadataRepositoryService.class);

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private ReportingEventService reportingEventService;

   private final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

   private static final String SYSTEM_USER = AuditService.SYSTEM_USER;
   static final String PROGRAM_ROOT = "/" + ProgramFolder.NAME;

   public String addEntity(BaseNode node) {
      Date date = new Date();
      String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(date);
      String nodePath = node.getPath();
      String snapshotFolderPath = nodePath + "/Snapshots";
      String snapshotPath = snapshotFolderPath + "/" + timestamp;
      if (node instanceof Protocol) {
         // TODO: This is getting messy, consider refactoring.
         Protocol protocol = ((Protocol) node);
         ((Protocol) node).setActiveFlag(isProtocolActive(protocol));
         ((Protocol) node).setSetupDate(date);
         ((Protocol) node).setModified(date);
         ((Protocol) node).setSetupBy(node.getModifiedBy());
      }
      GenericMasterNode masterNode = new GenericMasterNode(node);
      Map<String, Object> nodeMap = new HashMap<String, Object>();
      nodeMap.put(nodePath, masterNode);
      nodeMap.put(snapshotFolderPath, node.getFolder());
      nodeMap.putAll(buildNodeMap(node, nodePath, snapshotPath));
      Map<String, String> nodeIds = repositoryService.setNodes(nodeMap, HttpMethod.POST);
      try {
         logger.debug("Setting snapshot pointer for newly created masternode.");
         masterNode.setCurrentSnapshot(nodeIds.get(snapshotPath));
         masterNode.setModified(date);
         // TODO: What if it fails before this? Do we have a recent snapshot lying around that is invalid?
         String nodeId = repositoryService.updateNode(masterNode, nodePath);
         return nodeId;
      } catch (Exception e) {
         logger.error("Exception occured while adding initial snapshot to newly created master node: '{}'", nodePath, e);
         repositoryService.deleteNode(snapshotPath);
         throw new RuntimeException(e);
      }

   }

   public String updateEntity(BaseNode node) {
      String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
      String nodePath = node.getPath();
      String snapshotFolderPath = nodePath + "/Snapshots";
      String snapshotPath = snapshotFolderPath + "/" + timestamp;
      if (node instanceof Protocol) {
         // TODO: This is getting messy, consider refactoring.
         Protocol protocol = ((Protocol) node);
         ((Protocol) node).setActiveFlag(isProtocolActive(protocol));
      }
      GenericMasterNode masterNode = new GenericMasterNode(node);
      Map<String, Object> nodeMap = new HashMap<String, Object>();
      nodeMap.putAll(buildNodeMap(node, nodePath, snapshotPath));
      Map<String, String> nodeIds = repositoryService.setNodes(nodeMap, HttpMethod.POST);
      try {
         logger.debug("Updating masternode snapshot pointer.");
         masterNode.setCurrentSnapshot(nodeIds.get(snapshotPath));
         // TODO: What if it fails before this? Do we have a recent snapshot lying around that is invalid?
         String nodeId = repositoryService.updateNode(masterNode, nodePath);
         return nodeId;
      } catch (Exception e) {
         logger.error("Exception occured while adding snapshot to master node: '{}'", nodePath, e);
         repositoryService.deleteNode(snapshotPath);
         throw new RuntimeException(e);
      }
   }

   public Boolean isProtocolActive(Protocol protocol) {
      boolean activeFlag = true;

      // If the protocol current status is CANCELLED, COMPLETED or TERMINATED, then it's inactive.
      List<String> inactiveStatuses = Arrays.asList("CANCELLED", "COMPLETED", "TERMINATED");
      if (inactiveStatuses.contains(protocol.getStudyStatusCurrent())) {
         activeFlag = false;
      }

      // If the protocol has met the Final CSR Sign Off milestone (milestone exists and actual date is present), then it's inactive.
      Set<Milestone> milestones = protocol.getMilestones();
      if (milestones != null) {
         for (Milestone milestone : milestones) {
            if (milestone.getStudyTaskActivityValue() != null && milestone.getStudyTaskActivityValue().equals("Final CSR Sign Off")
                  && milestone.getActualDate() != null) {
               activeFlag = false;
            }
         }
      }

      // Only check when not during PODS load (when not DEFAULT_MODIFIED_BY) because reporting events don't change at that point.
      // This code will be entered when DF services updates protocol "modified" endpoint, it'll get triggered at that time.
      // If the protocol has at least one Released reporting event, then it's inactive:
//      if (!protocol.getModifiedBy().equals(SYSTEM_USER)) {
//         if (reportingEventService.hasReleasedReportingEvent(protocol.getProgramCode(), protocol.getStudyId(), protocol.getModifiedBy())) {
//            activeFlag = false;
//         }
//      }

      return activeFlag;
   }

   private Map<String, Object> buildNodeMap(BaseNode node, String nodePath, String snapshotPath) {
      Map<String, Object> nodeMap = new HashMap<String, Object>();
      nodeMap.put(snapshotPath, node);
      if (node.getChildren() != null) {
         for (String child : node.getChildren().keySet()) {
            int idx = 1;
            for (BaseNode childNode : node.getChildren().get(child)) {
               // Set modifiedBy property for PODS loads, for clarity/consistency with non-PODS
               if (childNode.getModifiedBy() == null) {
                  childNode.setModifiedBy(SYSTEM_USER);
               }
               String arrayMarker = "";
               arrayMarker = "[" + idx + "]";
               nodeMap.put(snapshotPath + "/" + childNode.getPath() + arrayMarker, childNode);
               idx++;
            }
         }
      }
      if (node.getDirectChildren() != null) {
         for (BaseNode childNode : node.getDirectChildren()) {
            nodeMap.put(snapshotPath + "/" + childNode.getPath(), childNode);
         }
      }
      // Now that we got the children, clear them so that it doesn't mess up serialiation into the repo:
      node.clearChildEntities();
      return nodeMap;
   }

   // Retrieve programs, include master node user-editable attributes
   public Map<String, Program> getPrograms() throws IOException {
      Map<String, Program> programs = new HashMap<String, Program>();

      ProgramFolder programFolder = repositoryService.getNode(ProgramFolder.class, PROGRAM_ROOT, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      for (Program masterProgram : programFolder.getChildren().values()) {
         if (masterProgram.getCurrentSnapshot() == null) {
            throw new MissingSnapshotException(String.format("Program '%s' is missing a value for currentSnapshot. Invalid data.", masterProgram.getProgramCode()));
         }
         Program snapshotProgram = repositoryService.getNodeByUrl(Program.class, masterProgram.getCurrentSnapshot());
         Set<Protocol> protocols = new HashSet<Protocol>();
         String nodePath = PROGRAM_ROOT + "/" + masterProgram.getProgramCode() + "/" + ProtocolFolder.NAME;

         ProtocolFolder protocolFolder = repositoryService.getNode(ProtocolFolder.class, nodePath, DepthType.WITH_DIRECT_CHILDREN);
         if (protocolFolder.getChildren() != null) {
            for (Protocol masterProtocol : protocolFolder.getChildren().values()) {
               if (masterProtocol.getCurrentSnapshot() == null) {
                  throw new MissingSnapshotException(String.format("Protocol '%s' is missing a value for currentSnapshot. Invalid data.", masterProtocol.getStudyId()));
               }
               Protocol snapshotProtocol = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot());
               ProtocolAliasFolder protocolAliasFolder = repositoryService.getNodeByUrl(ProtocolAliasFolder.class, masterProtocol.getCurrentSnapshot() + "/" + ProtocolAliasFolder.NAME, DepthType.WITH_DIRECT_CHILDREN);
               if (protocolAliasFolder.getChildren() != null) {
                  snapshotProtocol.setProtocolAliases(new HashSet<ProtocolAlias>(protocolAliasFolder.getChildren().values()));
               } else {
                  snapshotProtocol.setProtocolAliases(new HashSet<ProtocolAlias>());
               }
               snapshotProtocol.setCandidateProjectUuid(null);
               snapshotProtocol.setModified(masterProtocol.getModified());
               snapshotProtocol.setModifiedBy(masterProtocol.getModifiedBy());
               snapshotProtocol.setActiveFlag(masterProtocol.getActiveFlag());
               // FIXME: Change condition to just snapshotProtocol.getDeleteFlag. Currently insufficient data
               if (snapshotProtocol.getDeleteFlag() == null || !snapshotProtocol.getDeleteFlag()) {
                  protocols.add(snapshotProtocol);
               }
            }
         }
         snapshotProgram.setProtocols(protocols);
         programs.put(snapshotProgram.getProgramCode(), snapshotProgram);
      }

      return programs;
   }

   public Program getProgram(String programId) {
      Program masterProgram = repositoryService.getNode(Program.class, PROGRAM_ROOT + "/" + programId, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

      if (masterProgram.getCurrentSnapshot() == null) {
         throw new MissingSnapshotException(String.format("Program '%s' is missing a value for currentSnapshot. Invalid data.", masterProgram.getProgramCode()));
      }
      Program snapshotProgram = repositoryService.getNodeByUrl(Program.class, masterProgram.getCurrentSnapshot());
      Set<Protocol> protocols = new HashSet<Protocol>();
      String nodePath = PROGRAM_ROOT + "/" + masterProgram.getProgramCode() + "/" + ProtocolFolder.NAME;

      ProtocolFolder protocolFolder = repositoryService.getNode(ProtocolFolder.class, nodePath, DepthType.WITH_DIRECT_CHILDREN);
      if (protocolFolder.getChildren() != null) {
         for (Protocol masterProtocol : protocolFolder.getChildren().values()) {
            if (masterProtocol.getCurrentSnapshot() == null) {
               throw new MissingSnapshotException(String.format("Protocol '%s' is missing a value for currentSnapshot. Invalid data.", masterProtocol.getStudyId()));
            }
            Protocol snapshotProtocol = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot());
            protocols.add(snapshotProtocol);
         }
      }
      snapshotProgram.setProtocols(protocols);
      return snapshotProgram;
   }
   
   public Protocol getProtocol(String programId, String protocolId) {
      Protocol protocol = new Protocol(programId, protocolId);
      Protocol masterProtocol = repositoryService.getNode(Protocol.class, protocol.getPath(), DepthType.WITH_TOP_LEVEL);
      Protocol snapshotProtocol = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
      snapshotProtocol.setModified(masterProtocol.getModified());
      snapshotProtocol.setModifiedBy(masterProtocol.getModifiedBy());
      snapshotProtocol.setActiveFlag(masterProtocol.getActiveFlag());
      return snapshotProtocol;
   }
}
