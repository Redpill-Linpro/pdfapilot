package org.redpill.pdfapilot.server.web.rest;

import javax.annotation.Resource;

import org.redpill.pdfapilot.server.domain.Version;
import org.redpill.pdfapilot.server.service.VersionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController extends AbstractController {

  @Resource(name = "pps.VersionService")
  private VersionService _versionService;

  @RequestMapping("/version")
  public Version status() {
    return _versionService.getVersion();
  }

}
