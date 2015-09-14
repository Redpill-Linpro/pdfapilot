package org.redpill.alfresco.pdfapilot.check;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class LicenseCheckerJob implements Job {
  
  private final static Logger LOG = Logger.getLogger(LicenseCheckerJob.class);
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LicenseChecker licenseChecker = (LicenseChecker) context.getJobDetail().getJobDataMap().get("licenseChecker");
    
    LOG.debug("Checking license...");

    licenseChecker.checkLicense();
  }

}
