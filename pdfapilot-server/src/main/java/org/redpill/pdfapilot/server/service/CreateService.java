package org.redpill.pdfapilot.server.service;

import java.io.File;
import java.io.InputStream;

import org.redpill.pdfapilot.server.service.impl.CreateCallback;

public interface CreateService {

  void createPdf(InputStream inputStream, String filename, CreateCallback createCallback);

  void createPdfa(InputStream inputStream, String filename, String level, CreateCallback createCallback);

  /**
   * Pre-processes the source file.
   * 
   * @param sourceFile
   */
  void preProcess(File sourceFile);

  /**
   * Post-processes the target file.
   * 
   * @param targetFile
   */
  void postProcess(File targetFile);

  void registerProcessor(CreateProcessor processor);
  
}
