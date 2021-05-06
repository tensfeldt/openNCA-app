package com.pfizer.equip.shared.relational.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.Role;

@Transactional
public interface RoleRepository extends CrudRepository<Role, Long> {
   Set<Role> findBySystemId(String systemId);
   Role findBySystemIdAndExternalGroupName(String systemId, String externalGroupName);
   Set<Role> findBySystemIdAndExternalGroupNameIn(String systemId, Set<String> externalGroupName);
   Role findBySystemIdAndRoleName(String systemId, String roleName);
   Set<Role> findBySystemIdAndRoleNameIn(String systemId, Set<String> roleName);
}
