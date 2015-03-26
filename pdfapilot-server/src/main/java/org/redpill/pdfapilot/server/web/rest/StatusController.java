package org.redpill.pdfapilot.server.web.rest;

import javax.annotation.Resource;

import org.redpill.pdfapilot.server.domain.Status;
import org.redpill.pdfapilot.server.service.StatusService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController extends AbstractController {
  
  @Resource(name="pps.StatusService")
  private StatusService _statusService;

  @RequestMapping("/status")
  public Status status() {
    return _statusService.getStatus();
  }

}
