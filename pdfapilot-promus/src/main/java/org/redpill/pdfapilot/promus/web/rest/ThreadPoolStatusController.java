package org.redpill.pdfapilot.promus.web.rest;

import javax.annotation.Resource;

import org.redpill.pdfapilot.promus.domain.ThreadPoolStatus;
import org.redpill.pdfapilot.promus.service.proxies.CustomThreadPoolExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ThreadPoolStatusController extends AbstractController {
  
  @Resource(name = "ppc.threadPoolExecutor")
  private CustomThreadPoolExecutor _threadPoolExecutor;

  @RequestMapping(value = "/threadPoolStatus", method = RequestMethod.GET)
  public ThreadPoolStatus threadPoolStatus() {
    return _threadPoolExecutor.getThreadPoolStatus();
  }

}
