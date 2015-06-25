package org.redpill.pdfapilot.promus.service.impl;

import java.io.File;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public interface CreateCallback {

  void handleFile(File file, String id);

}
