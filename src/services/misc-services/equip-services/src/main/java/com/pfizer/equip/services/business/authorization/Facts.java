package com.pfizer.equip.services.business.authorization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pfizer.equip.shared.service.user.AccessFlag;
import com.pfizer.equip.shared.service.user.EntityMap;
import com.pfizer.equip.shared.types.EntityType;

/**
 * Class for holding facts about the object we're checking permissions for.
 */
public class Facts {
   private boolean isPromoted;
   private boolean isDataUnblinded;
   private boolean isRestricted;
   private boolean isStudyRestricted;
   private EntityMap parentEntities = new EntityMap();
   private HashMap<String, Boolean> studyBlindingStatuses = new HashMap<String, Boolean>();
   private String dataframeId;

   public Facts() {}

   public void addParentEntities(EntityType entityType, Set<String> entityId) {
      this.parentEntities.put(entityType, entityId);
   }

   public Set<String> getParentEntities(EntityType entityType) {
      return this.parentEntities.get(entityType);
   }

   public void addParentEntity(EntityType entityType, String entityId) {
      if (parentEntities.get(entityType) == null) {
         parentEntities.put(entityType, new HashSet<String>());
      }
      this.parentEntities.get(entityType).add(entityId);
   }

   public boolean hasReportingEvents() {
      return parentEntities.keySet().contains(EntityType.REPORTING_EVENT);
   }

   public String getDataframeId() {
      return dataframeId;
   }

   public void setDataframeId(String dataframeId) {
      this.dataframeId = dataframeId;
   }

   // For clarity, setIsPromoted flag but include getIsNonPromoted method,
   // for the sake of rules processing which needs the inverse of this value.
   public void setIsPromoted(boolean isPromoted) {
      this.isPromoted = isPromoted;
   }

   public boolean getIsPromoted() {
      return isPromoted;
   }

   public boolean getIsNonPromoted() {
      return !isPromoted;
   }

   public void setIsDataUnblinded(boolean isDataUnblinded) {
      this.isDataUnblinded = isDataUnblinded;
   }

   public boolean getIsIdentified() {
      return isDataUnblinded;
   }

   public void setIsRestricted(boolean isRestricted) {
      this.isRestricted = isRestricted;
   }

   public boolean getIsRestricted() {
      return isRestricted;
   }

   public void setIsStudyRestricted(boolean isStudyRestricted) {
      this.isStudyRestricted = isStudyRestricted;
   }

   public boolean getIsStudyRestricted() {
      return isStudyRestricted;
   }

   public void setIsStudyBlinded(String studyId, boolean isBlinded) {
      this.studyBlindingStatuses.put(studyId, isBlinded);
   }

   public boolean getIsStudyBlinded(String studyId) {
      return studyBlindingStatuses.get(studyId) && isDataUnblinded;
   }

   // Output HashMap of fact flags to be used by the authorization service for rule processing.
   public Map<AccessFlag, Boolean> getProtocolFactFlags(String studyId) {
      Map<AccessFlag, Boolean> factFlags = new HashMap<AccessFlag, Boolean>();
      factFlags.put(AccessFlag.NON_PROMOTED, getIsNonPromoted());
      factFlags.put(AccessFlag.RESTRICTED, getIsRestricted());
      factFlags.put(AccessFlag.STUDY_RESTRICTED, getIsStudyRestricted());
      factFlags.put(AccessFlag.STUDY_BLINDED, getIsStudyBlinded(studyId));
      return factFlags;
   }

   public Map<AccessFlag, Boolean> getReportingEventFactFlags(String studyId) {
      Map<AccessFlag, Boolean> factFlags = new HashMap<AccessFlag, Boolean>();
      factFlags.put(AccessFlag.STUDY_BLINDED, getIsStudyBlinded(studyId));
      factFlags.put(AccessFlag.STUDY_RESTRICTED, getIsStudyRestricted());
      factFlags.put(AccessFlag.RESTRICTED, getIsRestricted());
      return factFlags;
   }
}
