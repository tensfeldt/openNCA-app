package com.pfizer.equip.shared.relational.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.Group;

@Transactional
public interface GroupRepository extends CrudRepository<Group, Long> {
   Group findByExternalGroupName(String externalGroupName);
   Set<Group> findByExternalGroupNameIn(Set<String> externalGroupNames);
   Group findByGroupName(String groupName);
}
