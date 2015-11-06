package org.redpill.pdfapilot.promus.web.websocket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.redpill.pdfapilot.promus.config.Constants;
import org.redpill.pdfapilot.promus.domain.CreatePdfResult;
import org.redpill.pdfapilot.promus.security.SecurityUtils;
import org.redpill.pdfapilot.promus.service.CreateService;
import org.redpill.pdfapilot.promus.service.CreateService.CreatePdfEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class CreatePdfService implements ApplicationListener<CreatePdfEvent> {

  @Inject
  private SimpMessageSendingOperations _messagingTemplate;

  @Autowired
  private ApplicationEventPublisher _applicationEventPublisher;

  @Value("${pdfaPilot.node}")
  private String _node;

  @Override
  public void onApplicationEvent(CreatePdfEvent event) {
    sendWebSocketMessage(event.getCreatePdfResult());

    recordAudit(event.getCreatePdfResult());
  }

  private void recordAudit(CreatePdfResult createPdfResult) {
    String username = SecurityUtils.getCurrentLogin();
    String eventType = getEventType(createPdfResult.isPdfa(), createPdfResult.getStacktrace());

    Map<String, Object> data = new HashMap<String, Object>();

    if (createPdfResult.getSourceFilename() != null) {
      data.put("sourceFilename", createPdfResult.getSourceFilename());
    }

    if (createPdfResult.getSourceLength() != null) {
      data.put("sourceLength", createPdfResult.getSourceLength());
    }

    if (createPdfResult.getTargetFilename() != null) {
      data.put("targetFilename", createPdfResult.getTargetFilename());
    }

    if (createPdfResult.getTargetLength() != null) {
      data.put("targetLength", createPdfResult.getTargetLength());
    }
    
    if (createPdfResult.getDuration() != null) {
      data.put("duration", createPdfResult.getDuration());
    }
    
    if (createPdfResult.getStacktrace() != null) {
      data.put("stacktrace", createPdfResult.getStacktrace());
    }
    
    data.put("date", new Date().getTime());
    data.put("pdfa", createPdfResult.isPdfa());
    data.put("success", createPdfResult.isSuccess());
    data.put("node", _node);
    data.put("id", createPdfResult.getId());

    if (createPdfResult.getProperties() != null) {
      for (String property : createPdfResult.getProperties().keySet()) {
        data.put(property, createPdfResult.getProperties().get(property));
      }
    }

    AuditApplicationEvent event = new AuditApplicationEvent(username != null ? username : Constants.SYSTEM_ACCOUNT, eventType, data);
    _applicationEventPublisher.publishEvent(event);
  }

  private String getEventType(boolean pdfa, Object exception) {
    if (pdfa) {
      if (exception == null) {
        return CreateService.EVENT_CREATE_PDFA_SUCCESS;
      } else {
        return CreateService.EVENT_CREATE_PDFA_FAILURE;
      }
    } else {
      if (exception == null) {
        return CreateService.EVENT_CREATE_PDF_SUCCESS;
      } else {
        return CreateService.EVENT_CREATE_PDF_FAILURE;
      }
    }
  }

  private void sendWebSocketMessage(CreatePdfResult pdfResult) {
    boolean success = pdfResult.getStacktrace() == null;

    _messagingTemplate.convertAndSend("/topic/createPdf", success);
  }

}
