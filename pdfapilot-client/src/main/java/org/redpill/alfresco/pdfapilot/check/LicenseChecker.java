package org.redpill.alfresco.pdfapilot.check;

import static java.lang.String.format;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.redpill.alfresco.pdfapilot.client.PdfaPilotClient;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;

@Component
public class LicenseChecker extends AbstractLifecycleBean {
  
  private final static Logger LOG = Logger.getLogger(LicenseChecker.class);

  @Autowired
  private PdfaPilotClient _pdfaPilotClient;

  @Autowired
  private PdfaPilotWorker _pdfaPilotWorker;

  private ReentrantLock _lock = new ReentrantLock();

  @Override
  protected void onBootstrap(ApplicationEvent event) {
    checkLicense();
  }

  @Override
  protected void onShutdown(ApplicationEvent event) {
  }

  public void checkLicense() {
    boolean lockAcquired = _lock.tryLock();

    try {
      if (lockAcquired) {
        boolean licensed = _pdfaPilotClient.isLicensed();
        
        LOG.debug(format("Setting licensed to %b", licensed));

        _pdfaPilotWorker.setLicensed(licensed);
        
        return;
      }
      
      LOG.warn("The license check is still running, please increase the job interval.");
    } finally {
      if (lockAcquired) {
        _lock.unlock();
      }
    }
  }
  
}
