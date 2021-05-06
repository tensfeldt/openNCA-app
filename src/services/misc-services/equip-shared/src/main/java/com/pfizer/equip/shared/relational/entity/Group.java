package com.pfizer.equip.shared.relational.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "security_group", schema = "equip_owner")
public class Group {

   @OneToMany(fetch = FetchType.EAGER, mappedBy = "group") 
   @BatchSize(size = 1000)
   private Set<GroupAccess> groupAccesses;
   
   @Id
   @Column(name = "security_group_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long securityGroupId;
   
   @Column(name = "group_name")
   private String groupName;

   @Column(name = "external_group_name")
   private String externalGroupName;
   
   @Column(name = "active_yn", nullable = false)
   private String activeFlag = "Y";

   public Long getSecurityGroupId() {
      return securityGroupId;
   }

   public void setSecurityGroupId(Long securityGroupId) {
      this.securityGroupId = securityGroupId;
   }

   public String getGroupName() {
      return groupName;
   }

   public void setGroupName(String groupName) {
      this.groupName = groupName;
   }

   public String getExternalGroupName() {
      return externalGroupName;
   }

   public void setExternalGroupName(String externalGroupName) {
      this.externalGroupName = externalGroupName;
   }

   public Set<GroupAccess> getGroupAccesses() {
      return groupAccesses;
   }

   public void addGroupAccess(GroupAccess groupAccess) {
      groupAccesses.add(groupAccess);
   }
}
