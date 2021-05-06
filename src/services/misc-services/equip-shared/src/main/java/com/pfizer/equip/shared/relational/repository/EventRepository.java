package com.pfizer.equip.shared.relational.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.Event;

@Transactional(readOnly = true)
public interface EventRepository extends CrudRepository<Event, Long> {

}
