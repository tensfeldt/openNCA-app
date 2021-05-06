package com.pfizer.equip.shared.relational.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "security_role", schema = "equip_owner")
public class Role implements Serializable {
   // TODO: See what the issue is with serializable. 
   // system_key in joinColumns causes JPA to require this class to be serializeable
   private static final long serialVersionUID = 1L;

   @OneToMany(fetch = FetchType.EAGER, mappedBy = "role")
   @BatchSize(size = 1000)
   Set<RolePrivilege> rolePrivileges;

   @Id
   @Column(name = "security_role_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long roleId;
   
   @Column(name = "system_key")
   private String systemId;
   
   @Column(name = "role_name")
   private String roleName;

   @Column(name = "external_group_name")
   private String externalGroupName;
   
   @Column(name = "active_yn", nullable = false)
   private String activeFlag = "Y";
   

   @ManyToMany(cascade = { 
       CascadeType.PERSIST, 
       CascadeType.MERGE
   })
   @JoinTable(name = "security_role_event_type", schema = "equip_owner",
       joinColumns = @JoinColumn(name = "security_role_id"),
       inverseJoinColumns = @JoinColumn(name = "event_type_id")
   )
   private Set<EventType> eventTypes;
   
   public Long getRoleId() {
      return roleId;
   }

   public void setRoleId(Long securityRoleId) {
      this.roleId = securityRoleId;
   }

   public void setRolePrivs(Set<RolePrivilege> rolePrivileges) {
      this.rolePrivileges = rolePrivileges;
   }

   public void addRolePriv(RolePrivilege rolePrivilege) {
      this.rolePrivileges.add(rolePrivilege);
   }

   public String getSystemId() {
      return systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }

   public String getRoleName() {
      return roleName;
   }

   public void setRoleName(String roleName) {
      this.roleName = roleName;
   }

   public String getExternalGroupName() {
      return externalGroupName;
   }

   public void setExternalGroupName(String externalGroupName) {
      this.externalGroupName = externalGroupName;
   }

   public Set<RolePrivilege> getRolePrivileges() {
      return rolePrivileges;
   }

   public Set<EventType> getEventTypes() {
      return eventTypes;
   }

   public void setEventTypes(Set<EventType> eventTypes) {
      this.eventTypes = eventTypes;
   }
}
