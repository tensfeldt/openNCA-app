package com.pfizer.equip.shared.opmeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeNotFoundException;
import com.pfizer.equip.shared.opmeta.entity.BaseNode;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Project;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.UserProtocol;
import com.pfizer.equip.shared.opmeta.folder.ProgramFolder;
import com.pfizer.equip.shared.opmeta.folder.ProjectFolder;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;
import com.pfizer.equip.shared.opmeta.repository.ProgramRepository;
import com.pfizer.equip.shared.opmeta.repository.ProjectRepository;
import com.pfizer.equip.shared.opmeta.repository.ProtocolRepository;
import com.pfizer.equip.shared.relational.repository.OperationalMetadataJobRepository;
import com.pfizer.equip.shared.service.business.api.PimsService;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.utils.BeanUtils;

@Service
@Transactional
// Encapsulates logic for performing the opmeta ETL.
public class OperationalMetadataProcessorService {

   private static final Logger logger = LoggerFactory.getLogger(OperationalMetadataProcessorService.class);

   @Autowired
   ProgramRepository programRepository;

   @Autowired
   ProjectRepository projectRepository;

   @Autowired
   ProtocolRepository protocolRepository;

   @Autowired
   OperationalMetadataJobRepository jobRepository;
   
   @Autowired
   PimsService pimsService;

   @Autowired
   OperationalMetadataEventService operationalMetadataEventService;

   private Set<String> programsFilter;
   private Set<String> projectsFilter;
   private Set<String> protocolsFilter;

   private Set<Program> programs;
   private Set<Project> projects;
   private Set<Protocol> protocols;
   
   int MAX_FILTER = 999;

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;
   
   private static final String DEFAULT_MODIFIED_BY = AuditService.SYSTEM_USER;

   public void analyze() {
      programsFilter = new HashSet<String>();
      projectsFilter = new HashSet<String>();
      protocolsFilter = new HashSet<String>();

      String programsPath = String.format("/%s", ProgramFolder.NAME);
      ProgramFolder programFolder = repositoryService.getNode(ProgramFolder.class, programsPath, DepthType.WITH_DIRECT_CHILDREN);
      
      if (programFolder.getChildren() == null) {
         throw new RuntimeException("Programs folder appears to be empty.");
      }
      
      for (Program program : programFolder.getChildren().values()) {
         logger.debug("Build filter for program {}", program.getProgramCode());
         if (program.getSource().equals(SourceType.PODS)) {
            programsFilter.add(program.getProgramCode());
         }
         
         String projectPath = String.format("/%s/%s/%s", ProgramFolder.NAME, program.getProgramCode(), ProjectFolder.NAME);
         ProjectFolder projectFolder = repositoryService.getNode(ProjectFolder.class, projectPath, DepthType.WITH_DIRECT_CHILDREN);
         if (projectFolder.getChildren() != null) {
            logger.debug("Adding projects to filter {}", projectFolder.getChildren().keySet());
            for (Project project : projectFolder.getChildren().values()) {
               if (project.getSource().equals(SourceType.PODS)) {
                  projectsFilter.add(project.getProjectCode());
               }
            }
         }

         String protocolPath = String.format("/%s/%s/%s", ProgramFolder.NAME, program.getProgramCode(), ProtocolFolder.NAME);
         ProtocolFolder protocolFolder = repositoryService.getNode(ProtocolFolder.class, protocolPath, DepthType.WITH_DIRECT_CHILDREN);
         if (protocolFolder.getChildren() != null) {
            logger.debug("Adding projects to filter {}", protocolFolder.getChildren().keySet());
            for (Protocol protocol : protocolFolder.getChildren().values()) {
               if (protocol.getSource().equals(SourceType.PODS)) {
                  protocolsFilter.add(protocol.getStudyId());
               }
            }
         }
      }
   }

   public void extract(Date lastSuccessfulRunDate) {
      programs = new HashSet<Program>();
      projects = new HashSet<Project>();
      protocols = new HashSet<Protocol>();
      extractPrograms(programsFilter, lastSuccessfulRunDate);
      extractProjects(projectsFilter, lastSuccessfulRunDate);
      extractProtocols(protocolsFilter, lastSuccessfulRunDate);
   }
   
   private void extractPrograms (Set<String> programsFilter, Date startDate) {
      final List<List<String>> partitions = ListUtils.partition(new ArrayList<String>(programsFilter), MAX_FILTER);
      for (List<String> partition : partitions) {
         programs.addAll(programRepository.findByProgramCodeInAndSourceCreationTimestampAfter(partition, startDate));
      }
   }

   private void extractProjects (Set<String> projectsFilter, Date startDate) {
      final List<List<String>> partitions = ListUtils.partition(new ArrayList<String>(projectsFilter), MAX_FILTER);
      for (List<String> partition : partitions) {
         projects.addAll(projectRepository.findByProjectCodeInAndSourceCreationTimestampAfter(partition, startDate));
      }
   }

   private void extractProtocols (Set<String> protocolsFilter, Date startDate) {
      final List<List<String>> partitions = ListUtils.partition(new ArrayList<String>(protocolsFilter), MAX_FILTER);
      for (List<String> partition : partitions) {
         protocols.addAll(protocolRepository.findByStudyIdInAndSourceCreationTimestampAfter(partition, startDate));
      }
   }

