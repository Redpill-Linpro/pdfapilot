package org.redpill.pdfapilot.promus.web.rest;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.joda.time.LocalDateTime;
import org.redpill.pdfapilot.promus.security.AuthoritiesConstants;
import org.redpill.pdfapilot.promus.service.AuditEventService;
import org.redpill.pdfapilot.promus.web.propertyeditors.LocaleDateTimeEditor;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/api")
public class AuditResource {

  @Inject
  private AuditEventService auditEventService;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(LocalDateTime.class, new LocaleDateTimeEditor("yyyy-MM-dd", false));
  }

  @RequestMapping(value = "/audits/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(AuthoritiesConstants.ADMIN)
  public List<AuditEvent> findAll() {
    return auditEventService.findAll();
  }

  @RequestMapping(value = "/audits/byDates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(AuthoritiesConstants.ADMIN)
  public List<AuditEvent> findByDates(@RequestParam(value = "fromDate") LocalDateTime fromDate, @RequestParam(value = "toDate") LocalDateTime toDate) {
    return auditEventService.findByDates(fromDate, toDate);
  }

  @RequestMapping(value = "/audits/countByAuditEventType", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @RolesAllowed(AuthoritiesConstants.ADMIN)
  public String countByAuditEventType(@RequestParam(value = "auditEventType") String[] auditEventTypes) {
    int total = 0;

    for (String auditEventType : auditEventTypes) {
      total += auditEventService.countByAuditEventType(auditEventType);
    }

    return String.valueOf(total);
  }

  @RequestMapping(value = "/audits/findByAuditEventType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(AuthoritiesConstants.ADMIN)
  public Map<String, Object> findByAuditEventType(@RequestParam("auditEventType") List<String> auditEventTypes, @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam(value = "count", required = false, defaultValue = "10") Integer count, @RequestParam(value = "filename", required = false) String filename,
      @RequestParam(value = "username", required = false) String username, @RequestParam(value = "node", required = false) String node,
      @RequestParam(value = "success", required = false) String success, @RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to,
      @RequestParam(value = "verified", required = false) String verified, @RequestParam(value = "nodeRef", required = false) String nodeRef) {
    return auditEventService.findByAuditEventTypeIn(auditEventTypes, page, count, filename, username, node, success, from, to, nodeRef, verified);
  }

}
