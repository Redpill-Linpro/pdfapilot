package org.redpill.pdfapilot.promus.web.websocket;

import javax.inject.Inject;

import org.redpill.pdfapilot.promus.domain.ThreadPoolStatus;
import org.redpill.pdfapilot.promus.service.proxies.CustomThreadPoolExecutorImpl.ThreadPoolStatusEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ThreadPoolStatusService implements ApplicationListener<ThreadPoolStatusEvent> {

  @Inject
  private SimpMessageSendingOperations _messagingTemplate;
  
  @Override
  public void onApplicationEvent(ThreadPoolStatusEvent event) {
    ThreadPoolStatus threadPoolStatus = event.getThreadPoolStatus();

    _messagingTemplate.convertAndSend("/topic/threadPoolStatus", threadPoolStatus);
  }

}
