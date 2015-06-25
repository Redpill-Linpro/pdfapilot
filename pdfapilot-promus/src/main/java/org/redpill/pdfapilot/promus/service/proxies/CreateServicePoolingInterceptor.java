package org.redpill.pdfapilot.promus.service.proxies;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component("pps.createServicePoolingInterceptor")
public class CreateServicePoolingInterceptor extends PoolingInterceptor {

  @PostConstruct
  public void postConstruct() {
    _methods = new String[] { "createPdf", "createPdfa" };
  };

}
