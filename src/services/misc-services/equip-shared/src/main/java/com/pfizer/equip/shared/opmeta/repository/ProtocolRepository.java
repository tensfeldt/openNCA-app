package com.pfizer.equip.shared.opmeta.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.shared.opmeta.entity.Protocol;

@Transactional
public interface ProtocolRepository extends CrudRepository<Protocol, String> {
   public Set<Protocol> findBySourceCreationTimestampAfter(Date sourceCreationTimestamp);
   public Set<Protocol> findByStudyIdIn(Collection<String> studyIds);
   public Set<Protocol> findByStudyIdInAndSourceCreationTimestampAfter(Collection<String> studyIds, Date startDate);
}
