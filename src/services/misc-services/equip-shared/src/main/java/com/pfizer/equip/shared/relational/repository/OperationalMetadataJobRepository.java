package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.OperationalMetadataJob;

@Transactional
public interface OperationalMetadataJobRepository extends CrudRepository<OperationalMetadataJob, Long> {
   OperationalMetadataJob findFirstByStatusOrderByStartDateDesc(String name);
}