   public void load() {
      loadPrograms();
      loadProjects();
      loadProtocols();
   }

   public void loadPrograms() {
      loadPrograms(null);
   }
   public void loadPrograms(Collection<String> programsFilterOverride) {
      // allow for override to manually load newly created nodes from services 
      Set<Program> programsToLoad;
      if (programsFilterOverride == null) {
         programsToLoad = new HashSet<Program>(programs);
      } else {
         programsToLoad = programRepository.findByProgramCodeIn(programsFilterOverride);
      }
      logger.debug("Loading programs");
      for (Program program : programsToLoad) {
         program.setSource(SourceType.PODS);
         loadNode(program);
      }
   }

   public void loadProjects() {
      loadProjects(null);
   }
   public void loadProjects(Collection<String> projectsFilterOverride) {
      // allow for override to manually load newly created nodes from services 
      Set<Project> projectsToLoad;
      if (projectsFilterOverride == null) {
         projectsToLoad = new HashSet<Project>(projects);
      } else {
         projectsToLoad = projectRepository.findByProjectCodeIn(projectsFilterOverride);
      }
      logger.debug("Loading projects");
      for (Project project : projectsToLoad) {
         project.setSource(SourceType.PODS);
         loadNode(project);
      }
   }

   public void loadProtocols() {
      loadProtocols(null, DEFAULT_MODIFIED_BY);
   }

   public void loadProtocols(Collection<String> protocolsFilterOverride, String modifiedBy) {
      // allow for override to manually load newly created nodes from services 
      Set<Protocol> protocolsToLoad;
      if (protocolsFilterOverride == null) {
         protocolsToLoad = new HashSet<Protocol>(protocols);
      } else {
         protocolsToLoad = protocolRepository.findByStudyIdIn(protocolsFilterOverride);
      }
      logger.debug("Loading protocols");
      for (Protocol protocol : protocolsToLoad) {
         if (protocol.getProjectCode() != null) {
            // Load project if non-existent
            Project project = new Project(protocol.getProgramCode(), protocol.getProjectCode());
            try {
               project = repositoryService.getNode(Project.class, project.getPath(), DepthType.WITH_TOP_LEVEL);
            } catch (NodeNotFoundException e1) {
               
               Program program = new Program(protocol.getProgramCode());
               try {
                  logger.warn("Protocol {} references program {} that does not exist in repository, performing a one-off load.", protocol.getStudyId(), protocol.getProgramCode());
                  repositoryService.getNode(Program.class, program.getPath(), DepthType.WITH_TOP_LEVEL);
               } catch (NodeNotFoundException e2) {
                  loadPrograms(Arrays.asList(protocol.getProgramCode()));
                  repositoryService.getNode(Program.class, program.getPath(), DepthType.WITH_TOP_LEVEL);
               }
               
               logger.warn("Protocol {} references project {} that does not exist in repository, performing a one-off load.", protocol.getStudyId(), protocol.getProjectCode());
               loadProjects(Arrays.asList(protocol.getProjectCode()));
               project = repositoryService.getNode(Project.class, project.getPath(), DepthType.WITH_TOP_LEVEL);
            }

            protocol.setCandidateProjectUuid(project.getUuid());
         }

         // Ensure previous snapshot's user-editable fields are preserved
         Protocol masterProtocol = null;
         try {
            masterProtocol = repositoryService.getNode(Protocol.class, protocol.getPath());
         } catch (NodeNotFoundException e) {
            // this exception should only occur if this service call was invoked from the UI
            masterProtocol = null;
         }
         if (masterProtocol != null) {
            logger.debug("Copying previous protocol snapshot's user-editable fields.");
            Protocol currentSnapshot = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
            UserProtocol userProtocol = new UserProtocol();
            // filter out non-user-editable fields
            BeanUtils.copyNonNullProperties(currentSnapshot, userProtocol, null, "children");
            // overlay user-editable fields on top of PODS data
            BeanUtils.copyNonNullProperties(userProtocol, protocol, null, "children"); 
         }

         protocol.setSource(SourceType.PODS);
         protocol.setModifiedBy(modifiedBy);
         logger.error("Checking PIMS status for study {}", protocol.getStudyId());
         try {
            protocol.setPimsFlag(pimsService.isPims(protocol.getStudyId()));
         } catch (HttpStatusCodeException e) {
            logger.error("DF Pims check failed, setting to null");
            protocol.setPimsFlag(null);
         }
         loadNode(protocol);
      }
      Set<String> protocolPaths = protocolsToLoad.stream().map(protocol -> protocol.getPath()).collect(Collectors.toSet());
      operationalMetadataEventService.processMilestones(protocolPaths);
   }
   
   private void loadNode(BaseNode node) {
      logger.info("Loading node: {}", node.getPath());
      BaseNode existingNode = null;
      try {
         existingNode = repositoryService.getNode(node.getClass(), node.getPath(), DepthType.WITH_TOP_LEVEL);
      } catch (NodeNotFoundException e) {
         existingNode = null;
      }

      if (existingNode != null) {
         logger.debug("Node found in repository, updating.");
         operationalMetadataRepositoryService.updateEntity(node);
      } else {
         logger.debug("Node not found in repository, creating new master node and initial snapshot.");
         operationalMetadataRepositoryService.addEntity(node);
      }
   }
}
