package org.redpill.pdfapilot.promus.domain;

import java.util.Map;

public class CreatePdfResult {

  private String _id;

  private Boolean _pdfa;

  private String _sourceFilename;

  private Long _sourceLength;

  private String _targetFilename;

  private Long _targetLength;

  private Long _duration;

  private String _stacktrace;

  private String _username;

  private Map<String, Object> _properties;

  public CreatePdfResult(String id, String sourceFilename, Long sourceLength, String targetFilename, Long targetLength, Boolean pdfa, Map<String, Object> properties, Long duration, String stacktrace) {
    _id = id;
    _duration = duration;
    _pdfa = pdfa;
    _sourceFilename = sourceFilename;
    _sourceLength = sourceLength;
    _targetFilename = targetFilename;
    _targetLength = targetLength;
    _stacktrace = stacktrace;
    _properties = properties;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }

  public boolean isPdfa() {
    return _pdfa;
  }

  public void setPdfa(boolean pdfa) {
    _pdfa = pdfa;
  }

  public String getSourceFilename() {
    return _sourceFilename;
  }

  public void setSourceFilename(String sourceFilename) {
    _sourceFilename = sourceFilename;
  }

  public Long getSourceLength() {
    return _sourceLength;
  }

  public void setSourceLength(Long sourceLength) {
    _sourceLength = sourceLength;
  }

  public String getTargetFilename() {
    return _targetFilename;
  }

  public void setTargetFilename(String targetFilename) {
    _targetFilename = targetFilename;
  }

  public Long getTargetLength() {
    return _targetLength;
  }

  public void setTargetLength(Long targetLength) {
    _targetLength = targetLength;
  }

  public Long getDuration() {
    return _duration;
  }

  public void setDuration(Long duration) {
    _duration = duration;
  }

  public String getStacktrace() {
    return _stacktrace;
  }

  public void setStacktrace(String stacktrace) {
    _stacktrace = stacktrace;
  }

  public String getUsername() {
    return _username;
  }

  public void setUsername(String username) {
    _username = username;
  }

  public Map<String, Object> getProperties() {
    return _properties;
  }

  public void setProperties(Map<String, Object> properties) {
    _properties = properties;
  }

  public boolean isSuccess() {
    return _stacktrace == null;
  }

}
