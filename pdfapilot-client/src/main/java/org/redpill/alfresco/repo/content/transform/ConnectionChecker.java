package org.redpill.alfresco.repo.content.transform;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public class ConnectionChecker extends AbstractLifecycleBean {

  @Autowired
  private PdfaPilotClient _pdfaPilotClient;

  @Autowired
  private PdfaPilotWorker _pdfaPilotWorker;

  @Override
  protected void onBootstrap(ApplicationEvent event) {
    try {
      boolean expired = _pdfaPilotClient.getStatus().getBoolean("expired");

      _pdfaPilotWorker.setAvailable(!expired);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected void onShutdown(ApplicationEvent event) {
  }

}
