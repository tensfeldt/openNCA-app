package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.LockControl;

@Transactional(readOnly = true)
public interface LockControlRepository extends CrudRepository<LockControl, Long> {
   
   @Query("UPDATE LockControl lc set lc.lockFlag = true where lc.lockFlag = false and lc.lockName = :lockName")
   @Modifying
   @Transactional
   Integer tryLock(@Param("lockName") String lockName); 
   
   @Query("UPDATE LockControl lc set lc.lockFlag = false where lc.lockName = :lockName")
   @Modifying
   @Transactional
   Integer unlock(@Param("lockName") String lockName);
}
