package com.pfizer.equip.shared.opmeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.entity.Milestone;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.folder.ProtocolFolder;
import com.pfizer.equip.shared.service.business.notifications.EventService;

@Service
@Transactional
// Service for processing clinical database events in the operational metadata store.
public class OperationalMetadataEventService {

   private static final Logger logger = LoggerFactory.getLogger(OperationalMetadataEventService.class);

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   EventService eventService;

   private final String SNAPSHOTS_FOLDER = "Snapshots";
   private final String EVENT_QUEUE = "event"; // TODO: get from properties file.

   public void processMilestones(String protocolPath) {
      Set<String> protocolPaths = new HashSet<String>(Arrays.asList(protocolPath));
      processMilestones(protocolPaths);
   }

   public void processMilestones(Set<String> protocolPaths) {
      Protocol previousProtocolSnapshot = null;
      String previousSnapshotName = null;
      // Input is set of protocol paths such as ["/Programs/B153/Protocols/B1531008", "/Programs/B153/Protocols/B1531007", ...]

      // We need to analyze each protocol and generate and milestone-related notifications for each:
      for (String protocolPath : protocolPaths) {
         // To get the current protocol metadata, we need to retrieve the latest snapshot using the opmeta:currentSnapshot field
         // Get the current snapshot url from the master node
         String protocolSnapshotUrl = repositoryService.getNode(Protocol.class, protocolPath, DepthType.WITH_TOP_LEVEL).getCurrentSnapshot();

         // Get the current protocol from snapshot with a depth of 2
         Protocol currentProtocolSnapshot = repositoryService.getNodeByUrl(Protocol.class, protocolSnapshotUrl, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

         // Get all protocol snapshots and determine the previous one (i.e., the second oldest, the one right before currentSnapshot):
         ProtocolFolder protocolSnapshots = repositoryService.getNode(ProtocolFolder.class, currentProtocolSnapshot.getPath() + "/" + SNAPSHOTS_FOLDER, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

         List<Protocol> protocolSnapshotsList = new ArrayList<Protocol>(protocolSnapshots.getChildren().values());

         // If there is only 1 snapshot, then no need to perform anything
         if (protocolSnapshotsList.size() > 1) {
            // Sort the list by descending order
            protocolSnapshotsList.sort((o1, o2) -> {
               return o2.getSourceCreationTimestamp().compareTo(o1.getSourceCreationTimestamp());
            });

            previousSnapshotName = getKeysByValue(protocolSnapshots.getChildren(), protocolSnapshotsList.get(1)).toString();

            previousSnapshotName = previousSnapshotName.substring(1, previousSnapshotName.length() - 1);
            String previousProtocolSnapshotUrl = protocolSnapshotUrl.substring(0, protocolSnapshotUrl.length() - 23);

            // get the previous snapshot by Node Url
            previousProtocolSnapshot = repositoryService.getNodeByUrl(Protocol.class, previousProtocolSnapshotUrl + previousSnapshotName, DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);

            // Now we have the current protocol and the previousProtocolSnapshot. Iterate through the milestones of each protocol
            // Determine if anything has changed (e.g., new milestone)
            // If there is a new milestone, is it one of the events in the requirements?
            // The operational metadata ETL shall notify assigned users when a newer value of an externally sourced metadata field is available.
            Set<Milestone> milestoneEvents = new HashSet<Milestone>();
            if (currentProtocolSnapshot.getMilestones() != null) {
               for (Milestone milestone : currentProtocolSnapshot.getMilestones()) {
                  boolean doNotification = true;
                  // if previous milestones were null, then we have new ones:
                  if (previousProtocolSnapshot.getMilestones() != null) {
                     for (Milestone milestoneSnapshot : previousProtocolSnapshot.getMilestones()) {
                        if (milestoneSnapshot.getStudyTaskActivityValue() != null && milestone.getStudyTaskActivityValue() != null) {
                           if (milestone.getStudyTaskActivityValue().equalsIgnoreCase(milestoneSnapshot.getStudyTaskActivityValue())) {
                              doNotification = false;
                           }
                        }
                     }
                  }
                  // Logic for determining whether an event has to be triggered based on milestones
                  if (doNotification) {
                     milestoneEvents.add(milestone);
                  }
               }
            }

            for (Milestone milestone : milestoneEvents) {
               String eventType = null;
               Map<String, Object> description = new HashMap<>();
               description.put("comments", "Mismatch found between the current and previous protocol snapshot.");
               description.put("system_initiated", "false");
               description.put("milestone_id", milestone.getMilestoneId());
               description.put("study_task_activity_value", milestone.getStudyTaskActivityValue());
               if (milestone.getActualDate() != null) {
                  String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
                  SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                  String actualDate = simpleDateFormat.format(milestone.getActualDate());
                  description.put("actual_date", actualDate);
               }
               if (milestone.getStudyTaskActivityValue() != null) {

                  switch (milestone.getStudyTaskActivityValue()) {
                  // Need to check and update few more cases if needed
                  case "PK Contribution Delivered to Medical Writer":
                     eventType = StudyTaskActivityValueType.SEND_TO_WRITER.getStudyTaskActValType();
                     break;
                  case "Date Final CSR Published in AP System":
                     eventType = StudyTaskActivityValueType.PUBLISHING.getStudyTaskActValType();
                     break;
                  case "Final Concentration (and NCA PK Parameters, if required) Published":
                     eventType = StudyTaskActivityValueType.PUBLISHING.getStudyTaskActValType();
                     break;
                  case "Final Approved Protocol":
                     eventType = StudyTaskActivityValueType.FINAL_APPROVED_PROTOCOL.getStudyTaskActValType();
                     break;
                  case "Final Tables Production (Table\\ Lists\\Figures-Vaccines)":
                     eventType = StudyTaskActivityValueType.FINAL_TABLE_ACTUAL.getStudyTaskActValType();
                     break;
                  case "Final CSR Sign Off":
                     eventType = StudyTaskActivityValueType.CSR_APPROVAL_DATE.getStudyTaskActValType();
                     break;
                  case "Tables published to GDMS/ Start of CSR":
                     eventType = StudyTaskActivityValueType.PUBLISHING.getStudyTaskActValType();
                     break;

                  case "Date Last PK Sample Received by Assay Lab":
                     eventType = StudyTaskActivityValueType.ASSAY_LAB.getStudyTaskActValType();
                     break;
                  default:
                     break;
                  }
               }

               if (eventType != null) {
                  eventService.createEvent(this.getClass().toString(), new Date(), currentProtocolSnapshot.getPath(), "milestone", eventType,
                        currentProtocolSnapshot.getStudyId(), currentProtocolSnapshot.getProgramCode(), description, EVENT_QUEUE);
               }
            }
         }
      }
   }

   /**
    * Get the Key by value from the map
    * 
    * @param map
    * @param value
    * @return
    */
   public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
      return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey).collect(Collectors.toSet());
   }
}