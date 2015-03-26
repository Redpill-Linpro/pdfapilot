package org.redpill.pdfapilot.server.service;

import java.io.File;

public interface CreateProcessor {

  void postProcess(File file);
 
  void preProcess(File file);

}
