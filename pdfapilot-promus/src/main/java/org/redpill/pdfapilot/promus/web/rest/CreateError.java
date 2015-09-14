package org.redpill.pdfapilot.promus.web.rest;

import java.io.Serializable;
import java.util.List;

public class CreateError implements Serializable {

  private static final long serialVersionUID = 8755992987619418824L;

  private int code;

  private String description;
  
  private List<String> message;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
  public List<String> getMessage() {
    return message;
  }
  
  public void setMessage(List<String> message) {
    this.message = message;
  }

}
