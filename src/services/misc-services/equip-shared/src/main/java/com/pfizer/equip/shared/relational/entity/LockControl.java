package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "lock_control", schema = "equip_owner")
public class LockControl {

   @Id
   @Column(name = "lock_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long lockId;

   @Column(name = "lock_name")
   private String lockName;

   @Column(name = "lock_flag")
   private Boolean lockFlag;

   public LockControl() {
   }

   public LockControl(Long lockId, String lockName, Boolean lockFlag) {
      super();
      this.lockId = lockId;
      this.lockName = lockName;
      this.lockFlag = lockFlag;
   }

   public Long getLockId() {
      return lockId;
   }

   public void setLockId(Long lockId) {
      this.lockId = lockId;
   }

   public String getLockName() {
      return lockName;
   }

   public void setLockName(String lockName) {
      this.lockName = lockName;
   }

   public Boolean getLockFlag() {
      return lockFlag;
   }

   public void setLockFlag(Boolean lockFlag) {
      this.lockFlag = lockFlag;
   }

}
