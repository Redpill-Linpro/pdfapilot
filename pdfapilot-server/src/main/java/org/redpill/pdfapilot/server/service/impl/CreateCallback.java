package org.redpill.pdfapilot.server.service.impl;

import java.io.File;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public interface CreateCallback {

  void handleFile(File file);

}
