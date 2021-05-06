package com.pfizer.equip.shared.relational.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.relational.entity.RolePrivilege;
import com.pfizer.equip.shared.service.user.PrivilegeType;

@Transactional(readOnly = true)
public interface RolePrivilegeRepository extends CrudRepository<RolePrivilege, Long> {
   Set<RolePrivilege> findBySystemIdAndPrivilegeKey(String systemId, PrivilegeType privilegeKey);
}
