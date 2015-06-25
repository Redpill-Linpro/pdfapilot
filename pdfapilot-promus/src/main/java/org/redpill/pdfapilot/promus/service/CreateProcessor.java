package org.redpill.pdfapilot.promus.service;

import java.io.File;

public interface CreateProcessor {

  void postProcess(File sourceFile, File targetFile, Long duration, Boolean pdfa);
 
  void preProcess(File file);

  void errorProcess(File sourceFile, Boolean pdfa, Throwable ex);

}
