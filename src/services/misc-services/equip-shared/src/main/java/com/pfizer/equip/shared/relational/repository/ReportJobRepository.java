package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.ReportJob;

@Transactional
public interface ReportJobRepository extends CrudRepository<ReportJob, Long> {
}