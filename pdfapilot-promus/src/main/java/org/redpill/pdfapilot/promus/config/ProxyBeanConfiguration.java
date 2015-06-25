package org.redpill.pdfapilot.promus.config;

import javax.annotation.Resource;

import org.redpill.pdfapilot.promus.service.CreateService;
import org.redpill.pdfapilot.promus.service.StatusService;
import org.redpill.pdfapilot.promus.service.VersionService;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyBeanConfiguration {

  /*
  @Resource(name = "pps.statusService")
  private StatusService _statusService;

  @Resource(name = "pps.versionService")
  private VersionService _versionService;

  @Resource(name = "pps.createService")
  private CreateService _createService;

  @Bean(name = "pps.StatusService")
  public ProxyFactoryBean statusService() {
    ProxyFactoryBean proxy = new ProxyFactoryBean();

    proxy.setInterfaces(StatusService.class);
    proxy.setTarget(_statusService);
    proxy.setInterceptorNames("pps.poolingInterceptor");

    return proxy;
  }

  @Bean(name = "pps.VersionService")
  public ProxyFactoryBean versionService() {
    ProxyFactoryBean proxy = new ProxyFactoryBean();

    proxy.setInterfaces(VersionService.class);
    proxy.setTarget(_versionService);
    proxy.setInterceptorNames("pps.poolingInterceptor");

    return proxy;
  }

  @Bean(name = "pps.CreateService")
  public ProxyFactoryBean createService() {
    ProxyFactoryBean proxy = new ProxyFactoryBean();

    proxy.setInterfaces(CreateService.class);
    proxy.setTarget(_createService);
    proxy.setInterceptorNames("pps.createServicePoolingInterceptor");

    return proxy;
  }
  */

}
