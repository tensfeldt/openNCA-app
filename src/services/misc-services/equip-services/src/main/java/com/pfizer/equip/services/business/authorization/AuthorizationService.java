package com.pfizer.equip.services.business.authorization;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfizer.equip.services.business.api.dataframe.Dataframe;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.service.user.AccessFlag;
import com.pfizer.equip.shared.service.user.Permissions;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.EntityType;

/**
 * Engine for processing authorization-related data and determining access based on business rules.
 */
@Service
public class AuthorizationService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   RepositoryService repositoryService;

   @Autowired
   UserLookupService userLookupService;

   /**
    * Method for determining dataframe access based on a set of facts and
    * permissions.
    * <p>
    * 
    * @param facts
    * Facts about the object for which access is being determined.
    * @param permissions
    * The users' permissions.
    * @return Whether the user has access or not.
    */
   public boolean canViewDataframe(Permissions permissions, Facts facts) {
      Map<AccessFlag, Boolean> protocolPermissionFlags;
      Map<AccessFlag, Boolean> reportingEventPermissionFlags;
      Map<AccessFlag, Boolean> dataframePermissionFlags;
      Map<AccessFlag, Boolean> factFlags;
      boolean canView = true;
      Map<String, Map<AccessFlag, Boolean>> canViewFlags = new HashMap<String, Map<AccessFlag, Boolean>>();

      for (String studyId : facts.getParentEntities(EntityType.PROTOCOL)) {
         Map<AccessFlag, Boolean> entityViewFlags = new HashMap<AccessFlag, Boolean>();

         factFlags = facts.getProtocolFactFlags(studyId);

         protocolPermissionFlags = permissions.getPermissionFlags(EntityType.PROTOCOL, studyId);

         for (AccessFlag factType : AccessFlag.values()) {
            // Protocol-level is primary, user may be unable to view at this level, but the other levels may override
            if (!canView(factFlags.get(factType), protocolPermissionFlags.get(factType))) {
               entityViewFlags.put(factType, false);
            }
         }

         // Optional RE-level authorization, only do it if the dataframe is part of an RE
         if (facts.hasReportingEvents()) {
            for (String reportingEventId : facts.getParentEntities(EntityType.REPORTING_EVENT)) {
               reportingEventPermissionFlags = permissions.getPermissionFlags(EntityType.REPORTING_EVENT, reportingEventId);
               for (AccessFlag factType : AccessFlag.values()) {
                  // Reporting-event-level is optional and can override Protocol-level.
                  // If the user has clearance from at least one of the REs, then authorize them on that permission
                  if (canView(factFlags.get(factType), reportingEventPermissionFlags.get(factType))) {
                     entityViewFlags.put(factType, true);
                  }
               }
            }
         }

         // Optional DF-level authorization, only do it if user has some DF-level group access
         if (permissions.hasDataframeAccessRecord()) {
            dataframePermissionFlags = permissions.getPermissionFlags(EntityType.DATAFRAME, facts.getDataframeId());
            for (AccessFlag factType : AccessFlag.values()) {
               // Dataframe-level also optional, overrides
               if (canView(factFlags.get(factType), dataframePermissionFlags.get(factType))) {
                  entityViewFlags.put(factType, true);
               }
            }
         }
         canViewFlags.put(studyId, entityViewFlags);
      }

      for (Map<AccessFlag, Boolean> entityFlags : canViewFlags.values()) {
         if (entityFlags.containsValue(false)) {
            canView = false;
         }
      }
      return canView;
   }

   /**
    * Method for determining dataframe access based on a set of facts and
    * permissions for external users. No access (default) unless there exists an RE
    * that authorizes them and that RE access is compatible with the dataframe's
    * blinding/restricted flags.
    * <p>
    * 
    * @param facts
    * Facts about the object for which access is being determined.
    * @param permissions
    * The users' permissions.
    * @return Whether the user has access or not.
    */
   public boolean canViewDataframeExternal(Permissions permissions, Facts facts) {
      Map<AccessFlag, Boolean> reportingEventPermissionFlags;
      // Only support a single study:
      Map<AccessFlag, Boolean> factFlags = facts.getReportingEventFactFlags(facts.getParentEntities(EntityType.PROTOCOL).iterator().next());
      Map<String, Map<AccessFlag, Boolean>> canViewFlags = new HashMap<String, Map<AccessFlag, Boolean>>();

      Map<AccessFlag, Boolean> entityViewFlags;

      // No REs for the DF? Not authorized.
      if (!facts.hasReportingEvents()) {
         return false;
      }

      for (String reportingEventId : permissions.getReportingEventIds()) {
         reportingEventPermissionFlags = permissions.getPermissionFlags(EntityType.REPORTING_EVENT, reportingEventId);
         entityViewFlags = new HashMap<AccessFlag, Boolean>();
         for (AccessFlag factType : AccessFlag.values()) {
            if (factFlags.get(factType) != null && reportingEventPermissionFlags.get(factType) != null) {
               if (!canView(factFlags.get(factType), reportingEventPermissionFlags.get(factType))) {
                  entityViewFlags.put(factType, false);
               }
            }
         }
         canViewFlags.put(reportingEventId, entityViewFlags);
      }

      for (String reportingEventId : facts.getParentEntities(EntityType.REPORTING_EVENT)) {
         boolean canView = true;
         if (canViewFlags.get(reportingEventId) == null || canViewFlags.get(reportingEventId).containsValue(false)) {
            canView = false;
         }
         // If one of the REs grants access, that's sufficient for authorization.
         if (canView) {
            return true;
         }
      }
      return false;
   }

   /**
    * Method for determining facts about a dataframe in order to process them.
    * <p>
    * 
    * @param dataframe
    * Facts about the object for which access is being determined.
    * @return Facts object containing information about promotion, blinding,
    * restriction for multiple parent entities of different types for this
    * dataframe
    */
   public Facts getFacts(Dataframe dataframe, Protocol protocol) {
      Facts facts = new Facts();
      facts.setIsPromoted(dataframe.getIsPromoted());
      facts.setIsDataUnblinded(dataframe.getIsDataUnblinded());
      facts.setIsRestricted(dataframe.getIsRestricted());

      log.debug("Building input for dataframe id {}", dataframe.getId());

      log.debug("Determining study-level facts");
      facts.setIsStudyBlinded(String.format("%s:%s", protocol.getProgramCode(), protocol.getStudyId()), protocol.getIsStudyBlinded());
      facts.setIsStudyRestricted(protocol.getIsStudyRestricted());
      facts.addParentEntity(EntityType.PROTOCOL, String.format("%s:%s", protocol.getProgramCode(), protocol.getStudyId()));

      log.debug("Determining reporting-event-level facts");
      if (dataframe.getReportingEventIds() != null) {
         for (String reportingEventId : dataframe.getReportingEventIds()) {
            facts.addParentEntity(EntityType.REPORTING_EVENT, reportingEventId);
         }
      }

      log.debug("Determining dataframe-level facts");
      facts.setDataframeId(dataframe.getId());

      return facts;
   }

   /**
    * Method for determining access based on a single fact flag and a single permission flag.
    * 
    * @param factFlag Whether or not a certain fact is true (e.g., dataframe is blinded).
    * @param permissionFlag Whether or not a user has a certain permission (e.g., user can view blinded data)
    * @return Whether access is permitted in this situation.
    */
   public boolean canView(boolean factFlag, boolean permissionFlag) {
      // The following is equivalent to:
      // If factFlag is true, then permissionFlag must be true.
      return (!factFlag || permissionFlag);
   }
}
