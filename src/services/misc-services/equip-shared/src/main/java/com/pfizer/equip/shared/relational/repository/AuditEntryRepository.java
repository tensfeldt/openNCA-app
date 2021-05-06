package com.pfizer.equip.shared.relational.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.AuditEntry;

@Transactional(readOnly = true)
public interface AuditEntryRepository extends CrudRepository<AuditEntry, Long>, JpaSpecificationExecutor<AuditEntry> {
   AuditEntry findByEntityId(String entityId);

   List<AuditEntry> findAll(Specification<AuditEntry> spec);

   @Query("SELECT ae FROM AuditEntry ae WHERE ae.entityId = :entityId AND ae.action IN :actions AND ae.entityVersion <= :entityVersion " + 
         "ORDER BY ae.entityVersion ASC, ae.createDate ASC")
   List<AuditEntry> findAuditEntriesWithFilter(@Param("entityId") String entityId, @Param("actions") String[] actions,
         @Param("entityVersion") String entityVersion);

}
