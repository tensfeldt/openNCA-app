package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pfizer.equip.shared.types.EntityType;

@Entity
@Table(name = "security_group_access", schema = "equip_owner")
public class GroupAccess {
   
   @ManyToOne
   @JoinColumn(name="security_group_id", insertable=false, updatable=false)
   @JsonIgnore
   private Group group;
   
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "security_group_access_id")
   private String groupAccessId;
   
   @Column(name = "security_group_id")
   private Long groupId;
   
   @Column(name = "entity_id")
   private String entityId;

   @Column(name = "entity_type")
   @Enumerated(EnumType.STRING)
   private EntityType entityType;

   @Column(name = "data_source_id")
   private String dataSourceId;
   
   @Column(name = "restricted_access_yn")
   private String restrictedAccessFlag; 
   
   @Column(name = "blinded_access_yn")
   private String blindedAccessFlag;

   public String getGroupAccessId() {
      return groupAccessId;
   }

   public void setGroupAccessId(String securityGroupAccessId) {
      this.groupAccessId = securityGroupAccessId;
   }

   public Long getGroupId() {
      return groupId;
   }

   public void setGroupId(Long securityGroupId) {
      this.groupId = securityGroupId;
   }

   public String getEntityId() {
      return entityId;
   }

   public void setEntityId(String entityId) {
      this.entityId = entityId;
   }

   public EntityType getEntityType() {
      return entityType;
   }

   public void setEntityType(EntityType entityType) {
      this.entityType = entityType;
   }

   public String getDataSourceId() {
      return dataSourceId;
   }

   public void setDataSourceId(String dataSourceId) {
      this.dataSourceId = dataSourceId;
   }

   public boolean getRestrictedAccessFlag() {
      if (restrictedAccessFlag.equals("Y")) {
         return true;
      } else {
         return false;
      }
   }

   public void setRestrictedAccessFlag(boolean restrictedAccessFlag) {
      if (restrictedAccessFlag) {
         this.restrictedAccessFlag = "Y";
      } else {
         this.restrictedAccessFlag = "N";
      }
   }

   public boolean getBlindedAccessFlag() {
      if (blindedAccessFlag.equals("Y")) {
         return true;
      } else {
         return false;
      }
   }

   public void setBlindedAccessFlag(boolean blindedAccessFlag) {
      if (blindedAccessFlag) {
         this.blindedAccessFlag = "Y";
      } else {
         this.blindedAccessFlag = "N";
      }
   }

   public Group getGroup() {
      return group;
   }

   public void setGroup(Group group) {
      this.group = group;
   }
}
