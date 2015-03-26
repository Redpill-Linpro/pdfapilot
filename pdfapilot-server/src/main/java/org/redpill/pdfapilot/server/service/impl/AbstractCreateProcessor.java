package org.redpill.pdfapilot.server.service.impl;

import java.io.File;

import javax.annotation.PostConstruct;

import org.redpill.pdfapilot.server.service.CreateProcessor;
import org.redpill.pdfapilot.server.service.CreateService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCreateProcessor implements CreateProcessor {
  
  @Autowired
  private CreateService _createService;
  
  @Override
  public void preProcess(File file) {
    // do nothing
  }
  
  @Override
  public void postProcess(File file) {
    // do nothing
  }
  
  @PostConstruct
  public void postConstruct() {
    _createService.registerProcessor(this);
  }

}
