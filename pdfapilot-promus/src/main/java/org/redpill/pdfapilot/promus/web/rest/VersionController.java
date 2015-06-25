package org.redpill.pdfapilot.promus.web.rest;

import javax.annotation.Resource;

import org.redpill.pdfapilot.promus.domain.Version;
import org.redpill.pdfapilot.promus.service.VersionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
public class VersionController extends AbstractController {

  @Resource(name = "pps.VersionService")
  private VersionService _versionService;

  @RequestMapping(value = "/version", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Timed
  public Version status() {
    return _versionService.getVersion();
  }

}
