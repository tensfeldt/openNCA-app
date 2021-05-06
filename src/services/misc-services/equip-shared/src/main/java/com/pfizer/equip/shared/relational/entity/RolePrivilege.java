package com.pfizer.equip.shared.relational.entity;

import java.io.Serializable;

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
import com.pfizer.equip.shared.service.user.PrivilegeType;

@Entity
@Table(name = "security_role_priv", schema = "equip_owner")
public class RolePrivilege implements Serializable {
   private static final long serialVersionUID = 1L;

   @ManyToOne
   @JoinColumn(name="security_role_id", insertable=false, updatable=false)
   @JsonIgnore
   Role role;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "security_role_priv_id")
   private Long rolePrivilegeId;
   
   @Column(name = "security_role_id")
   private Long roleId;

   @Column(name = "security_priv_key")
   @Enumerated(EnumType.STRING)
   private PrivilegeType privilegeKey;

   @Column(name = "system_key")
   private String systemId;

   public Long getRoleId() {
      return roleId;
   }

   public void setRoleId(Long roleId) {
      this.roleId = roleId;
   }
   
   public Role getRole() {
      return role;
   }

   public void setRole(Role role) {
      this.role = role;
   }

   public PrivilegeType getPrivilegeKey() {
      return privilegeKey;
   }

   public void setPrivilegeKey(PrivilegeType privilegeKey) {
      this.privilegeKey = privilegeKey;
   }

   public String getSystemId() {
      return systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }

}
