package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.ListName;

@Transactional(readOnly = true)
public interface ListNameRepository extends CrudRepository<ListName, Long> {

   ListName findByName(String name);
}
