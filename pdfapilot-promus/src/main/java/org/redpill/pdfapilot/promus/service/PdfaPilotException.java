package org.redpill.pdfapilot.promus.service;

import java.util.List;

public class PdfaPilotException extends RuntimeException {

  private static final long serialVersionUID = 7971159360129567306L;

  private int _code;

  private List<String> _stdOut;
  
  private List<String> _stdErr;
  
  public PdfaPilotException(int code, String message, List<String> stdOut, List<String> stdErr) {
    super(message);
    
    _code = code;
    _stdOut = stdOut;
    _stdErr = stdErr;
  }

  public int getCode() {
    return _code;
  }

  public void setCode(int code) {
    _code = code;
  }

  public List<String> getStdOut() {
    return _stdOut;
  }
  
  public List<String> getStdErr() {
    return _stdErr;
  }

}
