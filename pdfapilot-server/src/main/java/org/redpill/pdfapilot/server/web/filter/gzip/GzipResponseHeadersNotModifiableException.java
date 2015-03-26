package org.redpill.pdfapilot.server.web.filter.gzip;

import javax.servlet.ServletException;

public class GzipResponseHeadersNotModifiableException extends ServletException {

  private static final long serialVersionUID = 2183946969841948828L;

  public GzipResponseHeadersNotModifiableException(String message) {
    super(message);
  }
}
