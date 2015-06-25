package org.redpill.pdfapilot.promus.service.impl;

import java.io.File;

import javax.annotation.PostConstruct;

import org.redpill.pdfapilot.promus.service.CreateProcessor;
import org.redpill.pdfapilot.promus.service.CreateService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCreateProcessor implements CreateProcessor {

  @Autowired
  private CreateService _createService;

  @Override
  public void preProcess(File file) {
    // do nothing
  }

  @Override
  public void postProcess(File sourceFile, File targetFile, Long duration, Boolean pdfa) {
    // do nothing
  }

  @Override
  public void errorProcess(File sourceFile, Boolean pdfa, Throwable ex) {
    // do nothing
  }

  @PostConstruct
  public void postConstruct() {
    _createService.registerProcessor(this);
  }

}
