package com.pfizer.equip.shared.service.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pfizer.equip.shared.types.EntityType;

/**
 * Class for holding the user's permissions; what the user can do.
 * <p>
 * Privileges are retrieved from the database and enumerated via PrivilegeType;
 */
public class Permissions {
   EntityMap unblindedEntities = new EntityMap();
   EntityMap unrestrictedEntities = new EntityMap();
   Set<String> reportingEventIds = new HashSet<>();
   Set<PrivilegeType> privileges = new HashSet<PrivilegeType>();
   boolean isExternalUser = false;

   public Permissions() {}

   public void setPrivileges(Set<PrivilegeType> privileges) {
      this.privileges = privileges;
   }

   public Set<PrivilegeType> getPrivileges() {
      return privileges;
   }

   public void setUnblindedEntities(EntityType entityType, Set<String> unblindedEntities) {
      this.unblindedEntities.put(entityType, unblindedEntities);
   }

   public Set<String> getUnblindedEntities(EntityType entityType) {
      return unblindedEntities.get(entityType);
   }

   public boolean isInUnblindingPlan(EntityType entityType, String entityId) {
      boolean inUnblindingPlan = false;

      // Added the below condition to fix the error while executing test2b -
      // Ruckmani
      if (unblindedEntities.get(entityType) != null) {
         for (String entity : unblindedEntities.get(entityType)) {
            if (entity.equals(entityId)) {
               inUnblindingPlan = true;
            }
         }
      }
      return inUnblindingPlan;
   }

   public void setUnrestrictedEntities(EntityType entityType, Set<String> unrestrictedEntities) {
      this.unrestrictedEntities.put(entityType, unrestrictedEntities);
   }

   public Set<String> getUnrestrictedEntities(EntityType entityType) {
      return unrestrictedEntities.get(entityType);
   }

   public boolean isInRestrictionPlan(EntityType entityType, String entityId) {
      boolean inRestrictionPlan = false;
      if (unrestrictedEntities.get(entityType) != null) {
         for (String entity : unrestrictedEntities.get(entityType)) {
            if (entity.equals(entityId)) {
               inRestrictionPlan = true;
            }
         }
      }
      return inRestrictionPlan;
   }

   public boolean hasPrivilege(PrivilegeType targetPrivilege) {
      boolean hasPriv = false;
      for (PrivilegeType priv : privileges) {
         if (priv == targetPrivilege) {
            hasPriv = true;
         }
      }
      return hasPriv;
   }

   public boolean canViewBlinded(EntityType entityType, String entityId) {
      return (hasPrivilege(PrivilegeType.VIEW_BLINDED) || isInUnblindingPlan(entityType, entityId));
   }

   public boolean canViewRestricted(EntityType entityType, String entityId) {
      return (isInRestrictionPlan(entityType, entityId));
   }

   public boolean canViewNonPromoted() {
      return hasPrivilege(PrivilegeType.VIEW_NON_PROMOTED);
   }

   // Output HashMap of permission flags to be used by the authorization service for rule processing.
   public Map<AccessFlag, Boolean> getPermissionFlags(EntityType entityType, String entityId) {
      Map<AccessFlag, Boolean> permissionFlags = new HashMap<AccessFlag, Boolean>();
      permissionFlags.put(AccessFlag.NON_PROMOTED, canViewNonPromoted());
      permissionFlags.put(AccessFlag.RESTRICTED, canViewRestricted(entityType, entityId));
      permissionFlags.put(AccessFlag.STUDY_RESTRICTED, canViewRestricted(entityType, entityId));
      permissionFlags.put(AccessFlag.STUDY_BLINDED, canViewBlinded(entityType, entityId));
      return permissionFlags;
   }

   public boolean isExternalUser() {
      return isExternalUser;
   }

   public void setIsExternalUser(boolean isExternalUser) {
      this.isExternalUser = isExternalUser;
   }

   public boolean hasProtocolAccessRecord() {
      if (getUnblindedEntities(EntityType.PROTOCOL) != null || getUnrestrictedEntities(EntityType.PROTOCOL) != null) {
         return true;
      } else {
         return false;
      }
   }
   
   public boolean hasDataframeAccessRecord() {
      if (getUnblindedEntities(EntityType.DATAFRAME) != null || getUnrestrictedEntities(EntityType.DATAFRAME) != null) {
         return true;
      } else {
         return false;
      }
   }

   public Set<String> getReportingEventIds() {
      return reportingEventIds;
   }

   public void setReportingEventIds(Set<String> reportingEventIds) {
      this.reportingEventIds = reportingEventIds;
   }

   public void addReportingEventId(String reportingEventId) {
      this.reportingEventIds.add(reportingEventId);
   }
}
