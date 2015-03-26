package org.redpill.pdfapilot.server.domain;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Status {

  private Long _time;

  private List<String> _status = new ArrayList<String>();

  public Long getTime() {
    return _time;
  }

  public void setTime(Long total) {
    _time = total;
  }

  public List<String> getStatus() {
    return _status;
  }

  public void setStatus(List<String> status) {
    _status = status;
  }

  public boolean isExpired() {
    String expirationDateString = null;

    for (String status : _status) {
      if (status.startsWith("Expiration Date")) {
        expirationDateString = StringUtils.replace(status, "Expiration Date", "").trim();
        break;
      }
    }
    
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    LocalDate expirationDate = LocalDate.parse(expirationDateString, fmt);
    LocalDate today = LocalDate.now();

    Period period = Period.between(today, expirationDate);
    
    return period.getDays() < 0;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
