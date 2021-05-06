package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.EventType;

@Transactional(readOnly = true)
public interface EventTypeRepository extends CrudRepository<EventType, Long> {

   EventType findByEventTypeName(String eventTypeName);
}
