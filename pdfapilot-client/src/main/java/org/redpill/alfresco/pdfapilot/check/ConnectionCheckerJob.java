package org.redpill.alfresco.pdfapilot.check;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class ConnectionCheckerJob implements Job {
  
  private final static Logger LOG = Logger.getLogger(ConnectionCheckerJob.class);
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ConnectionChecker connectionChecker = (ConnectionChecker) context.getJobDetail().getJobDataMap().get("connectionChecker");
    
    LOG.debug("Checking connection...");

    connectionChecker.checkConnection();
  }

}
