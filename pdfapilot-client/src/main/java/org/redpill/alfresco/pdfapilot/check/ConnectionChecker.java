package org.redpill.alfresco.pdfapilot.check;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.redpill.alfresco.pdfapilot.client.PdfaPilotClient;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;

@Component
public class ConnectionChecker extends AbstractLifecycleBean {

  private final static Logger LOG = Logger.getLogger(ConnectionChecker.class);

  @Autowired
  private PdfaPilotClient _pdfaPilotClient;

  @Autowired
  private PdfaPilotWorker _pdfaPilotWorker;

  private ReentrantLock _lock = new ReentrantLock();

  @Override
  protected void onBootstrap(ApplicationEvent event) {
    checkConnection();
  }

  @Override
  protected void onShutdown(ApplicationEvent event) {
  }

  public void checkConnection() {
    boolean lockAcquired = _lock.tryLock();

    try {
      if (lockAcquired) {
        boolean connected = _pdfaPilotClient.isConnected();

        _pdfaPilotWorker.setAvailable(connected);

        return;
      }

      LOG.warn("The connection check is still running, please increase the job interval.");
    } finally {
      if (lockAcquired) {
        _lock.unlock();
      }
    }

  }

}
