package com.pfizer.equip.shared.relational.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.types.EntityType;

@Transactional
public interface GroupAccessRepository extends CrudRepository<GroupAccess, Long> {
   Set<GroupAccess> findByEntityTypeAndEntityId(EntityType entityType, String entityId);
}
