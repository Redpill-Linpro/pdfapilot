package org.redpill.alfresco.pdfapilot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.TriggerBean;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.redpill.alfresco.pdfapilot.check.ConnectionChecker;
import org.redpill.alfresco.pdfapilot.check.ConnectionCheckerJob;
import org.redpill.alfresco.pdfapilot.check.LicenseChecker;
import org.redpill.alfresco.pdfapilot.check.LicenseCheckerJob;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;
import org.springframework.scheduling.quartz.JobDetailBean;

@Configuration
public class PdfaPilotClientConfiguration {

  @Autowired
  @Qualifier("schedulerFactory")
  private Scheduler _scheduler;

  @Autowired
  private ConnectionChecker _connectionChecker;

  @Autowired
  private LicenseChecker _licenseChecker;

  @Autowired
  private TenantService _tenantService;

  @Autowired
  private DictionaryDAO _dictionaryDAO;

  @Bean(name = "ppc.connectionCheckerJobDetail")
  public JobDetailBean connectionCheckerJobDetail() {
    JobDetailBean detail = new JobDetailBean();

    Map<String, ConnectionChecker> jobDataAsMap = new HashMap<String, ConnectionChecker>();
    jobDataAsMap.put("connectionChecker", _connectionChecker);

    detail.setJobClass(ConnectionCheckerJob.class);
    detail.setJobDataAsMap(jobDataAsMap);

    return detail;
  }

  @Bean(name = "ppc.connectionCheckerTrigger")
  public JobDetailAwareTrigger connectionCheckerTrigger() {
    TriggerBean trigger = new TriggerBean();

    trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    trigger.setRepeatInterval(30000);
    trigger.setJobDetail(connectionCheckerJobDetail());
    trigger.setScheduler(_scheduler);

    return trigger;
  }

  @Bean(name = "ppc.licenseCheckerJobDetail")
  public JobDetailBean licenseCheckerJobDetail() {
    JobDetailBean detail = new JobDetailBean();

    Map<String, LicenseChecker> jobDataAsMap = new HashMap<String, LicenseChecker>();
    jobDataAsMap.put("licenseChecker", _licenseChecker);

    detail.setJobClass(LicenseCheckerJob.class);
    detail.setJobDataAsMap(jobDataAsMap);

    return detail;
  }

  @Bean(name = "ppc.licenseCheckerTrigger")
  public JobDetailAwareTrigger licenseCheckerTrigger() {
    TriggerBean trigger = new TriggerBean();

    trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    trigger.setRepeatInterval(30000);
    trigger.setJobDetail(licenseCheckerJobDetail());
    trigger.setScheduler(_scheduler);

    return trigger;
  }

  @Bean(name = "ppc.dictionaryBootstrap", initMethod = "bootstrap")
  @DependsOn("dictionaryBootstrap")
  public DictionaryBootstrap dictionaryBootstrap() {
    DictionaryBootstrap dictionaryBootstrap = new DictionaryBootstrap();

    List<String> modelResources = new ArrayList<String>();
    modelResources.add("alfresco/subsystems/pdfaPilot/default/pdfapilot-model.xml");

    dictionaryBootstrap.setTenantService(_tenantService);
    dictionaryBootstrap.setDictionaryDAO(_dictionaryDAO);
    dictionaryBootstrap.setModels(modelResources);

    return dictionaryBootstrap;
  }
  
  @Bean(name="pdfaPilot.pdfaOptions")
  public PdfaPilotTransformationOptions pdfaOptions() {
    PdfaPilotTransformationOptions options = new PdfaPilotTransformationOptions();
    
    options.setReadLimitKBytes(-1);
    options.setReadLimitTimeMs(-1);
    options.setMaxSourceSizeKBytes(-1);
    options.setPageLimit(-1);
    options.setMaxPages(-1);
    options.setIncludeEmbedded(true);
    options.setOptimize(false);
    options.setLevel("2b");
    
    return options;
  }

  @Bean(name="pdfaPilot.pdfOptions")
  public PdfaPilotTransformationOptions pdfOptions() {
    PdfaPilotTransformationOptions options = new PdfaPilotTransformationOptions();
    
    options.setReadLimitKBytes(-1);
    options.setReadLimitTimeMs(-1);
    options.setMaxSourceSizeKBytes(-1);
    options.setPageLimit(-1);
    options.setMaxPages(-1);
    options.setIncludeEmbedded(true);
    options.setOptimize(true);
    
    return options;
  }
  
}
