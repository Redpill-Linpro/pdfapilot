package org.redpill.pdfapilot.promus.repository;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.redpill.pdfapilot.promus.domain.PersistentAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Spring Data MongoDB repository for the PersistentAuditEvent entity.
 */
public interface PersistenceAuditEventRepository extends MongoRepository<PersistentAuditEvent, String>, QueryDslPredicateExecutor<PersistentAuditEvent> {

  List<PersistentAuditEvent> findByPrincipal(String principal);

  List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfter(String principal, LocalDateTime after);

  List<PersistentAuditEvent> findAllByAuditEventDateBetween(LocalDateTime fromDate, LocalDateTime toDate);

  Long countByAuditEventType(String auditEventType);

  Page<PersistentAuditEvent> findByAuditEventType(String auditEventType, Pageable pageable);

  Page<PersistentAuditEvent> findByAuditEventTypeIn(Iterable<String> auditEventTypes, Pageable pageable);

}
