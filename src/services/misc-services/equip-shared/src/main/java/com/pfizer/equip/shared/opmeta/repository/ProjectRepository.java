package com.pfizer.equip.shared.opmeta.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.opmeta.entity.Project;

@Transactional
public interface ProjectRepository extends CrudRepository<Project, String> {
   public Set<Project> findBySourceCreationTimestampAfter(Date startDate);
   public Set<Project> findByProjectCodeIn(Collection<String> projectCodes);
   public Set<Project> findByProjectCodeInAndSourceCreationTimestampAfter(Collection<String> projectCodes, Date startDate);
}
