package org.redpill.pdfapilot.promus.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.redpill.pdfapilot.promus.config.audit.AuditEventConverter;
import org.redpill.pdfapilot.promus.domain.PersistentAuditEvent;
import org.redpill.pdfapilot.promus.domain.QPersistentAuditEvent;
import org.redpill.pdfapilot.promus.repository.PersistenceAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.mysema.query.BooleanBuilder;

/**
 * Service for managing audit events.
 * <p/>
 * <p>
 * This is the default implementation to support SpringBoot Actuator
 * AuditEventRepository
 * </p>
 */
@Service
public class AuditEventService {

  @Inject
  private PersistenceAuditEventRepository persistenceAuditEventRepository;

  @Inject
  private AuditEventConverter auditEventConverter;

  public List<AuditEvent> findAll() {
    return auditEventConverter.convertToAuditEvent(persistenceAuditEventRepository.findAll());
  }

  public List<AuditEvent> findByDates(LocalDateTime fromDate, LocalDateTime toDate) {
    List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate, toDate);

    return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
  }

  public Long countByAuditEventType(String auditEventType) {
    return persistenceAuditEventRepository.countByAuditEventType(auditEventType);
  }

  public List<AuditEvent> findByAuditEventType(String auditEventType, int page, int count) {
    Sort sort = new Sort(Direction.DESC, "auditEventDate");
    PageRequest pageRequest = new PageRequest(page - 1, count, sort);

    Page<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findByAuditEventType(auditEventType, pageRequest);

    return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
  }

  public Map<String, Object> findByAuditEventTypeIn(Collection<String> auditEventType, Integer page, Integer count, String filename, String username, String node, String success, String from,
      String to, String nodeRef, String verified) {
    Sort sort = new Sort(Direction.DESC, "auditEventDate");
    PageRequest pageRequest = new PageRequest(page - 1, count, sort);

    QPersistentAuditEvent persistentAuditEvent = QPersistentAuditEvent.persistentAuditEvent;

    BooleanBuilder builder = new BooleanBuilder();

    if (StringUtils.isNotBlank(filename)) {
      builder.and(persistentAuditEvent.data.get("sourceFilename").contains(filename));
    }

    if (StringUtils.isNotBlank(username)) {
      builder.and(persistentAuditEvent.principal.contains(username));
    }

    if (StringUtils.isNotBlank(node)) {
      builder.and(persistentAuditEvent.data.get("node").contains(node));
    }

    if (StringUtils.isNotBlank(success)) {
      builder.and(persistentAuditEvent.data.get("success").eq(success));
    }

    if (from != null) {
      DateTimeFormatter formatter = ISODateTimeFormat.dateParser();
      LocalDateTime date = LocalDate.parse(from, formatter).toLocalDateTime(LocalTime.MIDNIGHT);
      builder.and(persistentAuditEvent.auditEventDate.after(date));
    }

    if (to != null) {
      DateTimeFormatter formatter = ISODateTimeFormat.dateParser();
      LocalDateTime date = LocalDate.parse(to, formatter).toLocalDateTime(LocalTime.now());
      builder.and(persistentAuditEvent.auditEventDate.before(date));
    }

    if (StringUtils.isNotBlank(nodeRef)) {
      builder.and(persistentAuditEvent.data.get("nodeRef").contains(nodeRef));
    }

    if (StringUtils.isNotBlank(verified)) {
      builder.and(persistentAuditEvent.data.get("verified").eq(verified));
    }

    builder.and(persistentAuditEvent.auditEventType.in(auditEventType));

    Page<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll(builder, pageRequest);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("total", persistentAuditEvents.getTotalElements());
    result.put("events", auditEventConverter.convertToAuditEvent(persistentAuditEvents));

    return result;
  }

  public void auditCreate(String id, String verified) {
    QPersistentAuditEvent persistentAuditEvent = QPersistentAuditEvent.persistentAuditEvent;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(persistentAuditEvent.data.get("id").contains(id));

    PersistentAuditEvent event = persistenceAuditEventRepository.findOne(builder);
    Map<String, String> data = event.getData();
    data.put("verified", verified);

    persistenceAuditEventRepository.save(event);
  }

}
