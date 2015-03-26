package org.redpill.pdfapilot.server.security;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is throw in case of a not activated user trying to
 * authenticate.
 */
public class UserNotActivatedException extends AuthenticationException {

  private static final long serialVersionUID = -4701277959321438664L;

  public UserNotActivatedException(String message) {
    super(message);
  }

  public UserNotActivatedException(String message, Throwable t) {
    super(message, t);
  }
}
