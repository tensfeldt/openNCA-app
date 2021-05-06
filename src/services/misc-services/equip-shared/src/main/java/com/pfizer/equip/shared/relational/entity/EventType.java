package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "event_type", schema = "equip_owner")
public class EventType {

   @Id
   @Column(name = "event_type_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long eventTypeId;

   @Column(name = "event_type_name", nullable = false)
   private String eventTypeName;

   @Column(name = "event_category", nullable = false)
   private String eventCategory;

   @Column(name = "global_flag", nullable = false)
   private Boolean globalFlag;

   @Column(name = "event_description", nullable = false)
   private String eventDescription;
   
   @Column(name ="active", nullable = false)
   private Boolean active;

   public EventType() {
   }

   public EventType(Long eventTypeId, String eventTypeName, String eventCategory, Boolean globalFlag, String eventDescription, Boolean active) {
      this.eventTypeId = eventTypeId;
      this.eventTypeName = eventTypeName;
      this.eventCategory = eventCategory;
      this.globalFlag = globalFlag;
      this.eventDescription = eventDescription;
      this.active = active;
   }

   public Boolean getActive() {
      return active;
   }

   public void setActive(Boolean active) {
      this.active = active;
   }

   public Long getEventTypeId() {
      return eventTypeId;
   }

   public void setEventTypeId(Long eventTypeId) {
      this.eventTypeId = eventTypeId;
   }

   public String getEventTypeName() {
      return eventTypeName;
   }

   public void setEventTypeName(String eventTypeName) {
      this.eventTypeName = eventTypeName;
   }

   public String getEventCategory() {
      return eventCategory;
   }

   public void setEventCategory(String eventCategory) {
      this.eventCategory = eventCategory;
   }

   public Boolean getGlobalFlag() {
      return globalFlag;
   }

   public void setGlobalFlag(Boolean globalFlag) {
      this.globalFlag = globalFlag;
   }

   public String getEventDescription() {
      return eventDescription;
   }

   public void setEventDescription(String eventDescription) {
      this.eventDescription = eventDescription;
   }
}
