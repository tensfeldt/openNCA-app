package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.ListValue;

@Transactional(readOnly = true)
public interface ListValueRepository extends CrudRepository<ListValue, Long> {

}
