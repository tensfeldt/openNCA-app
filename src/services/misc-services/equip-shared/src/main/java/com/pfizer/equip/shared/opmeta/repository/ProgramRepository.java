package com.pfizer.equip.shared.opmeta.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.opmeta.entity.Program;

@Transactional
@Repository
public interface ProgramRepository extends CrudRepository<Program, String> {
   public Set<Program> findBySourceCreationTimestampAfter(Date startDate);
   public Set<Program> findByProgramCodeIn(Collection<String> programCodes);
   public Set<Program> findByProgramCodeInAndSourceCreationTimestampAfter(Collection<String> programCodes, Date startDate);
}
