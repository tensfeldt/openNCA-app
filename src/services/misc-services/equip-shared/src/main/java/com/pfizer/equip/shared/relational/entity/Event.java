package com.pfizer.equip.shared.relational.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.pfizer.equip.shared.contentrepository.ContentInfo;

@javax.persistence.Entity
@Table(name = "event", schema = "equip_owner")
public class Event {

   @Id
   @Column(name = "event_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long eventId;

   @Column(name = "component_name", nullable = false)
   private String componentName;

   @ManyToOne(optional = false)
   @JoinColumn(name = "event_type_id", nullable = false)
   private EventType eventType;

   @Column(name = "entity_id")
   private String entityId;

   @Column(name = "entity_type")
   private String entityType;

   @Column(name = "study_id")
   private String studyId;

   @Column(name = "program_number")
   private String programNumber;

   @Column(name = "description")
   private String description;

   @Column(name = "created_on", nullable = false)
   private Date createdOn;

   @Column(name = "attachment_content")
   private byte[] attachmentContent;

   @Column(name = "attachment_mime_type")
   private String attachmentMimeType;

   @Column(name = "attachment_filename")
   private String attachmentFilename;

   public Event() {
   }

   public Event(Long eventId, String componentName, EventType eventType, String entityId, String entityType, String studyId, String programNumber,
         String description, Date createdOn) {
      this.eventId = eventId;
      this.componentName = componentName;
      this.eventType = eventType;
      this.entityId = entityId;
      this.entityType = entityType;
      this.studyId = studyId;
      this.programNumber = programNumber;
      this.description = description;
      this.createdOn = createdOn;
   }

   public Long getEventId() {
      return eventId;
   }

   public void setEventId(Long eventId) {
      this.eventId = eventId;
   }

   public String getComponentName() {
      return componentName;
   }

   public void setComponentName(String componentName) {
      this.componentName = componentName;
   }

   public EventType getEventType() {
      return eventType;
   }

   public void setEventType(EventType eventType) {
      this.eventType = eventType;
   }

   public String getEntityId() {
      return entityId;
   }

   public void setEntityId(String entityId) {
      this.entityId = entityId;
   }

   public String getEntityType() {
      return entityType;
   }

   public void setEntityType(String entityType) {
      this.entityType = entityType;
   }

   public Date getCreatedOn() {
      return createdOn;
   }

   public void setCreatedOn(Date createdOn) {
      this.createdOn = createdOn;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getStudyId() {
      return studyId;
   }

   public void setStudyId(String studyId) {
      this.studyId = studyId;
   }

   public String getProgramNumber() {
      return programNumber;
   }

   public void setProgramNumber(String programNumber) {
      this.programNumber = programNumber;
   }

   public byte[] getAttachmentContent() {
      return attachmentContent;
   }

   public void setAttachmentContent(byte[] attachmentContent) {
      this.attachmentContent = attachmentContent;
   }

   public String getAttachmentMimeType() {
      return attachmentMimeType;
   }

   public void setAttachmentMimeType(String attachmentMimeType) {
      this.attachmentMimeType = attachmentMimeType;
   }

   public String getAttachmentFilename() {
      return attachmentFilename;
   }

   public void setAttachmentFilename(String attachmentFilename) {
      this.attachmentFilename = attachmentFilename;
   }
}
