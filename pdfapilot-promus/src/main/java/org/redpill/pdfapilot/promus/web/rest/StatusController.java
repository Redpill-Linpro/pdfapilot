package org.redpill.pdfapilot.promus.web.rest;

import javax.annotation.Resource;

import org.redpill.pdfapilot.promus.domain.Status;
import org.redpill.pdfapilot.promus.service.StatusService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
public class StatusController extends AbstractController {

  @Resource(name = "pps.statusService")
  private StatusService _statusService;

  @RequestMapping(value = "/status", method = RequestMethod.GET)
  @Timed
  public Status status() {
    return _statusService.getStatus();
  }

}
